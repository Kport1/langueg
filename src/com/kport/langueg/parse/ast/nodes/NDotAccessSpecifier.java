package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.util.Either;
import com.kport.langueg.util.Span;

import java.util.Objects;

public class NDotAccessSpecifier extends AST {
    public final Either<Integer, String> specifier;

    public NDotAccessSpecifier(Span location_, Either<Integer, String> specifier_) {
        super(location_);
        specifier = specifier_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[0];
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    protected String nToString() {
        return "." + specifier.match(Object::toString, s -> s);
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NDotAccessSpecifier a)) return false;
        return Objects.equals(specifier, a.specifier);
    }
}
