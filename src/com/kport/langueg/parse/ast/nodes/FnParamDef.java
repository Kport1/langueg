package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.typeCheck.types.Type;

public class FnParamDef {
    public Type type;
    public String name;

    public FnParamDef(Type type_, String name_){
        type = type_;
        name = name_;
    }

    @Override
    public String toString(){
        return type + " " + name;
    }
}
