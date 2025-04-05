package com.kport.langueg.parse.ast.nodes.expr.operators;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.util.Span;

public class NRef extends NExpr {
    public NExpr referent;

    public NRef(Span location_, NExpr right_) {
        super(location_, right_);
        referent = right_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{referent};
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public String nToString() {
        return "";
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NRef a)) return false;
        return referent.equals(a.referent);
    }
}
