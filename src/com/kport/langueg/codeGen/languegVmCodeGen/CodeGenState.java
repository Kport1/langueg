package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.parse.ast.nodes.NameTypePair;
import com.kport.langueg.parse.ast.nodes.NFn;
import com.kport.langueg.parse.ast.nodes.NProg;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NDotAccess;
import com.kport.langueg.typeCheck.types.TupleType;
import com.kport.langueg.typeCheck.types.UnionType;
import com.kport.langueg.util.Identifier;
import com.kport.langueg.util.Scope;

import java.util.*;

public class CodeGenState {
    public final List<FnData> generatedFns = new ArrayList<>();

    public final Stack<FnData> generatingFns = new Stack<>();

    public void enterProg(NProg prog){
        generatingFns.push(new FnData(new byte[0]));
        generatedFns.add(generatingFns.peek());
    }

    public void exitProg(){
        writeOp(Ops.RET);
        generatingFns.pop();
    }

    public int enterFn(NFn fn){
        byte[] paramSizes = new byte[fn.getFnHeader().paramTypes().length];
        for (int i = 0; i < fn.getFnHeader().paramTypes().length; i++) {
            int size = fn.getFnHeader().paramTypes()[i].getSize();
            if(size > 16) throw new Error("Type does not fit onto stack");
            paramSizes[i] = (byte)size;
        }
        generatingFns.push(new FnData(paramSizes));
        generatedFns.add(generatingFns.peek());
        for (NameTypePair param : fn.getFnHeader().params) {
            allocateLocal(new Identifier(fn.getBodyScope(), param.name), (byte)param.type.getSize());
        }
        return generatedFns.indexOf(generatingFns.peek());
    }

    public int exitFn(){
        writeOp(Ops.RET);
        return generatedFns.indexOf(generatingFns.pop());
    }

    public void enterScope(){
        Stack<ScopeData> scopeData = generatingFns.peek().scopeData;
        try {
            scopeData.push(scopeData.peek().clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public void exitScope(){
        generatingFns.peek().scopeData.pop();
    }

    public short allocateLocal(Identifier id, byte size){
        return generatingFns.peek().allocateLocal(id, size);
    }

    public short getLocalOffset(Identifier id){
        return generatingFns.peek().getLocalOffset(id);
    }

    public short allocateTempLocal(byte size){
        return generatingFns.peek().allocateTempLocal(size);
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

    public final SequencedMap<byte[], Integer> constIndices = new LinkedHashMap<>();
    private int constCount = 0;
    public short registerConst(byte[] val){
        if(val.length != 16) throw new Error();
        if(constIndices.containsKey(val)) return constIndices.get(val).shortValue();
        if(constCount == 2 << 16) throw new Error();
        constIndices.put(val, constCount);
        return (short)constCount++;
    }

    public void pushInt(int val){
        if(Integer.compareUnsigned(val, 2 << 8) < 0){
            writeOp(Ops.PUSH8, (byte)(val&0xFF));
            return;
        }
        if(Integer.compareUnsigned(val, 2 << 16) < 0){
            writeOp(Ops.PUSH16, (short)(val&0xFFFF));
            return;
        }
        writeOp(Ops.PUSHP, registerConst(new byte[]{
                (byte)(val & 0xFF),
                (byte)((val >> 8) & 0xFF),
                (byte)((val >> 16) & 0xFF),
                (byte)((val >> 24) & 0xFF),
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
        }));
    }

    public void pushLong(long val){
        if(Long.compareUnsigned(val, 2 << 8) < 0){
            writeOp(Ops.PUSH8, (byte)(val&0xFF));
            return;
        }
        if(Long.compareUnsigned(val, 2 << 16) < 0){
            writeOp(Ops.PUSH16, (short)(val&0xFFFF));
            return;
        }
        writeOp(Ops.PUSHP, registerConst(new byte[]{
                (byte)(val & 0xFF),
                (byte)((val >> 8) & 0xFF),
                (byte)((val >> 16) & 0xFF),
                (byte)((val >> 24) & 0xFF),
                (byte)((val >> 32) & 0xFF),
                (byte)((val >> 40) & 0xFF),
                (byte)((val >> 48) & 0xFF),
                (byte)((val >> 56) & 0xFF),
                0, 0, 0, 0,
                0, 0, 0, 0
        }));
    }

    public void writeOp(Ops op){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);

        fn.stackDepth += op.stackEffect;
        fn.maxStackDepth = Math.max(fn.stackDepth, fn.maxStackDepth);
    }

    public void writeOp(Ops op, byte b){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.write(b);

        fn.stackDepth += op.stackEffect;
        fn.maxStackDepth = Math.max(fn.stackDepth, fn.maxStackDepth);
    }

    public void writeOp(Ops op, short s){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.writeShort(s);

        fn.stackDepth += op.stackEffect;
        fn.maxStackDepth = Math.max(fn.stackDepth, fn.maxStackDepth);
    }

    public void writeOp(Ops op, int i){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.writeInt(i);

        fn.stackDepth += op.stackEffect;
        fn.maxStackDepth = Math.max(fn.stackDepth, fn.maxStackDepth);
    }

    public void writeOp(Ops op, long l){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.writeLong(l);

        fn.stackDepth += op.stackEffect;
        fn.maxStackDepth = Math.max(fn.stackDepth, fn.maxStackDepth);
    }

    public void writeOp(Ops op, short s, byte b){
        FnData fn = generatingFns.peek();
        fn.code.write(op.code);
        fn.code.writeShort(s);
        fn.code.write(b);

        fn.stackDepth += op.stackEffect;
        fn.maxStackDepth = Math.max(fn.stackDepth, fn.maxStackDepth);
    }

    public int getCurrentCodeIndex(){
        return generatingFns.peek().code.size();
    }

}
