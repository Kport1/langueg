package com.kport.langueg.typeCheck;

import com.kport.langueg.error.ErrorHandler;
import com.kport.langueg.error.Errors;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.astVals.ASTType;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.typeCheck.op.BinOpTypeMap;
import com.kport.langueg.typeCheck.op.BinOpTypeMappingSupplier;
import com.kport.langueg.typeCheck.op.DefaultBinOpTypeMappings;
import com.kport.langueg.typeCheck.types.*;
import com.kport.langueg.util.ScopeTree;
import com.kport.langueg.util.FnIdentifier;
import com.kport.langueg.util.VarIdentifier;

import static com.kport.langueg.parse.ast.ASTTypeE.*;

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
            if(expr.type == Block){
                ScopeTree.Node containingBlock = scopeTree.getNode(depthCount[0], depthCount[1]);
                containingBlock.addChildren(1);
            }
            expr.depth = depthCount[0];
            expr.count = depthCount[1];
        });

        //Find fn types
        ast.accept((expr, context) -> {
            if(expr.type == Fn){
                AST[] args = Arrays.copyOfRange(expr.children, 0, expr.children.length - 2);
                Type[] argTypes = Arrays.stream(args).map((arg) -> arg.val.getType()).toArray(Type[]::new);

                String name = expr.children[expr.children.length - 1].val.getStr();
                FnIdentifier identifier = new FnIdentifier(expr.depth, expr.count, name, argTypes);
                Type returnType = expr.val.getType();

                //Fn already exists
                if(fnExists(name, expr.depth, expr.count, argTypes)){
                    throw new Error("Duplicate function definition " + identifier + " in current scope");
                }

                fnTypes.put(identifier, returnType);
            }

            //Function params
            if(expr.type == Fn || expr.type == AnonFn){
                AST[] args = Arrays.copyOfRange(expr.children, 0, expr.children.length - (expr.type == Fn? 2 : 1));

                for (AST arg : args) {
                    fnParamTypes.put(new VarIdentifier(expr.depth, expr.count, arg.children[0].val.getStr()), arg.val.getType());
                }
            }
        }, null);

        //Find var types
        ast.accept((expr, context) -> {
            if(expr.type != Var) return;

            String name = expr.children[0].val.getStr();

            //Duplicate var
            if(varExistsInScope(name, expr.depth, expr.count)){
                throw new Error("Duplicate var " + name + " in current scope");
            }

            //Duplicate fn
            if(anyFnExists(name, expr.depth, expr.count)){
                throw new Error("Var and fn cannot have the same name (" + name + ")");
            }

            if (expr.children.length > 1) {
                Type varType = getExprType(expr.children[1], expr.depth, expr.count);
                if(varType.primitive() == TokenType.Void){
                    throw new Error("Cannot assign void to variable " + name);
                }
                Type expectedType = null;

                if(expr.val != null && expr.val.isType()){
                    expectedType = expr.val.getType();
                    if(!Objects.equals(varType, expectedType)){
                        throw new Error("Cannot assign value of type " + varType + " to variable of type " + expectedType);
                    }
                }

                varTypes.put(new VarIdentifier(expr.depth, expr.count, name), expectedType == null? varType : expectedType);
                expr.val = new ASTType(varType);
            }
            else if(expr.val == null || !expr.val.isType()){
                throw new Error("Can't infer type of variable " + name + ", because it is not initialized");
            }
            else {
                varTypes.put(new VarIdentifier(expr.depth, expr.count, name), expr.val.getType());
            }
        }, null);

        //Give expressions return type
        ast.accept((expr, context) -> {
            if(expr.parent != null && (expr.parent.type == Block || expr.parent.type == Prog || expr.parent.type == FnArg)) {
                expr.returnType = PrimitiveType.Void;
                return;
            }
            expr.returnType = getExprType(expr, expr.depth, expr.count);
        }, null);

        //Verify return of functions
        ast.accept((expr, context) -> {
            if(expr.type == Fn){
                Type returnType = expr.val.getType();
                if(returnType == PrimitiveType.Void) return;
                boolean isAnon = expr.children[expr.children.length - 1].type != Identifier;
                String name = isAnon? null : expr.children[expr.children.length - 1].val.getStr();
                int blockIndex = expr.children.length - (isAnon ? 1 : 2);
                Type[] params = Arrays.stream(Arrays.copyOfRange(expr.children, 0, blockIndex))
                        .map(a -> a.returnType).toArray(Type[]::new);

                boolean returnsOnAllPaths = ensureFnReturn(expr.children[blockIndex], returnType, new FnIdentifier(expr.depth, expr.count, name, params));
                if(!returnsOnAllPaths && name == null) errorHandler.error(Errors.CHECK_FN_DOESNT_RETURN_ON_ALL_PATHS_ANON, expr.line);
                if(!returnsOnAllPaths && name != null) errorHandler.error(Errors.CHECK_FN_DOESNT_RETURN_ON_ALL_PATHS, expr.line, name);
            }
        }, null);

        //Annotate enclosing function
        ast.accept((expr, context) -> {
            FnIdentifier id = (FnIdentifier) context.get("id");

            expr.enclosingFn = id;
            if(expr.type == Fn){
                String name = expr.children[expr.children.length - 1].val.getStr();
                AST[] args = Arrays.copyOfRange(expr.children, 0, expr.children.length - 2);
                id = new FnIdentifier(expr.depth, expr.count, name, Arrays.stream(args).map(a -> a.val.getType()).toArray(Type[]::new));
                context.put("id", id);
            }

            if(expr.type == AnonFn){
                AST[] args = Arrays.copyOfRange(expr.children, 0, expr.children.length - 1);
                id = new FnIdentifier(expr.depth, expr.count, null, Arrays.stream(args).map(a -> a.val.getType()).toArray(Type[]::new));
                context.put("id", id);
            }

        }, new VisitorContext(Map.of()));

        //System.out.println("ast:\n" + ast);

        pipeline.putAdditionalData("ScopeTree", scopeTree);
        pipeline.putAdditionalData("FunctionTypes", fnTypes);
        pipeline.putAdditionalData("VariableTypes", varTypes);
        pipeline.putAdditionalData("FunctionParameterTypes", fnParamTypes);

        return ast;
    }

    private boolean ensureFnReturn(AST ast, Type returnType, FnIdentifier fn){
        if(ast.type == Return){
            if(ast.children.length == 0 && fn.name() != null)
                errorHandler.error(Errors.CHECK_FN_RETURN_TYPE_MISMATCH_VOID, ast.line, fn.name(), returnType);

            if(ast.children.length == 0 && fn.name() == null)
                errorHandler.error(Errors.CHECK_FN_RETURN_TYPE_MISMATCH_VOID_ANON, ast.line, returnType);

            if(!ast.children[0].returnType.equals(returnType) && fn.name() != null)
                errorHandler.error(Errors.CHECK_FN_RETURN_TYPE_MISMATCH, ast.line, fn.name(), returnType, ast.children[0].returnType);

            if(!ast.children[0].returnType.equals(returnType) && fn.name() == null)
                errorHandler.error(Errors.CHECK_FN_RETURN_TYPE_MISMATCH_ANON, ast.line, returnType, ast.children[0].returnType);

            return true;
        }
        if(ast.type == If){
            return ast.children.length == 3
                    && ensureFnReturn(ast.children[1], returnType, fn)
                    && ensureFnReturn(ast.children[2], returnType, fn);
        }
        for (AST child : ast.children) {
            if(ensureFnReturn(child, returnType, fn))
                return true;
        }
        return false;
    }

    private void visitBlocks(AST ast, BiConsumer<AST, int[]> blockVisitor){
        HashMap<Integer, Integer> depthCounter = new HashMap<>();
        depthCounter.put(0, 0);


        ast.accept((expr, c) -> {

            int depth = (int) c.get("depth");
            int count = (int) c.get("count");
            blockVisitor.accept(expr, new int[]{depth, count});
            if(expr.type == Block){
                if(!depthCounter.containsKey(depth + 1)){
                    depthCounter.put(depth + 1, 0);
                }
                c.put("count", depthCounter.get(depth + 1));
                c.put("depth", depth + 1);
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

    private Type getExprType(AST expr, int depth, int count){
        switch(expr.type){
            case Prog, Type, Switch, While, For, Block, Return, FnArg -> {return PrimitiveType.Void;}
            case Str -> {
                return new CustomType("String");
            }
            case Float -> {
                return PrimitiveType.Float;
            }
            case Double -> {
                return PrimitiveType.Double;
            }
            case Byte -> {
                return PrimitiveType.Byte;
            }
            case Char -> {
                return PrimitiveType.Char;
            }
            case Short -> {
                return PrimitiveType.Short;
            }
            case Int -> {
                return PrimitiveType.Int;
            }
            case Long -> {
                return PrimitiveType.Long;
            }
            case Bool -> {
                return PrimitiveType.Boolean;
            }
            case If -> {
                if(expr.children.length < 3){
                    return PrimitiveType.Void;
                }

                Type ifType = getExprType(expr.children[1], depth, count);
                Type elseType = getExprType(expr.children[2], depth, count);

                if(!Objects.equals(ifType, elseType)){
                    throw new Error("Return types of if and else branch don't match: " + expr);
                }

                return ifType;
            }
            case Call -> {
                AST called = expr.children[0];
                Type[] args = Arrays.stream(Arrays.copyOfRange(expr.children, 1, expr.children.length))
                        .map((arg) -> getExprType(arg, depth, count)).toArray(Type[]::new);

                if(called.type == Identifier){
                    String name = called.val.getStr();
                    Type fnType = getFnType(name, depth, count, args);
                    if(fnType == null){
                        Type varType = getVarType(name, depth, count);
                        if(varType == null){
                            throw new Error("Function or variable " + name + " doesn't exist in current scope " +
                                    "or cannot be called with " + Arrays.toString(args));
                        }
                        return getCalledVarReturn(varType, name, args);
                    }
                    return fnType;
                }

                Type calledExprType = getExprType(called, depth, count);
                return getReturnTypeAndVerifyArgs(calledExprType, args);
            }
            case Fn -> {
                AST[] args = Arrays.copyOfRange(expr.children, 0, expr.children.length - 1);
                Type[] argTypes = Arrays.stream(args).map((arg) -> arg.val.getType()).toArray(Type[]::new);

                return new FnType(expr.val.getType(), argTypes);
            }
            case AnonFn -> {
                AST[] args = Arrays.copyOfRange(expr.children, 0, expr.children.length - 1);
                Type[] argTypes = Arrays.stream(args).map((arg) -> arg.val.getType()).toArray(Type[]::new);

                return new FnType(expr.val.getType(), argTypes);
            }
            case Tuple -> {
                if(expr.children.length == 0){
                    return new TupleType();
                }
                Type[] tupTypes = Arrays.stream(expr.children).map((type) -> getExprType(type, depth, count)).toArray(Type[]::new);
                return new TupleType(tupTypes);
            }
            //case Class -> {}

            case Var -> {
                if(expr.children.length < 2){
                    return PrimitiveType.Void;
                }
                return getExprType(expr.children[1], depth, count);
            }

            case VarDestruct -> {
                return getExprType(expr.children[expr.children.length - 1], depth, count);
            }

            case Cast -> {
                return expr.val.getType();
            }
            case BinOp -> {
                TokenType op = expr.val.getTok();
                Type left = getExprType(expr.children[0], depth, count);
                Type right = getExprType(expr.children[1], depth, count);

                BinOpTypeMap map = binOpTypeMappings.getFromOp(op);
                if(map == null){
                    throw new Error("Cannot apply operator " + op + " to " + left + " and " + right);
                }
                return map.getType(left, right, expr);
            }
            case UnaryOpBefore -> {}
            case UnaryOpAfter -> {}
            case Modifier -> {}
            case Identifier -> {
                String name = expr.val.getStr();
                boolean varExists = varExists(name, depth, count);
                boolean anyFnExists = anyFnExists(name, depth, count);
                ScopeTree.Node parentScope = scopeTree.getNode(depth, count).getParent();
                boolean fnParamExists = parentScope != null && fnParamExists(name, parentScope.depth, parentScope.count);

                if(varExists && anyFnExists){
                    throw new Error("There cannot be a variable and a function with the same name (" + name + ")");
                }

                if(fnParamExists){
                    if(varTypes.get(new VarIdentifier(depth, count, name)) != null){
                        throw new Error("Cannot have var and function parameter with the same name (" + name + ") in the same scope");
                    }
                    return getFnParamType(name, parentScope.depth, parentScope.count);
                }

                if(varExists){
                    return getVarType(name, depth, count);
                }

                if(anyFnExists){
                    Type[] fns = getAllFnTypes(name, depth, count);
                    if(fns.length == 1){
                        return fns[0];
                    }
                    //TODO: Array of all matching functions
                }
                throw new Error("Variable " + name + " doesn't exist");
            }
        }
        return PrimitiveType.Void;
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
