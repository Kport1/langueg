package com.kport.langueg.typeCheck;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
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
import java.util.Objects;
import java.util.function.BiConsumer;

public class DefaultTypeChecker implements TypeChecker{

    //Interface for supplying mappings for binary operators
    private final BinOpTypeMappingSupplier binOpTypeMappings = DefaultBinOpTypeMappings.PLUS;

    private final ScopeTree scopeTree = new ScopeTree();

    public final HashMap<FnIdentifier, Type> fnTypes = new HashMap<>();
    public final HashMap<VarIdentifier, Type> varTypes = new HashMap<>();
    public final HashMap<VarIdentifier, Type> fnParamTypes = new HashMap<>();

    @Override
    public AST process(Object ast_, LanguegPipeline<?, ?> pipeline) {
        AST ast = (AST) ast_;
        //construct scope tree
        searchBlocks(ast, 0, true, (expr, depthCount) -> {
            if(expr.type == Block){
                ScopeTree.Node containingBlock = scopeTree.getNode(depthCount[0], depthCount[1]);
                containingBlock.addChildren(1);
            }
        });
        resetSearchBlocks();

        //Find fn types
        searchBlocks(ast, 0, true, (expr, depthCount) -> {
            if(isNamedFn(expr)){
                AST[] args = Arrays.copyOfRange(expr.children, 0, expr.children.length - 2);
                Type[] argTypes = Arrays.stream(args).map((arg) -> arg.val.getType()).toArray(Type[]::new);

                String name = expr.children[expr.children.length - 1].val.getStr();
                FnIdentifier identifier = new FnIdentifier(depthCount[0], depthCount[1], name, argTypes);
                Type returnType = expr.val.getType();

                //Fn already exists
                if(fnExists(name, depthCount[0], depthCount[1], argTypes)){
                    throw new Error("Duplicate function definition " + identifier + " in current scope");
                }

                fnTypes.put(identifier, returnType);
            }

            //Function params
            if(expr.type == Fn){
                AST[] args = Arrays.copyOfRange(expr.children, 0, expr.children.length - (isNamedFn(expr)? 2 : 1));

                for (AST arg : args) {
                    fnParamTypes.put(new VarIdentifier(depthCount[0], depthCount[1], arg.children[0].val.getStr()), arg.val.getType());
                }
            }
        });
        resetSearchBlocks();

        //Find var types
        searchBlocks(ast, 0, true, (expr, depthCount) -> {
            if(expr.type == Var){
                String name = expr.children[0].val.getStr();

                //Duplicate var
                if(varExists(name, depthCount[0], depthCount[1])){
                    throw new Error("Duplicate var " + name + " in current scope");
                }

                //Duplicate fn
                if(anyFnExists(name, depthCount[0], depthCount[1])){
                    throw new Error("Var and fn cannot have the same name (" + name + ")");
                }

                if (expr.children.length > 1) {
                    Type varType = getExprType(expr.children[1], depthCount[0], depthCount[1]);
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

                    varTypes.put(new VarIdentifier(depthCount[0], depthCount[1], name), expectedType == null? varType : expectedType);
                    expr.val = new ASTType(varType);
                }
                else if(expr.val == null || !expr.val.isType()){
                    throw new Error("Can't infer type of variable " + name + ", because it is not initialized");
                }
                else {
                    varTypes.put(new VarIdentifier(depthCount[0], depthCount[1], name), expr.val.getType());
                }
            }
        });
        resetSearchBlocks();

        //type check
        searchBlocks(ast, 0, true, (expr, depthCount) -> {
            typeCheck(expr, depthCount[0], depthCount[1]);
        });

        //System.out.println("ast:\n" + ast);

        pipeline.putAdditionalData("ScopeTree", scopeTree);
        pipeline.putAdditionalData("FunctionTypes", fnTypes);
        pipeline.putAdditionalData("VariableTypes", varTypes);
        pipeline.putAdditionalData("FunctionParameterTypes", fnParamTypes);

        return ast;
    }

