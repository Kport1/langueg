package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.util.Span;

import java.util.Arrays;

public class NBlock extends NExpr {

    public AST[] statements;

    public NBlock(Span location_, AST... statements_) {
        super(location_, statements_);
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

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NBlock a)) return false;
        return Arrays.deepEquals(statements, a.statements);
    }
}
