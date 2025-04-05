package com.kport.langueg.parse.ast.nodes.expr.assignable;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.util.Span;

public abstract sealed class NAssignable extends NExpr permits NDeRef, NDotAccess, NIdent {
    public NAssignable(Span location_, AST... children) {
        super(location_, children);
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        super.accept(visitor, context);
        visitor.visit(this, context);
    }
}
