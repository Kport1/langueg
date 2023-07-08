package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NExpr;

public class NBool extends NExpr {

    public boolean bool;

    public NBool(int line_, int column_, boolean bool_) {
        super(line_, column_);
        bool = bool_;
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
        return Boolean.toString(bool);
    }
}
