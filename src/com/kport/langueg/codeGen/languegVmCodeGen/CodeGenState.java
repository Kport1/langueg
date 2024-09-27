package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.parse.ast.nodes.NameTypePair;
import com.kport.langueg.parse.ast.nodes.NFn;
import com.kport.langueg.parse.ast.nodes.NProg;
import com.kport.langueg.typeCheck.types.FnType;
import com.kport.langueg.typeCheck.types.RefType;
import com.kport.langueg.util.Identifier;
import com.kport.langueg.util.Scope;
import com.kport.langueg.util.Util;

import java.util.*;

public class CodeGenState {
    public final List<FnData> generatedFns = new ArrayList<>();

    public final Stack<FnData> generatingFns = new Stack<>();

    public void enterProg(NProg prog){
        generatingFns.push(new FnData((short)0, (short)0));
        generatedFns.add(generatingFns.peek());
    }

    public void exitProg(){
        writeOp(Ops.RET, (short)0);
        generatingFns.pop();
    }

    public int enterFn(NFn fn){
        int paramLocalsSize = Arrays.stream(fn.getFnHeader().paramTypes()).reduce(0, (i, t) -> i + t.getSize(), Integer::sum);
        if(paramLocalsSize >= LanguegVmCodeGenerator.LOCALS_MAX_SIZE) throw new Error("Parameters too large");
        int retLocalsSize = fn.getFnHeader().returnType.getSize();
        if(paramLocalsSize >= LanguegVmCodeGenerator.LOCALS_MAX_SIZE) throw new Error("Return too large");

        generatingFns.push(new FnData((short)paramLocalsSize, (short)retLocalsSize));
        generatedFns.add(generatingFns.peek());
        for (NameTypePair param : fn.getFnHeader().params) {
            allocateLocal(new Identifier(fn.getBodyScope(), param.name), (byte)param.type.getSize());
        }
        return generatedFns.indexOf(generatingFns.peek());
    }

    public int exitFn(){
        if(generatingFns.peek().retLocalsSize == 0) writeOp(Ops.RET, (short)0);
        return generatedFns.indexOf(generatingFns.pop());
    }

    public void enterScope(){
        generatingFns.peek().enterScope();
    }

    public void exitScope(){
        generatingFns.peek().exitScope();
    }

    public short allocateLocal(Identifier id, int size){
        return generatingFns.peek().allocateLocal(id, size);
    }

    public short getLocalOffset(Identifier id){
        return generatingFns.peek().getLocalOffset(id);
    }

    public short allocateTempLocal(int size){
        return generatingFns.peek().allocateTempLocal(size);
    }

    public short allocateAnonLocal(int size){
        return generatingFns.peek().allocateAnonLocal(size);
    }

    private final Map<Identifier, Integer> fnIndices = new HashMap<>();
    public void registerFn(Identifier id, Integer index){
        fnIndices.put(id, index);
    }
    public short getFnIndex(Identifier id){
        Scope parentScope = id.scope();
        while (parentScope != null){
            if(fnIndices.containsKey(new Identifier(parentScope, id.name())))
                return fnIndices.get(new Identifier(parentScope, id.name())).shortValue();
            parentScope = parentScope.parent;
        }
        throw new Error();
    }

    public void pushFn(short fnIndex){
        writeOp(Ops.LOADFN, allocateAnonLocal(FnType.FN_REF_BYTES), fnIndex);
    }

    public void popStack(int size){
        generatingFns.peek().popStack(size);
    }

    public void rewindLocalsTo(int index){
        generatingFns.peek().rewindLocalsTo(index);
    }

    public short nextUnallocatedByte(){
        return generatingFns.peek().nextUnallocatedByte();
    }

    public void pushAllocDirect(int size){
        writeOp(Ops.ALLOC_DIRECT, allocateAnonLocal(RefType.REF_BYTES), size);
    }

    public final SequencedMap<byte[], Integer> constIndices = new LinkedHashMap<>();
    private int constCount = 0;
    public short registerConst(byte[] val){
        if(constIndices.containsKey(val)) return constIndices.get(val).shortValue();
        if(constCount == 2 << 16) throw new Error();
        constIndices.put(val, constCount);
        return (short)constCount++;
    }

    private void load(short to, byte[] val){
        if(to + val.length > LanguegVmCodeGenerator.LOCALS_MAX_SIZE) throw new Error();
        //val = Util.trimByteArr(val);
        short size = (short)val.length;
        //long l = val[0] | val[1] << 8L | val[2] << 16L | val[3] << 24L | (long)val[4] << 32L | (long)val[5] << 40L | (long)val[6] << 48L | (long)val[7] << 56L;
        //System.out.println(l);

        if(size == 1)
            writeOp(Ops.LOAD8, to, val[0]);
        else if(size == 2)
            writeOp(Ops.LOAD16, to, Util.fromBytesS(val));
        else if(size == 4)
            writeOp(Ops.LOAD32, to, Util.fromBytesI(val));
        else if(size == 8)
            writeOp(Ops.LOAD64, to, Util.fromBytesL(val));
        else
            writeOp(Ops.LOADP, to, registerConst(val));
    }

    public void loadByte(short to, byte val){
        load(to, new byte[]{val});
    }

    public void loadShort(short to, short val){
        load(to, new byte[]{
                (byte)(val & 0xFF),
                (byte)((val >>> 8) & 0xFF),
        });
    }

    public void loadInt(short to, int val){
        load(to, new byte[]{
                (byte)(val & 0xFF),
                (byte)((val >>> 8) & 0xFF),
                (byte)((val >>> 16) & 0xFF),
                (byte)((val >>> 24) & 0xFF)
        });
    }

