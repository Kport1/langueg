package com.kport.langueg;

import com.kport.langueg.codeGen.languegVmCodeGen.CodeGenState;
import com.kport.langueg.codeGen.languegVmCodeGen.FnData;
import com.kport.langueg.codeGen.languegVmCodeGen.LanguegVmCodeGenerator;
import com.kport.langueg.desugar.DefaultDesugarer;
import com.kport.langueg.fileWrite.DefaultFileWriter;
import com.kport.langueg.lex.DefaultLexer;
import com.kport.langueg.parse.DefaultParser;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.pipeline.LanguegPipelineBuilder;
import com.kport.langueg.typeCheck.DefaultTypeChecker;
import com.sun.jdi.InvalidTypeException;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    public static void main(String[] args) throws IOException {

        Options cliOptions = new Options();
        cliOptions.addOption("h", "help", false, "print this help message");
        cliOptions.addOption(Option.builder().option("o").longOpt("output").hasArg().argName("output-file").desc("output file").build());

        CommandLineParser parser = new org.apache.commons.cli.DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(cliOptions, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("guegc [OPTIONS...] <input-file>\nOPTIONS:", cliOptions);
            System.exit(1);
        }

        if(cmd.hasOption('h')){
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("guegc [OPTIONS...] <gueg-file>\nOPTIONS:", cliOptions);
            System.exit(0);
        }

        if(cmd.getArgs().length != 1){
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("guegc [OPTIONS...] <input-file>\nOPTIONS:", cliOptions);
            System.exit(1);
        }



        Path inputPath = Path.of(cmd.getArgs()[0]);
        Path outputPath = Path.of(cmd.getOptionValue('o', "./" + inputPath.getFileName().toString().replaceFirst("(\\..*)|$", ".lala")));

        System.out.println("Reading " + inputPath);

        String code = Files.readString(inputPath);

        System.out.println("Compiling...");

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
                                        try {
                                            CodeGenState state = p.getAdditionalData("State", CodeGenState.class);
                                            for (FnData fn : state.generatedFns) {
                                                System.out.println(fn);
                                                System.out.println();
                                            }
                                        } catch (InvalidTypeException e) {
                                            throw new RuntimeException(e);
                                        }
                                    })
                .addComponent(new DefaultFileWriter(outputPath),
                        (o, p) ->   {
                                        System.out.println("Writing to " + outputPath);
                                        time.set(System.nanoTime());
                                    },
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
