package com.kport.langueg.parse.ast.astVals;

import com.kport.langueg.typeCheck.types.Type;

public class ASTType extends ASTValue {
    public Type type;

    public ASTType(Type type_){
        type = type_;
    }

    @Override
    public boolean isType(){
        return true;
    }

    @Override
    public Type getType(){
        return type;
    }

    @Override
    public String toString(){
        return type.toString();
    }
}