    public void loadLong(short to, long val){
        load(to, new byte[]{
                (byte)(val & 0xFF),
                (byte)((val >>> 8) & 0xFF),
                (byte)((val >>> 16) & 0xFF),
                (byte)((val >>> 24) & 0xFF),
                (byte)((val >>> 32) & 0xFF),
                (byte)((val >>> 40) & 0xFF),
                (byte)((val >>> 48) & 0xFF),
                (byte)((val >>> 56) & 0xFF)
        });
    }

    private void push(byte[] val){
        short size = (short)val.length;
        short localsOffset = allocateAnonLocal(size);
        load(localsOffset, val);
    }

    public void pushByte(byte val){
        push(new byte[]{val});
    }

    public void pushShort(short val){
        push(new byte[]{
                (byte)(val & 0xFF),
                (byte)((val >>> 8) & 0xFF),
        });
    }

    public void pushInt(int val){
        push(new byte[]{
                (byte)(val & 0xFF),
                (byte)((val >>> 8) & 0xFF),
                (byte)((val >>> 16) & 0xFF),
                (byte)((val >>> 24) & 0xFF)
        });
    }

    public void pushLong(long val){
        push(new byte[]{
                (byte)(val & 0xFF),
                (byte)((val >>> 8) & 0xFF),
                (byte)((val >>> 16) & 0xFF),
                (byte)((val >>> 24) & 0xFF),
                (byte)((val >>> 32) & 0xFF),
                (byte)((val >>> 40) & 0xFF),
                (byte)((val >>> 48) & 0xFF),
                (byte)((val >>> 56) & 0xFF)
        });
    }

    public void mov(short bytes, short to, short from, boolean typeContainsRefs){
        if(bytes == 0) return;
        if(bytes == RefType.REF_BYTES && typeContainsRefs){
            writeOp(Ops.MOV_REF, to, from);
            return;
        }

        switch (bytes){
            case 1 -> writeOp(Ops.MOV8, to, from);
            case 2 -> writeOp(Ops.MOV16, to, from);
            case 4 -> writeOp(Ops.MOV32, to, from);
            case 8 -> writeOp(Ops.MOV64, to, from);
            default -> writeOp(Ops.MOV, bytes, to, from, (byte)(typeContainsRefs? 1 : 0));
        }
    }

    public void movToHeapDirect(short bytes, int to, short from, short refIndex, boolean typeContainsRefs){
        if(bytes == 0) return;
        if(to == 0) {
            writeOp(Ops.MOV_TO_HEAP_ZERO, bytes, from, refIndex, (byte) (typeContainsRefs ? 1 : 0));
            return;
        }
        writeOp(Ops.MOV_TO_HEAP_DIRECT, bytes, to, from, refIndex, (byte) (typeContainsRefs ? 1 : 0));
    }

    public void movFromHeapDirect(short bytes, short to, int from, short refIndex, boolean typeContainsRefs){
        if(bytes == 0) return;
        if(from == 0) {
            writeOp(Ops.MOV_FROM_HEAP_ZERO, bytes, to, refIndex, (byte) (typeContainsRefs ? 1 : 0));
            return;
        }
        writeOp(Ops.MOV_FROM_HEAP_DIRECT, bytes, to, from, refIndex, (byte) (typeContainsRefs ? 1 : 0));
    }

    public void writeOp(Ops op){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
    }

    public void writeOp(Ops op, byte b){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.write(b);
    }

    public void writeOp(Ops op, short s){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.writeShort(s);
    }

    public void writeOp(Ops op, int i){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.writeInt(i);
    }

    public void writeOp(Ops op, long l){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.writeLong(l);
    }

    public void writeOp(Ops op, short s1, short s2, byte[] bytes){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.writeShort(s1);
        fn.code.writeShort(s2);
        fn.code.writeBytes(bytes);
    }

    public void writeOp(Ops op, short s, byte b){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.writeShort(s);
        fn.code.write(b);
    }

    public void writeOp(Ops op, short s1, short s2){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.writeShort(s1);
        fn.code.writeShort(s2);
    }

    public void writeOp(Ops op, short s, int i){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.writeShort(s);
        fn.code.writeInt(i);
    }

    public void writeOp(Ops op, short s, int i, short s2, short s3, byte b){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.writeShort(s);
        fn.code.writeInt(i);
        fn.code.writeShort(s2);
        fn.code.writeShort(s3);
        fn.code.write(b);
    }

    public void writeOp(Ops op, short s, short s2, int i, short s3, byte b){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.writeShort(s);
        fn.code.writeShort(s2);
        fn.code.writeInt(i);
        fn.code.writeShort(s3);
        fn.code.write(b);
    }

    public void writeOp(Ops op, short s, long l){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.writeShort(s);
        fn.code.writeLong(l);
    }

    public void writeOp(Ops op, short s1, short s2, short s3){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.writeShort(s1);
        fn.code.writeShort(s2);
        fn.code.writeShort(s3);
    }

    public void writeOp(Ops op, short s1, short s2, short s3, byte b){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.writeShort(s1);
        fn.code.writeShort(s2);
        fn.code.writeShort(s3);
        fn.code.write(b);
    }

    public void writeOp(Ops op, short s1, short s2, short s3, short s4){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.writeShort(s1);
        fn.code.writeShort(s2);
        fn.code.writeShort(s3);
        fn.code.writeShort(s4);
    }

    public void writeOp(Ops op, short s1, short s2, int i){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.writeShort(s1);
        fn.code.writeShort(s2);
        fn.code.writeInt(i);
    }

    public void writeOp(Ops op, short s1, short s2, short s3, int i){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.writeShort(s1);
        fn.code.writeShort(s2);
        fn.code.writeShort(s3);
        fn.code.writeInt(i);
    }

    public int getCurrentCodeIndex(){
        return generatingFns.peek().code.size();
    }

}
