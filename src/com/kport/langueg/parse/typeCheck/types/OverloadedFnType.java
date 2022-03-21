package com.kport.langueg.parse.typeCheck.types;

import com.kport.langueg.parse.typeCheck.types.Type;

public class OverloadedFnType extends Type {

    public OverloadedFnType(Type... overloadedFns_){
        super();
        overloadedFns = overloadedFns_;
        isOverloaded = true;
    }
}
