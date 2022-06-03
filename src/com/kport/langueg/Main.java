package com.kport.langueg;

import com.kport.langueg.codeGen.mcDataCodeGen.MCDataCodeGenerator;
import com.kport.langueg.lex.DefaultLexer;
import com.kport.langueg.parse.DefaultParser;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.pipeline.LanguegPipelineBuilder;
import com.kport.langueg.typeCheck.DefaultTypeChecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    public static void main(String[] args) throws IOException {

        String code = Files.readString(Path.of("src/com/kport/langueg/test.txt"));

        AtomicLong time = new AtomicLong();
        LanguegPipelineBuilder<String, AST> pipelineBuilder = new LanguegPipelineBuilder<>();
        pipelineBuilder.addComponent(new DefaultLexer(),
                        (o) -> time.set(System.nanoTime()),
                        (o) ->  {
                                    System.out.println("lex time: " + ((System.nanoTime() - time.get()) / 1_000_000_000f));
                                })
                .addComponent(new DefaultParser(),
                        (o) -> time.set(System.nanoTime()),
                        (o) ->  {
                                    System.out.println("parse time: " + ((System.nanoTime() - time.get()) / 1_000_000_000f));
                                })
                .addComponent(new DefaultTypeChecker(),
                        (o) -> time.set(System.nanoTime()),
                        (o) ->  {
                                    System.out.println("type check time: " + ((System.nanoTime() - time.get()) / 1_000_000_000f));
                                })
                .addComponent(new MCDataCodeGenerator(),
                        (o) -> {},
                        (o) -> {});

        LanguegPipeline<String, AST> pipeline = pipelineBuilder.get();
        System.out.println(pipeline.evaluate(code));
    }
}
