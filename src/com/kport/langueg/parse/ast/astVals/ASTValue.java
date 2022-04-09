package com.kport.langueg.parse.ast.astVals;


import com.kport.langueg.lex.TokenType;
import com.kport.langueg.typeCheck.types.Type;

//String, Integer, Double
public abstract class ASTValue {

    public boolean isStr() {
        return false;
    }

    public boolean isInt() {
        return false;
    }

    public boolean isLong() {
        return false;
    }

    public boolean isFloat() {
        return false;
    }

    public boolean isDub() {
        return false;
    }

    public boolean isBool(){
        return false;
    }

    public boolean isTok() {
        return false;
    }

    public boolean isType(){
        return false;
    }

    public String getStr() {
        return "";
    }

    public int getInt() {
        return Integer.MIN_VALUE;
    }

    public long getLong() {
        return Long.MIN_VALUE;
    }

    public float getFloat() {
        return Float.NaN;
    }

    public double getDub() {
        return Double.NaN;
    }

    public boolean getBool(){
        return false;
    }

    public TokenType getTok() {
        return TokenType.Undefined;
    }

    public Type getType(){
        return null;
    }

}
