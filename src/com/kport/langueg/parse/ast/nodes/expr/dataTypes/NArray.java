package com.kport.langueg.parse.ast.nodes.expr.dataTypes;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.util.Span;

import java.util.Arrays;

public class NArray extends NExpr {
    public NExpr[] elements;

    public NArray(Span location_, NExpr... elements_) {
        super(location_, elements_);
        elements = elements_;
    }

    @Override
    public AST[] getChildren() {
        return elements;
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
        if (!(o instanceof NArray a)) return false;
        return Arrays.deepEquals(elements, a.elements);
    }
}
