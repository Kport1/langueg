package com.kport.langueg.typeCheck.types;

import com.kport.langueg.lex.TokenType;

import java.util.Arrays;
import java.util.Objects;

public abstract class Type {

    public boolean isPrimitive(){
        return false;
    }

    public boolean isCustom(){
        return false;
    }

    public boolean isFn(){
        return false;
    }

    public boolean isTuple(){
        return false;
    }

    public TokenType primitive() {
        return null;
    }

    public String name(){
        return null;
    }

    public Type getFnReturn(){
        return null;
    }

    public Type[] getFnArgs() {
        return null;
    }

    public Type[] getTupleTypes(){
        return null;
    }
}
