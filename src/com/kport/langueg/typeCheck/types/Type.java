package com.kport.langueg.typeCheck.types;

import com.kport.langueg.lex.TokenType;

import java.util.Arrays;
import java.util.Objects;

public interface Type {

    default boolean isPrimitive(){
        return false;
    }

    default boolean isCustom(){
        return false;
    }

    default boolean isFn(){
        return false;
    }

    default boolean isTuple(){
        return false;
    }

    default TokenType primitive() {
        return null;
    }

    default String name(){
        return null;
    }

    default Type getFnReturn(){
        return null;
    }

    default Type[] getFnArgs() {
        return null;
    }

    default Type[] getTupleTypes(){
        return null;
    }
}
