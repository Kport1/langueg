package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NStatement;

public class NBlock extends NStatement {

    public AST[] statements;

    public NBlock(int line_, int column_, AST... statements_) {
        super(line_, column_, statements_);
        statements = statements_;
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
