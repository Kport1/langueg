package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.codeGen.CodeGenerator;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTTypeE;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
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
import java.util.*;

public class LanguegVmCodeGenerator implements CodeGenerator {
    /*
        .LaLa file format

        MAGIC

        Constant Pool
            32, 64 bit

        Functions
            id
            returnType
            amntLocals
            maxStackDepth
            LineInfo
            Code
     */


    private CodeOutputStream output;

    //Constants
    private final Map<Integer, Integer> constIndices32 = new LinkedHashMap<>();
    private final Map<Long, Integer> constIndices64 = new LinkedHashMap<>();

    //Type check data
    private ScopeTree scopeTree;
    private Map<FnIdentifier, Type> fnTypes;
    private Map<VarIdentifier, Type> varTypes;
    private Map<VarIdentifier, Type> fnParamTypes;

    //line info
    private final Map<FnIdentifier, CodeOutputStream> progIndexLineInfo = new LinkedHashMap<>();
    private final Map<FnIdentifier, CodeOutputStream> newLineLineInfo = new LinkedHashMap<>();

    //Variable information
    private final Map<VarIdentifier, Integer> localVariableIndices = new HashMap<>();

    //Function information
    private final Map<FnIdentifier, EnumMap<LanguegVmValSize, Integer>> fnAmntLocals = new LinkedHashMap<>();
    private final Map<FnIdentifier, EnumMap<LanguegVmValSize, Integer>> fnMaxStackDepth = new LinkedHashMap<>();
    private final Map<FnIdentifier, CodeOutputStream> fnCodeOutputs = new LinkedHashMap<>();


    public LanguegVmCodeGenerator() {

    }


