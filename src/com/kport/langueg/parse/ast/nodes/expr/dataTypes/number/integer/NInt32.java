package com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.integer;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.util.Span;

public class NInt32 extends NExpr {

    public int val;

    public NInt32(Span location_, int val_) {
        super(location_);
        val = val_;
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
        return Integer.toString(val);
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NInt32 a)) return false;
        return val == a.val;
    }
}
