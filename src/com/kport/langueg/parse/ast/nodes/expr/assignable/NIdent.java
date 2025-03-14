package com.kport.langueg.parse.ast.nodes.expr.assignable;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;

public final class NIdent extends NAssignable {

    public String identifier;

    public NIdent(int offset_, String identifier_) {
        super(offset_);
        identifier = identifier_;
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
        return identifier;
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NIdent ident)) return false;
        return identifier.equals(ident.identifier);
    }
}
