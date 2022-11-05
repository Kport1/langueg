package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.codeGen.CodeGenerator;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.typeCheck.types.PrimitiveType;
import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.CodeOutputStream;
import com.kport.langueg.util.ScopeTree;
import com.kport.langueg.util.FnIdentifier;
import com.kport.langueg.util.VarIdentifier;
import com.sun.jdi.InvalidTypeException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Stack;

public class LanguegVmCodeGenerator implements CodeGenerator {
    private final CodeOutputStream output;

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


    //line info
    private final CodeOutputStream progIndexLineInfo = new CodeOutputStream(256);
    private final CodeOutputStream newLineLineInfo = new CodeOutputStream(256);

    //Variable information
    private final HashMap<VarIdentifier, Integer> localVariableIndices = new HashMap<>();



    public LanguegVmCodeGenerator() {
        output = new CodeOutputStream(1024);
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

        //System.out.println("outBytes:");
        //for (byte b : output.toByteArray()) {
        //    System.out.print(Integer.toHexString(b) + "  ");
        //}
        //System.out.println();

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
                writeOp(Ops.PUSH_BYTE, ast.line, ast.val.getByte());
            }
            case Long -> {
            }
            case Bool -> {
                writeOp(Ops.PUSH_BYTE, ast.line, (byte)(ast.val.getBool()? 1 : 0));
            }
            case If -> {
                gen(ast.children[0]);
                writeOp(Ops.JMP_IF_FALSE, ast.line, (short)0);
                int jmpOffsetIndex = output.size() - 2;
                gen(ast.children[1]);
                if(ast.children.length == 3){
                    writeOp(Ops.JMP, ast.children[1].line, (short)0);
                    int jmpOffsetIndexElse = output.size() - 2;
                    output.writeShort((short)(output.size() - jmpOffsetIndex - 2), jmpOffsetIndex);
                    gen(ast.children[2]);
                    output.writeShort((short)(output.size() - jmpOffsetIndexElse - 2), jmpOffsetIndexElse);
                }
                else {
                    output.writeShort((short)(output.size() - jmpOffsetIndex - 2), jmpOffsetIndex);
                }
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
                enterScope();
                for (AST child : ast.children) {
                    gen(child);
                }
                exitScope();
            }
            case Return -> {
            }
            case Var -> {
                VarIdentifier identifier = new VarIdentifier(ast.depth, ast.count, ast.children[0].val.getStr());

                if(ast.val.getType().isPrimitive()) {
                    PrimitiveType primType = (PrimitiveType) ast.val.getType();
                    localVariableIndices.put(identifier, getNextLocalVarIndex(primType));
                    if (ast.children.length == 2) {
                        gen(ast.children[1]);
                        writeOp(Ops.ofGeneric(Ops.Generic.STORE, primType), ast.line, (short)localVariableIndices.get(identifier).intValue());
                    }
                }
            }
            case BinOp -> {
                if(ast.val.getTok() == TokenType.Assign){
                    PrimitiveType primType = (PrimitiveType) ast.children[0].returnType;
                    gen(ast.children[1]);
                    writeOp(Ops.ofGeneric(Ops.Generic.STORE, primType), ast.line, (short)localVariableIndices.get(new VarIdentifier(ast.depth, ast.count, ast.children[0].val.getStr())).intValue());
                    return;
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
                if(ast.returnType.isPrimitive()) {
                    PrimitiveType primType = (PrimitiveType) ast.returnType;
                    writeOp(Ops.ofGeneric(Ops.Generic.LOAD, primType), ast.line, (short)localVariableIndices.get(new VarIdentifier(ast.depth, ast.count, ast.val.getStr())).intValue());
                }
            }
        }
    }

    private final EnumMap<PrimitiveType, Stack<Integer>> localVarScopedIndices = new EnumMap<>(PrimitiveType.class);
    {
        for (PrimitiveType t : PrimitiveType.values()) {
            Stack<Integer> stack = new Stack<>();
            stack.push(-1);
            localVarScopedIndices.put(t, stack);
        }
    }
    private int getNextLocalVarIndex(PrimitiveType type){
        Stack<Integer> stack = localVarScopedIndices.get(type);
        int i = stack.pop() + 1;
        stack.push(i);
        return i;
    }

    private void enterScope(){
        for (PrimitiveType t : PrimitiveType.values()) {
            Stack<Integer> stack = localVarScopedIndices.get(t);
            stack.push(stack.peek());
        }
    }

    private void exitScope(){
        for (PrimitiveType t : PrimitiveType.values()) {
            localVarScopedIndices.get(t).pop();
        }
    }


    private void writeOp(Ops op, int line, byte... operands) throws IOException {
        addLine(line);
        output.write(op.code);
        output.write(operands);
    }

    private void writeOp(Ops op, int line, short... operands) {
        addLine(line);
        output.write(op.code);
        for (short operand : operands) {
            output.writeShort(operand);
        }
    }

    private void writeOp(Ops op, int line, int... operands) {
        addLine(line);
        output.write(op.code);
        for (int operand : operands) {
            output.writeInt(operand);
        }
    }

    private void writeOp(Ops op, int line, long... operands) {
        addLine(line);
        output.write(op.code);
        for (long operand : operands) {
            output.writeLong(operand);
        }
    }

    int prevLine = 0;
    private void addLine(int line) {
        if(prevLine == line) return;
        progIndexLineInfo.writeLong(output.size());
        newLineLineInfo.writeInt(line);
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
