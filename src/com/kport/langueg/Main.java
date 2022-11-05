package com.kport.langueg;

import com.kport.langueg.codeGen.languegVmCodeGen.LanguegVmCodeGenerator;
import com.kport.langueg.codeGen.mcDataCodeGen.MCDataCodeGenerator;
import com.kport.langueg.fileWrite.DefaultFileWriter;
import com.kport.langueg.lex.DefaultLexer;
import com.kport.langueg.lex.Token;
import com.kport.langueg.parse.DefaultParser;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.pipeline.LanguegPipelineBuilder;
import com.kport.langueg.typeCheck.DefaultTypeChecker;
import com.kport.langueg.util.ScopeTree;
import com.sun.jdi.InvalidTypeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {

        String code = Files.readString(Path.of("src/com/kport/langueg/test.gueg"));

        AtomicLong time = new AtomicLong();
        LanguegPipelineBuilder<String, Void> pipelineBuilder = new LanguegPipelineBuilder<>();
        pipelineBuilder.addComponent(new DefaultLexer(),
                        (o, p) -> time.set(System.nanoTime()),
                        (o, p) ->  {
                                    System.out.println("lex time: " + ((System.nanoTime() - time.get()) / 1_000_000_000f));
                                    System.out.println("Tokens: " + o);
                                })
                .addComponent(new DefaultParser(),
                        (o, p) -> time.set(System.nanoTime()),
                        (o, p) ->  {
                                    System.out.println("parse time: " + ((System.nanoTime() - time.get()) / 1_000_000_000f));
                                })
                .addComponent(new DefaultTypeChecker(),
                        (o, p) -> time.set(System.nanoTime()),
                        (o, p) ->  {
                                    System.out.println("type check time: " + ((System.nanoTime() - time.get()) / 1_000_000_000f));
                                    System.out.println("AST: " + o);
                                    try {
                                        System.out.println("Scope Tree: " + p.getAdditionalData("ScopeTree", ScopeTree.class));
                                    } catch (InvalidTypeException e) {
                                        e.printStackTrace();
                                    }
                                })
                .addComponent(new LanguegVmCodeGenerator(),
                        (o, p) -> time.set(System.nanoTime()),
                        (o, p) ->  {
                                    System.out.println("code gen time: " + ((System.nanoTime() - time.get()) / 1_000_000_000f));
                                    System.out.println("Bytes: " + Arrays.toString((byte[]) o));
                                })
                .addComponent(new DefaultFileWriter("./test.lala"),
                        (o, p) -> time.set(System.nanoTime()),
                        (o, p) ->  {
                                    System.out.println("file write time: " + ((System.nanoTime() - time.get()) / 1_000_000_000f));
                                });

        LanguegPipeline<String, Void> pipeline = pipelineBuilder.get();
        pipeline.evaluate(code);

    }
}
