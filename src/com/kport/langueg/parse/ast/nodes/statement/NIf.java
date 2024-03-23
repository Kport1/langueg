package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.NStatement;
import com.kport.langueg.parse.ast.nodes.expr.integer.NInt8;

public class NIf extends NStatement {

    public NExpr cond;
    public AST ifBlock;

    public NIf(int offset_, NExpr cond_, AST ifBlock_){
        super(offset_, cond_, ifBlock_);
        cond = cond_;
        ifBlock = ifBlock_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{cond, ifBlock};
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
        ifBlock.accept(visitor, VisitorContext.tryClone(context));
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NIf a)) return false;
        return cond.equals(a.cond) && ifBlock.equals(a.ifBlock);
    }
}
