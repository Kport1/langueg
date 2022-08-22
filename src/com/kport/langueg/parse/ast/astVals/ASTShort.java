package com.kport.langueg.parse.ast.astVals;

public class ASTShort extends ASTValue {
    private final short val;

    @Override
    public boolean isShort(){return true;}

    @Override
    public short getShort(){return val;}

    public ASTShort(short val_){
        val = val_;
    }

    @Override
    public String toString(){
        return String.valueOf(val);
    }
}
