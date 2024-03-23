package com.kport.langueg.parse.ast.nodes.expr.assignable;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;

public class NDotAccess extends NAssignable {
    public NExpr expr;
    public NExpr accessor;

    public NDotAccess(int offset_, NExpr expr_, NExpr accessor_){
        super(offset_);
        expr = expr_;
        accessor = accessor_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{expr, accessor};
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
        if(!(o instanceof NDotAccess a)) return false;
        return expr.equals(a.expr) && accessor.equals(a.accessor);
    }
}
