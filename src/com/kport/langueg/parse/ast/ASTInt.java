package com.kport.langueg.parse.ast;

public class ASTInt extends ASTValue{
    private final int val;

    @Override
    public boolean isInt(){return true;}

    @Override
    public int getInt(){return val;}

    public ASTInt(int val_){
        val = val_;
    }

    @Override
    public String toString(){
        return String.valueOf(val);
    }
}
