package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.statement.NVar;
import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.Util;

import java.util.Arrays;

public class NAnonFn extends NExpr {

    public Type returnType;
    public NVar[] params;
    public AST block;

    public NAnonFn(int line_, int column_, Type returnType_, NVar[] params_, AST block_){
        super(line_, column_, Util.concatArrays(params_, new AST[]{block_}, AST[].class));
        returnType = returnType_;
        params = params_;
        block = block_;
    }

    public Type[] getParamTypes(){
        return Arrays.stream(params).map(p -> p.type).toArray(Type[]::new);
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{block};
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public String nToString(){
        return "r: " + returnType.toString() + ", p: " + Arrays.toString(params);
    }
}
