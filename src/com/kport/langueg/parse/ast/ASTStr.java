package com.kport.langueg.parse.ast;

public class ASTStr extends ASTValue{
    private final String val;

    @Override
    public boolean isStr(){return true;}

    @Override
    public String getStr(){return val;}

    public ASTStr(String val_){
        val = val_;
    }

    @Override
    public String toString(){
        return val;
    }
}
