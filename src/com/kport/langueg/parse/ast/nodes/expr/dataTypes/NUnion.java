package com.kport.langueg.parse.ast.nodes.expr.dataTypes;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NDotAccessSpecifier;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.util.Span;

import java.util.Objects;

public class NUnion extends NExpr {
    public NDotAccessSpecifier specifier;
    public NExpr initElement;

    public NUnion(Span location_, NExpr initElement_, NDotAccessSpecifier specifier_) {
        super(location_, initElement_, specifier_);
        initElement = initElement_;
        specifier = specifier_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{initElement, specifier};
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public String nToString() {
        return specifier.toString();
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NUnion a)) return false;
        return Objects.equals(specifier, a.specifier) && Objects.equals(initElement, a.initElement);
    }
}
