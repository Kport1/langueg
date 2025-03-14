package com.kport.langueg.parse.ast.nodes.expr.assignable;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;

public final class NDeRef extends NAssignable {
    public NExpr reference;

    public NDeRef(int offset_, NExpr reference_) {
        super(offset_);
        reference = reference_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{reference};
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
        if (!(o instanceof NDeRef a)) return false;
        return reference.equals(a.reference);
    }
}
