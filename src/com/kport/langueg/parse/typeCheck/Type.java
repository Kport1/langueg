package com.kport.langueg.parse.typeCheck;

import com.kport.langueg.lex.TokenType;

public class Type {
    private TokenType primitiveType = null;
    private String typeName = null;

    public Type(TokenType primitive){
        primitiveType = primitive;
    }

    public Type(String typeName_){
        typeName = typeName_;
    }

    public boolean isPrimitive(){
        return primitiveType != null;
    }

    public TokenType primitive() {
        return primitiveType;
    }

    public String name(){
        return typeName;
    }

    @Override
    public String toString(){
        if(isPrimitive()){
            return primitiveType.name();
        }
        return typeName;
    }
}
