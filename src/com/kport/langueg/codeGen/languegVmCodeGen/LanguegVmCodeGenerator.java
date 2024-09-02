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
import com.kport.langueg.typeCheck.types.*;
import com.kport.langueg.util.Identifier;
import com.kport.langueg.util.Pair;
import com.sun.jdi.InvalidTypeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LanguegVmCodeGenerator implements CodeGenerator {

    private final OpCodeGenSupplier opCodeGenSupplier = new LanguegVmOpCodeGen();

    private SymbolTable symbolTable;

    private final CodeGenState state = new CodeGenState();

    public static int LOCALS_MAX_SIZE = 2 << 16;


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

            case NTypeDef ignored -> {}

            case NAnonFn aFn -> {
                state.enterFn(aFn);
                gen(aFn.body);
                int fnIndex = state.exitFn();
                state.pushFn((short)fnIndex);
            }

            case NAssign assign -> {
                gen(assign.right);
                genAssign(assign.left);
            }

            case NBinOp binOp -> {
                short op1 = state.nextUnallocatedByte();
                gen(binOp.left);
                short op2 = state.nextUnallocatedByte();
                gen(binOp.right);
                opCodeGenSupplier.binOpCodeGen(binOp.op, binOp.left.exprType, binOp.right.exprType).gen(state, op1, op1, op2);

                state.rewindLocalsTo(op1 + binOp.exprType.getSize());
            }

            case NAssignCompound assignCompound -> {
                short op1 = state.nextUnallocatedByte();
                gen(assignCompound.left);
                short op2 = state.nextUnallocatedByte();
                gen(assignCompound.right);
                opCodeGenSupplier.binOpCodeGen(BinOp.fromCompoundAssign(assignCompound.op), assignCompound.left.exprType, assignCompound.right.exprType)
                        .gen(state, op1, op1, op2);

                state.rewindLocalsTo(op1 + assignCompound.exprType.getSize());

                genAssign(assignCompound.left);
            }

            case NBool bool ->
                state.pushByte((byte)(bool.bool? 1 : 0));

            case NCall call -> {
                short paramsBegin = state.nextUnallocatedByte();
                for (int i = 0; i < call.args.length; i++) {
                    gen(call.args[i]);
                }

                if(call.callee instanceof NIdent ident && symbolTable.fnExists(new Identifier(ident.scope, ident.identifier))){
                    state.writeOp(Ops.CALL_DIRECT, state.getFnIndex(new Identifier(ident.scope, ident.identifier)), paramsBegin, paramsBegin);
                }
                else {
                    short calleeOffset = state.nextUnallocatedByte();
                    gen(call.callee);
                    state.writeOp(Ops.CALL, calleeOffset, paramsBegin, paramsBegin);
                }

                state.rewindLocalsTo(paramsBegin);
                state.allocateAnonLocal(call.exprType.getSize());
            }

            case NFloat32 float32 -> state.pushInt(Float.floatToRawIntBits(float32.val));

            case NFloat64 float64 -> state.pushLong(Double.doubleToRawLongBits(float64.val));

            case NIdent ident -> {
                switch(symbolTable.getById(new Identifier(ident.scope, ident.identifier))){
                    case SymbolTable.Identifiable.Variable ignored -> {
                        short size = (short)ident.exprType.getSize();
                        state.mov(size, state.allocateAnonLocal(size), state.getLocalOffset(new Identifier(ident.scope, ident.identifier)), isOrContainsRef(ident.exprType));
                    }

                    case SymbolTable.Identifiable.Function ignored ->
                        state.pushFn(state.getFnIndex(new Identifier(ident.scope, ident.identifier)));

                    case SymbolTable.Identifiable.NamedType ignored -> throw new Error();
                }
            }

            case NDotAccess dotAccess -> {
                if(!(symbolTable.tryInstantiateType(dotAccess.accessed.exprType) instanceof TupleType tupType)) throw new Error();
                short accessedOffset = state.nextUnallocatedByte();
                gen(dotAccess.accessed);

                int tupIndex = dotAccess.accessor.match((uint) -> uint, tupType::indexByName);
                Type tupElementType = tupType.tupleTypes()[tupIndex];
                short tupElementSize = (short) tupElementType.getSize();
                state.mov(tupElementSize, accessedOffset, (short)(accessedOffset + tupType.getStride(tupIndex)), isOrContainsRef(tupElementType));

                state.rewindLocalsTo(accessedOffset + tupElementSize);
            }

            case NInt8 int8 -> state.pushByte(int8.val);
            case NInt16 int16 -> state.pushShort(int16.val);
            case NInt32 int32 -> state.pushInt(int32.val);
            case NInt64 int64 -> state.pushLong(int64.val);

            case NUInt8 uint8 -> state.pushByte(uint8.val);
            case NUInt16 uint16 -> state.pushShort(uint16.val);
            case NUInt32 uint32 -> state.pushInt(uint32.val);
            case NUInt64 uint64 -> state.pushLong(uint64.val);

            case NTuple tuple -> {
                TupleType tupleType = ((TupleType)symbolTable.tryInstantiateType(tuple.exprType));
                short tupOffset = state.allocateAnonLocal(tupleType.getSize());
                short genOffset = state.nextUnallocatedByte();

                for (int i = 0; i < tuple.elements.length; i++) {
                    NExpr expr = tuple.elements[i].right;
                    gen(expr);
                    int stride = tupleType.getStride(tuple.elements[i].left == null? i : tupleType.resolveElementIndex(tuple.elements[i].left));
                    state.mov((short)expr.exprType.getSize(), (short)(tupOffset + stride), genOffset, isOrContainsRef(expr.exprType));
                    state.popStack(expr.exprType.getSize());
                }
            }

            case NUnion union -> {
                state.pushShort((short)((UnionType)symbolTable.tryInstantiateType(union.exprType)).resolveElementIndex(union.initializedElementPosition));
                gen(union.initializedElement);
                state.allocateAnonLocal(union.exprType.getSize() - 2 - union.initializedElement.exprType.getSize());
            }

            case NRef ref -> {
                short size = (short) ref.right.exprType.getSize();

                short refIndex = state.nextUnallocatedByte();
                state.pushAllocDirect(size);

                short valIndex = state.nextUnallocatedByte();
                gen(ref.right);

                state.movToArrDirect(size, refIndex, valIndex, (short)0);
                state.rewindLocalsTo(valIndex);
            }

            case NBlock block -> {
                state.enterScope();
                for (AST statement : block.statements) {
                    gen(statement);
                }
                state.exitScope();
            }

            case NBlockYielding block -> {
                state.enterScope();
                for (AST statement : block.statements) {
                    gen(statement);
                }
                short stackTop = state.nextUnallocatedByte();
                short valSize = (short) block.value.exprType.getSize();
                gen(block.value);
                state.exitScope();

                state.mov(valSize, state.allocateAnonLocal(valSize), stackTop, isOrContainsRef(block.value.exprType));
            }

            case NIf if_ -> {
                short stackTop = state.nextUnallocatedByte();
                gen(if_.cond);
                state.writeOp(Ops.JMP_IF_FALSE, stackTop, (short)0);
                state.popStack(if_.cond.exprType.getSize());

                int index = state.getCurrentCodeIndex();
                gen(if_.ifBlock);
                state.generatingFns.peek().code.writeShort((short)(state.getCurrentCodeIndex() - index), index - 2);
            }

            case NIfElse ifElse -> {
                short stackTop = state.nextUnallocatedByte();

                gen(ifElse.cond);
                state.writeOp(Ops.JMP_IF_FALSE, stackTop, (short)0); //patch to jump over if block
                state.popStack(ifElse.cond.exprType.getSize());
                int jmpFalseIndex = state.getCurrentCodeIndex();

                gen(ifElse.ifBlock);
                state.writeOp(Ops.JMP, (short)0); //patch to jump over else block
                int elseJmpIndex = state.getCurrentCodeIndex();
                state.generatingFns.peek().code.writeShort((short)(state.getCurrentCodeIndex() - jmpFalseIndex), jmpFalseIndex - 2);

                gen(ifElse.elseBlock);
                state.generatingFns.peek().code.writeShort((short)(state.getCurrentCodeIndex() - elseJmpIndex), elseJmpIndex - 2);
            }

            case NMatch match -> {
                UnionType matchValUnionType = (UnionType) symbolTable.tryInstantiateType(match.value.exprType);
                short stackTop = state.nextUnallocatedByte();
                short matchProducedValSize = (short) match.exprType.getSize();

                gen(match.value);
                state.writeOp(Ops.BRANCH, stackTop);

                int branchTableBegin = state.getCurrentCodeIndex();
                for(int i = 0; i < matchValUnionType.nameTypePairs.length; i++)
                    state.generatingFns.peek().code.writeShort((short)0);

                List<Integer> jmpEndPatchList = new ArrayList<>();

                for (Pair<NMatch.Pattern, NExpr> branch : match.branches) {
                    switch(branch.left) {
                        case NMatch.Pattern.Union unionPattern -> {
                            short jmpDelta = (short)(state.getCurrentCodeIndex() - branchTableBegin);
                            state.generatingFns.peek().code.writeShort(
                                    jmpDelta,
                                    branchTableBegin + 2 * matchValUnionType.resolveElementIndex(unionPattern.element)
                            );

                            state.enterScope();
                            Type elemType = matchValUnionType.resolveElementType(unionPattern.element);
                            int elemSize = elemType.getSize();

                            short elemVar = state.allocateLocal(
                                    new Identifier(
                                            branch.right.scope,
                                            unionPattern.elementVarName
                                    ),
                                    elemSize
                            );
                            state.mov((short)elemSize, elemVar, (short)(stackTop + UnionType.UNION_TAG_BYTES), isOrContainsRef(elemType));

                            short branchStackTop = state.nextUnallocatedByte();
                            gen(branch.right);
                            state.rewindLocalsTo(stackTop);

                            state.mov(matchProducedValSize, state.nextUnallocatedByte(), branchStackTop, isOrContainsRef(match.exprType));
                            state.exitScope();

                            state.writeOp(Ops.JMP, (short)0);
                            jmpEndPatchList.add(state.getCurrentCodeIndex());
                        }

                        case NMatch.Pattern.Default ignored -> {

                        }

                        default -> throw new IllegalStateException("Unexpected value: " + branch.left);
                    }
                }

                for (Integer i : jmpEndPatchList) {
                    short jmpDelta = (short)(state.getCurrentCodeIndex() - i);
                    state.generatingFns.peek().code.writeShort(jmpDelta, i - 2);
                }
            }

            case NNamedFn namedFn -> {
                state.registerFn(new Identifier(namedFn.scope, namedFn.name), state.enterFn(namedFn));
                gen(namedFn.body);
                state.exitFn();
            }

            case NReturn return_ -> {
                short stackTop = state.nextUnallocatedByte();
                gen(return_.expr);
                state.writeOp(Ops.RET, stackTop);
                state.popStack(return_.expr.exprType.getSize());
            }

            case NVar var ->
                state.allocateLocal(new Identifier(var.scope, var.name), (byte)var.type.getSize());

            case NVarInit var -> {
                gen(var.init);
                int size = var.type.getSize();
                state.popStack(size);

                state.allocateLocal(new Identifier(var.scope, var.name), size);
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

        if(ast instanceof NExpr expr && expr.isExprStmnt)
            state.generatingFns.peek().popStack(expr.exprType.getSize());
    }

    private void genAssign(NAssignable assignable){
        short size = (short)assignable.exprType.getSize();
        short stackTop = (short)(state.nextUnallocatedByte() - size);

        short valOffset = genAssignAlgebraic(assignable);
        state.mov(size, valOffset, stackTop, isOrContainsRef(assignable.exprType));
    }

    private short genAssignAlgebraic(NAssignable assignable){
        return switch (assignable){
            case NIdent ident -> state.getLocalOffset(new Identifier(ident.scope, ident.identifier));
            case NDotAccess dotAccess -> {
                short baseOffset = genAssignAlgebraic((NAssignable) dotAccess.accessed);
                yield switch (symbolTable.tryInstantiateType(dotAccess.accessed.exprType)) {
                    case TupleType tupleType ->
                            (short)(baseOffset + tupleType.getStride(tupleType.resolveElementIndex(dotAccess.accessor)));

                    /*case UnionType unionType -> {
                        state.loadShort(baseOffset, unionType.resolveElementIndex(dotAccess.accessed));
                        yield (short)(2 + baseOffset);
                    }*/

                    default -> throw new IllegalStateException("Unexpected value: " + dotAccess.accessed.exprType);
                };
            }

            default -> throw new IllegalStateException("Unexpected value: " + assignable);
        };
    }

    private boolean isOrContainsRef(Type t){
        return switch (t){
            case ArrayType ignored -> true;
            case FnType ignored -> false;
            case NamedType namedType -> isOrContainsRef(symbolTable.instantiateType(namedType));
            case PrimitiveType ignored -> false;
            case RefType ignored -> true;
            case TupleType tupleType -> tupleType.tupleTypes().length != 0 && Arrays.stream(tupleType.tupleTypes()).allMatch(this::isOrContainsRef);
            case UnionType unionType -> unionType.unionTypes().length != 0 && Arrays.stream(unionType.unionTypes()).allMatch(this::isOrContainsRef);
        };
    }

}
