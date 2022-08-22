package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.codeGen.CodeGenerator;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.pipeline.LanguegPipeline;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class LanguegVmCodeGenerator implements CodeGenerator {
    private static final byte[] MAGIC = {'l', 'a', 'l', 'a'};

    private BufferedOutputStream output;
    private final HashMap<Integer, Integer> intConstIndices = new HashMap<>();
    private final HashMap<Long, Integer> longConstIndices = new HashMap<>();
    private final HashMap<Float, Integer> floatConstIndices = new HashMap<>();
    private final HashMap<Double, Integer> doubleConstIndices = new HashMap<>();

    public LanguegVmCodeGenerator(String outputPath){
        try {
            output = new BufferedOutputStream(new FileOutputStream(outputPath));
        } catch (FileNotFoundException e) {
            throw new Error("Failed to open output stream for file " + outputPath);
        }
    }


    @Override
    public Object process(Object input, LanguegPipeline<?, ?> pipeline) {
        AST ast = (AST) input;
        extractConstIndices(ast);
        try {
            gen(ast);
        } catch (IOException e) {
            throw new Error("Failed to write to file");
        }

        return null;
    }

    private void gen(AST ast) throws IOException {
        switch (ast.type){
            case Prog -> {
                output.write(MAGIC);
                writeHeaderConstants();
            }
            case Type -> {
            }
            case Cast -> {
            }
            case Fn -> {
            }
            case FnArg -> {
            }
            case Tuple -> {
            }
            case Class -> {
            }
            case Str -> {
            }
            case Double -> {
            }
            case Float -> {
            }
            case Int -> {
            }
            case Byte -> {
            }
            case Long -> {
            }
            case Bool -> {
            }
            case If -> {
            }
            case Switch -> {
            }
            case While -> {
            }
            case For -> {
            }
            case Call -> {
            }
            case Block -> {
            }
            case Return -> {
            }
            case Var -> {
            }
            case BinOp -> {
            }
            case UnaryOpBefore -> {
            }
            case UnaryOpAfter -> {
            }
            case Modifier -> {
            }
            case Identifier -> {
            }
        }
    }

    private void writeHeaderConstants() throws IOException {
        output.write(intConstIndices.size());
        output.write(intConstIndices.size() >> 8);
        for(int i : intConstIndices.keySet()){
            output.write(i);
            output.write(i >>> 8);
            output.write(i >>> 16);
            output.write(i >>> 24);
        }

        output.write(longConstIndices.size());
        output.write(longConstIndices.size() >> 8);
        for(long l : longConstIndices.keySet()){
            output.write((int) l);
            output.write((int) (l >>> 8));
            output.write((int) (l >>> 16));
            output.write((int) (l >>> 24));
            output.write((int) (l >>> 32));
            output.write((int) (l >>> 40));
            output.write((int) (l >>> 48));
            output.write((int) (l >>> 56));
        }

        output.write(floatConstIndices.size());
        output.write(floatConstIndices.size() >> 8);
        for(float f : floatConstIndices.keySet()){
            int fb = Float.floatToRawIntBits(f);
            output.write(fb);
            output.write(fb >>> 8);
            output.write(fb >>> 16);
            output.write(fb >>> 24);
        }

        output.write(doubleConstIndices.size());
        output.write(doubleConstIndices.size() >> 8);
        for(double d : doubleConstIndices.keySet()){
            long db = Double.doubleToRawLongBits(d);
            output.write((int) db);
            output.write((int) (db >>> 8));
            output.write((int) (db >>> 16));
            output.write((int) (db >>> 24));
            output.write((int) (db >>> 32));
            output.write((int) (db >>> 40));
            output.write((int) (db >>> 48));
            output.write((int) (db >>> 56));
        }
    }

    private int intIndexCount = 0;
    private int longIndexCount = 0;
    private int floatIndexCount = 0;
    private int doubleIndexCount = 0;
    private void extractConstIndices(AST ast){
        switch(ast.type){
            case Int -> {
                if(intConstIndices.containsKey(ast.val.getInt())) return;
                if(intIndexCount >= 65536) throw new Error("Too many int constants");
                intConstIndices.put(ast.val.getInt(), intIndexCount);

                intIndexCount++;
            }

            case Long -> {
                if(longConstIndices.containsKey(ast.val.getLong())) return;
                if(longIndexCount >= 65536) throw new Error("Too many long constants");
                longConstIndices.put(ast.val.getLong(), longIndexCount);

                longIndexCount++;
            }

            case Float -> {
                if(floatConstIndices.containsKey(ast.val.getFloat())) return;
                if(floatIndexCount >= 65536) throw new Error("Too many float constants");
                floatConstIndices.put(ast.val.getFloat(), floatIndexCount);

                floatIndexCount++;
            }

            case Double -> {
                if(doubleConstIndices.containsKey(ast.val.getDub())) return;
                if(doubleIndexCount >= 65536) throw new Error("Too many double constants");
                doubleConstIndices.put(ast.val.getDub(), doubleIndexCount);

                doubleIndexCount++;
            }

            default -> {
                if(ast.children != null){
                    for (AST child : ast.children) {
                        extractConstIndices(child);
                    }
                }
            }
        }
    }
}
