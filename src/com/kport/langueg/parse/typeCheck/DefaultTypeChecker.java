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
        //Find fn types
        searchBlocks(ast, 0, (expr, depthCount) -> {
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
        System.out.println(fnTypes);

    }

    private final HashMap<FnIdentifier, Type> fnTypes = new HashMap<>();

    //0,0 is global scope
    private final HashMap<Integer, Integer> depthCounter = new HashMap<>();
    public void searchBlocks(AST ast, int blockDepth, BiConsumer<AST, Map.Entry<Integer, Integer>> consumer){
        if(hasChildren(ast)){

            if(!depthCounter.containsKey(blockDepth)){
                depthCounter.put(blockDepth, 0);
            }
            int count = depthCounter.get(blockDepth);

            for (AST expr : ast.children) {
                consumer.accept(expr, Map.entry(blockDepth, count));
                if(expr.type == ASTType.Block){
                    //System.out.println("1 Found block: d = " + (blockDepth + 1) + "  c = " + count);
                    searchBlocks(expr, blockDepth + 1, consumer);
                    depthCounter.put(blockDepth, ++count);
                    continue;
                }

                //Expressions containing blocks (fn, while, for)
                //TODO Fix this bitchnugget
                if(hasChildren(expr)) {
                    for (AST subExpr : expr.children) {
                        if(subExpr.type == ASTType.Block){
                            searchBlocks(subExpr, blockDepth + 1, consumer);
                            depthCounter.put(blockDepth, ++count);
                        }
                    }
                }
            }
        }
    }

    private boolean hasChildren(AST ast){
        return ast.children != null && ast.children.length != 0;
    }

    private boolean isNamedFn(AST fn){
        return fn.children[fn.children.length - 1].type == ASTType.Identifier;
    }
}
