package com.kport.langueg.parse.ast.nodes.expr.assignable;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;

public abstract class NAssignable extends NExpr {
    public boolean isLValue = false;

    public NAssignable(int offset_, AST... children) {
        super(offset_, children);
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit(this, context);
    }
}
