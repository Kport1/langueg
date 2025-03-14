package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;

import java.util.Arrays;

public class NProg extends AST {

    public AST[] statements;

    public NProg(int offset_, AST... children) {
        super(offset_, children);
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

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NProg a)) return false;
        return Arrays.deepEquals(statements, a.statements);
    }
}
