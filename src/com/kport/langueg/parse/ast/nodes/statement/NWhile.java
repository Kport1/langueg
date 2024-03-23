package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.NStatement;
import com.kport.langueg.parse.ast.nodes.expr.integer.NInt8;

public class NWhile extends NStatement {

    public NExpr cond;
    public AST block;

    public NWhile(int offset_, NExpr condition_, AST block_){
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
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit(this, context);
        cond.accept(visitor, VisitorContext.tryClone(context));
        block.accept(visitor, VisitorContext.tryClone(context));
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NWhile a)) return false;
        return cond.equals(a.cond) && block.equals(a.block);
    }
}
