package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.util.Scope;

public final class NAnonFn extends NExpr implements NFn {

    public FnHeader header;
    public NExpr body;

    public NAnonFn(int offset_, FnHeader header_, NExpr body_){
        super(offset_, body_);
        header = header_;
        body = body_;
    }

    @Override
    public FnHeader getFnHeader(){
        return header;
    }

    @Override
    public AST getBody() {
        return body;
    }

    private Scope bodyScope = null;
    @Override
    public Scope getBodyScope(){
        return bodyScope;
    }

    @Override
    public void setBodyScope(Scope scope_){
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
    public String nToString(){
        return header.toString();
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        super.accept(visitor, context);
        visitor.visit((NFn) this, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NAnonFn a)) return false;
        return header.equals(a.header) && body.equals(a.body);
    }
}
