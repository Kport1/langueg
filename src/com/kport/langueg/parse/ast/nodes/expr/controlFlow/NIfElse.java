package com.kport.langueg.parse.ast.nodes.expr.controlFlow;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;

public class NIfElse extends NExpr {

    public NExpr cond;
    public NExpr ifBlock, elseBlock;

    public NIfElse(int offset_, NExpr cond_, NExpr ifBlock_, NExpr elseBlock_){
        super(offset_, cond_, ifBlock_, elseBlock_);
        cond = cond_;
        ifBlock = ifBlock_;
        elseBlock = elseBlock_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{cond, ifBlock, elseBlock};
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
        if(!(o instanceof NIfElse a)) return false;
        return cond.equals(a.cond) && ifBlock.equals(a.ifBlock) && elseBlock.equals(a.elseBlock);
    }
}
