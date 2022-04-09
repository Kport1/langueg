package com.kport.langueg.parse.ast.astVals;

public class ASTDouble extends ASTValue {
    private final double val;

    @Override
    public boolean isDub(){return true;}

    @Override
    public double getDub(){return val;}

    public ASTDouble(double val_){
        val = val_;
    }

    @Override
    public String toString(){
        return String.valueOf(val);
    }
}
