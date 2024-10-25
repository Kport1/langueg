package com.kport.langueg.parse.ast.nodes.expr.operators;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;

public class NRef extends NExpr {
    public NExpr referent;

    public NRef(int offset_, NExpr right_){
        super(offset_, right_);
        referent = right_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{referent};
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
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NRef a)) return false;
        return referent.equals(a.referent);
    }
}
