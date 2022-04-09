package com.kport.langueg;

import com.kport.langueg.lex.DefaultLexer;
import com.kport.langueg.lex.Lexer;
import com.kport.langueg.lex.Token;
import com.kport.langueg.parse.DefaultParser;
import com.kport.langueg.parse.Parser;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.typeCheck.DefaultTypeChecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException {

        String code = Files.readString(Path.of("src/com/kport/langueg/test.txt"));

        Lexer lex = new DefaultLexer();
        long t1 = System.nanoTime();
        ArrayList<Token> tokens = lex.lex(code);
        System.out.println("lex time: " + ((System.nanoTime() - t1) / 1_000_000_000f));
        System.out.println(tokens);

        Parser pars = new DefaultParser();
        t1 = System.nanoTime();
        AST block = pars.parse(tokens);
        System.out.println("parse time: " + ((System.nanoTime() - t1) / 1_000_000_000f));
        System.out.println(block);

        t1 = System.nanoTime();
        new DefaultTypeChecker().check(block);
        System.out.println("type check time: " + ((System.nanoTime() - t1) / 1_000_000_000f));
    }
}
