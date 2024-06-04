package com.kport.langueg.typeCheck;

import com.kport.langueg.error.ErrorHandler;
import com.kport.langueg.error.Errors;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.BinOp;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.*;
import com.kport.langueg.parse.ast.nodes.expr.*;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NDotAccess;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NIdent;
import com.kport.langueg.parse.ast.nodes.expr.integer.*;
import com.kport.langueg.parse.ast.nodes.statement.*;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.typeCheck.cast.CastAllowlist;
import com.kport.langueg.typeCheck.cast.DefaultCastAllowlist;
import com.kport.langueg.typeCheck.op.*;
import com.kport.langueg.typeCheck.types.*;
import com.kport.langueg.util.Identifier;
import com.kport.langueg.util.Scope;

import java.util.Arrays;
import java.util.Map;

public class DefaultTypeChecker implements TypeChecker{

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
            }

            @Override
            public void visit(NamedType namedType, VisitorContext context){
                namedType.scope = (Scope) context.get("scope");
            }

            @Override
            public void visit(NTypeDef typeDef, VisitorContext context){
                typeDef.definition.accept(new ASTVisitor() {
                    @Override
                    public void visit(NamedType namedType, VisitorContext context_) {
                        namedType.scope = (Scope) context.get("scope");
                    }
                }, null);
            }

            @Override
            public void visit(NBlock block, VisitorContext context){
                Scope oldScope = (Scope) context.get("scope");
                Scope newScope = new Scope(oldScope, false);
                oldScope.children.add(newScope);
                context.put("scope", newScope);
            }

            @Override
            public void visit(NFn fn, VisitorContext context){
                Scope oldScope = (Scope) context.get("scope");
                Scope newScope = new Scope(oldScope, true);
                oldScope.children.add(newScope);
                context.put("scope", newScope);

                fn.setBodyScope(newScope);
            }
        }, new VisitorContext(Map.of("scope", new Scope(null, true))));

        //Register types
        ast.accept(new ASTVisitor() {
            @Override
            public void visit(NTypeDef typeDef, VisitorContext context) {
                if (!symbolTable.registerType(new Identifier(typeDef.scope, typeDef.name), typeDef))
                    throw new Error("Duplicate name " + typeDef.name + " in the same scope");
            }
        }, null);

        //Resolve Type Sizes
        ast.accept(new ASTVisitor() {
            @Override
            public void visit(NamedType namedType, VisitorContext context) {
                namedType.size = symbolTable.getNamedTypeSize(namedType);
            }
        }, null);

        //Register named functions and function parameters
        ast.accept(new ASTVisitor() {
            @Override
            public void visit(NNamedFn fn, VisitorContext context) {
                if(!symbolTable.registerFn(new Identifier(fn.scope, fn.name), fn.header))
                    throw new Error("Duplicate name " + fn.name + " in the same scope");
            }

            @Override
            public void visit(NFn fn, VisitorContext context) {
                for (NameTypePair param : fn.getFnHeader().params) {
                    if(!symbolTable.registerVar(new Identifier(fn.getBodyScope(), param.name), param.type))
                        throw new Error("Duplicate name " + param.name + " in the same scope");
                }
            }
        }, null);

        //Register variables
        ast.accept(new ASTVisitor() {
            @Override
            public void visit(NVar var, VisitorContext context) {
                if(var.type == null) throw new Error("Can't infer type of variable " + var.name + ", because it is not initialized");

                if(!symbolTable.registerVar(new Identifier(var.scope, var.name), var.type))
                    throw new Error("Duplicate name " + var.name + " in the same scope");
            }

            @Override
            public void visit(NVarInit varInit, VisitorContext context){
                Type inferredType = getExprType(varInit.init);

                if(varInit.type != null) {
                    if (!castAllowlist.allowCastImplicit(varInit.type, inferredType, symbolTable))
                        throw new Error("Cannot assign value of type " + inferredType + " to variable of type " + varInit.type);
                }
                else varInit.type = inferredType;

                if(!symbolTable.registerVar(new Identifier(varInit.scope, varInit.name), varInit.type))
                    throw new Error("Duplicate name " + varInit.name + " in the same scope");
            }
        }, null);

        //Annotate expression types
        ast.accept(new ASTVisitor() {
            @Override
            public void visit(NExpr expr, VisitorContext context) {
                expr.exprType = getExprType(expr);
            }
        }, null);

        //Find expression statements
        ast.accept(new ASTVisitor() {
            @Override
            public void visit(NProg prog, VisitorContext context) {
                for (AST stmnt : prog.statements) {
                    if(stmnt instanceof NExpr expr) expr.isExprStmnt = true;
                }
            }

            @Override
            public void visit(NFn fn, VisitorContext context) {
                if(fn.getBody() instanceof NExpr expr) expr.isExprStmnt = true;
            }

            @Override
            public void visit(NBlock block, VisitorContext context) {
                for (AST stmnt : block.statements) {
                    if(stmnt instanceof NExpr expr) expr.isExprStmnt = true;
                }
            }

            @Override
            public void visit(NIf if_, VisitorContext context) {
                if(if_.ifBlock instanceof NExpr expr) expr.isExprStmnt = true;
            }

            @Override
            public void visit(NIfElse ifElse, VisitorContext context) {
                if(ifElse.ifBlock instanceof NExpr expr) expr.isExprStmnt = true;
                if(ifElse.elseBlock instanceof NExpr expr) expr.isExprStmnt = true;
            }

            @Override
            public void visit(NWhile while_, VisitorContext context) {
                if(while_.block instanceof NExpr expr) expr.isExprStmnt = true;
            }
        }, null);

        //Verify return of functions
        ast.accept(new ASTVisitor() {
            @Override
            public void visit(NAnonFn anonFn, VisitorContext context) {
                if(!ensureFnReturn(anonFn.body, anonFn.header.returnType, null)) errorHandler.error(Errors.CHECK_FN_DOESNT_RETURN_ON_ALL_PATHS_ANON, anonFn.offset);
            }

            @Override
            public void visit(NNamedFn fn, VisitorContext context) {
                if(!ensureFnReturn(fn.body, fn.header.returnType, fn.name)) errorHandler.error(Errors.CHECK_FN_DOESNT_RETURN_ON_ALL_PATHS, fn.offset, fn.name);
            }
        }, null);

        //Verify expected type of statements
        ast.accept(new ASTVisitor() {
            @Override
            public void visit(NIf if_, VisitorContext context) {
                if(if_.cond.exprType != PrimitiveType.Bool) throw new Error("Type of if condition is not a boolean");
            }

            @Override
            public void visit(NIfElse ifElse, VisitorContext context) {
                if(ifElse.cond.exprType != PrimitiveType.Bool) throw new Error("Type of if-else condition is not a boolean");
            }

            @Override
            public void visit(NWhile while_, VisitorContext context) {
                if(while_.cond.exprType != PrimitiveType.Bool) throw new Error("Type of while condition is not a boolean");
            }
        }, null);

        pipeline.putAdditionalData("SymbolTable", symbolTable);

        return ast;
    }

    private boolean ensureFnReturn(AST ast, Type returnType, String name){
        if(ast instanceof NReturn ret){
            if(!castAllowlist.allowCastImplicit(ret.expr.exprType, returnType, symbolTable) && name != null)
                errorHandler.error(Errors.CHECK_FN_RETURN_TYPE_MISMATCH, ast.offset, name, returnType, ret.expr.exprType);

            if(!castAllowlist.allowCastImplicit(ret.expr.exprType, returnType, symbolTable) && name == null)
                errorHandler.error(Errors.CHECK_FN_RETURN_TYPE_MISMATCH_ANON, ast.offset, returnType, ret.expr.exprType);

            return true;
        }

        if(ast instanceof NIfElse ifElse){
            return     ensureFnReturn(ifElse.ifBlock, returnType, name)
                    && ensureFnReturn(ifElse.elseBlock, returnType, name);
        }

        if(ast instanceof NBlock block) {
            for (AST child : block.statements) {
                if (ensureFnReturn(child, returnType, name))
                    return true;
            }
        }
        return false;
    }

    private Type getExprType(NExpr expr){
        return switch(expr){

            case NStr ignored -> new NamedType("string");

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

            case NCall call -> {
                Type[] args = Arrays.stream(call.args).map(this::getExprType).toArray(Type[]::new);

                Type calledExprType = getExprType(call.callee);
                if(!(calledExprType instanceof FnType fnType)) throw new Error("Cannot call value of type " + calledExprType + " with params " + Arrays.toString(args));
                yield getReturnTypeAndVerifyArgs(fnType, args);
            }

            case NAnonFn fn -> fn.header.getFnType();

            case NAssign assign -> {
                Type leftExpectedType = getExprType(assign.left);
                Type rightType = getExprType(assign.right);

                if(!castAllowlist.allowCastImplicit(leftExpectedType, rightType, symbolTable)){
                    throw new Error("Cannot assign value of type " + rightType + " to location expecting type " + leftExpectedType);
                }

                yield leftExpectedType;
            }

            case NTuple tup -> {
                NameTypePair[] tupTypes = Arrays.stream(tup.elements).map((e) -> new NameTypePair(getExprType(e), null)).toArray(NameTypePair[]::new);
                yield new TupleType(tupTypes);
            }

            case NCast cast -> {
                Type castedType = getExprType(cast.expr);
                if(!castAllowlist.allowCastExplicit(castedType, cast.type, symbolTable)) throw new Error("Cannot cast from " + castedType + " to " + cast.type);
                yield cast.type;
            }

            case NBinOp binOp -> {
                Type left = getExprType(binOp.left);
                Type right = getExprType(binOp.right);

                BinOpTypeMap map = opTypeMappings.binOpTypeMap(binOp.op);
                if(map == null){
                    throw new Error("Cannot apply operator " + binOp.op + " to " + left + " and " + right);
                }
                yield map.getType(left, right);
            }

            case NAssignCompound assignCompound -> {
                Type left = getExprType(assignCompound.left);
                Type right = getExprType(assignCompound.right);

                BinOpTypeMap map = opTypeMappings.binOpTypeMap(BinOp.fromCompoundAssign(assignCompound.op));
                if(map == null){
                    throw new Error("Cannot apply operator" + assignCompound.op + " to " + left + " and " + right);
                }
                yield map.getType(left, right);
            }

            case NUnaryOpPost uOp -> {
                Type operandType = getExprType(uOp.operand);

                UnaryOpPostTypeMap map = opTypeMappings.unaryOpPostTypeMap(uOp.op);
                if(map == null){
                    throw new Error("Cannot apply postfix operator " + uOp.op + " to " + operandType);
                }
                yield map.getType(operandType, uOp);
            }

            case NUnaryOpPre uOp -> {
                Type operandType = getExprType(uOp.operand);

                UnaryOpPreTypeMap map = opTypeMappings.unaryOpPreTypeMap(uOp.op);
                if(map == null){
                    throw new Error("Cannot apply prefix operator " + uOp.op + " to " + operandType);
                }
                yield map.getType(operandType, uOp);
            }

            case NIdent ident -> {
                Identifier id = new Identifier(ident.scope, ident.identifier);
                if(!symbolTable.anyExists(id)) throw new Error("Nothing with the name " + id.name() + " exists in scope " + id.scope());

                SymbolTable.Identifiable identifiable = symbolTable.getById(id);
                yield switch (identifiable){
                    case SymbolTable.Identifiable.Function fn -> fn.fnHeader.getFnType();
                    case SymbolTable.Identifiable.Variable var -> var.varType;
                    case SymbolTable.Identifiable.NamedType ignored -> throw new Error(ident.identifier + " refers to a type in this scope and cannot be used as part of an expression");
                };
            }

            case NRef ref -> new RefType(getExprType(ref.right));

            case NDotAccess dotAccess -> {
                Type accessedType_ = getExprType(dotAccess.accessed);
                Type accessedType = symbolTable.tryInstantiateType(accessedType_);

                yield switch (accessedType){
                    case TupleType tupleType ->
                        dotAccess.accessor.match((uint) -> {
                            if (Integer.compareUnsigned(uint, tupleType.tupleTypes().length) >= 0) {
                                throw new Error("The tuple being accessed has no element at this index");
                            }
                            return tupleType.tupleTypes()[uint];
                        }, (ident) -> {
                            Type t = tupleType.typeByName(ident);
                            if(t == null){
                                throw new Error("The tuple being accessed has no element with this name");
                            }
                            return t;
                        });

                    case UnionType unionType ->
                        dotAccess.accessor.match((uint) -> {
                            if (Integer.compareUnsigned(uint, unionType.unionTypes().length) >= 0) {
                                throw new Error("The union being accessed has no element at this index");
                            }
                            return unionType.unionTypes()[uint];
                        }, (ident) -> {
                            Type t = unionType.typeByName(ident);
                            if(t == null){
                                throw new Error("The union being accessed has no element with this name");
                            }
                            return t;
                        });

                    default -> throw new Error("The dot access operator cannot be applied to this type: " + accessedType);
                };
            }

            default -> throw new IllegalStateException("Unexpected value: " + expr);
        };
    }

    private Type getReturnTypeAndVerifyArgs(FnType fnType, Type... args){
        if(fnType.fnParams().length != args.length) throw new Error("Mismatching number of function arguments: Cannot call function " + fnType + " with params " + Arrays.toString(args));
        for (int i = 0; i < fnType.fnParams().length; i++) {
            if(!castAllowlist.allowCastImplicit(args[i], fnType.fnParams()[i], symbolTable)) throw new Error("Mismatching type of function arguments: Cannot call function " + fnType + " with params " + Arrays.toString(args));
        }

        return fnType.fnReturn();
    }
}
