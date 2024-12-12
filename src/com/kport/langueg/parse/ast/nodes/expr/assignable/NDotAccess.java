package com.kport.langueg.parse.ast.nodes.expr.assignable;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.util.Either;

public class NDotAccess extends NAssignable {
    public NExpr accessed;
    public Either<Integer, String> accessor;

    public NDotAccess(int offset_, NExpr accessed_, Either<Integer, String> accessor_){
        super(offset_);
        accessed = accessed_;
        accessor = accessor_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{accessed};
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
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NDotAccess a)) return false;
        return accessed.equals(a.accessed) && accessor.equals(a.accessor);
    }
}
