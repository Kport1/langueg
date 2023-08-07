package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.NStatement;
import com.sun.jdi.InvalidTypeException;

public class NIfElse extends NStatement {

    public NExpr cond;
    public AST ifBlock, elseBlock;

    public NIfElse(int line_, int column_, NExpr cond_, AST ifBlock_, AST elseBlock_){
        super(line_, column_, cond_, ifBlock_, elseBlock_);
        cond = cond_;
        ifBlock = ifBlock_;
        elseBlock = elseBlock_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{cond, ifBlock, elseBlock};
    }

    @Override
    public void setChild(int index, AST ast) throws InvalidTypeException {
        switch (index){
            case 0 -> {
                if(!(ast instanceof NExpr expr)) throw new InvalidTypeException();
                cond = expr;
            }
            case 1 -> ifBlock = ast;
            case 2 -> elseBlock = ast;
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
        ifBlock.accept(visitor, VisitorContext.tryClone(context));
        elseBlock.accept(visitor, VisitorContext.tryClone(context));
    }
}
