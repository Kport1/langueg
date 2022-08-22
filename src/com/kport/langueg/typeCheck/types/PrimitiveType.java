package com.kport.langueg.typeCheck.types;

import com.kport.langueg.lex.TokenType;

public class PrimitiveType extends Type{
    private final TokenType type;

    public PrimitiveType(TokenType type_){
        type = type_;
    }

    @Override
    public boolean isPrimitive(){
        return true;
    }

    @Override
    public TokenType primitive() {
        return type;
    }

    @Override
    public String toString(){
        return type.name();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof PrimitiveType t){
            return t.type == type;
        }
        return false;
    }

    @Override
    public int hashCode(){
        return type.hashCode();
    }
}
