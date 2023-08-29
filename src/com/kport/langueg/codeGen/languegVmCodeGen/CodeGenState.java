package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.util.FnIdentifier;
import com.kport.langueg.util.VarIdentifier;

import java.util.*;

public class CodeGenState {
    public final List<FnData> generatedFns = new ArrayList<>();


    private final Stack<FnData> generatingFns = new Stack<>();

    public void enterFn(LanguegVmValSize returnSize){
        generatingFns.push(new FnData(returnSize));
    }

    public int exitFn(){
        generatedFns.add(generatingFns.pop());
        return generatedFns.size() - 1;
    }

    public void enterScope(){
        for (LanguegVmValSize size : LanguegVmValSize.values()) {
            Stack<Integer> s = generatingFns.peek().localCount.get(size);
            s.push(s.peek());
        }
    }

    public void exitScope(){
        for (LanguegVmValSize size : LanguegVmValSize.values()) {
            generatingFns.peek().localCount.get(size).pop();
        }
    }

    public short registerLocal(VarIdentifier id, LanguegVmValSize size){
        Stack<Integer> localCount = generatingFns.peek().localCount.get(size);
        int toAllocateIndex = localCount.peek();

        if(toAllocateIndex == 2 << 16) throw new Error();

        generatingFns.peek().localIndices.get(size).put(id, toAllocateIndex);

        localCount.pop();
        localCount.push(toAllocateIndex + 1);

        generatingFns.peek().amntLocals.computeIfPresent(size, (_size, prev) -> Math.max(prev, localCount.peek()));
        return (short)(toAllocateIndex);
    }
    public int getLocalIndex(VarIdentifier id, LanguegVmValSize size){
        if(generatingFns.peek().localIndices.get(size).containsKey(id))
            return generatingFns.peek().localIndices.get(size).get(id);
        if(id.scope().parent == null) throw new Error();
        return getLocalIndex(new VarIdentifier(id.scope().parent, id.name()), size);
    }

    private final Map<FnIdentifier, Integer> fnIndices = new HashMap<>();
    public void registerFn(FnIdentifier id, Integer index){
        fnIndices.put(id, index);
    }
    public int getFnIndex(FnIdentifier id){
        if(fnIndices.containsKey(id)) return fnIndices.get(id);
        if(id.scope().parent == null) throw new Error();
        return getFnIndex(new FnIdentifier(id.scope().parent, id.name(), id.params()));
    }

    private final Map<Integer, Integer> const32Indices = new HashMap<>();
    private int const32Count = 0;
    public short registerConst32(int val){
        if(const32Indices.containsKey(val)) return const32Indices.get(val).shortValue();
        if(const32Count == 2 << 16) throw new Error();
        const32Indices.put(val, const32Count);
        return (short)const32Count++;
    }

    private final Map<Long, Integer> const64Indices = new HashMap<>();
    private int const64Count = 0;
    public short registerConst64(long val){
        if(const64Indices.containsKey(val)) return const64Indices.get(val).shortValue();
        if(const64Count == 2 << 16) throw new Error();
        const64Indices.put(val, const64Count);
        return (short)const64Count++;
    }

    public void writeOp(Ops op){
        generatingFns.peek().code.write(op.code);
    }

    public void writeOp(Ops op, byte b){
        generatingFns.peek().code.write(op.code);
        generatingFns.peek().code.write(b);
    }

    public void writeOp(Ops op, short s){
        generatingFns.peek().code.write(op.code);
        generatingFns.peek().code.writeShort(s);
    }

    public void writeOp(Ops op, int i){
        generatingFns.peek().code.write(op.code);
        generatingFns.peek().code.writeInt(i);
    }

    public void writeOp(Ops op, long l){
        generatingFns.peek().code.write(op.code);
        generatingFns.peek().code.writeLong(l);
    }

}
