package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NExpr;

public class NIdent extends NAssignable {

    public String name;

    public NIdent(int line_, int column_, String name_) {
        super(line_, column_);
        name = name_;
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
        return name;
    }
}
