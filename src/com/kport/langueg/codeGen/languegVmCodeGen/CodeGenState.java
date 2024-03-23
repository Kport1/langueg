package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.parse.ast.nodes.NameTypePair;
import com.kport.langueg.parse.ast.nodes.NFn;
import com.kport.langueg.parse.ast.nodes.NProg;
import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.Identifier;
import com.kport.langueg.util.Scope;

import java.util.*;

public class CodeGenState {
    public final List<FnData> generatedFns = new ArrayList<>();

    public final Stack<FnData> generatingFns = new Stack<>();

    public void enterProg(NProg prog){
        generatingFns.push(new FnData(null, new LanguegVmValSize[0]));
        generatedFns.add(generatingFns.peek());
    }

    public void exitProg(){
        writeOp(Ops.RET);
        generatingFns.pop();
    }

    public int enterFn(NFn fn){
        generatingFns.push(new FnData(
                fn.getFnHeader().returnType.getSize(),
                Arrays.stream(fn.getFnHeader().paramTypes()).map(Type::getSize).toArray(LanguegVmValSize[]::new)
        ));
        generatedFns.add(generatingFns.peek());
        for (NameTypePair param : fn.getFnHeader().params) {
            allocateLocal(new Identifier(fn.getBodyScope(), param.name), param.type.getSize());
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

    public short allocateLocal(Identifier id, LanguegVmValSize size){
        return generatingFns.peek().allocateLocal(id, size);
    }

    public short getLocalIndex(Identifier id, LanguegVmValSize size){
        return generatingFns.peek().getLocalIndex(id, size);
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
