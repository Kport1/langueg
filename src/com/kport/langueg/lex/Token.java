package com.kport.langueg.lex;

import com.kport.langueg.parse.ast.CodeLocatable;
import com.kport.langueg.util.Span;

public class Token implements CodeLocatable {
    public TokenType tok;
    public String val;

    public Span location;

    public Token(TokenType tok_) {
        tok = tok_;
    }

    public Token(TokenType tok_, String val_) {
        tok = tok_;
        val = val_;
    }

    @Override
    public String toString() {
        return tok.name() + "( " +
                (val != null ? val + ", " : "") +
                "l: " + location +
                " )";
    }

    @Override
    public Span location() {
        return location;
    }
}
