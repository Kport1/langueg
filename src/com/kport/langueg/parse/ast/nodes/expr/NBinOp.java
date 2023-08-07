package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.sun.jdi.InvalidTypeException;

public class NBinOp extends NExpr {
    public NExpr left, right;
    public TokenType op;

    public NBinOp(int line_, int column_, NExpr left_, NExpr right_, TokenType op_){
        super(line_, column_, left_, right_);
        left = left_;
        right = right_;
        op = op_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{left, right};
    }

    @Override
    public void setChild(int index, AST ast) throws InvalidTypeException {
        switch (index){
            case 0 -> {
                if(!(ast instanceof NExpr expr)) throw new InvalidTypeException();
                left = expr;
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
        return op.name();
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit(this, context);
        left.accept(visitor, VisitorContext.tryClone(context));
        right.accept(visitor, VisitorContext.tryClone(context));
    }
}
