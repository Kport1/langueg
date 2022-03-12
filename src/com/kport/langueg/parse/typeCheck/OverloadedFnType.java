package com.kport.langueg.parse.typeCheck;

public class OverloadedFnType extends Type{

    public OverloadedFnType(Type... overloadedFns_){
        super();
        overloadedFns = overloadedFns_;
        isOverloaded = true;
    }
}
