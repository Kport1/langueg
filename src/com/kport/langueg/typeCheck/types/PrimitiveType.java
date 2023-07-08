package com.kport.langueg.typeCheck.types;

import com.kport.langueg.lex.TokenType;

public enum PrimitiveType implements Type{
    Void(TokenType.Void),

    Bool(TokenType.Boolean),
    Char(TokenType.Char),

    U8(TokenType.Byte),
    I16(TokenType.Short),
    I32(TokenType.Int),
    I64(TokenType.Long),

    F32(TokenType.Float),
    F64(TokenType.Double);


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
