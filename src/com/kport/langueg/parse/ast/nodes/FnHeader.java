package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.typeCheck.types.FnType;
import com.kport.langueg.typeCheck.types.Type;

import java.util.Arrays;

public final class FnHeader {
    public NameTypePair[] params;
    public Type returnType;

    public FnHeader(NameTypePair[] params_, Type returnType_){
        params = params_;
        returnType = returnType_;
    }

    public FnType getFnType(){
        return new FnType(returnType, paramTypes());
    }

    public Type[] paramTypes(){
        return Arrays.stream(params).map(p -> p.type).toArray(Type[]::new);
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder("(");
        for (NameTypePair param : params) {
            str.append(param);
            str.append(", ");
        }
        str.delete(str.length() - 2, str.length());
        str.append(") -> ");
        str.append(returnType);
        return str.toString();
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof FnHeader a)) return false;
        return Arrays.deepEquals(params, a.params) && returnType.equals(a.returnType);
    }
}
