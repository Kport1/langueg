package com.kport.langueg.lex;

public class Token {
    public TokenType tok;
    public String val;

    public int offset;

    public Token(TokenType tok_){
        tok = tok_;
    }

    public Token(TokenType tok_, String val_){
        tok = tok_;
        val = val_;
    }

    @Override
    public String toString(){
        return  tok.name() + "( " +
                (val != null? val + ", " : "") +
                "o: " + offset +
                " )";
    }
}
