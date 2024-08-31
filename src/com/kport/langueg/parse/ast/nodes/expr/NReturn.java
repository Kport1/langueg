package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.NStatement;

public class NReturn extends NExpr {

    public NExpr expr;

    public NReturn(int offset_, NExpr expr_) {
        super(offset_, expr_);
        expr = expr_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{expr};
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    protected String nToString() {
        return "";
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NReturn a)) return false;
        return expr.equals(a.expr);
    }
}
