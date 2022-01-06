package com.kport.langueg;

import com.kport.langueg.lex.DefaultLexer;
import com.kport.langueg.lex.Lexer;
import com.kport.langueg.lex.Token;
import com.kport.langueg.parse.DefaultParser;
import com.kport.langueg.parse.Parser;
import com.kport.langueg.parse.ast.AST;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        Lexer lex = new DefaultLexer();
        long t1 = System.nanoTime();
        ArrayList<Token> tokens = lex.lex(Files.readString(Path.of("src/com/kport/langueg/test.txt")));
        System.out.println("lex time: " + ((System.nanoTime() - t1) / 1_000_000_000f));
        System.out.println(tokens);
        Parser pars = new DefaultParser();
        t1 = System.nanoTime();
        AST block = pars.parse(tokens);
        System.out.println("parse time: " + ((System.nanoTime() - t1) / 1_000_000_000f));
        System.out.println(block);


        /*
        Comments:
        lex: 1.21s
        parse: 0.34s

        Intense syntax yea:
        lex: 3.58s
        parse:1.27s

        Strings:
        lex: 1.93s
        parse: 0.61s
         */
    }
}
