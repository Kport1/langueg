package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.typeCheck.SymbolTable;
import com.kport.langueg.util.CodeOutputStream;
import com.kport.langueg.util.Identifier;
import com.kport.langueg.util.Scope;

import java.util.*;

public class FnData {
    public final LanguegVmValSize returnValSize;
    public final LanguegVmValSize[] paramValSizes;

    public final Stack<ScopeData> scopeData = new Stack<>();

    public final Map<LanguegVmValSize, Integer> amntLocals = new EnumMap<>(LanguegVmValSize.class);
    public final Map<LanguegVmValSize, Integer> maxStackDepth = new EnumMap<>(LanguegVmValSize.class);
    public final Map<LanguegVmValSize, Map<Identifier, Integer>> localIndices = new EnumMap<>(LanguegVmValSize.class);
    public final Map<LanguegVmValSize, Integer> stackDepthCount = new EnumMap<>(LanguegVmValSize.class);

    public final CodeOutputStream code = new CodeOutputStream();

    public FnData(LanguegVmValSize returnValSize_, LanguegVmValSize[] paramValSizes_){
        returnValSize = returnValSize_;
        paramValSizes = paramValSizes_;
        for (LanguegVmValSize size : LanguegVmValSize.values()) {
            amntLocals.put(size, 0);
            maxStackDepth.put(size, 0);
            localIndices.put(size, new HashMap<>());
            stackDepthCount.put(size, 0);
        }
        scopeData.push(new ScopeData());
    }

    public short allocateLocal(Identifier id, LanguegVmValSize size){
        int nextIndex = scopeData.peek().localCount.get(size);
        if(nextIndex >= 2 << 16) throw new Error();

        localIndices.get(size).put(id, nextIndex);
        scopeData.peek().localCount.put(size, nextIndex + 1);
        amntLocals.computeIfPresent(size, (_size, prev) -> Math.max(prev, nextIndex + 1));
        return (short) nextIndex;
    }

    public short getLocalIndex(Identifier id, LanguegVmValSize size){
        Scope parentScope = id.scope();
        while (parentScope != null){
            if(localIndices.get(size).containsKey(new Identifier(parentScope, id.name())))
                return localIndices.get(size).get(new Identifier(parentScope, id.name())).shortValue();
            parentScope = parentScope.parent;
        }
        throw new Error();
    }

    @Override
    public String toString(){
        StringBuilder s = new StringBuilder();
        s.append("RetS: ").append(returnValSize).append("\n");

        for (LanguegVmValSize size : LanguegVmValSize.values()) {
            s.append("AmntL ").append(size).append(": ").append(amntLocals.get(size)).append("\n");
        }

        for (LanguegVmValSize size : LanguegVmValSize.values()) {
            s.append("MaxSD ").append(size).append(": ").append(maxStackDepth.get(size)).append("\n");
        }

        for (LanguegVmValSize size : LanguegVmValSize.values()) {
            s.append("EndSD ").append(size).append(": ").append(stackDepthCount.get(size)).append("\n");
        }

        for (LanguegVmValSize size : LanguegVmValSize.values()) {
            Map<Identifier, Integer> locals = localIndices.get(size);
            for (Map.Entry<Identifier, Integer> localIndex : locals.entrySet()) {
                s.append("Local ").append(size).append(": ").append(localIndex.getKey()).append(" = ").append(localIndex.getValue()).append("\n");
            }
        }

        s.append("Code :\n").append(code);

        return s.toString();
    }
}
