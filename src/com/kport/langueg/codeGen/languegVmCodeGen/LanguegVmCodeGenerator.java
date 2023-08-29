package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.codeGen.CodeGenerator;
import com.kport.langueg.codeGen.languegVmCodeGen.op.LanguegVmOpCodeGen;
import com.kport.langueg.codeGen.languegVmCodeGen.op.OpCodeGenSupplier;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NProg;
import com.kport.langueg.parse.ast.nodes.expr.*;
import com.kport.langueg.parse.ast.nodes.statement.*;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.typeCheck.SymbolTable;
import com.kport.langueg.typeCheck.types.PrimitiveType;
import com.kport.langueg.util.CodeOutputStream;
import com.kport.langueg.util.VarIdentifier;
import com.sun.jdi.InvalidTypeException;

import java.util.List;

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

    private OpCodeGenSupplier opCodeGenSupplier = new LanguegVmOpCodeGen();

    private SymbolTable symbolTable;

    private final CodeGenState state = new CodeGenState();


    public LanguegVmCodeGenerator() {

    }


    @Override
    public Void process(Object input, LanguegPipeline<?, ?> pipeline) {
        AST ast = (AST) input;

        try {
            symbolTable = pipeline.getAdditionalData("SymbolTable", SymbolTable.class);
        }
        catch (InvalidTypeException e){
            e.printStackTrace();
        }

        gen(ast);

        for (FnData fn : state.generatedFns) {
            System.out.println(fn);
            System.out.println();
        }

        pipeline.putAdditionalData("State", state);

        return null;
    }

    void gen(AST ast) {
        switch (ast){
            case NProg prog -> {
                state.enterFn(null);
                for (AST statement : prog.statements) {
                    gen(statement);
                }
                state.exitFn();
            }

            case NAnonFn aFn -> {
                state.enterFn(aFn.returnType.getSize());
                gen(aFn.block);
                int fnIndex = state.exitFn();

                if(aFn.isExprStmnt) return;
                state.writeOp(Ops.PUSHFN, (short)fnIndex);
            }

            case NAssign assign -> {
                switch (assign.left){
                    case NIdent ident -> {
                        state.writeOp(
                                Ops.ofGeneric(ident.exprType.getSize(), Ops.Generic.STORE),
                                state.getLocalIndex(new VarIdentifier(ident.scope, ident.name), ident.exprType.getSize())
                        );
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + assign.left);
                }
            }

            case NBinOp binOp -> {
                gen(binOp.left);
                gen(binOp.right);
                opCodeGenSupplier.binOpCodeGen(binOp.op, binOp.left.exprType, binOp.right.exprType).gen(binOp, state);
                if(binOp.isExprStmnt) state.writeOp(Ops.ofGeneric(binOp.exprType.getSize(), Ops.Generic.POP));
            }

            case NBool bool -> {}

            case NCall call -> {}

            case NCast cast -> {}

            case NChar char_ -> {}

            case NFloat32 float32 -> state.writeOp(Ops.PUSH32, state.registerConst32(Float.floatToRawIntBits(float32.val)));

            case NFloat64 float64 -> state.writeOp(Ops.PUSH64, state.registerConst64(Double.doubleToRawLongBits(float64.val)));

            case NIdent ident -> {}

            case NInt8 int8 -> state.writeOp(Ops.PUSH8, int8.val);

            case NInt16 int16 -> state.writeOp(Ops.PUSH16, int16.val);

            case NInt32 int32 -> state.writeOp(Ops.PUSH32, state.registerConst32(int32.val));

            case NInt64 int64 -> state.writeOp(Ops.PUSH64, state.registerConst64(int64.val));

            case NStr str -> {}

            case NTuple tuple -> {}

            case NUInt8 uint8 -> state.writeOp(Ops.PUSH8, uint8.val);

            case NUInt16 uint16 -> state.writeOp(Ops.PUSH16, uint16.val);

            case NUInt32 uint32 -> state.writeOp(Ops.PUSH32, state.registerConst32(uint32.val));

            case NUInt64 uint64 -> state.writeOp(Ops.PUSH64, state.registerConst64(uint64.val));

            case NUnaryOpPost unaryOpPost -> {}

            case NUnaryOpPre unaryOpPre -> {}

            case NBlock block -> {
                state.enterScope();
                for (AST statement : block.statements) {
                    gen(statement);
                }
                state.exitScope();
            }

            case NFor for_ -> {

            }

            case NIf if_ -> {

            }

            case NIfElse ifElse -> {

            }

            case NNamedFn namedFn -> {
                state.enterFn(namedFn.returnType.getSize());
                gen(namedFn.block);
                int index = state.exitFn();
                state.registerFn(namedFn.getId(), index);
            }

            case NReturn return_ -> {

            }

            case NReturnVoid returnVoid -> {

            }

            case NVar var -> {
                state.registerLocal(new VarIdentifier(var.scope, var.name), var.type.getSize());
            }

            case NVarDestruct varDestruct -> {

            }

            case NVarInit var -> {
                short index = state.registerLocal(new VarIdentifier(var.scope, var.name), var.type.getSize());
                gen(var.init);
                state.writeOp(Ops.ofGeneric(var.type.getSize(), Ops.Generic.STORE), index);
            }

            case NWhile while_ -> {

            }

            default -> throw new IllegalStateException("Unexpected value: " + ast);
        }
    }


}
