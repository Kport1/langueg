package com.kport.langueg.typeCheck.types;

public class OverloadedFnType extends Type {

    public OverloadedFnType(Type... overloadedFns_){
        super();
        overloadedFns = overloadedFns_;
        isOverloaded = true;
    }
}
