package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.codeGen.CodeGenerator;
import com.kport.langueg.codeGen.languegVmCodeGen.op.LanguegVmOpCodeGen;
import com.kport.langueg.codeGen.languegVmCodeGen.op.OpCodeGenSupplier;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.BinOp;
import com.kport.langueg.parse.ast.nodes.NAnonFn;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.NNamedFn;
import com.kport.langueg.parse.ast.nodes.NProg;
import com.kport.langueg.parse.ast.nodes.expr.*;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NAssignable;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NDotAccess;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NIdent;
import com.kport.langueg.parse.ast.nodes.expr.integer.*;
import com.kport.langueg.parse.ast.nodes.statement.*;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.typeCheck.SymbolTable;
import com.kport.langueg.typeCheck.types.TupleType;
import com.kport.langueg.typeCheck.types.UnionType;
import com.kport.langueg.util.Identifier;
import com.sun.jdi.InvalidTypeException;

import java.util.function.Function;

public class LanguegVmCodeGenerator implements CodeGenerator {
    /*
        Vm Architecture
            Stack           - max 2^16 elements - 128 bit size
            Local Variables - max 2^16 bytes

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
                state.enterProg(prog);
                for (AST statement : prog.statements) {
                    gen(statement);
                }
                state.exitProg();
            }

            case NAnonFn aFn -> {
                state.enterFn(aFn);
                gen(aFn.body);
                int fnIndex = state.exitFn();

                if(aFn.isExprStmnt) return;
                state.writeOp(Ops.PUSHFN, (short)fnIndex);
            }

            case NAssign assign -> {
                gen(assign.right);
                if(!assign.isExprStmnt) state.writeOp(Ops.DUP);
                genAssign(assign.left);
            }

            case NBinOp binOp -> {
                gen(binOp.left);
                gen(binOp.right);
                opCodeGenSupplier.binOpCodeGen(binOp.op, binOp.left.exprType, binOp.right.exprType).gen(state);
                if(binOp.isExprStmnt) state.writeOp(Ops.POP);
            }

            case NAssignCompound assignCompound -> {
                gen(assignCompound.left);
                gen(assignCompound.right);
                opCodeGenSupplier.binOpCodeGen(BinOp.fromCompoundAssign(assignCompound.op), assignCompound.left.exprType, assignCompound.right.exprType)
                        .gen(state);
                if(!assignCompound.isExprStmnt) state.writeOp(Ops.DUP);
                genAssign(assignCompound.left);
            }

            case NBool bool -> {
                state.writeOp(Ops.PUSH8, (byte)(bool.bool? 1 : 0));
            }

            case NCall call -> {
                for (int i = call.args.length - 1; i >= 0; i--) {
                    gen(call.args[i]);
                }
                gen(call.callee);
                state.writeOp(Ops.CALL);

                state.generatingFns.peek().stackDepth -= (call.args.length - 1);

                if(call.isExprStmnt) state.writeOp(Ops.POP);
            }

            case NCast cast -> {}

            case NChar char_ -> {}

            case NFloat32 float32 -> state.pushInt(Float.floatToRawIntBits(float32.val));

            case NFloat64 float64 -> state.pushLong(Double.doubleToRawLongBits(float64.val));

            case NIdent ident -> {
                switch(symbolTable.getById(new Identifier(ident.scope, ident.identifier))){
                    case SymbolTable.Identifiable.Variable var ->
                        state.writeOp(Ops.LOAD, state.getLocalOffset(new Identifier(ident.scope, ident.identifier)), (byte)ident.exprType.getSize());

                    case SymbolTable.Identifiable.Function fn ->
                        state.writeOp(Ops.PUSHFN, state.getFnIndex(new Identifier(ident.scope, ident.identifier)));

                    case SymbolTable.Identifiable.NamedType type -> throw new Error();
                }
                if(ident.isExprStmnt) state.writeOp(Ops.POP);
            }

            case NDotAccess dotAccess -> {
                gen(dotAccess.accessed);
                if(!(dotAccess.accessed.exprType instanceof TupleType tupType)) throw new Error();
                byte size = (byte)dotAccess.accessed.exprType.getSize();
                short offset = state.allocateTempLocal(size);
                state.writeOp(Ops.STORE, offset, size);
                int tupIndex = dotAccess.accessor.match((uint) -> uint, tupType::indexOfName);
                state.writeOp(Ops.LOAD, (short)(offset + tupType.getStride(tupIndex)), (byte)tupType.tupleTypes()[tupIndex].getSize());
            }

            case NInt8 int8 -> state.writeOp(Ops.PUSH8, int8.val);
            case NInt16 int16 -> state.writeOp(Ops.PUSH16, int16.val);
            case NInt32 int32 -> state.pushInt(int32.val);
            case NInt64 int64 -> state.pushLong(int64.val);

            case NUInt8 uint8 -> state.writeOp(Ops.PUSH8, uint8.val);
            case NUInt16 uint16 -> state.writeOp(Ops.PUSH16, uint16.val);
            case NUInt32 uint32 -> state.pushInt(uint32.val);
            case NUInt64 uint64 -> state.pushLong(uint64.val);

            case NStr str -> {}

            case NTuple tuple -> {
                int tupSize = tuple.exprType.getSize();
                if(tupSize > 16) throw new Error("");

                short offset = state.allocateTempLocal((byte)tupSize);
                short elementOffset = offset;
                for (int i = 0; i < tuple.elements.length; i++) {
                    gen(tuple.elements[i]);
                    byte elementSize = (byte)tuple.elements[i].exprType.getSize();
                    state.writeOp(Ops.STORE, elementOffset, elementSize);
                    elementOffset += elementSize;
                }

                state.writeOp(Ops.LOAD, offset, (byte)tupSize);
            }

            case NUnaryOpPost unaryOpPost -> {}

            case NUnaryOpPre unaryOpPre -> {}

            case NBlock block -> {
                state.enterScope();
                for (AST statement : block.statements) {
                    gen(statement);
                }
                state.exitScope();
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
                state.registerFn(new Identifier(namedFn.scope, namedFn.name), state.enterFn(namedFn));
                gen(namedFn.body);
                state.exitFn();
            }

            case NReturn return_ -> {
                gen(return_.expr);
                state.writeOp(Ops.RET);
                state.generatingFns.peek().stackDepth -= 1;
            }

            case NVar var -> {
                state.allocateLocal(new Identifier(var.scope, var.name), (byte)var.type.getSize());
            }

            case NVarDestruct varDestruct -> {

            }

            case NVarInit var -> {
                gen(var.init);
                byte size = (byte)var.type.getSize();
                short offset = state.allocateLocal(new Identifier(var.scope, var.name), size);
                state.writeOp(Ops.STORE, offset, size);
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

    private void genAssign(NAssignable assignable){
        switch (assignable){
            case NIdent ident ->
                    state.writeOp(
                            Ops.STORE,
                            state.getLocalOffset(new Identifier(ident.scope, ident.identifier)),
                            (byte)ident.exprType.getSize()
                    );

            default -> {
                short valOffset = genAssignAlgebraic(assignable);
                state.writeOp(Ops.STORE, valOffset, (byte)assignable.exprType.getSize());
            }
        }
    }

    private short genAssignAlgebraic(NAssignable assignable){
        return switch (assignable){
            case NIdent ident -> state.getLocalOffset(new Identifier(ident.scope, ident.identifier));
            case NDotAccess dotAccess -> {
                short baseOffset = genAssignAlgebraic((NAssignable) dotAccess.accessed);
                yield switch (dotAccess.accessed.exprType) {
                    case TupleType tupleType ->
                            (short)(baseOffset + tupleType.getStride(dotAccess.accessor.match((uint) -> uint, tupleType::indexOfName)));
                    case UnionType unionType -> {
                        state.writeOp(Ops.PUSH16, dotAccess.accessor.match((uint) -> uint, unionType::indexOfName).shortValue());
                        state.writeOp(Ops.STORE, baseOffset, (byte) 2);
                        yield (short)(2 + baseOffset);
                    }

                    default -> throw new IllegalStateException("Unexpected value: " + dotAccess.accessed.exprType);
                };
            }

            default -> throw new IllegalStateException("Unexpected value: " + assignable);
        };
    }

}
