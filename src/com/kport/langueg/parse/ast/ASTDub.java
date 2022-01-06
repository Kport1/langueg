package com.kport.langueg.parse.ast;

public class ASTDub extends ASTValue{
    private final double val;

    @Override
    public boolean isDub(){return true;}

    @Override
    public double getDub(){return val;}

    public ASTDub(double val_){
        val = val_;
    }

    @Override
    public String toString(){
        return String.valueOf(val);
    }
}