    @Override
    @SuppressWarnings("unchecked")
    public Void process(Object input, LanguegPipeline<?, ?> pipeline) {
        AST ast = (AST) input;

        try {
            scopeTree = pipeline.getAdditionalData("ScopeTree", ScopeTree.class);
            fnTypes = pipeline.getAdditionalData("FunctionTypes", HashMap.class);
            varTypes = pipeline.getAdditionalData("VariableTypes", HashMap.class);
            fnParamTypes = pipeline.getAdditionalData("FunctionParameterTypes", HashMap.class);
        }
        catch (InvalidTypeException e){
            e.printStackTrace();
        }

        extractConstIndices(ast);

        try {
            gen(ast);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        pipeline.putAdditionalData("ConstIndices", constIndicesAsByteArray());

        Map<FnIdentifier, byte[]> lineInfo =  lineInfoAsByteArray();
        Map<FnIdentifier, byte[]> fnCode = fnCodeAsByteArray();

        Map<FnIdentifier, FnData> fnData = new HashMap<>();
        for (FnIdentifier id : fnAmntLocals.keySet()) {
            fnData.put(id, new FnData(id == null? LanguegVmValSize.NONE : LanguegVmValSize.ofType(fnTypes.get(id)), fnAmntLocals.get(id), fnMaxStackDepth.get(id), lineInfo.get(id), fnCode.get(id)));
        }

        pipeline.putAdditionalData("FnData", fnData);

        return null;
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
                boolean isAnon = ast.children[ast.children.length - 1].type != ASTTypeE.Identifier;

                int blockIndex = ast.children.length - (isAnon ? 1 : 2);
                Type[] paramTypes = Arrays.stream(Arrays.copyOfRange(ast.children, 0, blockIndex)).map(a -> a.val.getType()).toArray(Type[]::new);
                String name = isAnon? null : ast.children[ast.children.length - 1].val.getStr();

                FnIdentifier id = new FnIdentifier(ast.depth, ast.count, name, paramTypes);

                enterFn(id);
                gen(ast.children[blockIndex]);
                exitFn();
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
                writeOp(Ops.PUSH_INTC, ast.line, (short) constIndices32.get(ast.val.getInt()).intValue());
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
                int jmpIndex = output.size() - 2;
                gen(ast.children[1]);
                if(ast.children.length == 3){
                    writeOp(Ops.JMP, ast.children[1].line, (short)0);
                    int jmpIndexElse = output.size() - 2;
                    output.writeShort((short)(output.size() - jmpIndex - 2), jmpIndex);
                    gen(ast.children[2]);
                    output.writeShort((short)(output.size() - jmpIndexElse - 2), jmpIndexElse);
                }
                else {
                    output.writeShort((short)(output.size() - jmpIndex - 2), jmpIndex);
                }
            }
            case Switch -> {
            }
            case While -> {
                int jmpIndexCond = output.size();
                gen(ast.children[0]);
                writeOp(Ops.JMP_IF_FALSE, ast.line, (short)0);
                int jmpIndexJumpOver = output.size() - 2;
                gen(ast.children[1]);
                writeOp(Ops.JMP, ast.line, (short)(jmpIndexCond - output.size() - 3));
                output.writeShort((short)(output.size() - jmpIndexJumpOver - 2), jmpIndexJumpOver);
            }
            case For -> {
            }
            case Call -> {
                AST called = ast.children[0];
                AST[] args = Arrays.copyOfRange(ast.children, 1, ast.children.length);
                for (AST arg : args) {
                    gen(arg);
                }
                if (called.type == ASTTypeE.Identifier &&
                        fnTypes.containsKey(new FnIdentifier(called.depth, called.count, called.val.getStr(), called.returnType.getFnArgs()))) {

                }
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
                    localVariableIndices.put(identifier, getNextLocalVarIndex(LanguegVmValSize.ofPrimitive(primType)));
                    if (ast.children.length == 2) {
                        gen(ast.children[1]);
                        writeOp(Ops.ofGeneric(Ops.Generic.STORE, primType), ast.line, (short)localVariableIndices.get(identifier).intValue());
                    }
                }
            }
            case BinOp -> {
                TokenType op = ast.val.getTok();
                if(op == TokenType.Assign){
                    PrimitiveType primType = (PrimitiveType) ast.children[0].returnType;
                    gen(ast.children[1]);

                    if(!(ast.returnType instanceof PrimitiveType prim && prim == PrimitiveType.Void))
                        writeOp(Ops.ofGeneric(Ops.Generic.DUP, primType), ast.line);

                    writeOp(Ops.ofGeneric(Ops.Generic.STORE, primType), ast.line, getLocalVarIndex(ast.depth, ast.count, ast.children[0].val.getStr()));
                    return;
                }

                if(op.isOpAssign()){
                    TokenType binOp = op.getOpOfOpAssign();
                    PrimitiveType primType = (PrimitiveType) ast.children[0].returnType;
                    writeOp(Ops.ofGeneric(Ops.Generic.LOAD, primType), ast.line, getLocalVarIndex(ast.depth, ast.count, ast.children[0].val.getStr()));
                    gen(ast.children[1]);
                    writeOp(Ops.ofBinOp(primType, binOp), ast.line);

                    if(!(ast.returnType instanceof PrimitiveType prim && prim == PrimitiveType.Void))
                        writeOp(Ops.ofGeneric(Ops.Generic.DUP, primType), ast.line);

                    writeOp(Ops.ofGeneric(Ops.Generic.STORE, primType), ast.line, getLocalVarIndex(ast.depth, ast.count, ast.children[0].val.getStr()));
                    return;
                }

                if(op.isUnaryOp()){

                }

                if(op.isBinOp()){
                    gen(ast.children[0]);
                    gen(ast.children[1]);
                    writeOp(Ops.ofBinOp(ast), ast.line);
                }
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
                    writeOp(Ops.ofGeneric(Ops.Generic.LOAD, primType), ast.line, getLocalVarIndex(ast.depth, ast.count, ast.val.getStr()));
                }
            }
        }
    }

    private final Stack<EnumMap<LanguegVmValSize, Stack<Integer>>> localVarScopedIndices = new Stack<>();
    private final Stack<FnIdentifier> fnStack = new Stack<>();
    {
        enterFn(null);
    }
    private int getNextLocalVarIndex(LanguegVmValSize size){
        Stack<Integer> stack = localVarScopedIndices.peek().get(size);
        int i = stack.pop() + 1;
        stack.push(i);
        int curMaxLocals = fnAmntLocals.get(fnStack.peek()).get(size);
        fnAmntLocals.get(fnStack.peek()).put(size, Math.max(curMaxLocals, i + 1));
        return i;
    }

    private void enterScope(){
        for (LanguegVmValSize size : LanguegVmValSize.values()) {
            Stack<Integer> stack = localVarScopedIndices.peek().get(size);
            stack.push(stack.peek());
        }
    }

    private void exitScope(){
        for (LanguegVmValSize size : LanguegVmValSize.values()) {
            localVarScopedIndices.peek().get(size).pop();
        }
    }

    private void enterFn(FnIdentifier id){
        localVarScopedIndices.push(new EnumMap<>(LanguegVmValSize.class));
        fnAmntLocals.put(id, new EnumMap<>(LanguegVmValSize.class));
        for (LanguegVmValSize size : LanguegVmValSize.values()) {
            Stack<Integer> stack = new Stack<>();
            stack.push(-1);
            localVarScopedIndices.peek().put(size, stack);
            fnAmntLocals.get(id).put(size, 0);
        }
        fnStack.push(id);

        //Separate code output and line info for function
        output = new CodeOutputStream(512);
        fnCodeOutputs.put(id, output);

        progIndexLineInfo.put(id, new CodeOutputStream(128));
        newLineLineInfo.put(id, new CodeOutputStream(128));
    }

    private void exitFn(){
        localVarScopedIndices.pop();
        fnStack.pop();
        output = fnCodeOutputs.get(fnStack.peek());
    }

    private short getLocalVarIndex(int depth, int count, String name){
        Integer index = localVariableIndices.get(new VarIdentifier(depth, count, name));
        if(index != null) return (short)index.intValue();
        ScopeTree.Node scope = scopeTree.getNode(depth, count);
        if(scope.getParent() == null) throw new Error("No such variable: " + name);
        return getLocalVarIndex(scope.getParent().depth, scope.getParent().count, name);
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
        progIndexLineInfo.get(fnStack.peek()).writeLong(output.size() - 1);
        newLineLineInfo.get(fnStack.peek()).writeInt(line);
        prevLine = line;
    }

    private Map<FnIdentifier, byte[]> lineInfoAsByteArray(){
        LinkedHashMap<FnIdentifier, byte[]> out = new LinkedHashMap<>();

        for (FnIdentifier id : progIndexLineInfo.keySet()) {
            CodeOutputStream fnProgIndexLineInfo = progIndexLineInfo.get(id);
            CodeOutputStream fnNewLineLineInfo = newLineLineInfo.get(id);

            ByteBuffer buffer = ByteBuffer.allocate(8 + fnProgIndexLineInfo.size() + fnNewLineLineInfo.size());
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            buffer.putLong(fnProgIndexLineInfo.size());
            buffer.put(fnProgIndexLineInfo.toByteArray());
            buffer.put(fnNewLineLineInfo.toByteArray());

            out.put(id, buffer.array());
        }
        return out;
    }

    private byte[] constIndicesAsByteArray(){
        ByteBuffer buffer = ByteBuffer.allocate(2 + indexCount32*4 + 2 + indexCount64*8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.putShort((short) indexCount32);
        constIndices32.forEach((k, v) -> buffer.putInt(k));
        buffer.putShort((short) indexCount64);
        constIndices64.forEach((k, v) -> buffer.putLong(k));

        return buffer.array();
    }

    private Map<FnIdentifier, byte[]> fnCodeAsByteArray(){
        LinkedHashMap<FnIdentifier, byte[]> out = new LinkedHashMap<>();
        fnCodeOutputs.forEach((id, stream) -> {
            out.put(id, stream.toByteArray());
        });
        return out;
    }

    private int indexCount32 = 0;
    private int indexCount64 = 0;
    private void extractConstIndices(AST ast_){
        ast_.accept((ast, context) -> {
            switch(ast.type){
                case Int -> {
                    if(constIndices32.containsKey(ast.val.getInt())) return;
                    if(indexCount32 > 65535) throw new Error("Too many 32 bit constants");
                    constIndices32.put(ast.val.getInt(), indexCount32);

                    indexCount32++;
                }

                case Long -> {
                    if(constIndices64.containsKey(ast.val.getLong())) return;
                    if(indexCount64 > 65535) throw new Error("Too many 64 bit constants");
                    constIndices64.put(ast.val.getLong(), indexCount64);

                    indexCount64++;
                }

                case Float -> {
                    if(constIndices32.containsKey(Float.floatToRawIntBits(ast.val.getFloat()))) return;
                    if(indexCount32 > 65535) throw new Error("Too many 32 bit constants");
                    constIndices32.put(Float.floatToRawIntBits(ast.val.getInt()), indexCount32);

                    indexCount32++;
                }

                case Double -> {
                    if(constIndices64.containsKey(Double.doubleToRawLongBits(ast.val.getDouble()))) return;
                    if(indexCount64 > 65535) throw new Error("Too many 64 bit constants");
                    constIndices64.put(Double.doubleToRawLongBits(ast.val.getLong()), indexCount64);

                    indexCount64++;
                }
            }
        }, null);
    }
}
