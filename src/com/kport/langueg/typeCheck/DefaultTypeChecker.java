package com.kport.langueg.typeCheck;

import com.kport.langueg.error.Errors;
import com.kport.langueg.error.stage.typecheck.TypeCheckException;
import com.kport.langueg.error.stage.typecheck.TypeSynthesisException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.BinOp;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.*;
import com.kport.langueg.parse.ast.nodes.expr.NAssign;
import com.kport.langueg.parse.ast.nodes.expr.NBlock;
import com.kport.langueg.parse.ast.nodes.expr.NBlockYielding;
import com.kport.langueg.parse.ast.nodes.expr.NCast;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NAssignable;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NDeRef;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NDotAccess;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NIdent;
import com.kport.langueg.parse.ast.nodes.expr.controlFlow.*;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.*;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.NNumInfer;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.floating.NFloat32;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.floating.NFloat64;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.integer.*;
import com.kport.langueg.parse.ast.nodes.expr.operators.*;
import com.kport.langueg.parse.ast.nodes.statement.NTypeDef;
import com.kport.langueg.parse.ast.nodes.statement.NVar;
import com.kport.langueg.parse.ast.nodes.statement.NVarInit;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.typeCheck.cast.CastAllowlist;
import com.kport.langueg.typeCheck.cast.DefaultCastAllowlist;
import com.kport.langueg.typeCheck.op.*;
import com.kport.langueg.typeCheck.types.*;
import com.kport.langueg.util.Either;
import com.kport.langueg.util.Identifier;
import com.kport.langueg.util.Pair;
import com.kport.langueg.util.Scope;

import java.util.Arrays;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

public class DefaultTypeChecker implements TypeChecker {

    private final OpTypeMappingSupplier opTypeMappings = new DefaultOpTypeMappings();
    private final CastAllowlist castAllowlist = new DefaultCastAllowlist();

    private LanguegPipeline<?, ?> pipeline;

    private final SymbolTable symbolTable = new SymbolTable();

