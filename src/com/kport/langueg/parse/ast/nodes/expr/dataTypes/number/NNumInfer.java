package com.kport.langueg.parse.ast.nodes.expr.dataTypes.number;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.util.Span;

import java.util.Objects;

public class NNumInfer extends NExpr {

    public String valString;

    public NNumInfer(Span location_, String valString_) {
        super(location_);
        valString = valString_;
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
        return valString;
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NNumInfer a)) return false;
        return Objects.equals(valString, a.valString);
    }
}

