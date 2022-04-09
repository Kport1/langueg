package com.kport.langueg.parse.ast.astVals;

public class ASTFloat extends ASTValue {
    private final float val;

    @Override
    public boolean isFloat(){return true;}

    @Override
    public float getFloat(){return val;}

    public ASTFloat(float val_){
        val = val_;
    }

    @Override
    public String toString(){
        return String.valueOf(val);
    }
}
