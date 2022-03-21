package com.kport.langueg.parse.typeCheck.types;

public class TupleType extends Type{

    public TupleType(Type... types){
        super();
        tupleTypes = types;
        isTuple = true;
    }
}
