package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.util.CodeOutputStream;
import com.kport.langueg.util.FnIdentifier;
import com.kport.langueg.util.VarIdentifier;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class FnData {
    public final LanguegVmValSize returnValSize;

    public final Map<LanguegVmValSize, Integer> amntLocals = new EnumMap<>(LanguegVmValSize.class);
    {
        for (LanguegVmValSize size : LanguegVmValSize.values()) {
            amntLocals.put(size, 0);
        }
    }
    public final Map<LanguegVmValSize, Integer> maxStackDepth = new EnumMap<>(LanguegVmValSize.class);
    {
        for (LanguegVmValSize size : LanguegVmValSize.values()) {
            maxStackDepth.put(size, 0);
        }
    }

    public final Map<LanguegVmValSize, Stack<Integer>> localCount = new EnumMap<>(LanguegVmValSize.class);
    {
        for (LanguegVmValSize size : LanguegVmValSize.values()) {
            localCount.put(size, new Stack<>());
            localCount.get(size).push(0);
        }
    }

    public final Map<LanguegVmValSize, Map<VarIdentifier, Integer>> localIndices = new EnumMap<>(LanguegVmValSize.class);
    {
        for (LanguegVmValSize size : LanguegVmValSize.values()) {
            localIndices.put(size, new HashMap<>());
        }
    }

    public final CodeOutputStream code = new CodeOutputStream();

    public FnData(LanguegVmValSize returnValSize_){
        returnValSize = returnValSize_;
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
            Map<VarIdentifier, Integer> locals = localIndices.get(size);
            for (Map.Entry<VarIdentifier, Integer> localIndex : locals.entrySet()) {
                s.append("Local ").append(size).append(": ").append(localIndex.getKey()).append(" = ").append(localIndex.getValue()).append("\n");
            }
        }

        s.append("Code :\n").append(code);

        return s.toString();
    }
}
