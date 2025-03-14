package com.kport.langueg.codeGen.languegVmCodeGen;

public final class ScopeData implements Cloneable {

    public int nextUnallocatedLocalByte = 0;

    public ScopeData() {
    }

    @Override
    public ScopeData clone() throws CloneNotSupportedException {
        ScopeData scopeData = (ScopeData) super.clone();
        scopeData.nextUnallocatedLocalByte = nextUnallocatedLocalByte;
        return scopeData;
    }
}