    @Override
    public AST process(Object ast_, LanguegPipeline<?, ?> pipeline_) {
        AST ast = (AST) ast_;
        pipeline = pipeline_;

        //Annotate scope
        ast.accept(new ASTVisitor() {
            @Override
            public void visit(AST ast, VisitorContext context) {
                ast.scope = (Scope) context.get("scope");
                ASTVisitor.super.visit(ast, context);
            }

            @Override
            public void visit(NamedType namedType, VisitorContext context) {
                namedType.scope = (Scope) context.get("scope");
                ASTVisitor.super.visit(namedType, context);
            }

            @Override
            public void visit(NTypeDef typeDef, VisitorContext context) {
                typeDef.definition.accept(new ASTVisitor() {
                    @Override
                    public void visit(NamedType namedType, VisitorContext context_) {
                        namedType.scope = (Scope) context.get("scope");
                    }
                }, null);
                ASTVisitor.super.visit(typeDef, context);
            }

            @Override
            public void visit(NBlock block, VisitorContext context) {
                Scope oldScope = (Scope) context.get("scope");
                Scope newScope = new Scope(oldScope, block, false);
                context.put("scope", newScope);
                ASTVisitor.super.visit(block, context);
            }

            @Override
            public void visit(NBlockYielding blockYielding, VisitorContext context) {
                Scope oldScope = (Scope) context.get("scope");
                Scope newScope = new Scope(oldScope, blockYielding, false);
                context.put("scope", newScope);
                ASTVisitor.super.visit(blockYielding, context);
            }

            @Override
            public void visit(NMatch match, VisitorContext context) {
                match.value.accept(this, VisitorContext.clone(context));
                Scope oldScope = (Scope) context.get("scope");
                for (Pair<NMatch.Pattern, NExpr> branch : match.branches) {
                    Scope newScope = new Scope(oldScope, match, false);
                    context.put("scope", newScope);
                    branch.right.accept(this, VisitorContext.clone(context));
                }
            }

            @Override
            public void visit(NFn fn, VisitorContext context) {
                Scope oldScope = (Scope) context.get("scope");
                Scope newScope = new Scope(oldScope, (AST) fn, true);
                context.put("scope", newScope);

                fn.setBodyScope(newScope);
                ASTVisitor.super.visit(fn, context);
            }
        }, new VisitorContext(Map.of("scope", new Scope(null, ast, true))));

        //Register types
        ast.accept(new ASTVisitor() {
            @Override
            public void visit(NTypeDef typeDef, VisitorContext context) {
                if (!symbolTable.registerType(new Identifier(typeDef.scope, typeDef.name), typeDef))
                    throw new Error("Duplicate name " + typeDef.name + " in the same scope");
                ASTVisitor.super.visit(typeDef, context);
            }
        }, null);

        //Resolve Type Sizes
        ast.accept(new ASTVisitor() {
            @Override
            public void visit(NamedType namedType, VisitorContext context) {
                namedType.size = symbolTable.getNamedTypeSize(namedType);
                ASTVisitor.super.visit(namedType, context);
            }
        }, null);

        //Annotate LValues
        ast.accept(new ASTVisitor() {
            private void setLValueRec(NAssignable assignable) {
                assignable.isLValue = true;
                switch (assignable) {
                    case NIdent ignored -> {
                    }

                    case NDotAccess dotAccess -> {
                        if (!(dotAccess.accessed instanceof NAssignable dotAssignable)) throw new Error();
                        setLValueRec(dotAssignable);
                    }

                    case NDeRef deRef -> {
                        if (!(deRef.reference instanceof NAssignable deRefAssignable)) throw new Error();
                        setLValueRec(deRefAssignable);
                    }

                    default -> throw new Error();
                }
            }

            @Override
            public void visit(NAssign assign, VisitorContext context) {
                setLValueRec(assign.left);
                ASTVisitor.super.visit(assign, context);
            }

            @Override
            public void visit(NAssignCompound assignCompound, VisitorContext context) {
                setLValueRec(assignCompound.left);
                ASTVisitor.super.visit(assignCompound, context);
            }
        }, null);

        //Register named functions and function parameters
        ast.accept(new ASTVisitor() {
            @Override
            public void visit(NNamedFn namedFn, VisitorContext context) {
                if (!symbolTable.registerFn(new Identifier(namedFn.scope, namedFn.name), namedFn.header))
                    throw new Error("Duplicate name " + namedFn.name + " in the same scope");

                ASTVisitor.super.visit(namedFn, context);
            }

            @Override
            public void visit(NFn fn, VisitorContext context) {
                for (NameTypePair param : fn.getFnHeader().params) {
                    if (!symbolTable.registerVar(new Identifier(fn.getBodyScope(), param.name), param.type))
                        throw new Error("Duplicate name " + param.name + " in the same scope");
                }
                ASTVisitor.super.visit(fn, context);
            }
        }, null);

        //Check and annotate expr types / register vars
        annotateTypes(ast);

        //Verify variables are initialized before use
        ast.accept(new ASTVisitor() {
            @Override
            public void visit(NVar var, VisitorContext context) {
                if (!varIsInitializedBeforeUse(var.scope.scopeOpeningNode, new Identifier(var.scope, var.name)))
                    throw new Error("Variable " + var.name + " is not initialized before use");

                ASTVisitor.super.visit(var, context);
            }
        }, null);

        //Find expression statements
        ast.accept(new ASTVisitor() {
            @Override
            public void visit(NProg prog, VisitorContext context) {
                for (AST stmnt : prog.statements) {
                    if (stmnt instanceof NExpr expr) expr.isExprStmnt = true;
                }
                ASTVisitor.super.visit(prog, context);
            }

            @Override
            public void visit(NBlock block, VisitorContext context) {
                for (AST stmnt : block.statements) {
                    if (stmnt instanceof NExpr expr) expr.isExprStmnt = true;
                }
                ASTVisitor.super.visit(block, context);
            }

            @Override
            public void visit(NBlockYielding blockYielding, VisitorContext context) {
                for (AST stmnt : blockYielding.statements) {
                    if (stmnt instanceof NExpr expr) expr.isExprStmnt = true;
                }
                ASTVisitor.super.visit(blockYielding, context);
            }
        }, null);

        pipeline.putAdditionalData("SymbolTable", symbolTable);

        return ast;
    }

