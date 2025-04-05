package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.typeCheck.types.FnType;
import com.kport.langueg.util.Scope;
import com.kport.langueg.util.Span;

import java.util.Objects;

public final class NAnonFn extends NExpr implements NFn {

    public FnType type;
    public NExpr body;

    public NAnonFn(Span location_, FnType type_, NExpr body_) {
        super(location_, body_);
        type = type_;
        body = body_;
    }

    @Override
    public FnType getFnType() {
        return type;
    }

    @Override
    public AST getBody() {
        return body;
    }

    private Scope bodyScope = null;

    @Override
    public Scope getBodyScope() {
        return bodyScope;
    }

    @Override
    public void setBodyScope(Scope scope_) {
        bodyScope = scope_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{body};
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public String nToString() {
        return type.toString();
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        super.accept(visitor, context);
        visitor.visit((NFn) this, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NAnonFn a)) return false;
        return Objects.equals(type, a.type) && Objects.equals(body, a.body);
    }
}
