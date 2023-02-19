package com.kport.langueg.typeCheck.types;

import com.kport.langueg.parse.ast.AST;

public class TypeConversionException extends Exception{
    public AST notType;

    public TypeConversionException(AST notType_){
        notType = notType_;
    }
}
