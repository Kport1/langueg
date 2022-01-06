package com.kport.langueg.parse.ast;


import com.kport.langueg.lex.TokenType;

//String, Integer, Double
public abstract class ASTValue {

    public boolean isStr() {
        return false;
    }

    public boolean isInt() {
        return false;
    }

    public boolean isDub() {
        return false;
    }

    public boolean isOp() {
        return false;
    }

    public String getStr() {
        return "";
    }

    public int getInt() {
        return Integer.MIN_VALUE;
    }

    public double getDub() {
        return Double.NaN;
    }

    public TokenType getOp() {
        return TokenType.Undefined;
    }

}
