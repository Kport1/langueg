package com.kport.langueg.parse.ast.nodes.expr.controlFlow;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;

public class NWhile extends NExpr {

    public NExpr cond;
    public NExpr block;

    public NWhile(int offset_, NExpr condition_, NExpr block_){
        super(offset_, condition_, block_);
        cond = condition_;
        block = block_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{cond, block};
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
        if(!(o instanceof NWhile a)) return false;
        return cond.equals(a.cond) && block.equals(a.block);
    }
}
