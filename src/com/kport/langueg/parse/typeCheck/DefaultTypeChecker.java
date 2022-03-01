package com.kport.langueg.parse.typeCheck;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DefaultTypeChecker implements TypeChecker{
    @Override
    public void check(AST ast) {
        //construct block tree
        searchBlocks(ast, 0, true, (expr, depthCount) -> {
            if(expr.type == ASTType.Block){
                BlockTree containingBlock = blockTree.findInChildren(depthCount.getKey(), depthCount.getValue());
                int newBlockDepth = depthCount.getKey() + 1;
                int newBlockCount = depthCounter.get(newBlockDepth);
                containingBlock.addChildren(new BlockTree(null, newBlockDepth, newBlockCount));
            }
        });
        resetSearchBlocks();
        System.out.println(blockTree);

        //Find fn types
        searchBlocks(ast, 0, true, (expr, depthCount) -> {
            if(expr.type == ASTType.Fn && isNamedFn(expr)){
                AST[] args = Arrays.copyOfRange(expr.children, 0, expr.children.length - 2);
                Type[] argTypes = Arrays.stream(args).map((arg) -> {
                    if (arg.val.isTok()) {
                        return new Type(arg.val.getTok());
                    }
                    return new Type(arg.val.getStr());
                }).toArray(Type[]::new);

                FnIdentifier identifier = new FnIdentifier(depthCount, expr.children[expr.children.length - 1].val.getStr(), argTypes);

                Type returnType;
                if(expr.val.isTok()){
                    returnType = new Type(expr.val.getTok());
                }
                else {
                    returnType = new Type(expr.val.getStr());
                }

                fnTypes.put(identifier, returnType);
            }
        });
        resetSearchBlocks();
        System.out.println(fnTypes);


    }

    private final BlockTree blockTree = new BlockTree(null, 0, 0);

    private final HashMap<FnIdentifier, Type> fnTypes = new HashMap<>();
    private final HashMap<VarIdentifier, Type> varTypes = new HashMap<>();

    //0,0 is global scope
    private HashMap<Integer, Integer> depthCounter;
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

                if(expr.type == ASTType.Block){
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
        depthCounter = new HashMap<>();
        depthCounter.put(0,0);
    }

    /*private Type getFnReturnType(AST fnBlock){
        boolean isBlock = fnBlock.type == ASTType.Block;

        //find return statement
        if(isBlock){
            resetSearchBlocks();
            searchBlocks(fnBlock, 0, true, (expr, depthCount) -> {
                
            });
            resetSearchBlocks();
        }
    }*/

    /*private Type getVarType(){

    }*/

    private Type getFnType(String name, int depth, int count, Type... args){
        Type inScope = fnTypes.get(new FnIdentifier(Map.entry(depth, count), name, args));
        if(inScope == null){
            BlockTree scope = blockTree.findInChildren(depth, count);
            if(scope.parent == null){
                //throw new Error("Function " + name + Arrays.toString(args) + " does not exist in current scope");
                return null;
            }
            return getFnType(name, scope.parent.depth, scope.parent.count, args);
        }
        return inScope;
    }

    /*private Type getExprReturnType(AST expr, int depth, int count){
        switch(expr.type){
            case Byte -> {
                return new Type(TokenType.Byte);
            }

            case Int -> {
                return new Type(TokenType.Int);
            }

            case Long -> {
                return new Type(TokenType.Long);
            }

            case Float -> {
                return new Type(TokenType.Float);
            }

            case Double -> {
                return new Type(TokenType.Double);
            }

            case Var -> {
                if(expr.children.length == 1) {
                    return new Type(TokenType.Null);
                }
                return getExprReturnType(expr.children[1], depth, count);
            }

            case Call -> {
                AST called = expr.children[0];
                AST[] args = Arrays.copyOfRange(expr.children, 1, expr.children.length);
                Type[] argTypes = Arrays.stream(args).map((arg) -> getExprReturnType(arg, depth, count)).toArray(Type[]::new);
                if(called.type == ASTType.Identifier){
                    return getFnType(called.val.getStr(), depth, count, argTypes);
                }
            }
        }
    }*/

    private boolean hasChildren(AST ast){
        return ast.children != null && ast.children.length != 0;
    }

    private boolean isNamedFn(AST fn){
        return fn.children[fn.children.length - 1].type == ASTType.Identifier;
    }
}
