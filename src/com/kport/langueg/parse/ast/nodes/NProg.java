package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.parse.ast.AST;

public class NProg extends AST {

    public AST[] statements;

    public NProg(int line_, int column_, AST... children) {
        super(line_, column_, children);
        statements = children;
    }

    @Override
    public AST[] getChildren() {
        return statements;
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
