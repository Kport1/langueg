package com.kport.langueg.lex;

public class Token {
    public TokenType tok;
    public String val;

    public int lineNum;
    public int columnNum;

    public Token(TokenType tok_){
        tok = tok_;
    }

    public Token(TokenType tok_, String val_){
        tok = tok_;
        val = val_;
    }

    public Token(TokenType tok_, int lineNum_, int columnNum_){
        tok = tok_;
        lineNum = lineNum_;
        columnNum = columnNum_;
    }

    public Token(TokenType tok_, String val_, int lineNum_, int columnNum_){
        tok = tok_;
        val = val_;
        lineNum = lineNum_;
        columnNum = columnNum_;
    }

    @Override
    public String toString(){
        return  tok.name() + "( " +
                (val != null? val + ", " : "") +
                "l: " + lineNum + ", " +
                "c: " + columnNum +
                " )";
    }
}
