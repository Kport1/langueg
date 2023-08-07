package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.NStatement;
import com.sun.jdi.InvalidTypeException;

public class NWhile extends NStatement {

    public NExpr cond;
    public AST block;

    public NWhile(int line_, int column_, NExpr condition_, AST block_){
        super(line_, column_, condition_, block_);
        cond = condition_;
        block = block_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{cond, block};
    }

    @Override
    public void setChild(int index, AST ast) throws InvalidTypeException {
        switch (index){
            case 0 -> {
                if(!(ast instanceof NExpr expr)) throw new InvalidTypeException();
                cond = expr;
            }
            case 1 -> block = ast;
            default -> throw new ArrayIndexOutOfBoundsException();
        }
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
}
