package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.typeCheck.types.Type;

public abstract class NExpr extends AST {

    public Type exprType = null;

    public NExpr(int line_, int column_, AST... children) {
        super(line_, column_, children);
    }
}
