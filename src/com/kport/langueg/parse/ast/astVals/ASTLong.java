package com.kport.langueg.parse.ast.astVals;

public class ASTLong extends ASTValue {
    private final long val;

    @Override
    public boolean isLong(){return true;}

    @Override
    public long getLong(){return val;}

    public ASTLong(long val_){
        val = val_;
    }

    @Override
    public String toString(){
        return String.valueOf(val);
    }
}
