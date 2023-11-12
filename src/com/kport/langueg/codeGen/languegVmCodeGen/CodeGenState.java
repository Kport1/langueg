package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.parse.ast.nodes.FnParamDef;
import com.kport.langueg.parse.ast.nodes.NFn;
import com.kport.langueg.parse.ast.nodes.NProg;
import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.FnHeader;
import com.kport.langueg.util.FnIdentifier;
import com.kport.langueg.util.VarIdentifier;

import java.util.*;

public class CodeGenState {
    public final List<FnData> generatedFns = new ArrayList<>();

    public final Stack<FnData> generatingFns = new Stack<>();

    public FnHeader modInterface;

    public void enterProg(NProg prog){
        modInterface = prog.moduleInterface.getHeader();

        generatingFns.push(new FnData(null, new LanguegVmValSize[0]));
        generatedFns.add(generatingFns.peek());
    }

    public void exitProg(){
        writeOp(Ops.RET);
        generatingFns.pop();
    }

    public int enterFn(NFn fn){
        generatingFns.push(new FnData(
                fn.getReturnType().getSize(),
                Arrays.stream(fn.getParamTypes()).map(Type::getSize).toArray(LanguegVmValSize[]::new)
        ));
        generatedFns.add(generatingFns.peek());
        for (FnParamDef param : fn.getParams()) {
            registerLocal(new VarIdentifier(fn.getBlockScope(), param.name), param.type.getSize());
        }
        return generatedFns.indexOf(generatingFns.peek());
    }

    public int exitFn(){
        writeOp(Ops.RET);
        return generatedFns.indexOf(generatingFns.pop());
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
    public short getLocalIndex(VarIdentifier id, LanguegVmValSize size){
        if(generatingFns.peek().localIndices.get(size).containsKey(id))
            return generatingFns.peek().localIndices.get(size).get(id).shortValue();
        if(id.scope().parent == null) throw new Error();
        return getLocalIndex(new VarIdentifier(id.scope().parent, id.name()), size);
    }

    private final Map<FnIdentifier, Integer> fnIndices = new HashMap<>();
    public void registerFn(FnIdentifier id, Integer index){
        fnIndices.put(id, index);
    }
    public short getFnIndex(FnIdentifier id){
        if(fnIndices.containsKey(id)) return fnIndices.get(id).shortValue();
        if(id.scope().parent == null) throw new Error();
        return getFnIndex(new FnIdentifier(id.scope().parent, id.name(), id.params()));
    }

    public final List<Integer> const32List = new ArrayList<>();
    private final Map<Integer, Integer> const32Indices = new HashMap<>();
    private int const32Count = 0;
    public short registerConst32(int val){
        if(const32Indices.containsKey(val)) return const32Indices.get(val).shortValue();
        if(const32Count == 2 << 16) throw new Error();
        const32Indices.put(val, const32Count);
        const32List.add(val);
        return (short)const32Count++;
    }

    public final List<Long> const64List = new ArrayList<>();
    private final Map<Long, Integer> const64Indices = new HashMap<>();
    private int const64Count = 0;
    public short registerConst64(long val){
        if(const64Indices.containsKey(val)) return const64Indices.get(val).shortValue();
        if(const64Count == 2 << 16) throw new Error();
        const64Indices.put(val, const64Count);
        const64List.add(val);
        return (short)const64Count++;
    }

    public void writeOp(Ops op){
        generatingFns.peek().code.write(op.code);
        for (Map.Entry<LanguegVmValSize, Integer> sizeEffect : op.stackSizeEffects.entrySet()) {
            generatingFns.peek().stackDepthCount.computeIfPresent(sizeEffect.getKey(), (size, depth) -> depth + sizeEffect.getValue());
            generatingFns.peek().maxStackDepth.computeIfPresent(sizeEffect.getKey(), (size, maxDepth) -> Math.max(maxDepth, generatingFns.peek().stackDepthCount.get(size)));
        }
    }

    public void writeOp(Ops op, byte b){
        generatingFns.peek().code.write(op.code);
        generatingFns.peek().code.write(b);
        for (Map.Entry<LanguegVmValSize, Integer> sizeEffect : op.stackSizeEffects.entrySet()) {
            generatingFns.peek().stackDepthCount.computeIfPresent(sizeEffect.getKey(), (size, depth) -> depth + sizeEffect.getValue());
            generatingFns.peek().maxStackDepth.computeIfPresent(sizeEffect.getKey(), (size, maxDepth) -> Math.max(maxDepth, generatingFns.peek().stackDepthCount.get(size)));
        }
    }

    public void writeOp(Ops op, short s){
        generatingFns.peek().code.write(op.code);
        generatingFns.peek().code.writeShort(s);
        for (Map.Entry<LanguegVmValSize, Integer> sizeEffect : op.stackSizeEffects.entrySet()) {
            generatingFns.peek().stackDepthCount.computeIfPresent(sizeEffect.getKey(), (size, depth) -> depth + sizeEffect.getValue());
            generatingFns.peek().maxStackDepth.computeIfPresent(sizeEffect.getKey(), (size, maxDepth) -> Math.max(maxDepth, generatingFns.peek().stackDepthCount.get(size)));
        }
    }

    public void writeOp(Ops op, int i){
        generatingFns.peek().code.write(op.code);
        generatingFns.peek().code.writeInt(i);
        for (Map.Entry<LanguegVmValSize, Integer> sizeEffect : op.stackSizeEffects.entrySet()) {
            generatingFns.peek().stackDepthCount.computeIfPresent(sizeEffect.getKey(), (size, depth) -> depth + sizeEffect.getValue());
            generatingFns.peek().maxStackDepth.computeIfPresent(sizeEffect.getKey(), (size, maxDepth) -> Math.max(maxDepth, generatingFns.peek().stackDepthCount.get(size)));
        }
    }

    public void writeOp(Ops op, long l){
        generatingFns.peek().code.write(op.code);
        generatingFns.peek().code.writeLong(l);
        for (Map.Entry<LanguegVmValSize, Integer> sizeEffect : op.stackSizeEffects.entrySet()) {
            generatingFns.peek().stackDepthCount.computeIfPresent(sizeEffect.getKey(), (size, depth) -> depth + sizeEffect.getValue());
            generatingFns.peek().maxStackDepth.computeIfPresent(sizeEffect.getKey(), (size, maxDepth) -> Math.max(maxDepth, generatingFns.peek().stackDepthCount.get(size)));
        }
    }

    public int getCurrentCodeIndex(){
        return generatingFns.peek().code.size();
    }

}