    //0,0 is global scope
    private final HashMap<Integer, Integer> depthCounter = new HashMap<>();
    {
        resetSearchBlocks();
    }
    private void searchBlocks(AST ast, int blockDepth, boolean insideBlock, BiConsumer<AST, int[]> consumer){
        if(hasChildren(ast)){

            if(!depthCounter.containsKey(blockDepth + 1)){
                depthCounter.put(blockDepth + 1, 0);
            }
            int count = depthCounter.get(blockDepth + 1);

            for (AST expr : ast.children) {
                if(insideBlock || expr.type == Block) {
                    consumer.accept(expr, new int[] {blockDepth, depthCounter.get(blockDepth)} );
                }

                if(expr.type == Block){
                    searchBlocks(expr, blockDepth + 1, true, consumer);
                    //System.out.println("1 Found block: d = " + blockDepth + "  c = " + count);
                    depthCounter.put(blockDepth + 1, ++count);
                }
                else{
                    searchBlocks(expr, blockDepth, false, consumer);
                }
            }
        }
    }

    private void resetSearchBlocks(){
        depthCounter.clear();
        depthCounter.put(0,0);
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

    private void typeCheck(AST ast, int depth, int count){
        ast.depth = depth;
        ast.count = count;
        ast.returnType = getExprType(ast, depth, count);

        if(ast.children == null || ast.type == FnArg || ast.type == Block)
            return;

        for (AST child : ast.children) {
            if(child.type != Block)
                typeCheck(child, depth, count);
        }
    }

    private Type getExprType(AST expr, int depth, int count){
        switch(expr.type){
            case Prog, Type, Switch, While, For, Block, Return, FnArg -> {return  new PrimitiveType(TokenType.Void);}
            case Str -> {
                return new CustomType("String");
            }
            case Float -> {
                return new PrimitiveType(TokenType.Float);
            }
            case Double -> {
                return new PrimitiveType(TokenType.Double);
            }
            case Byte -> {
                return new PrimitiveType(TokenType.Byte);
            }
            case Char -> {
                return new PrimitiveType(TokenType.Char);
            }
            case Short -> {
                return new PrimitiveType(TokenType.Short);
            }
            case Int -> {
                return new PrimitiveType(TokenType.Int);
            }
            case Long -> {
                return new PrimitiveType(TokenType.Long);
            }
            case Bool -> {
                return new PrimitiveType(TokenType.Boolean);
            }
            case If -> {
                if(expr.children.length < 3){
                    return new PrimitiveType(TokenType.Void);
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
                if(!isNamedFn(expr)){
                    AST[] args = Arrays.copyOfRange(expr.children, 0, expr.children.length - 1);
                    Type[] argTypes = Arrays.stream(args).map((arg) -> arg.val.getType()).toArray(Type[]::new);

                    //Functions params
                    for (AST arg : args) {
                        fnParamTypes.put(new VarIdentifier(depth, count, arg.children[0].val.getStr()), arg.val.getType());
                    }

                    return new FnType(expr.val.getType(), argTypes);
                }
            }
            case Tuple -> {
                if(expr.children == null || expr.children.length == 0){
                    return new TupleType();
                }
                Type[] tupTypes = Arrays.stream(expr.children).map((type) -> getExprType(type, depth, count)).toArray(Type[]::new);
                return new TupleType(tupTypes);
            }
            //case Class -> {}

            case Var -> {
                if(expr.children.length < 2){
                    return new PrimitiveType(TokenType.Void);
                }
                return getExprType(expr.children[1], depth, count);
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
        return new PrimitiveType(TokenType.Void);
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

    private boolean hasChildren(AST ast){
        return ast.children != null && ast.children.length != 0;
    }

    private boolean isNamedFn(AST fn){
        return fn.type == Fn && fn.children[fn.children.length - 1].type == Identifier;
    }
}
