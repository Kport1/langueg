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
import com.kport.langueg.parse.ast.nodes.expr.assignable.NDeRef;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NDotAccess;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NIdent;
import com.kport.langueg.parse.ast.nodes.expr.controlFlow.*;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.NBool;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.NTuple;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.NUnion;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.floating.NFloat32;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.floating.NFloat64;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.integer.*;
import com.kport.langueg.parse.ast.nodes.expr.operators.NAssignCompound;
import com.kport.langueg.parse.ast.nodes.expr.operators.NBinOp;
import com.kport.langueg.parse.ast.nodes.expr.operators.NRef;
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
                opCodeGenSupplier.binOpCodeGen(assignCompound.op, assignCompound.left.exprType, assignCompound.right.exprType)
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
                UnionType unionType = (UnionType)symbolTable.tryInstantiateType(union.exprType);
                state.pushShort((short)unionType.resolveElementIndex(union.initializedElementPosition));
                gen(union.initializedElement);
                state.allocateAnonLocal(union.exprType.getSize() - 2 - union.initializedElement.exprType.getSize());
            }

            case NRef ref -> {
                short size = (short) ref.referent.exprType.getSize();

                short refIndex = state.nextUnallocatedByte();
                state.pushAllocDirect(size);

                short valIndex = state.nextUnallocatedByte();
                gen(ref.referent);

                state.movToHeapDirect(size, (short)0, valIndex, refIndex, isOrContainsRef(ref.referent.exprType));
                state.rewindLocalsTo(valIndex);
            }

            case NDeRef deRef -> {
                Type referentType = ((RefType)deRef.reference.exprType).referentType;
                short size = (short) referentType.getSize();

                short valIndex = state.nextUnallocatedByte();
                state.allocateAnonLocal(size);

                short refIndex = state.nextUnallocatedByte();
                gen(deRef.reference);

                state.movFromHeapDirect(size, valIndex, (short) 0, refIndex, isOrContainsRef(referentType));
                state.rewindLocalsTo(refIndex);
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
                state.writeOp(Ops.JMP_IF_FALSE, stackTop, (short)0);
                state.popStack(ifElse.cond.exprType.getSize());
                int jmpFalseIndex = state.getCurrentCodeIndex();

                gen(ifElse.ifBlock);
                state.writeOp(Ops.JMP, (short)0);
                int elseJmpIndex = state.getCurrentCodeIndex();
                state.generatingFns.peek().code.writeShort((short)(state.getCurrentCodeIndex() - jmpFalseIndex), jmpFalseIndex - 2);

                gen(ifElse.elseBlock);
                state.generatingFns.peek().code.writeShort((short)(state.getCurrentCodeIndex() - elseJmpIndex), elseJmpIndex - 2);
            }

            case NMatch match -> {
                short matchValSize = (short) match.exprType.getSize();

                UnionType matchValUnionType = (UnionType) symbolTable.tryInstantiateType(match.value.exprType);
                int numUnionElements = matchValUnionType.nameTypePairs.length;

                short matchedValIndex = state.nextUnallocatedByte();
                gen(match.value);

                state.writeOp(Ops.BRANCH, matchedValIndex);
                int branchTableBegin = state.getCurrentCodeIndex();
                for(int i = 0; i < numUnionElements; i++) {
                    state.generatingFns.peek().code.writeShort((short) 0);
                }
                short[] branchTable = new short[numUnionElements];

                List<Integer> jmpEndPatchList = new ArrayList<>();

                for (Pair<NMatch.Pattern, NExpr> branch : match.branches) {
                    switch(branch.left) {
                        case NMatch.Pattern.Union unionPattern -> {
                            short jmpDelta = (short)(state.getCurrentCodeIndex() - branchTableBegin);
                            branchTable[matchValUnionType.resolveElementIndex(unionPattern.element)] = jmpDelta;

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
                            state.mov((short)elemSize, elemVar, (short)(matchedValIndex + UnionType.UNION_TAG_BYTES), isOrContainsRef(elemType));

                            short branchValIndex = state.nextUnallocatedByte();
                            gen(branch.right);
                            state.mov(matchValSize, matchedValIndex, branchValIndex, isOrContainsRef(match.exprType));

                            state.exitScope();

                            state.writeOp(Ops.JMP, (short)0);
                            jmpEndPatchList.add(state.getCurrentCodeIndex());
                        }

                        case NMatch.Pattern.Default ignored -> {
                            short jmpDelta = (short)(state.getCurrentCodeIndex() - branchTableBegin);
                            for (int i = 0; i < branchTable.length; i++) {
                                if(branchTable[i] == 0){
                                    branchTable[i] = jmpDelta;
                                }
                            }

                            state.enterScope();

                            short branchValIndex = state.nextUnallocatedByte();
                            gen(branch.right);
                            state.mov(matchValSize, matchedValIndex, branchValIndex, isOrContainsRef(match.exprType));

                            state.exitScope();
                        }

                        default -> throw new IllegalStateException("Unexpected value: " + branch.left);
                    }
                }

                for (int i = 0; i < branchTable.length; i++) {
                    state.generatingFns.peek().code.writeShort(branchTable[i], branchTableBegin + 2 * i);
                }

                for (Integer i : jmpEndPatchList) {
                    short jmpDelta = (short)(state.getCurrentCodeIndex() - i);
                    state.generatingFns.peek().code.writeShort(jmpDelta, i - 2);
                }

                state.rewindLocalsTo(matchedValIndex + matchValSize);
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
                short condValIndex = state.nextUnallocatedByte();
                gen(while_.cond);
                state.writeOp(Ops.JMP_IF_FALSE, condValIndex, (short)0);
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
        short valueIndex = (short)(state.nextUnallocatedByte() - size);

        int offset = 0;
        while(true){
            switch (assignable){
                case NIdent ident -> {
                    offset += state.getLocalOffset(new Identifier(ident.scope, ident.identifier));
                    state.mov(size, (short)offset, valueIndex, isOrContainsRef(assignable.exprType));
                    return;
                }

                case NDotAccess dotAccess -> {
                    TupleType tupleType = (TupleType) symbolTable.tryInstantiateType(dotAccess.accessed.exprType);
                    offset += (short) tupleType.getStride(tupleType.resolveElementIndex(dotAccess.accessor));
                    assignable = (NAssignable) dotAccess.accessed;
                }

                case NDeRef deRef -> {
                    short refIndex = state.nextUnallocatedByte();
                    gen(deRef.reference);
                    state.movToHeapDirect(size, offset, valueIndex, refIndex, isOrContainsRef(assignable.exprType));
                    state.rewindLocalsTo(refIndex);
                    return;
                }

                default -> throw new IllegalStateException("Unexpected value: " + assignable);
            }
        }

    }

    private boolean isOrContainsRef(Type t){
        return switch (t){
            case ArrayType ignored -> true;
            case FnType ignored -> false;
            case NamedType namedType -> isOrContainsRef(symbolTable.instantiateType(namedType));
            case PrimitiveType ignored -> false;
            case RefType ignored -> true;
            case TupleType tupleType -> tupleType.tupleTypes().length != 0 && Arrays.stream(tupleType.tupleTypes()).anyMatch(this::isOrContainsRef);
            case UnionType unionType -> unionType.unionTypes().length != 0 && Arrays.stream(unionType.unionTypes()).anyMatch(this::isOrContainsRef);
        };
    }

}
