package com.kport.langueg.util;

import com.kport.langueg.parse.ast.nodes.FnParamDef;
import com.kport.langueg.typeCheck.types.Type;

import java.util.Arrays;

public record FnHeader(Type returnType, FnParamDef[] params, String name) {

    public Type[] getParamTypes(){
        return Arrays.stream(params).map(p -> p.type).toArray(Type[]::new);
    }

}
