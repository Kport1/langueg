package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.codeGen.CodeGenerator;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.ScopeTree;
import com.kport.langueg.util.FnIdentifier;
import com.kport.langueg.util.VarIdentifier;
import com.sun.jdi.InvalidTypeException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class LanguegVmCodeGenerator implements CodeGenerator {
    private final ByteArrayOutputStream output;

    //Constants
    private final LinkedHashMap<Integer, Integer> intConstIndices = new LinkedHashMap<>();
    private final LinkedHashMap<Long, Integer> longConstIndices = new LinkedHashMap<>();
    private final LinkedHashMap<Float, Integer> floatConstIndices = new LinkedHashMap<>();
    private final LinkedHashMap<Double, Integer> doubleConstIndices = new LinkedHashMap<>();

    //Type check data
    private ScopeTree scopeTree;
    private HashMap<FnIdentifier, Type> fnTypes;
    private HashMap<VarIdentifier, Type> varTypes;
    private HashMap<VarIdentifier, Type> fnParamTypes;


    private final ByteArrayOutputStream progIndexLineInfo = new ByteArrayOutputStream(256);
    private final ByteArrayOutputStream newLineLineInfo = new ByteArrayOutputStream(256);



    public LanguegVmCodeGenerator() {
        output = new ByteArrayOutputStream(1024);
    }


    @Override
    @SuppressWarnings("unchecked")
    public byte[] process(Object input, LanguegPipeline<?, ?> pipeline) {
        AST ast = (AST) input;

        extractConstIndices(ast);
        try {
            scopeTree = pipeline.getAdditionalData("ScopeTree", ScopeTree.class);
            fnTypes = pipeline.getAdditionalData("FunctionTypes", HashMap.class);
            varTypes = pipeline.getAdditionalData("VariableTypes", HashMap.class);
            fnParamTypes = pipeline.getAdditionalData("FunctionParameterTypes", HashMap.class);
        }
        catch (InvalidTypeException e){
            throw new Error(e.getMessage());
        }


        try {
            gen(ast);
        }
        catch (IOException e) {
            throw new Error();
        }

        pipeline.putAdditionalData("LineInfo", lineInfoAsByteArray());
        pipeline.putAdditionalData("ConstIndices", constIndicesAsByteArray());
        return output.toByteArray();
    }

    private void gen(AST ast) throws IOException {
        switch (ast.type){
            case Prog -> {
                for (AST child : ast.children) {
                    gen(child);
                }
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
                writeOp(Ops.PUSH_INTC, ast.line, (short)intConstIndices.get(ast.val.getInt()).intValue());
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
                if(ast.val.getTok() == TokenType.Assign){

                }

                gen(ast.children[0]);
                gen(ast.children[1]);
                writeOp(Ops.ofBinOp(ast), ast.line);
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

    private void writeToStream(short s, OutputStream stream) throws IOException {
        stream.write(s);
        stream.write((s >>> 8));
    }

    private void writeToStream(int i, OutputStream stream) throws IOException {
        stream.write(i);
        stream.write(i >>> 8);
        stream.write(i >>> 16);
        stream.write(i >>> 24);
    }

    private void writeToStream(long l, OutputStream stream) throws IOException {
        stream.write((int) l         & 0xFF);
        stream.write((int)(l >>> 8)  & 0xFF);
        stream.write((int)(l >>> 16) & 0xFF);
        stream.write((int)(l >>> 24) & 0xFF);
        stream.write((int)(l >>> 32) & 0xFF);
        stream.write((int)(l >>> 40) & 0xFF);
        stream.write((int)(l >>> 48) & 0xFF);
        stream.write((int)(l >>> 56) & 0xFF);
    }


    private void writeOp(Ops op, int line, byte... operands) throws IOException {
        addLine(line);
        output.write(op.code);
        output.write(operands);
    }

    private void writeOp(Ops op, int line, short... operands) throws IOException {
        addLine(line);
        output.write(op.code);
        for (short operand : operands) {
            writeToStream(operand, output);
        }
    }

    private void writeOp(Ops op, int line, int... operands) throws IOException {
        addLine(line);
        output.write(op.code);
        for (int operand : operands) {
            writeToStream(operand, output);
        }
    }

    private void writeOp(Ops op, int line, long... operands) throws IOException {
        addLine(line);
        output.write(op.code);
        for (long operand : operands) {
            writeToStream(operand, output);
        }
    }

    int prevLine = 0;
    private void addLine(int line) throws IOException {
        if(prevLine == line) return;
        writeToStream((long)output.size(), progIndexLineInfo);
        writeToStream(line, newLineLineInfo);
        prevLine = line;
    }

    private byte[] lineInfoAsByteArray(){
        ByteBuffer buffer = ByteBuffer.allocate(8 + progIndexLineInfo.size() + newLineLineInfo.size());
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.putLong(progIndexLineInfo.size() >>> 3);
        buffer.put(progIndexLineInfo.toByteArray());
        buffer.put(newLineLineInfo.toByteArray());
        return buffer.array();
    }

    private byte[] constIndicesAsByteArray(){
        ByteBuffer buffer = ByteBuffer.allocate(2 + intIndexCount*4 + 2 + longIndexCount*8 + 2 + floatIndexCount*4 + 2 + doubleIndexCount*8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.putShort((short) intIndexCount);
        intConstIndices.forEach((k, v) -> buffer.putInt(k));
        buffer.putShort((short) longIndexCount);
        longConstIndices.forEach((k, v) -> buffer.putLong(k));
        buffer.putShort((short) floatIndexCount);
        floatConstIndices.forEach((k, v) -> buffer.putFloat(k));
        buffer.putShort((short) doubleIndexCount);
        doubleConstIndices.forEach((k, v) -> buffer.putDouble(k));

        return buffer.array();
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
