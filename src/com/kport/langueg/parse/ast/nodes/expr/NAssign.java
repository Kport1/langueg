package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.sun.jdi.InvalidTypeException;

public class NAssign extends NExpr {
    public NAssignable left;
    public NExpr right;

    public NAssign(int line_, int column_, NAssignable left_, NExpr right_){
        super(line_, column_, left_, right_);
        left = left_;
        right = right_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{left, right};
    }

    @Override
    public void setChild(int index, AST ast) throws InvalidTypeException {
        switch (index){
            case 0 -> {
                if(!(ast instanceof NAssignable assignable)) throw new InvalidTypeException();
                left = assignable;
            }
            case 1 -> {
                if(!(ast instanceof NExpr expr)) throw new InvalidTypeException();
                right = expr;
            }
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
        left.accept(visitor, VisitorContext.tryClone(context));
        right.accept(visitor, VisitorContext.tryClone(context));
    }
}
