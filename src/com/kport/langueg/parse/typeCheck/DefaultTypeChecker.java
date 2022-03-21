package com.kport.langueg.parse.typeCheck;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.astVals.ASTType;
import com.kport.langueg.parse.typeCheck.typedAST.TypedAST;
import com.kport.langueg.parse.typeCheck.types.OverloadedFnType;
import com.kport.langueg.parse.typeCheck.types.TupleType;
import com.kport.langueg.parse.typeCheck.types.Type;

import static com.kport.langueg.parse.ast.ASTTypeE.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class DefaultTypeChecker implements TypeChecker{
    @Override
    public void check(AST ast) {
        //construct block tree
        searchBlocks(ast, 0, true, (expr, depthCount) -> {
            if(expr.type == Block){
                BlockTree containingBlock = blockTree.findInChildren(depthCount.getKey(), depthCount.getValue());
                int newBlockDepth = depthCount.getKey() + 1;
                int newBlockCount = depthCounter.get(newBlockDepth);
                containingBlock.addChildren(new BlockTree(null, newBlockDepth, newBlockCount));
            }
        });
        resetSearchBlocks();
        //System.out.println(blockTree);

        //Find fn types
        searchBlocks(ast, 0, true, (expr, depthCount) -> {
            if(isNamedFn(expr)){
                AST[] args = Arrays.copyOfRange(expr.children, 0, expr.children.length - 2);
                Type[] argTypes = Arrays.stream(args).map((arg) -> arg.val.getType()).toArray(Type[]::new);

                String name = expr.children[expr.children.length - 1].val.getStr();
                FnIdentifier identifier = new FnIdentifier(depthCount, name, argTypes);
                Type returnType = expr.val.getType();

                //Fn already exists
                if(fnExists(name, depthCount.getKey(), depthCount.getValue(), argTypes)){
                    throw new Error("Duplicate function definition " + identifier + " in current scope");
                }

                fnTypes.put(identifier, returnType);
            }
        });
        resetSearchBlocks();
        System.out.println(fnTypes);

        //Find var types
        searchBlocks(ast, 0, true, (expr, depthCount) -> {
            if(expr.type == Var){
                String name = expr.children[0].val.getStr();

                //Duplicate var
                if(varExists(name, depthCount.getKey(), depthCount.getValue())){
                    throw new Error("Duplicate var " + name + " in current scope");
                }

                //Duplicate fn
                if(anyFnExists(name, depthCount.getKey(), depthCount.getValue())){
                    throw new Error("Var and fn cannot have the same name (" + name + ")");
                }

                if (expr.children.length > 1) {
                    Type varType = getExprType(expr.children[1], depthCount.getKey(), depthCount.getValue());
                    Type expectedType = null;

                    if(expr.val != null && expr.val.isType()){
                        expectedType = expr.val.getType();
                        if(!Objects.equals(varType, expectedType) && !varType.anyOverloadedFnMatches(expectedType)){
                            throw new Error("Cannot assign value of type " + varType + " to variable of type " + expectedType);
                        }
                    }

                    varTypes.put(new VarIdentifier(depthCount, name), expectedType == null? varType : expectedType);
                    expr.val = new ASTType(varType);
                }
                else if(expr.val == null || !expr.val.isType()){
                    throw new Error("Can't infer type of variable " + name + ", because it is not initialized");
                }
                else {
                    varTypes.put(new VarIdentifier(depthCount, name), expr.val.getType());
                }
            }
        });
        resetSearchBlocks();

        //type check
        searchBlocks(ast, 0, true, (expr, depthCount) -> {
            typeCheck(expr, depthCount.getKey(), depthCount.getValue());
        });
        System.out.println(varTypes);

        TypedAST typedAST = new TypedAST(ast, new Type(TokenType.Void));
    }

    private final BlockTree blockTree = new BlockTree(null, 0, 0);

    private final HashMap<FnIdentifier, Type> fnTypes = new HashMap<>();
    private final HashMap<VarIdentifier, Type> varTypes = new HashMap<>();

    //0,0 is global scope
    private final HashMap<Integer, Integer> depthCounter = new HashMap<>();
    {
        resetSearchBlocks();
    }
    private void searchBlocks(AST ast, int blockDepth, boolean insideBlock, BiConsumer<AST, Map.Entry<Integer, Integer>> consumer){
        if(hasChildren(ast)){

            if(!depthCounter.containsKey(blockDepth + 1)){
                depthCounter.put(blockDepth + 1, 0);
            }
            int count = depthCounter.get(blockDepth + 1);

            for (AST expr : ast.children) {
                if(insideBlock) {
                    consumer.accept(expr, Map.entry(blockDepth, depthCounter.get(blockDepth)));
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
        FnIdentifier idNoArgs = new FnIdentifier(Map.entry(depth, count), name, new Type[0]);
        boolean inScope = fnTypes.keySet().stream().anyMatch((id) -> id.equalsIgnoreArgs(idNoArgs));
        if(!inScope){
            BlockTree scope = blockTree.findInChildren(depth, count);
            if(scope.parent == null){
                return false;
            }
            return anyFnExists(name, scope.parent.depth, scope.parent.count);
        }
        return true;
    }

    private boolean varExists(String name, int depth, int count){
        return getVarType(name, depth, count) != null;
    }

    private Type getFnType(String name, int depth, int count, Type... args){
        Type inScope = fnTypes.get(new FnIdentifier(Map.entry(depth, count), name, args));
        if(inScope == null){
            BlockTree scope = blockTree.findInChildren(depth, count);
            if(scope.parent == null){
                return null;
            }
            return getFnType(name, scope.parent.depth, scope.parent.count, args);
        }
        return inScope;
    }

    private Type[] getAllFnTypes(String name, int depth, int count){
        FnIdentifier idNoArgs = new FnIdentifier(Map.entry(depth, count), name, new Type[0]);
        Type[] inScope = fnTypes.keySet().stream().filter(idNoArgs::equalsIgnoreArgs)
                .map((id) -> new Type(fnTypes.get(id), id.args())).toArray(Type[]::new);

        BlockTree scope = blockTree.findInChildren(depth, count);
        if(scope.parent == null){
            return inScope;
        }
        Type[] parentScopes = getAllFnTypes(name, scope.parent.depth, scope.parent.count);
        Type[] out = Arrays.copyOfRange(inScope, 0, inScope.length + parentScopes.length);
        System.arraycopy(parentScopes, 0, out, inScope.length, parentScopes.length);
        return out;
    }

    private Type getVarType(String name, int depth, int count){
        Type inScope = varTypes.get(new VarIdentifier(Map.entry(depth, count), name));
        if(inScope == null){
            BlockTree scope = blockTree.findInChildren(depth, count);
            if(scope.parent == null){
                return null;
            }
            return getVarType(name, scope.parent.depth, scope.parent.count);
        }
        return inScope;
    }

    private void typeCheck(AST ast, int depth, int count){
        switch(ast.type){
            case BinOp -> {
                TokenType op = ast.val.getTok();
                AST left = ast.children[0];
                AST right = ast.children[1];

                if(op == TokenType.Assign){

                    //left is not a var
                    if(left.val == null || !left.val.isStr()){
                        throw new Error("Cannot assign value to something, that is not a variable (" + left + ")");
                    }

                    String varName = left.val.getStr();

                    //left doesn't exist
                    if(!varExists(varName, depth, count)){
                        throw new Error("Variable " + varName + " doesn't exist in current scope");
                    }

                    Type rightType = getExprType(right, depth, count);
                    Type varType = getVarType(varName, depth, count);

                    if(!Objects.equals(rightType, varType)){
                        throw new Error("Variable " + varName + " of type " + varType + " cannot be assigned to expression " + right + " of type " + rightType);
                    }
                }
                else {
                    Type leftType = getExprType(left, depth, count);
                    Type rightType = getExprType(left, depth, count);

                    //TODO: implicit type conversion and operators, that alter type
                    if(!Objects.equals(leftType, rightType)){
                        throw new Error("Operator " + op + " cannot be applied to values of type " + leftType + " and " + rightType);
                    }
                }

            }

            case Identifier -> {

            }

            case Call -> {
                AST called = ast.children[0];
                Type[] args = Arrays.stream(Arrays.copyOfRange(ast.children, 1, ast.children.length))
                        .map((arg) -> getExprType(arg, depth, count)).toArray(Type[]::new);

                if(called.type == Identifier){
                    boolean fnExists = fnExists(called.val.getStr(), depth, count, args);
                    boolean varExists = varExists(called.val.getStr(), depth, count);


                }

            }
        }

        if(ast.children == null)
            return;

        for (AST child : ast.children) {
            if(child.type != Block)
                typeCheck(child, depth, count);
        }
    }

    private Type getExprType(AST expr, int depth, int count){
        switch(expr.type){
            //case Prog -> {}
            //case Type -> {}
            case Str -> {
                return new Type("String");
            }
            case Double -> {
                return new Type(TokenType.Double);
            }
            case Float -> {
                return new Type(TokenType.Float);
            }
            case Int -> {
                return new Type(TokenType.Int);
            }
            case Byte -> {
                return new Type(TokenType.Byte);
            }
            case Long -> {
                return new Type(TokenType.Long);
            }
            case Bool -> {
                return new Type(TokenType.Boolean);
            }
            case If -> {
                if(expr.children.length < 3){
                    return new Type(TokenType.Void);
                }

                Type ifType = getExprType(expr.children[1], depth, count);
                Type elseType = getExprType(expr.children[2], depth, count);

                if(!Objects.equals(ifType, elseType)){
                    throw new Error("Return types of if and else branch don't match: " + expr);
                }

                return ifType;
            }
            case Switch -> {
            }
            case While -> {
            }
            case For -> {
            }
            case Call -> {
                AST called = expr.children[0];
                Type[] args = Arrays.stream(Arrays.copyOfRange(expr.children, 1, expr.children.length))
                        .map((arg) -> getExprType(arg, depth, count)).toArray(Type[]::new);

                if(called.type == Identifier){
                    Type fnType = getFnType(called.val.getStr(), depth, count, args);
                    if(fnType == null){
                        Type varType = getVarType(called.val.getStr(), depth, count);
                        if(varType == null){
                            throw new Error("Function or variable " + called.val.getStr() + " doesn't exist or cannot be called with " + Arrays.toString(args));
                        }
                        if(varType.isOverloaded()){
                            Type[] correctFnFromArgs = Arrays.stream(varType.getOverloadedFns()).filter((fn) -> Arrays.equals(fn.getFnArgs(), args)).toArray(Type[]::new);

                            if(correctFnFromArgs.length > 1){
                                throw new Error("oh no");
                            }

                            return correctFnFromArgs[0];
                        }
                        if(varType.isFn()) {
                            //return varType.getFnReturn();
                            return getReturnTypeAndVerifyArgs(varType, args);
                        }
                        throw new Error("Called variable " + called.val.getStr() + " has type " + varType);
                    }
                    return fnType;
                }

                Type calledExprType = getExprType(called, depth, count);
                return getReturnTypeAndVerifyArgs(calledExprType, args);
            }
            case Block -> {
                return new Type(TokenType.Void);
            }
            //case Return -> {}
            //case Fn -> {}
            //case FnArg -> {}

            //TODO: tuple types
            case Tuple -> {
                Type[] tupTypes = Arrays.stream(expr.children).map((type) -> getExprType(type, depth, count)).toArray(Type[]::new);
                return new TupleType(tupTypes);
            }
            //case Class -> {}

            case Var -> {
                if(expr.children.length < 2){
                    return new Type(TokenType.Void);
                }
                return getExprType(expr.children[1], depth, count);
            }
            case BinOp -> {
                TokenType op = expr.val.getTok();
                AST left = expr.children[0];
                AST right = expr.children[1];

                return getExprType(left, depth, count);
            }
            case UnaryOpBefore -> {
            }
            case UnaryOpAfter -> {
            }
            case Modifier -> {
            }
            case Identifier -> {
                String id = expr.val.getStr();
                boolean varExists = varExists(id, depth, count);
                boolean anyFnExists = anyFnExists(id, depth, count);

                if(varExists && anyFnExists){
                    throw new Error("There cannot be a variable and a function with the same name (" + id + ")");
                }
                else if(varExists){
                    return getVarType(id, depth, count);
                }
                else if(anyFnExists){
                    Type[] fns = getAllFnTypes(id, depth, count);
                    if(fns.length == 1){
                        return fns[0];
                    }
                    return new OverloadedFnType(fns);
                }
                throw new Error("Variable " + id + " doesn't exist");
            }
        }
        return null;
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
