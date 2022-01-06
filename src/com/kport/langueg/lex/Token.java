package com.kport.langueg.lex;

public class Token {
    public TokenType tok;
    public String val;

    public int lineNum;

    public Token(TokenType tok_){
        tok = tok_;
    }

    public Token(TokenType tok_, String val_){
        tok = tok_;
        val = val_;
    }

    public Token(TokenType tok_, int lineNum_){
        tok = tok_;
        lineNum = lineNum_;
    }

    public Token(TokenType tok_, String val_, int lineNum_){
        tok = tok_;
        val = val_;
        lineNum = lineNum_;
    }

    @Override
    public String toString(){
        if(tok == null){
            System.out.println(val + "  :  " + lineNum);
        }

        return tok.name() + (val == null ? "" : "('" + val + "')");
    }
}
