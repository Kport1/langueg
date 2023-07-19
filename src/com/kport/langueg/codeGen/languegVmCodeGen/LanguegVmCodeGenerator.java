package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.codeGen.CodeGenerator;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.expr.*;
import com.kport.langueg.parse.ast.nodes.statement.*;
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
        switch (ast){
            case NCast cast -> {
            }
            case NFn fn -> {
                FnIdentifier id = new FnIdentifier(ast.depth, ast.count, fn.name, fn.getParamTypes());

                enterFn(id);
                gen(fn.block);
                exitFn();
            }
            case NTuple tup -> {
            }
            case NStr str -> {
            }
            case NFloat64 f64 -> {
            }
            case NFloat32 f32 -> {
            }
            case NInt32 i32 -> {
                writeOp(Ops.PUSH32, i32.line, (short) registerConst32(i32.val));
            }
            case NUInt8 u8 -> {
                writeOp(Ops.PUSH8, u8.line, u8.val);
            }
            case NInt64 i64 -> {
            }
            case NBool bool -> {
                writeOp(Ops.PUSH8, bool.line, (byte)(bool.bool? 1 : 0));
            }
            case NIf nif -> {
                gen(nif.cond);
                writeOp(Ops.JMP_IF_FALSE, nif.line, (short)0);
                int jmpIndex = output.size() - 2;
                gen(nif.ifBlock);
                output.writeShort((short)(output.size() - jmpIndex - 2), jmpIndex);
            }
            case NIfElse ifelse -> {
                gen(ifelse.cond);
                writeOp(Ops.JMP_IF_FALSE, ifelse.line, (short)0);
                int jmpIndex = output.size() - 2;
                gen(ifelse.ifBlock);

                writeOp(Ops.JMP, ifelse.ifBlock.line, (short)0);
                int jmpIndexElse = output.size() - 2;
                output.writeShort((short)(output.size() - jmpIndex - 2), jmpIndex);
                gen(ifelse.elseBlock);
                output.writeShort((short)(output.size() - jmpIndexElse - 2), jmpIndexElse);

            }
            case NWhile nwhile -> {
                int jmpIndexCond = output.size();
                gen(nwhile.condition);
                writeOp(Ops.JMP_IF_FALSE, nwhile.line, (short)0);
                int jmpIndexJumpOver = output.size() - 2;
                gen(nwhile.block);
                writeOp(Ops.JMP, nwhile.line, (short)(jmpIndexCond - output.size() - 3));
                output.writeShort((short)(output.size() - jmpIndexJumpOver - 2), jmpIndexJumpOver);
            }
            case NFor nfor -> {
            }
            case NCall call -> {
                for (AST arg : call.args) {
                    gen(arg);
                }
                if (call.callee instanceof NIdent ident &&
                        fnTypes.containsKey(new FnIdentifier(ident.depth, ident.count, ident.name, ident.exprType.getFnArgs()))) {

                }
            }
            case NBlock block -> {
                enterScope();
                for (AST child : block.statements) {
                    gen(child);
                }
                exitScope();
            }
            case NReturn ret -> {
            }
            case NVarInit varInit -> {
                VarIdentifier identifier = new VarIdentifier(varInit.depth, varInit.count, varInit.name);

                if(varInit.type.isPrimitive()) {
                    PrimitiveType primType = (PrimitiveType) varInit.type;
                    localVariableIndices.put(identifier, getNextLocalVarIndex(LanguegVmValSize.ofPrimitive(primType)));
                    gen(varInit.init);
                    writeOp(Ops.ofGeneric(Ops.Generic.STORE, LanguegVmValSize.ofPrimitive(primType)), varInit.line, (short)localVariableIndices.get(identifier).intValue());
                }
            }
            case NVar var -> {
                VarIdentifier identifier = new VarIdentifier(var.depth, var.count, var.name);

                if(var.type.isPrimitive()) {
                    PrimitiveType primType = (PrimitiveType) var.type;
                    localVariableIndices.put(identifier, getNextLocalVarIndex(LanguegVmValSize.ofPrimitive(primType)));
                }
            }
            case NBinOp op -> {
                if(op.op == TokenType.Assign){
                    gen(op.right);

                    if(!(op.exprType instanceof PrimitiveType prim && prim == PrimitiveType.Void))
                        writeOp(Ops.ofGeneric(Ops.Generic.DUP, LanguegVmValSize.ofType(op.exprType)), op.line);

                    if(op.left instanceof NIdent ident) {
                        writeOp(Ops.ofGeneric(Ops.Generic.STORE, LanguegVmValSize.ofType(op.exprType)), op.line, getLocalVarIndex(op.depth, op.count, ident.name));
                    }
                    else {

                    }
                    return;
                }

                /*if(op.op.isOpAssign()){
                    TokenType binOp = op.op.getOpOfOpAssign();
                    PrimitiveType primType = (PrimitiveType) ast.children[0].returnType;
                    writeOp(Ops.ofGeneric(Ops.Generic.LOAD, LanguegVmValSize.ofPrimitive(primType)), ast.line, getLocalVarIndex(ast.depth, ast.count, ast.children[0].val.getStr()));
                    gen(ast.children[1]);
                    writeOp(Ops.ofOP(primType, binOp), ast.line);

                    if(!(ast.returnType instanceof PrimitiveType prim && prim == PrimitiveType.Void))
                        writeOp(Ops.ofGeneric(Ops.Generic.DUP, LanguegVmValSize.ofPrimitive(primType)), ast.line);

                    writeOp(Ops.ofGeneric(Ops.Generic.STORE, LanguegVmValSize.ofPrimitive(primType)), ast.line, getLocalVarIndex(ast.depth, ast.count, ast.children[0].val.getStr()));
                    return;
                }*/

                if(op.exprType instanceof PrimitiveType prim && prim == PrimitiveType.Void) return;

                gen(op.left);
                gen(op.right);
                writeOp(Ops.ofOP(op), ast.line);

            }
            case NUnaryOpPre op -> {
            }
            case NUnaryOpPost op -> {
            }
            case NIdent ident -> {
                writeOp(Ops.ofGeneric(Ops.Generic.LOAD, LanguegVmValSize.ofType(ident.exprType)), ident.line, getLocalVarIndex(ident.depth, ident.count, ident.name));
            }

            default -> throw new IllegalStateException("Unexpected value: " + ast);
        }
    }

    //
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
    private int registerConst32(int val){
        if(indexCount32 == 65536) throw new Error();
        if(constIndices32.containsKey(val)) return constIndices32.get(val);
        constIndices32.put(val, indexCount32);
        return indexCount32++;
    }

    private int registerConst64(long val){
        if(indexCount64 == 65536) throw new Error();
        if(constIndices64.containsKey(val)) return constIndices64.get(val);
        constIndices64.put(val, indexCount64);
        return indexCount64++;
    }
}
