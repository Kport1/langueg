package com.kport.langueg.parse;

import com.kport.langueg.lex.Token;
import com.kport.langueg.parse.ast.AST;

import java.util.ArrayList;

public interface Parser {

    AST parse(ArrayList<Token> tokens);

}
