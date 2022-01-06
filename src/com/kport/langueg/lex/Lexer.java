package com.kport.langueg.lex;

import java.util.ArrayList;

public interface Lexer {

    ArrayList<Token> lex(String code);

}