    private boolean varIsInitializedBeforeUse(AST ast, Identifier id) {
        final boolean[] used = {false};
        Type varType = symbolTable.tryInstantiateType(((SymbolTable.Identifiable.Variable) symbolTable.getById(id)).varType);
        Type.InitTracker initTracker = Type.InitTracker.getInitTracker(varType);

        linearCodeScan(ast, node -> {
            if (used[0]) return;

            if (node instanceof NIdent ident && !ident.isLValue &&
                    symbolTable.identifiersReferToSameThing(new Identifier(ident.scope, ident.identifier), id))
                used[0] = true;

            if (node instanceof NAssign || node instanceof NAssignCompound) {
                Stack<Integer> dotAccessIndices = new Stack<>();
                NAssignable assignable = node instanceof NAssign assign ? assign.left : ((NAssignCompound) node).left;
                do {
                    switch (assignable) {
                        case NIdent ident -> {
                            if (!symbolTable.identifiersReferToSameThing(new Identifier(ident.scope, ident.identifier), id))
                                return;
                        }

                        case NDotAccess dotAccess -> {
                            dotAccessIndices.push(dotAccess.accessor.match(i -> i, str ->
                                    switch (symbolTable.tryInstantiateType(dotAccess.accessed.exprType)) {
                                        case TupleType tupleType -> tupleType.indexByName(str);
                                        case UnionType unionType -> unionType.indexByName(str);
                                        default -> throw new Error();
                                    }));
                            assignable = (NAssignable) dotAccess.accessed;
                        }

                        default -> throw new Error();
                    }
                } while (!(assignable instanceof NIdent));

                Type.InitTracker initTrackerTmp = initTracker;
                while (!dotAccessIndices.empty()) {
                    if (initTrackerTmp instanceof Type.InitTracker.TupleInitTracker tupleInitTracker)
                        initTrackerTmp = tupleInitTracker.getElement(dotAccessIndices.pop());
                    else throw new Error();
                }
                initTrackerTmp.init();
            }
        });

        if (!used[0]) return true;
        return initTracker.isInit();
    }

    private void linearCodeScan(AST ast, Consumer<AST> consumer) {
        consumer.accept(ast);

        if (ast instanceof NFn) return;
        if (ast instanceof NIfElse ifElse) linearCodeScan(ifElse.cond, consumer);
        if (ast instanceof NIf if_) linearCodeScan(if_.cond, consumer);
        if (ast instanceof NWhile while_) linearCodeScan(while_.cond, consumer);

        if (!ast.hasChildren()) return;
        for (AST child : ast.getChildren()) {
            linearCodeScan(child, consumer);
        }
    }

    private void annotateTypes(AST ast) {
        try {
            switch (ast) {
                case NProg prog -> {
                    for (AST statement : prog.statements) {
                        annotateTypes(statement);
                    }
                }

                case NVar var -> {
                    if (var.type == null)
                        throw new Error("Can't infer type of variable " + var.name + ", because it is not initialized");

                    if (!symbolTable.registerVar(new Identifier(var.scope, var.name), var.type))
                        throw new Error("Duplicate name " + var.name + " in the same scope");
                }

                case NVarInit varInit -> {
                    if (varInit.type == null) {
                        try {
                            varInit.type = synthesizeType(varInit.init);
                        } catch (TypeSynthesisException reason) {
                            throw new TypeSynthesisException(
                                    Errors.CHECK_SYNTHESIZE_VAR_INIT,
                                    reason,
                                    varInit.codeOffset(), pipeline.getSource(), varInit.name
                            );
                        }
                    } else {
                        try {
                            checkType(varInit.init, varInit.type);
                        } catch (TypeCheckException reason) {
                            throw new TypeCheckException(
                                    Errors.CHECK_CHECK_VAR_INIT,
                                    reason,
                                    varInit.codeOffset(), pipeline.getSource(), varInit.type
                            );
                        }
                    }

                    if (!symbolTable.registerVar(new Identifier(varInit.scope, varInit.name), varInit.type)) {
                        throw new Error("Duplicate name " + varInit.name + " in the same scope");
                    }
                }

                case NNamedFn namedFn -> annotateTypes(namedFn.body);

                case NExpr expr -> {
                    try {
                        synthesizeType(expr);
                    } catch(TypeSynthesisException reason){
                        throw new TypeSynthesisException(
                                Errors.CHECK_SYNTHESIZE_EXPR_STMNT,
                                reason,
                                expr.codeOffset(), pipeline.getSource()
                        );
                    }
                    expr.isExprStmnt = true;
                }

                default -> {
                }
            }
        } catch (TypeCheckException exception) {
            System.err.println(exception.format());
            System.exit(1);
        } catch (TypeSynthesisException exception) {
            System.err.println(exception.format());
            System.exit(1);
        }
    }

