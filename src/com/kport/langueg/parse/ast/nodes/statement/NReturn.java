package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.NStatement;

public class NReturn extends NStatement {

    public NExpr expr;

    public NReturn(int line_, int column_, NExpr expr_) {
        super(line_, column_, expr_);
        expr = expr_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{expr};
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    protected String nToString() {
        return "";
    }
}
