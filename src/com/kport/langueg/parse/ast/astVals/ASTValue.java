package com.kport.langueg.parse.ast.astVals;


import com.kport.langueg.lex.TokenType;
import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.FnIdentifier;

//String, Integer, Double
public abstract class ASTValue {

    public boolean isStr() {
        return false;
    }

    public boolean isByte() {
        return false;
    }

    public boolean isShort(){
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

    public byte getByte() {
        return Byte.MIN_VALUE;
    }

    public short getShort(){
        return Short.MIN_VALUE;
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

    public double getDouble() {
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
