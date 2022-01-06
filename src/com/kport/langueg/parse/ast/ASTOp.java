package com.kport.langueg.parse.ast;

import com.kport.langueg.lex.TokenType;

public class ASTOp extends ASTValue{
    public TokenType op;

    public ASTOp(TokenType op_){
        op = op_;
    }

    @Override
    public boolean isOp(){
        return true;
    }

    @Override
    public TokenType getOp(){
        return op;
    }

    @Override
    public String toString(){
        return op.name();
    }
}
