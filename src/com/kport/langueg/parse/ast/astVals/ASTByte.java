package com.kport.langueg.parse.ast.astVals;

public class ASTByte extends ASTValue {
    private final byte val;

    @Override
    public boolean isByte(){return true;}

    @Override
    public byte getByte(){return val;}

    public ASTByte(byte val_){
        val = val_;
    }

    @Override
    public String toString(){
        return String.valueOf(val);
    }
}
