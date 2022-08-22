package com.kport.langueg.typeCheck.types;

import java.util.Arrays;

public class TupleType extends Type{
    private final Type[] tupleTypes;

    public TupleType(Type... types){
        tupleTypes = types;
    }

    @Override
    public boolean isTuple(){
        return true;
    }

    @Override
    public Type[] getTupleTypes(){
        return tupleTypes;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder("( ");

        for (Type type : tupleTypes) {
            builder.append(type);
            builder.append(", ");
        }

        builder.deleteCharAt(builder.length() - 2);
        builder.append(")");

        return builder.toString();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof TupleType t){
            return Arrays.equals(t.tupleTypes, tupleTypes);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return Arrays.hashCode(tupleTypes);
    }
}
