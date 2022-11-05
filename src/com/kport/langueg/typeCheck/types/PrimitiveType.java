package com.kport.langueg.typeCheck.types;

import com.kport.langueg.lex.TokenType;

public enum PrimitiveType implements Type{
    Void(TokenType.Void),

    Boolean(TokenType.Boolean),
    Char(TokenType.Char),

    Byte(TokenType.Byte),
    Short(TokenType.Short),
    Int(TokenType.Int),
    Long(TokenType.Long),

    Float(TokenType.Float),
    Double(TokenType.Double);


    private final TokenType type;

    PrimitiveType(TokenType type_){
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
}
