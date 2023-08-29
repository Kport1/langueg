package com.kport.langueg;

import com.kport.langueg.codeGen.languegVmCodeGen.LanguegVmCodeGenerator;
import com.kport.langueg.desugar.DefaultDesugarer;
import com.kport.langueg.fileWrite.DefaultFileWriter;
import com.kport.langueg.lex.DefaultLexer;
import com.kport.langueg.parse.DefaultParser;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.pipeline.LanguegPipelineBuilder;
import com.kport.langueg.typeCheck.DefaultTypeChecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    public static void main(String[] args) throws IOException {

        String code = Files.readString(Path.of("src/com/kport/langueg/test.gueg"));

        AtomicLong time = new AtomicLong();
        LanguegPipelineBuilder<String, Void> pipelineBuilder = new LanguegPipelineBuilder<>(code);
        pipelineBuilder.addComponent(new DefaultLexer(),
                        (o, p) -> time.set(System.nanoTime()),
                        (o, p) ->   {
                                        System.out.println("lex time: " + ((System.nanoTime() - time.get()) / 1_000_000_000f));
                                        System.out.println("Tokens: " + o);
                                    })
                .addComponent(new DefaultParser(),
                        (o, p) -> time.set(System.nanoTime()),
                        (o, p) ->   {
                                        System.out.println("parse time: " + ((System.nanoTime() - time.get()) / 1_000_000_000f));
                                        System.out.println("AST: " + o);
                                    })
                .addComponent(new DefaultDesugarer(),
                        (o, p) -> time.set(System.nanoTime()),
                        (o, p) ->   {
                                        System.out.println("desugar time: " + ((System.nanoTime() - time.get()) / 1_000_000_000f));
                                        System.out.println("desugared AST: " + o);
                                    })
                .addComponent(new DefaultTypeChecker(),
                        (o, p) -> time.set(System.nanoTime()),
                        (o, p) ->   {
                                        System.out.println("type check time: " + ((System.nanoTime() - time.get()) / 1_000_000_000f));
                                        System.out.println("annotated AST: " + o);
                                    })
                .addComponent(new LanguegVmCodeGenerator(),
                        (o, p) -> time.set(System.nanoTime()),
                        (o, p) ->   {
                                        System.out.println("code gen time: " + ((System.nanoTime() - time.get()) / 1_000_000_000f));
                                    })
                .addComponent(new DefaultFileWriter("./test.lala"),
                        (o, p) -> time.set(System.nanoTime()),
                        (o, p) ->   {
                                        System.out.println("file write time: " + ((System.nanoTime() - time.get()) / 1_000_000_000f));
                                    });

        LanguegPipeline<String, Void> pipeline = pipelineBuilder.get();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        pipeline.evaluate(code);

    }
}
