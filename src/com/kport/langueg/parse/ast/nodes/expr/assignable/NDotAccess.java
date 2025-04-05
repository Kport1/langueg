package com.kport.langueg.parse.ast.nodes.expr.assignable;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NDotAccessSpecifier;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.util.Span;

public final class NDotAccess extends NAssignable {
    public NExpr accessed;
    public NDotAccessSpecifier specifier;

    public NDotAccess(Span location_, NExpr accessed_, NDotAccessSpecifier specifier_) {
        super(location_, accessed_, specifier_);
        accessed = accessed_;
        specifier = specifier_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{accessed, specifier};
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
        if (!(o instanceof NDotAccess a)) return false;
        return accessed.equals(a.accessed) && specifier.equals(a.specifier);
    }
}
