package com.kport.langueg.typeCheck;

import com.kport.langueg.error.ErrorHandler;
import com.kport.langueg.error.Errors;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.BinOp;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.*;
import com.kport.langueg.parse.ast.nodes.expr.*;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NAssignable;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NDotAccess;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NIdent;
import com.kport.langueg.parse.ast.nodes.expr.integer.*;
import com.kport.langueg.parse.ast.nodes.statement.*;
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
import java.util.function.Predicate;

public class DefaultTypeChecker implements TypeChecker {

    private final OpTypeMappingSupplier opTypeMappings = new DefaultOpTypeMappings();
    private final CastAllowlist castAllowlist = new DefaultCastAllowlist();

    private final SymbolTable symbolTable = new SymbolTable();

    private ErrorHandler errorHandler;

    @Override
    public AST process(Object ast_, LanguegPipeline<?, ?> pipeline) {
        AST ast = (AST) ast_;
        errorHandler = pipeline.getErrorHandler();

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
            public void visit(NMatch match, VisitorContext context){
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
            private void setLValueRec(NAssignable assignable){
                assignable.isLValue = true;
                if (assignable instanceof NIdent) return;
                if (assignable instanceof NDotAccess dotAccess){
                    if(!(dotAccess.accessed instanceof NAssignable dotAssignable)) throw new Error();
                    setLValueRec(dotAssignable);
                    return;
                }
                throw new Error();
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
                if(!varIsInitializedBeforeUse(var.scope.scopeOpeningNode, new Identifier(var.scope, var.name)))
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

        //Verify return of functions
        ast.accept(new ASTVisitor() {
            @Override
            public void visit(NAnonFn anonFn, VisitorContext context) {
                if (!fnReturnsOnAllPaths(anonFn.body, anonFn.header.returnType, null))
                    errorHandler.error(Errors.CHECK_FN_DOESNT_RETURN_ON_ALL_PATHS_ANON, anonFn.codeOffset());

                ASTVisitor.super.visit(anonFn, context);
            }

            @Override
            public void visit(NNamedFn fn, VisitorContext context) {
                if (!fnReturnsOnAllPaths(fn.body, fn.header.returnType, fn.name))
                    errorHandler.error(Errors.CHECK_FN_DOESNT_RETURN_ON_ALL_PATHS, fn.codeOffset(), fn.name);

                ASTVisitor.super.visit(fn, context);
            }
        }, null);

        pipeline.putAdditionalData("SymbolTable", symbolTable);

        return ast;
    }

    private boolean fnReturnsOnAllPaths(AST ast, Type returnType, String name) {
        return propertyHoldsInAllBranches(ast, dep -> {
            if (!(dep instanceof NReturn ret)) return false;

            if (!castAllowlist.allowCastImplicit(ret.expr.exprType, returnType, symbolTable) && name != null) {
                errorHandler.error(Errors.CHECK_FN_RETURN_TYPE_MISMATCH, dep.codeOffset(), name, returnType, ret.expr.exprType);
            }

            if (!castAllowlist.allowCastImplicit(ret.expr.exprType, returnType, symbolTable) && name == null) {
                errorHandler.error(Errors.CHECK_FN_RETURN_TYPE_MISMATCH_ANON, dep.codeOffset(), returnType, ret.expr.exprType);
            }

            return true;
        });
    }

    private boolean varIsInitializedBeforeUse(AST ast, Identifier id) {
        final boolean[] used = {false};
        Type varType = symbolTable.tryInstantiateType(((SymbolTable.Identifiable.Variable) symbolTable.getById(id)).varType);
        Type.InitTracker initTracker = Type.InitTracker.getInitTracker(varType);

        linearCodeScan(ast, node -> {
            if(used[0]) return;

            if(node instanceof NIdent ident && !ident.isLValue &&
                    symbolTable.identifiersReferToSameThing(new Identifier(ident.scope, ident.identifier), id)) used[0] = true;

            if(node instanceof NAssign || node instanceof NAssignCompound){
                Stack<Integer> dotAccessIndices = new Stack<>();
                NAssignable assignable = node instanceof NAssign assign? assign.left : ((NAssignCompound)node).left;
                do{
                    switch (assignable){
                        case NIdent ident -> {
                            if(!symbolTable.identifiersReferToSameThing(new Identifier(ident.scope, ident.identifier), id)) return;
                        }

                        case NDotAccess dotAccess -> {
                            dotAccessIndices.push(dotAccess.accessor.match(i -> i, str ->
                                    switch (symbolTable.tryInstantiateType(dotAccess.accessed.exprType)){
                                        case TupleType tupleType -> tupleType.indexByName(str);
                                        case UnionType unionType -> unionType.indexByName(str);
                                        default -> throw new Error();
                                    }));
                            assignable = (NAssignable) dotAccess.accessed;
                        }

                        default -> throw new Error();
                    }
                } while(!(assignable instanceof NIdent));

                Type.InitTracker initTrackerTmp = initTracker;
                while (!dotAccessIndices.empty()){
                    if (initTrackerTmp instanceof Type.InitTracker.TupleInitTracker tupleInitTracker)
                        initTrackerTmp = tupleInitTracker.getElement(dotAccessIndices.pop());
                    else throw new Error();
                }
                initTrackerTmp.init();
            }
        });

        if(!used[0]) return true;
        return initTracker.isInit();
    }

    //Don't enter functions or control flow structures
    private void linearCodeScan(AST ast, Consumer<AST> consumer) {
        consumer.accept(ast);

        if(ast instanceof NFn) return;
        if(ast instanceof NIfElse ifElse) linearCodeScan(ifElse.cond, consumer);
        if(ast instanceof NIf if_) linearCodeScan(if_.cond, consumer);
        if(ast instanceof NWhile while_) linearCodeScan(while_.cond, consumer);

        if(!ast.hasChildren()) return;
        for (AST child : ast.getChildren()) {
            linearCodeScan(child, consumer);
        }
    }

    private boolean propertyHoldsInAllBranches(AST ast, Predicate<AST> prop) {
        if(prop.test(ast)) return true;

        if(ast instanceof NIfElse ifElse) {
            return propertyHoldsInAllBranches(ifElse.ifBlock, prop) && propertyHoldsInAllBranches(ifElse.elseBlock, prop) || propertyHoldsInAllBranches(ifElse.cond, prop);
        }

        if(ast instanceof NIf if_) return propertyHoldsInAllBranches(if_.cond, prop);
        if(ast instanceof NWhile while_) return propertyHoldsInAllBranches(while_.cond, prop);
        if(ast instanceof NFn) return false;

        if(!ast.hasChildren()) return false;
        for (AST child : ast.getChildren()) {
            if (propertyHoldsInAllBranches(child, prop)) return true;
        }

        return false;
    }

    private void annotateTypes(AST ast){
        switch (ast){
            case NProg prog -> {
                for(AST statement : prog.statements){
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
                if(varInit.type == null) {
                    Type synthesizedType = synthesizeType(varInit.init);
                    if(synthesizedType == null){
                        throw new Error("Can't infer type of expression used to initialize variable " + varInit.name);
                    }
                    varInit.type = synthesizedType;
                }
                else {
                    if(!checkType(varInit.init, varInit.type)){
                        throw new Error("Variable initializer can't be made to have type " + varInit.type);
                    }
                }

                if (!symbolTable.registerVar(new Identifier(varInit.scope, varInit.name), varInit.type)) {
                    throw new Error("Duplicate name " + varInit.name + " in the same scope");
                }
            }

            case NNamedFn namedFn -> annotateTypes(namedFn.body);

            case NExpr expr -> {
                synthesizeType(expr);
                expr.isExprStmnt = true;
            }

            default -> {}
        }
    }

    private boolean checkType(NExpr expr, Type type){
        expr.exprType = type;
        return switch (expr){
            case NTuple tuple -> {
                if(!(symbolTable.tryInstantiateType(type) instanceof TupleType tupleType)) yield false;
                if(tuple.elements.length != tupleType.nameTypePairs().length) yield false;

                boolean allElemOk = true;
                boolean[] elemIsSet = new boolean[tupleType.nameTypePairs().length];
                for (int i = 0; i < tuple.elements.length; i++) {
                    Pair<Either<Integer, String>, NExpr> element = tuple.elements[i];

                    int tupleTypeIndex = i;
                    if(element.left != null) {
                        if(!tupleType.hasElement(element.left)){
                            allElemOk = false;
                            continue;
                        }
                        tupleTypeIndex = tupleType.resolveElementIndex(element.left);
                    }

                    if(elemIsSet[tupleTypeIndex]) allElemOk = false;
                    allElemOk &= checkType(element.right, tupleType.tupleTypes()[tupleTypeIndex]);
                    elemIsSet[tupleTypeIndex] = true;
                }

                yield allElemOk;
            }

            case NUnion union -> {
                if(!(symbolTable.tryInstantiateType(type) instanceof UnionType unionType)) yield false;
                if(!unionType.hasElement(union.initializedElementPosition)) yield false;

                Type expectedType = unionType.resolveElementType(union.initializedElementPosition);
                yield checkType(union.initializedElement, expectedType);
            }

            case NIfElse ifElse -> {
                boolean allOk = true;
                allOk &= checkType(ifElse.cond, PrimitiveType.Bool);
                allOk &= checkType(ifElse.ifBlock, type);
                allOk &= checkType(ifElse.elseBlock, type);

                yield allOk;
            }

            case NRef ref -> {
                if(!(symbolTable.tryInstantiateType(type) instanceof RefType refType)) yield false;
                yield checkType(ref.right, refType.referentType());
            }

            case NExpr exp -> {
                Type synthesizedType = synthesizeType(exp);
                yield castAllowlist.allowCastImplicit(synthesizedType, type, symbolTable);
            }
        };
    }

    private Type synthesizeType(NExpr expr) {
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
                yield synthesizeType(block.value);
            }

            case NCall call -> {
                Type calledExprType = synthesizeType(call.callee);
                if (!(calledExprType instanceof FnType fnType))
                    throw new Error("Cannot call value of type " + calledExprType);

                for (int i = 0; i < call.args.length; i++) {
                    if(!checkType(call.args[i], fnType.fnParams()[i]))
                        throw new Error("Invalid argument type for function expecting " + Arrays.toString(fnType.fnParams()));
                }

                yield fnType.fnReturn();
            }

            case NIf if_ -> {
                if(!checkType(if_.cond, PrimitiveType.Bool)){
                    throw new Error("Condition of if expression can't be made to have boolean type");
                }
                annotateTypes(if_.ifBlock);

                yield Type.UNIT;
            }

            case NIfElse ifElse -> {
                if(!checkType(ifElse.cond, PrimitiveType.Bool)){
                    throw new Error("Condition of if-else expression can't be made to have boolean type");
                }

                Type ifType = synthesizeType(ifElse.ifBlock);
                if(!checkType(ifElse.elseBlock, ifType)){
                    throw new Error("If-else returns a value of type " + ifType + " in if branch, but else branch can't be made to return the same type");
                }

                yield ifType;
            }

            case NWhile while_ -> {
                if(!checkType(while_.cond, PrimitiveType.Bool)){
                    throw new Error("Condition of while-loop can't be made to have boolean type");
                }

                if(!checkType(while_.block, Type.UNIT)) {
                    throw new Error("Loop block of while-loop can't be made to to have unit type");
                }

                yield Type.UNIT;
            }

            case NMatch match -> {
                Type valType = synthesizeType(match.value);
                if(!(symbolTable.tryInstantiateType(valType) instanceof UnionType unionType)) throw new Error("Cannot match value of non union type " + valType);

                boolean[] casesCovered = new boolean[unionType.unionTypes().length];
                Type expectedBranchType = null;
                for (int i = 0; i < match.branches.length; i++) {
                    switch (match.branches[i].left){
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

                    if(expectedBranchType == null){
                        expectedBranchType = synthesizeType(match.branches[i].right);
                    }

                    if(!checkType(match.branches[i].right, expectedBranchType)){
                        throw new Error("Branch of match expression cant be made to have type " + expectedBranchType + " expected by first branch");
                    }
                }

                for (int i = 0; i < casesCovered.length; i++) {
                    if(!casesCovered[i]) throw new Error("Match doesn't cover case " + i);
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
                if(!checkType(return_.expr, expectedType)){
                    throw new Error("Returned value can't be made to have type " + expectedType);
                }

                yield Type.UNIT;
            }

            case NAssign assign -> {
                Type leftExpectedType = synthesizeType(assign.left);

                if (!checkType(assign.right, leftExpectedType)) {
                    throw new Error("Cannot assign to location expecting " + leftExpectedType);
                }

                yield leftExpectedType;
            }

            case NTuple tup -> {
                NameTypePair[] nameTypePairs = new NameTypePair[tup.elements.length];

                for (int i = 0; i < tup.elements.length; i++) {
                    Pair<Either<Integer, String>, NExpr> element = tup.elements[i];

                    int index = i;
                    if(element.left instanceof Either.Left<Integer, String> ind){
                        index = ind.value();
                    }

                    if(nameTypePairs[index] != null) yield null;
                    nameTypePairs[index] = new NameTypePair(synthesizeType(element.right), element.left == null? null : element.left.match(i_ -> null, str -> str));
                }

                yield new TupleType(nameTypePairs);
            }

            case NUnion ignored -> null;

            case NCast cast -> {
                if(checkType(cast.expr, cast.type) ||
                        castAllowlist.allowCastExplicit(
                                synthesizeType(cast.expr),
                                cast.type, symbolTable)
                ) yield cast.type;
                throw new Error("Cannot cast expr to " + cast.type);
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

                BinOpTypeMap map = opTypeMappings.binOpTypeMap(BinOp.fromCompoundAssign(assignCompound.op));
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

            case NRef ref -> new RefType(synthesizeType(ref.right));

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