    private void checkType(NExpr expr, Type type) throws TypeCheckException {
        expr.exprType = type;
        switch (expr) {
            case NTuple tuple -> {
                if (!(symbolTable.tryInstantiateType(type) instanceof TupleType tupleType))
                    throw new TypeCheckException(Errors.CHECK_CHECK_TUPLE, tuple.codeOffset(), pipeline.getSource(), type);
                if (tuple.elements.length != tupleType.nameTypePairs().length)
                    throw new TypeCheckException(Errors.CHECK_CHECK_TUPLE, tuple.codeOffset(), pipeline.getSource(), tupleType);

                boolean[] elemIsSet = new boolean[tupleType.nameTypePairs().length];
                for (int i = 0; i < tuple.elements.length; i++) {
                    Pair<Either<Integer, String>, NExpr> element = tuple.elements[i];

                    int tupleTypeIndex = i;
                    if (element.left != null) {
                        if (!tupleType.hasElement(element.left)) {
                            if (element.left instanceof Either.Left<Integer, String> index) {
                                throw new TypeCheckException(
                                        Errors.CHECK_CHECK_TUPLE,
                                        new TypeCheckException(Errors.CHECK_CHECK_TUPLE_NO_INDEX, element.right.codeOffset(), pipeline.getSource(), index.value()),
                                        tuple.codeOffset(), pipeline.getSource(), tupleType
                                );
                            }
                            if (element.left instanceof Either.Right<Integer, String> string) {
                                throw new TypeCheckException(
                                        Errors.CHECK_CHECK_TUPLE,
                                        new TypeCheckException(Errors.CHECK_CHECK_TUPLE_NO_NAME, element.right.codeOffset(), pipeline.getSource(), string.value()),
                                        tuple.codeOffset(), pipeline.getSource(), tupleType
                                );
                            }
                        }
                        tupleTypeIndex = tupleType.resolveElementIndex(element.left);
                    }

                    if (elemIsSet[tupleTypeIndex]) {
                        throw new TypeCheckException(
                                Errors.CHECK_CHECK_TUPLE,
                                new TypeCheckException(Errors.CHECK_CHECK_TUPLE_ALREADY_INIT, element.right.codeOffset(), pipeline.getSource()),
                                tuple.codeOffset(), pipeline.getSource(), tupleType
                        );
                    }

                    try {
                        checkType(element.right, tupleType.tupleTypes()[tupleTypeIndex]);
                    } catch (TypeCheckException reason) {
                        throw new TypeCheckException(
                                Errors.CHECK_CHECK_TUPLE,
                                reason,
                                tuple.codeOffset(), pipeline.getSource(), tupleType
                        );
                    }
                    elemIsSet[tupleTypeIndex] = true;
                }
            }

            case NUnion union -> {
                if (!(symbolTable.tryInstantiateType(type) instanceof UnionType unionType))
                    throw new TypeCheckException(Errors.CHECK_CHECK_UNION, union.codeOffset(), pipeline.getSource(), type);
                if (!unionType.hasElement(union.initializedElementPosition)) {
                    if (union.initializedElementPosition instanceof Either.Left<Integer, String> integer) {
                        throw new TypeCheckException(
                                Errors.CHECK_CHECK_UNION,
                                new TypeCheckException(Errors.CHECK_CHECK_UNION_NO_INDEX, union.codeOffset(), pipeline.getSource(), integer.value()),
                                union.codeOffset(), pipeline.getSource(), unionType
                        );
                    }
                    if (union.initializedElementPosition instanceof Either.Right<Integer, String> string) {
                        throw new TypeCheckException(
                                Errors.CHECK_CHECK_UNION,
                                new TypeCheckException(Errors.CHECK_CHECK_UNION_NO_NAME, union.codeOffset(), pipeline.getSource(), string.value()),
                                union.codeOffset(), pipeline.getSource(), unionType
                        );
                    }
                }

                Type expectedType = unionType.resolveElementType(union.initializedElementPosition);
                try {
                    checkType(union.initializedElement, expectedType);
                } catch (TypeCheckException reason) {
                    throw new TypeCheckException(
                            Errors.CHECK_CHECK_UNION,
                            reason,
                            union.codeOffset(), pipeline.getSource(), unionType
                    );
                }
            }

            case NIfElse ifElse -> {
                try {
                    checkType(ifElse.cond, PrimitiveType.Bool);
                } catch (TypeCheckException reason) {
                    throw new TypeCheckException(
                            Errors.CHECK_CHECK_IF_ELSE_COND,
                            reason,
                            ifElse.cond.codeOffset(), pipeline.getSource()
                    );
                }

                try {
                    checkType(ifElse.ifBlock, type);
                } catch (TypeCheckException reason) {
                    throw new TypeCheckException(
                            Errors.CHECK_CHECK_IF_ELSE_IF,
                            reason,
                            ifElse.ifBlock.codeOffset(), pipeline.getSource(), type
                    );
                }

                try {
                    checkType(ifElse.elseBlock, type);
                } catch (TypeCheckException reason) {
                    throw new TypeCheckException(
                            Errors.CHECK_CHECK_IF_ELSE_ELSE,
                            reason,
                            ifElse.elseBlock.codeOffset(), pipeline.getSource(), type
                    );
                }
            }

            case NRef ref -> {
                if (!(symbolTable.tryInstantiateType(type) instanceof RefType refType))
                    throw new TypeCheckException(Errors.CHECK_CHECK_REF, ref.codeOffset(), pipeline.getSource(), type);
                try {
                    checkType(ref.referent, refType.referentType());
                } catch (TypeCheckException exception) {
                    throw new TypeCheckException(
                            Errors.CHECK_CHECK_REF,
                            exception,
                            ref.codeOffset(), pipeline.getSource(), type
                    );
                }
            }

            case NNumInfer numInfer -> {
                if (!(symbolTable.tryInstantiateType(type) instanceof PrimitiveType primitiveType && primitiveType.isNumeric()))
                    throw new TypeCheckException(Errors.CHECK_CHECK_NUM_INFER, numInfer.codeOffset(), pipeline.getSource(), type);

            }

            case NArray array -> {
                if (!(symbolTable.tryInstantiateType(type) instanceof ArrayType arrayType))
                    throw new TypeCheckException(Errors.CHECK_CHECK_ARRAY, array.codeOffset(), pipeline.getSource(), type);
                for (NExpr element : array.elements) {
                    try {
                        checkType(element, arrayType.type);
                    } catch (TypeCheckException reason){
                        throw new TypeCheckException(
                                Errors.CHECK_CHECK_ARRAY,
                                reason,
                                array.codeOffset(), pipeline.getSource(), arrayType
                        );
                    }
                }
            }

            case NExpr exp -> {
                try {
                    Type synthesizedType = synthesizeType(exp);
                    if (!castAllowlist.allowCastImplicit(synthesizedType, type, symbolTable)) {
                        throw new TypeCheckException(Errors.CHECK_CHECK_GENERIC, exp.codeOffset(), pipeline.getSource(), type, synthesizedType);
                    }
                } catch (TypeSynthesisException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private Type synthesizeType(NExpr expr) throws TypeSynthesisException, TypeCheckException {
        Type synthesizedType = switch (expr) {

            case NStr ignored -> new NamedType("String");

            case NFloat32 ignored -> PrimitiveType.F32;
            case NFloat64 ignored -> PrimitiveType.F64;
            case NUInt8 ignored -> PrimitiveType.U8;
            case NInt8 ignored -> PrimitiveType.I8;
            case NChar ignored -> PrimitiveType.Char;
            case NUInt16 ignored -> PrimitiveType.U16;
            case NInt16 ignored -> PrimitiveType.I16;
            case NUInt32 ignored -> PrimitiveType.U32;
            case NInt32 ignored -> PrimitiveType.I32;
            case NUInt64 ignored -> PrimitiveType.U64;
            case NInt64 ignored -> PrimitiveType.I64;
            case NBool ignored -> PrimitiveType.Bool;

            case NBlock block -> {
                for (AST statement : block.statements) {
                    annotateTypes(statement);
                }
                yield Type.UNIT;
            }

            case NBlockYielding block -> {
                for (AST statement : block.statements) {
                    annotateTypes(statement);
                }
                try {
                    yield synthesizeType(block.value);
                } catch (TypeSynthesisException reason) {
                    throw new TypeSynthesisException(
                            Errors.CHECK_SYNTHESIZE_BLOCK_VAL,
                            reason,
                            block.value.codeOffset(), pipeline.getSource()
                    );
                }
            }

            case NCall call -> {
                Type calledExprType;
                try {
                    calledExprType = synthesizeType(call.callee);
                } catch (TypeSynthesisException reason) {
                    throw new TypeSynthesisException(
                            Errors.CHECK_SYNTHESIZE_CALLED,
                            reason,
                            call.codeOffset(), pipeline.getSource()
                    );
                }
                if (!(calledExprType instanceof FnType fnType))
                    throw new Error("Cannot call value of type " + calledExprType);

                for (int i = 0; i < call.args.length; i++) {
                    try {
                        checkType(call.args[i], fnType.fnParams()[i]);
                    } catch (TypeCheckException reason) {
                        throw new TypeCheckException(
                                Errors.CHECK_CHECK_FN_ARG,
                                reason,
                                call.args[i].codeOffset(), pipeline.getSource(), fnType.fnParams()[i]
                        );
                    }
                }

                yield fnType.fnReturn();
            }

            case NIf if_ -> {
                try {
                    checkType(if_.cond, PrimitiveType.Bool);
                } catch (TypeCheckException reason) {
                    throw new TypeCheckException(
                            Errors.CHECK_CHECK_IF_COND,
                            reason,
                            if_.cond.codeOffset(), pipeline.getSource()
                    );
                }

                annotateTypes(if_.ifBlock);

                yield Type.UNIT;
            }

            case NIfElse ifElse -> {
                try {
                    checkType(ifElse.cond, PrimitiveType.Bool);
                } catch (TypeCheckException reason) {
                    throw new TypeCheckException(
                            Errors.CHECK_CHECK_IF_ELSE_COND,
                            reason,
                            ifElse.cond.codeOffset(), pipeline.getSource()
                    );
                }

                try {
                    Type ifType = synthesizeType(ifElse.ifBlock);
                    try {
                        checkType(ifElse.elseBlock, ifType);
                    } catch (TypeCheckException reason) {
                        throw new TypeCheckException(
                                Errors.CHECK_CHECK_IF_ELSE_ELSE_SYN_FROM_IF,
                                reason,
                                ifElse.elseBlock.codeOffset(), pipeline.getSource(), ifType
                        );
                    }

                    yield ifType;
                } catch (TypeSynthesisException reason) {
                    throw new TypeSynthesisException(
                            Errors.CHECK_SYNTHESIZE_IF_ELSE_FIRST_IF,
                            reason,
                            ifElse.ifBlock.codeOffset(), pipeline.getSource()
                    );
                }
            }

            case NWhile while_ -> {
                try {
                    checkType(while_.cond, PrimitiveType.Bool);
                } catch (TypeCheckException reason) {
                    throw new TypeCheckException(
                            Errors.CHECK_CHECK_WHILE_COND,
                            reason,
                            while_.cond.codeOffset(), pipeline.getSource()
                    );
                }

                try {
                    checkType(while_.block, Type.UNIT);
                } catch (TypeCheckException reason) {
                    throw new TypeCheckException(
                            Errors.CHECK_CHECK_WHILE_BODY,
                            reason,
                            while_.block.codeOffset(), pipeline.getSource()
                    );
                }

                yield Type.UNIT;
            }

            case NMatch match -> {
                Type valType;
                try {
                    valType = synthesizeType(match.value);
                } catch (TypeSynthesisException reason) {
                    throw new TypeSynthesisException(
                            Errors.CHECK_SYNTHESIZE_MATCHED_VAL,
                            reason,
                            match.value.codeOffset(), pipeline.getSource()
                    );
                }
                if (!(symbolTable.tryInstantiateType(valType) instanceof UnionType unionType))
                    throw new Error("Cannot match value of non union type " + valType);

                boolean[] casesCovered = new boolean[unionType.unionTypes().length];
                Type expectedBranchType = null;
                for (int i = 0; i < match.branches.length; i++) {
                    switch (match.branches[i].left) {
                        case NMatch.Pattern.Union unionPattern -> {
                            int unionTypeElementIndex = unionType.resolveElementIndex(unionPattern.element);

                            Type unionElementType = unionType.unionTypes()[unionTypeElementIndex];
                            symbolTable.registerVar(new Identifier(match.branches[i].right.scope, unionPattern.elementVarName), unionElementType);

                            if (casesCovered[unionTypeElementIndex]) {
                                throw new Error("Case already covered");
                            }
                            casesCovered[unionTypeElementIndex] = true;
                        }

                        case NMatch.Pattern.Default ignored -> Arrays.fill(casesCovered, true);

                        default -> throw new IllegalStateException("Unexpected value: " + match.branches[i].left);
                    }

                    if (expectedBranchType == null) {
                        try {
                            expectedBranchType = synthesizeType(match.branches[i].right);
                        } catch (TypeSynthesisException reason) {
                            throw new TypeSynthesisException(
                                    Errors.CHECK_SYNTHESIZE_MATCH_BRANCH,
                                    reason,
                                    match.branches[i].right.codeOffset(), pipeline.getSource()
                            );
                        }
                    }

                    try {
                        checkType(match.branches[i].right, expectedBranchType);
                    } catch (TypeCheckException reason) {
                        throw new TypeCheckException(
                                Errors.CHECK_CHECK_MATCH,
                                reason,
                                match.branches[i].right.codeOffset(), pipeline.getSource(), expectedBranchType
                        );
                    }
                }

                for (int i = 0; i < casesCovered.length; i++) {
                    if (!casesCovered[i]) throw new Error("Match doesn't cover case " + i);
                }

                yield expectedBranchType;
            }

            case NAnonFn fn -> {
                annotateTypes(fn.body);
                yield fn.header.getFnType();
            }

            case NReturn return_ -> {
                NFn enclosingFn = (NFn) return_.scope.enclosingFnScope().scopeOpeningNode;

                Type expectedType = enclosingFn.getFnHeader().returnType;
                try {
                    checkType(return_.expr, expectedType);
                } catch (TypeCheckException reason) {
                    throw new TypeCheckException(
                            Errors.CHECK_CHECK_RETURN,
                            reason,
                            return_.expr.codeOffset(), pipeline.getSource(), expectedType
                    );
                }

                yield Type.UNIT;
            }

            case NAssign assign -> {
                Type leftExpectedType;
                try {
                    leftExpectedType = synthesizeType(assign.left);
                } catch (TypeSynthesisException reason) {
                    throw new TypeSynthesisException(
                            Errors.CHECK_SYNTHESIZE_ASSIGN_LEFT,
                            reason,
                            assign.left.codeOffset(), pipeline.getSource()
                    );
                }

                try {
                    checkType(assign.right, leftExpectedType);
                } catch (TypeCheckException reason) {
                    throw new TypeCheckException(
                            Errors.CHECK_CHECK_ASSIGN,
                            reason,
                            assign.right.codeOffset(), pipeline.getSource(), leftExpectedType
                    );
                }

                yield leftExpectedType;
            }

            case NTuple tup -> {
                NameTypePair[] nameTypePairs = new NameTypePair[tup.elements.length];

                for (int i = 0; i < tup.elements.length; i++) {
                    Pair<Either<Integer, String>, NExpr> element = tup.elements[i];

                    int finalI = i;
                    int index = element.left == null ? i : element.left.match(integer -> integer, ignored -> finalI);

                    if (nameTypePairs[index] != null){
                        throw new TypeSynthesisException(
                                Errors.CHECK_SYNTHESIZE_TUPLE_MULTI_INIT,
                                element.right.codeOffset(), pipeline.getSource()
                        );
                    }

                    Type elemType;
                    try {
                        elemType = synthesizeType(element.right);
                    } catch (TypeSynthesisException reason) {
                        throw new TypeSynthesisException(
                                Errors.CHECK_SYNTHESIZE_TUPLE_ELEM,
                                reason,
                                element.right.codeOffset(), pipeline.getSource()
                        );
                    }
                    nameTypePairs[index] = new NameTypePair(elemType, element.left == null ? null : element.left.match(i_ -> null, str -> str));
                }

                yield new TupleType(nameTypePairs);
            }

            case NUnion union ->
                    throw new TypeSynthesisException(Errors.CHECK_SYNTHESIZE_UNION, union.codeOffset(), pipeline.getSource());

            case NNumInfer numInfer ->
                    throw new TypeSynthesisException(Errors.CHECK_SYNTHESIZE_NUM_INFER, numInfer.codeOffset(), pipeline.getSource());

            case NCast cast -> {
                try {
                    checkType(cast.expr, cast.type);
                    yield cast.type;
                } catch (TypeCheckException reason) {
                    if (castAllowlist.allowCastExplicit(synthesizeType(cast.expr), cast.type, symbolTable))
                        yield cast.type;
                    throw new Error("Cannot cast expr to " + cast.type);
                }
            }

            case NBinOp binOp -> {
                Type left = synthesizeType(binOp.left);
                Type right = synthesizeType(binOp.right);

                BinOpTypeMap map = opTypeMappings.binOpTypeMap(binOp.op);
                if (map == null)
                    throw new Error("Cannot apply operator " + binOp.op + " to " + left + " and " + right);

                yield map.getType(left, right);
            }

            case NAssignCompound assignCompound -> {
                Type left = synthesizeType(assignCompound.left);
                Type right = synthesizeType(assignCompound.right);

                BinOpTypeMap map = opTypeMappings.binOpTypeMap(assignCompound.op);
                if (map == null)
                    throw new Error("Cannot apply operator" + assignCompound.op + " to " + left + " and " + right);

                yield map.getType(left, right);
            }

            case NUnaryOpPost uOp -> {
                Type operandType = synthesizeType(uOp.operand);

                UnaryOpPostTypeMap map = opTypeMappings.unaryOpPostTypeMap(uOp.op);
                if (map == null)
                    throw new Error("Cannot apply postfix operator " + uOp.op + " to " + operandType);

                yield map.getType(operandType, uOp);
            }

            case NUnaryOpPre uOp -> {
                Type operandType = synthesizeType(uOp.operand);

                UnaryOpPreTypeMap map = opTypeMappings.unaryOpPreTypeMap(uOp.op);
                if (map == null)
                    throw new Error("Cannot apply prefix operator " + uOp.op + " to " + operandType);

                yield map.getType(operandType, uOp);
            }

            case NIdent ident -> {
                Identifier id = new Identifier(ident.scope, ident.identifier);
                if (!symbolTable.anyExists(id))
                    throw new Error("Nothing with the name " + id.name() + " exists in scope " + id.scope());

                SymbolTable.Identifiable identifiable = symbolTable.getById(id);
                yield switch (identifiable) {
                    case SymbolTable.Identifiable.Function fn -> fn.fnHeader.getFnType();
                    case SymbolTable.Identifiable.Variable var -> var.varType;
                    case SymbolTable.Identifiable.NamedType ignored ->
                            throw new Error(ident.identifier + " refers to a type in this scope and cannot be used as a value");
                };
            }

            case NRef ref -> {
                try {
                    yield new RefType(synthesizeType(ref.referent));
                } catch (TypeSynthesisException reason) {
                    throw new TypeSynthesisException(
                            Errors.CHECK_SYNTHESIZE_REF_REFERENT,
                            reason,
                            ref.referent.codeOffset(), pipeline.getSource()
                    );
                }
            }

            case NDeRef deRef -> {
                Type deRefType;
                try {
                    deRefType = synthesizeType(deRef.reference);
                } catch (TypeSynthesisException reason) {
                    throw new TypeSynthesisException(
                            Errors.CHECK_SYNTHESIZE_DEREF_REF,
                            reason,
                            deRef.reference.codeOffset(), pipeline.getSource()
                    );
                }

                if (!(deRefType instanceof RefType refType))
                    throw new Error("Trying to dereference value of non reference type");

                yield refType.referentType;
            }

            case NDotAccess dotAccess -> {
                Type accessedType_ = synthesizeType(dotAccess.accessed);
                Type accessedType = symbolTable.tryInstantiateType(accessedType_);

                if (!(accessedType instanceof TupleType tupleType))
                    throw new Error("The dot access operator cannot be applied to this type: " + accessedType);

                yield dotAccess.accessor.match(
                        (uint) -> {
                            if (Integer.compareUnsigned(uint, tupleType.tupleTypes().length) >= 0) {
                                throw new Error("The tuple being accessed has no element at this index");
                            }
                            return tupleType.tupleTypes()[uint];
                        },
                        (ident) -> {
                            Type t = tupleType.typeByName(ident);
                            if (t == null) {
                                throw new Error("The tuple being accessed has no element with this name");
                            }
                            return t;
                        }
                );
            }

            default -> throw new IllegalStateException("Unexpected value: " + expr);
        };

        expr.exprType = synthesizedType;
        return synthesizedType;
    }
}
