package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NExpr;

public class NChar extends NExpr {

    public char val;

    public NChar(int line_, int column_, char val_) {
        super(line_, column_);
        val = val_;
    }

    @Override
    public AST[] getChildren() {
        return null;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    protected String nToString() {
        return Character.toString(val);
    }
}
