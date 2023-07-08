package com.kport.langueg.parse.ast.astVals;

import com.kport.langueg.lex.TokenType;

public class ASTTok extends ASTValue {
    private final TokenType tok;

    public ASTTok(TokenType op_){
        tok = op_;
    }

    @Override
    public boolean isTok(){
        return true;
    }

    @Override
    public TokenType getTok(){
        return tok;
    }

    @Override
    public String toString(){
        return tok.name();
    }
}
