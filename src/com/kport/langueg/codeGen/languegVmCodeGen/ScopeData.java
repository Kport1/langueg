package com.kport.langueg.codeGen.languegVmCodeGen;

import java.util.EnumMap;
import java.util.Map;

public final class ScopeData implements Cloneable{

    public final Map<LanguegVmValSize, Integer> localCount = new EnumMap<>(LanguegVmValSize.class);

    public ScopeData(){
        for (LanguegVmValSize size : LanguegVmValSize.values()) {
            localCount.put(size, 0);
        }
    }

    @Override
    public ScopeData clone() throws CloneNotSupportedException {
        ScopeData scopeData = (ScopeData) super.clone();
        scopeData.localCount.putAll(localCount);
        return scopeData;
    }
}
