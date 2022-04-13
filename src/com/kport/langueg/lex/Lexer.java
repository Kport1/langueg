package com.kport.langueg.lex;

import com.kport.langueg.pipeline.LanguegComponent;

import java.util.ArrayList;

public interface Lexer extends LanguegComponent {

    @Override
    ArrayList<Token> process(Object input);

}
