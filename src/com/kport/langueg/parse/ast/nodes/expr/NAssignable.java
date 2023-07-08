package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NExpr;

public abstract class NAssignable extends NExpr {
    public NAssignable(int line_, int column_, AST... children) {
        super(line_, column_, children);
    }
}
