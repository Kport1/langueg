package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.codeGen.CodeGenerator;
import com.kport.langueg.codeGen.languegVmCodeGen.op.LanguegVmOpCodeGen;
import com.kport.langueg.codeGen.languegVmCodeGen.op.OpCodeGenSupplier;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.NProg;
import com.kport.langueg.parse.ast.nodes.expr.*;
import com.kport.langueg.parse.ast.nodes.statement.*;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.typeCheck.SymbolTable;
import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.FnIdentifier;
import com.kport.langueg.util.VarIdentifier;
import com.sun.jdi.InvalidTypeException;

import java.util.Arrays;

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

    private final OpCodeGenSupplier opCodeGenSupplier = new LanguegVmOpCodeGen();

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

        pipeline.putAdditionalData("State", state);

        return null;
    }

    void gen(AST ast) {
        switch (ast){
            case NProg prog -> {
                gen(prog.moduleInterface);
                state.enterProg(prog);
                for (AST statement : prog.statements) {
                    gen(statement);
                }
                state.exitProg();
            }

            case NAnonFn aFn -> {
                state.enterFn(aFn);
                gen(aFn.block);
                int fnIndex = state.exitFn();

                if(aFn.isExprStmnt) return;
                state.writeOp(Ops.PUSHFN, (short)fnIndex);
            }

            case NAssign assign -> {
                gen(assign.right);
                if(!assign.isExprStmnt) state.writeOp(Ops.ofGeneric(assign.exprType.getSize(), Ops.Generic.DUP));
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

            case NBool bool -> {
                state.writeOp(Ops.PUSH8, (byte)(bool.bool? 1 : 0));
            }

            case NCall call -> {
                for (int i = call.args.length - 1; i >= 0; i--) {
                    gen(call.args[i]);
                }
                if(call.callee instanceof NIdent ident){
                    state.writeOp(Ops.PUSHFN, state.getFnIndex(new FnIdentifier(call.callee.scope, ident.name, Arrays.stream(call.args).map(a -> a.exprType).toArray(Type[]::new))));
                } else {
                    gen(call.callee);
                }
                state.writeOp(Ops.CALL);
                for (NExpr arg : call.args) {
                    state.generatingFns.peek().stackDepthCount.computeIfPresent(arg.exprType.getSize(), (size, count) -> count - 1);
                }
                state.generatingFns.peek().stackDepthCount.computeIfPresent(call.exprType.getSize(), (size, count) -> count + 1);
                if(call.isExprStmnt && call.exprType.getSize() != null) state.writeOp(Ops.ofGeneric(call.exprType.getSize(), Ops.Generic.POP));
            }

            case NCast cast -> {}

            case NChar char_ -> {}

            case NFloat32 float32 -> state.writeOp(Ops.PUSH32, state.registerConst32(Float.floatToRawIntBits(float32.val)));

            case NFloat64 float64 -> state.writeOp(Ops.PUSH64, state.registerConst64(Double.doubleToRawLongBits(float64.val)));

            case NIdent ident -> {
                if(symbolTable.varExists(new VarIdentifier(ident.scope, ident.name))){
                    state.writeOp(Ops.ofGeneric(ident.exprType.getSize(), Ops.Generic.LOAD), state.getLocalIndex(new VarIdentifier(ident.scope, ident.name), ident.exprType.getSize()));
                }
            }

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
                gen(if_.cond);
                state.writeOp(Ops.JMP_IF_FALSE, (short)0);
                int index = state.getCurrentCodeIndex();
                gen(if_.ifBlock);
                state.generatingFns.peek().code.writeShort((short)(state.getCurrentCodeIndex() - index), index - 2);
            }

            case NIfElse ifElse -> {
                gen(ifElse.cond);
                state.writeOp(Ops.JMP_IF_FALSE, (short)0);
                int jmpFalseIndex = state.getCurrentCodeIndex();

                gen(ifElse.ifBlock);
                state.writeOp(Ops.JMP, (short)0);
                int elseJmpIndex = state.getCurrentCodeIndex();
                state.generatingFns.peek().code.writeShort((short)(state.getCurrentCodeIndex() - jmpFalseIndex), jmpFalseIndex - 2);

                gen(ifElse.elseBlock);
                state.generatingFns.peek().code.writeShort((short)(state.getCurrentCodeIndex() - elseJmpIndex), elseJmpIndex - 2);
            }

            case NNamedFn namedFn -> {
                state.registerFn(namedFn.getId(), state.enterFn(namedFn));
                gen(namedFn.block);
                state.exitFn();
            }

            case NReturn return_ -> {
                gen(return_.expr);
                state.writeOp(Ops.RET);
                state.generatingFns.peek().stackDepthCount.computeIfPresent(return_.expr.exprType.getSize(), (size, count) -> count - 1);
            }

            case NReturnVoid returnVoid -> {
                state.writeOp(Ops.RET);
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
                int jmpBackIndex = state.getCurrentCodeIndex();
                gen(while_.cond);
                state.writeOp(Ops.JMP_IF_FALSE, (short)0);
                int jmpFalseIndex = state.getCurrentCodeIndex();
                gen(while_.block);
                state.writeOp(Ops.JMP, (short)(jmpBackIndex - state.getCurrentCodeIndex() - 3));
                state.generatingFns.peek().code.writeShort((short)(state.getCurrentCodeIndex() - jmpFalseIndex), jmpFalseIndex - 2);
            }

            default -> throw new IllegalStateException("Unexpected value: " + ast);
        }
    }


}
