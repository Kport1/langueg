package com.kport.langueg.parse.ast;

public class ASTBool extends ASTValue {
    private final boolean val;

    public ASTBool(boolean val_){
        val = val_;
    }

    @Override
    public boolean isBool(){
        return false;
    }

    @Override
    public boolean getBool(){
        return val;
    }

    @Override
    public String toString(){
        return String.valueOf(val);
    }
}
