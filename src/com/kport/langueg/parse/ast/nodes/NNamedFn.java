package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.util.Scope;

public final class NNamedFn extends NStatement implements NFn {

    public String name;
    public FnHeader header;
    public NExpr body;

    public NNamedFn(int offset_, String name_, FnHeader header_ , NExpr body_){
        super(offset_, body_);
        name = name_;
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

    public FnHeader getHeader(){
        return header;
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
        return name + " " + header;
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit((NFn) this, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NNamedFn a)) return false;
        return name.equals(a.name) && header.equals(a.header) && body.equals(a.body);
    }
}