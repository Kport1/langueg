package com.kport.langueg.typeCheck;

import com.kport.langueg.error.ErrorHandler;
import com.kport.langueg.error.Errors;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.*;
import com.kport.langueg.parse.ast.nodes.expr.*;
import com.kport.langueg.parse.ast.nodes.statement.*;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.typeCheck.op.BinOpTypeMap;
import com.kport.langueg.typeCheck.op.BinOpTypeMappingSupplier;
import com.kport.langueg.typeCheck.op.DefaultBinOpTypeMappings;
import com.kport.langueg.typeCheck.types.*;
import com.kport.langueg.util.ScopeTree;
import com.kport.langueg.util.FnIdentifier;
import com.kport.langueg.util.VarIdentifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class DefaultTypeChecker implements TypeChecker{

    //Interface for supplying mappings for binary operators
    private final BinOpTypeMappingSupplier binOpTypeMappings = DefaultBinOpTypeMappings.PLUS;

    private final ScopeTree scopeTree = new ScopeTree();

    public final HashMap<FnIdentifier, Type> fnTypes = new HashMap<>();
    public final HashMap<VarIdentifier, Type> varTypes = new HashMap<>();
    public final HashMap<VarIdentifier, Type> fnParamTypes = new HashMap<>();

    private ErrorHandler errorHandler;

    @Override
    public AST process(Object ast_, LanguegPipeline<?, ?> pipeline) {
        AST ast = (AST) ast_;
        errorHandler = pipeline.getErrorHandler();

        //construct scope tree and annotate depth, count
        visitBlocks(ast, (expr, depthCount) -> {
            if(expr instanceof NBlock){
                ScopeTree.Node containingBlock = scopeTree.getNode(depthCount[0], depthCount[1]);
                containingBlock.addChildren(1);
            }
            expr.depth = depthCount[0];
            expr.count = depthCount[1];
        });

        //Find fn types
        ast.accept(new ASTVisitor() {
            @Override
            public void visit(NFn fn, VisitorContext context) {
                Type[] paramTypes = Arrays.stream(fn.params).map((param) -> param.type).toArray(Type[]::new);
                FnIdentifier identifier = new FnIdentifier(fn.depth, fn.count, fn.name, paramTypes);

                //Fn already exists
                if(fnExists(fn.name, fn.depth, fn.count, paramTypes)){
                    throw new Error("Duplicate function definition " + identifier + " in current scope");
                }

                fnTypes.put(identifier, fn.returnType);

                for (FnParamDef param : fn.params) {
                    fnParamTypes.put(new VarIdentifier(fn.depth, fn.count, param.name), param.type);
                }
            }

            @Override
            public void visit(NAnonFn anonFn, VisitorContext context) {
                for (FnParamDef param : anonFn.params) {
                    fnParamTypes.put(new VarIdentifier(anonFn.depth, anonFn.count, param.name), param.type);
                }
            }
        }, null);

        //Find var types
        ast.accept(new ASTVisitor() {
            @Override
            public void visit(NVar var, VisitorContext context) {
                //Duplicate var
                if(varExistsInScope(var.name, var.depth, var.count)){
                    throw new Error("Duplicate var " + var.name + " in current scope");
                }

                //Duplicate fn
                if(anyFnExists(var.name, var.depth, var.count)){
                    throw new Error("Var and fn cannot have the same name (" + var.name + ")");
                }

                if(var.type == null){
                    throw new Error("Can't infer type of variable " + var.name + ", because it is not initialized");
                }

                varTypes.put(new VarIdentifier(var.depth, var.count, var.name), var.type);
            }

            @Override
            public void visit(NVarInit varInit, VisitorContext context){
                //Duplicate var
                if(varExistsInScope(varInit.name, varInit.depth, varInit.count)){
                    throw new Error("Duplicate var " + varInit.name + " in current scope");
                }

                //Duplicate fn
                if(anyFnExists(varInit.name, varInit.depth, varInit.count)){
                    throw new Error("Var and fn cannot have the same name (" + varInit.name + ")");
                }

                Type inferredType = getExprType(varInit.init, varInit.depth, varInit.count);
                if(inferredType.primitive() == TokenType.Void){
                    throw new Error("Cannot assign void to variable " + varInit.name);
                }

                if(varInit.type != null){
                    if(!Objects.equals(inferredType, varInit.type)){
                        throw new Error("Cannot assign value of type " + inferredType + " to variable of type " + varInit.type);
                    }
                }
                else {
                    varInit.type = inferredType;
                }

                varTypes.put(new VarIdentifier(varInit.depth, varInit.count, varInit.name), varInit.type);
            }
        }, null);

        //Annotate expression types
        ast.accept(new ASTVisitor() {
            @Override
            public void visit(NExpr expr, VisitorContext context) {
                expr.exprType = getExprType(expr, expr.depth, expr.count);
            }
        }, null);

        //Verify return of functions
        ast.accept(new ASTVisitor() {
            @Override
            public void visit(NAnonFn anonFn, VisitorContext context) {
                if(anonFn.returnType == PrimitiveType.Void) return;

                if(!ensureFnReturn(anonFn.block, anonFn.returnType, null)) errorHandler.error(Errors.CHECK_FN_DOESNT_RETURN_ON_ALL_PATHS_ANON, anonFn.line);
            }

            @Override
            public void visit(NFn fn, VisitorContext context) {
                if(fn.returnType == PrimitiveType.Void) return;

                if(!ensureFnReturn(fn.block, fn.returnType, fn.name)) errorHandler.error(Errors.CHECK_FN_DOESNT_RETURN_ON_ALL_PATHS, fn.line, fn.name);
            }
        }, null);

        //Annotate enclosing function
        ast.accept(new ASTVisitor() {
            @Override
            public void visit(AST ast, VisitorContext context) {
                ast.enclosingFn = (FnIdentifier) context.get("id");
            }

            @Override
            public void visit(NFn fn, VisitorContext context) {
                context.put("id", new FnIdentifier(fn.depth, fn.count, fn.name, fn.getParamTypes()));
            }

            @Override
            public void visit(NAnonFn anonFn, VisitorContext context) {
                context.put("id", new FnIdentifier(anonFn.depth, anonFn.count, null, anonFn.getParamTypes()));
            }
        }, new VisitorContext(Map.of()));

        pipeline.putAdditionalData("ScopeTree", scopeTree);
        pipeline.putAdditionalData("FunctionTypes", fnTypes);
        pipeline.putAdditionalData("VariableTypes", varTypes);
        pipeline.putAdditionalData("FunctionParameterTypes", fnParamTypes);

        return ast;
    }

    private boolean ensureFnReturn(AST ast, Type returnType, String name){
        if(ast instanceof NReturnVoid){
            if(name != null)
                errorHandler.error(Errors.CHECK_FN_RETURN_TYPE_MISMATCH_VOID, ast.line, name, returnType);

            if(name == null)
                errorHandler.error(Errors.CHECK_FN_RETURN_TYPE_MISMATCH_VOID_ANON, ast.line, returnType);
        }
        else if(ast instanceof NReturn ret){
            if(!ret.expr.exprType.equals(returnType) && name != null)
                errorHandler.error(Errors.CHECK_FN_RETURN_TYPE_MISMATCH, ast.line, name, returnType, ret.expr.exprType);

            if(!ret.expr.exprType.equals(returnType) && name == null)
                errorHandler.error(Errors.CHECK_FN_RETURN_TYPE_MISMATCH_ANON, ast.line, returnType, ret.expr.exprType);

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

    private void visitBlocks(AST ast, BiConsumer<AST, int[]> blockVisitor){
        HashMap<Integer, Integer> depthCounter = new HashMap<>();
        depthCounter.put(0, 0);

        ast.accept(new ASTVisitor() {
            @Override
            public void visit(AST ast, VisitorContext context) {
                blockVisitor.accept(ast, new int[]{(int)context.get("depth"), (int)context.get("count")});
            }

            @Override
            public void visit(NBlock block, VisitorContext context){
                int depth = (int)context.get("depth");
                int count = (int)context.get("count");
                if(!depthCounter.containsKey(depth + 1)){
                    depthCounter.put(depth + 1, 0);
                }
                context.put("count", depthCounter.get(depth + 1));
                context.put("depth", depth + 1);
                depthCounter.put(depth + 1, count + 1);
            }
        }, new VisitorContext(Map.of("depth", 0, "count", 0)));
    }

    private boolean fnExists(String name, int depth, int count, Type... args){
        return getFnType(name, depth, count, args) != null;
    }

    private boolean anyFnExists(String name, int depth, int count){
        FnIdentifier idNoArgs = new FnIdentifier(depth, count, name, new Type[0]);
        boolean inScope = fnTypes.keySet().stream().anyMatch((id) -> id.equalsIgnoreArgs(idNoArgs));
        if(!inScope){
            ScopeTree.Node scope = scopeTree.getNode(depth, count);
            if(scope.isRoot()){
                return false;
            }
            return anyFnExists(name, scope.getParent().depth, scope.getParent().count);
        }
        return true;
    }

    private boolean varExists(String name, int depth, int count){
        return getVarType(name, depth, count) != null;
    }

    private boolean varExistsInScope(String name, int depth, int count){
        return varTypes.containsKey(new VarIdentifier(depth, count, name));
    }

    private boolean fnParamExists(String name, int depth, int count){
        return getFnParamType(name, depth, count) != null;
    }

    private Type getFnType(String name, int depth, int count, Type... args){
        Type inScope = fnTypes.get(new FnIdentifier(depth, count, name, args));
        if(inScope == null){
            ScopeTree.Node scope = scopeTree.getNode(depth, count);
            if(scope.isRoot()){
                return null;
            }
            return getFnType(name, scope.getParent().depth, scope.getParent().count, args);
        }
        return inScope;
    }

    private Type[] getAllFnTypes(String name, int depth, int count){
        FnIdentifier idNoArgs = new FnIdentifier(depth, count, name, new Type[0]);
        Type[] inScope = fnTypes.keySet().stream().filter(idNoArgs::equalsIgnoreArgs)
                .map((id) -> new FnType(fnTypes.get(id), id.args())).toArray(Type[]::new);

        ScopeTree.Node scope = scopeTree.getNode(depth, count);
        if(scope.isRoot()){
            return inScope;
        }
        Type[] parentScopes = getAllFnTypes(name, scope.getParent().depth, scope.getParent().count);
        Type[] out = Arrays.copyOfRange(inScope, 0, inScope.length + parentScopes.length);
        System.arraycopy(parentScopes, 0, out, inScope.length, parentScopes.length);
        return out;
    }

    private Type getVarType(String name, int depth, int count){
        Type inScope = varTypes.get(new VarIdentifier(depth, count, name));
        if(inScope == null){
            ScopeTree.Node scope = scopeTree.getNode(depth, count);
            if(scope.isRoot()){
                return null;
            }
            return getVarType(name, scope.getParent().depth, scope.getParent().count);
        }
        return inScope;
    }

    private Type getFnParamType(String name, int depth, int count){
        Type inScope = fnParamTypes.get(new VarIdentifier(depth, count, name));
        if(inScope == null){
            ScopeTree.Node scope = scopeTree.getNode(depth, count);
            if(scope.isRoot()){
                return null;
            }
            return getFnParamType(name, scope.getParent().depth, scope.getParent().count);
        }
        return inScope;
    }

    private Type getExprType(NExpr expr, int depth, int count){
        switch(expr){

            case NStr _i -> {
                return new CustomType("String");
            }

            case NFloat32 _i -> {
                return PrimitiveType.F32;
            }

            case NFloat64 _i -> {
                return PrimitiveType.F64;
            }

            case NUInt8 _i -> {
                return PrimitiveType.U8;
            }

            case NChar _i -> {
                return PrimitiveType.Char;
            }

            case NInt16 _i -> {
                return PrimitiveType.I16;
            }

            case NInt32 _i -> {
                return PrimitiveType.I32;
            }

            case NInt64 _i -> {
                return PrimitiveType.I64;
            }

            case NBool _i -> {
                return PrimitiveType.Bool;
            }

            case NCall call -> {
                Type[] args = Arrays.stream(call.args).map((arg) -> getExprType(arg, depth, count)).toArray(Type[]::new);

                if(call.callee instanceof NIdent ident){
                    Type fnType = getFnType(ident.name, depth, count, args);
                    if(fnType == null){
                        Type varType = getVarType(ident.name, depth, count);
                        if(varType == null){
                            throw new Error("Function or variable " + ident.name + " doesn't exist in current scope " +
                                    "or cannot be called with " + Arrays.toString(args));
                        }
                        return getCalledVarReturn(varType, ident.name, args);
                    }
                    return fnType;
                }

                Type calledExprType = getExprType(call.callee, depth, count);
                return getReturnTypeAndVerifyArgs(calledExprType, args);
            }

            case NAnonFn fn -> {
                return new FnType(fn.returnType, fn.getParamTypes());
            }

            case NTuple tup -> {
                Type[] tupTypes = Arrays.stream(tup.elements).map((elem) -> getExprType(elem, depth, count)).toArray(Type[]::new);
                return new TupleType(tupTypes);
            }

            case NCast cast -> {
                return cast.type;
            }

            case NBinOp binOp -> {
                Type left = getExprType(binOp.left, depth, count);
                Type right = getExprType(binOp.right, depth, count);

                BinOpTypeMap map = binOpTypeMappings.getFromOp(binOp.op);
                if(map == null){
                    throw new Error("Cannot apply operator " + binOp.op + " to " + left + " and " + right);
                }
                return map.getType(left, right, binOp);
            }

            //case UnaryOpBefore -> {}
            //case UnaryOpAfter -> {}
            //case Modifier -> {}

            case NIdent ident -> {
                boolean varExists = varExists(ident.name, depth, count);
                boolean anyFnExists = anyFnExists(ident.name, depth, count);
                ScopeTree.Node parentScope = scopeTree.getNode(depth, count).getParent();
                boolean fnParamExists = parentScope != null && fnParamExists(ident.name, parentScope.depth, parentScope.count);

                if(varExists && anyFnExists){
                    throw new Error("There cannot be a variable and a function with the same name (" + ident.name + ")");
                }

                if(fnParamExists){
                    if(varTypes.get(new VarIdentifier(depth, count, ident.name)) != null){
                        throw new Error("Cannot have var and function parameter with the same name (" + ident.name + ") in the same scope");
                    }
                    return getFnParamType(ident.name, parentScope.depth, parentScope.count);
                }

                if(varExists){
                    return getVarType(ident.name, depth, count);
                }

                if(anyFnExists){
                    Type[] fns = getAllFnTypes(ident.name, depth, count);
                    if(fns.length == 1){
                        return fns[0];
                    }

                }
                throw new Error("Variable " + ident.name + " doesn't exist");
            }

            default -> throw new IllegalStateException("Unexpected value: " + expr);
        }
    }

    private Type getCalledVarReturn(Type varType, String varName, Type... args){
        if(varType.isFn()) {
            return getReturnTypeAndVerifyArgs(varType, args);
        }
        throw new Error("Called variable " + varName + " has type " + varType);
    }

    private Type getReturnTypeAndVerifyArgs(Type fnType, Type... args){
        if(!fnType.isFn()){
            throw new Error("Cannot call value of type " + fnType + " with args " + Arrays.toString(args));
        }

        if(!Arrays.equals(fnType.getFnArgs(), args)){
            throw new Error("Mismatching function arguments: Cannot call function " + fnType + " with args " + Arrays.toString(args));
        }

        return fnType.getFnReturn();
    }
}
