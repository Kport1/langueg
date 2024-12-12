package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NAssignable;

public class NAssign extends NExpr {
    public NAssignable left;
    public NExpr right;

    public NAssign(int offset_, NAssignable left_, NExpr right_){
        super(offset_, left_, right_);
        left = left_;
        right = right_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{left, right};
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public String nToString(){
        return "";
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NAssign a)) return false;
        return left.equals(a.left) && right.equals(a.right);
    }
}
