package com.kport.langueg.typeCheck;

import com.kport.langueg.error.ErrorHandler;
import com.kport.langueg.error.Errors;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.*;
import com.kport.langueg.parse.ast.nodes.expr.*;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NIdent;
import com.kport.langueg.parse.ast.nodes.expr.integer.*;
import com.kport.langueg.parse.ast.nodes.statement.*;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.typeCheck.op.*;
import com.kport.langueg.typeCheck.types.*;
import com.kport.langueg.util.Identifier;
import com.kport.langueg.util.Scope;

import java.util.Arrays;
import java.util.Map;

public class DefaultTypeChecker implements TypeChecker{

    private final OpTypeMappingSupplier opTypeMappings = new DefaultOpTypeMappings();

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

        //Resolve Named Types TODO

        //Register named functions and parameters
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
                if(inferredType instanceof PrimitiveType pt && pt == PrimitiveType.Void){
                    throw new Error("Cannot assign void to variable " + varInit.name);
                }

                if(varInit.type != null)
                    if(!varInit.type.equals(inferredType))
                        throw new Error("Cannot assign value of type " + inferredType + " to variable of type " + varInit.type);
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
                if(anonFn.header.returnType == PrimitiveType.Void) return;

                if(!ensureFnReturn(anonFn.body, anonFn.header.returnType, null)) errorHandler.error(Errors.CHECK_FN_DOESNT_RETURN_ON_ALL_PATHS_ANON, anonFn.offset);
            }

            @Override
            public void visit(NNamedFn fn, VisitorContext context) {
                if(fn.header.returnType == PrimitiveType.Void) return;

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
        if(ast instanceof NReturnVoid){
            if(name != null)
                errorHandler.error(Errors.CHECK_FN_RETURN_TYPE_MISMATCH_VOID, ast.offset, name, returnType);

            if(name == null)
                errorHandler.error(Errors.CHECK_FN_RETURN_TYPE_MISMATCH_VOID_ANON, ast.offset, returnType);
        }
        else if(ast instanceof NReturn ret){
            if(!ret.expr.exprType.equals(returnType) && name != null)
                errorHandler.error(Errors.CHECK_FN_RETURN_TYPE_MISMATCH, ast.offset, name, returnType, ret.expr.exprType);

            if(!ret.expr.exprType.equals(returnType) && name == null)
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

            case NStr ignored -> new CustomType("string");

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
                yield getReturnTypeAndVerifyArgs(calledExprType, args);
            }

            case NAnonFn fn -> fn.header.getFnType();

            case NAssign assign -> {
                Type leftExpectedType = getExprType(assign.left);
                Type rightType = getExprType(assign.right);

                if(!leftExpectedType.equals(rightType)){
                    throw new Error("Cannot assign value of type " + rightType + " to location expecting type " + leftExpectedType);
                }

                yield leftExpectedType;
            }

            case NTuple tup -> {
                NameTypePair[] tupTypes = Arrays.stream(tup.elements).map((e) -> new NameTypePair(getExprType(e), null)).toArray(NameTypePair[]::new);
                yield new TupleType(tupTypes);
            }

            case NCast cast -> cast.type; //TODO check if valid cast

            case NBinOp binOp -> {
                Type left = getExprType(binOp.left);
                Type right = getExprType(binOp.right);

                BinOpTypeMap map = opTypeMappings.binOpTypeMap(binOp.op);
                if(map == null){
                    throw new Error("Cannot apply operator " + binOp.op + " to " + left + " and " + right);
                }
                yield map.getType(left, right, binOp);
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
                SymbolTable.Identifiable identifiable = symbolTable.getById(new Identifier(ident.scope, ident.identifier));
                yield switch (identifiable){
                    case SymbolTable.Identifiable.Function fn -> fn.fnHeader.getFnType();
                    case SymbolTable.Identifiable.Variable var -> var.varType;
                    case SymbolTable.Identifiable.NamedType type -> throw new Error(ident.identifier + " refers to a type in this scope and cannot be used as part of an expression");
                };
            }

            default -> throw new IllegalStateException("Unexpected value: " + expr);
        };
    }

    private Type getReturnTypeAndVerifyArgs(Type fnType, Type... args){
        if(!(fnType instanceof FnType fnT)){
            throw new Error("Cannot call value of type " + fnType + " with params " + Arrays.toString(args));
        }

        if(!Arrays.equals(fnT.fnParams(), args)){
            throw new Error("Mismatching function arguments: Cannot call function " + fnType + " with params " + Arrays.toString(args));
        }

        return fnT.fnReturn();
    }
}
