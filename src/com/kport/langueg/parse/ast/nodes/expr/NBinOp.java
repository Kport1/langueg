package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.expr.integer.NInt8;

public class NBinOp extends NExpr {
    public NExpr left, right;
    public TokenType op;

    public NBinOp(int offset_, NExpr left_, NExpr right_, TokenType op_){
        super(offset_, left_, right_);
        left = left_;
        right = right_;
        op = op_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{left, right};
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

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NBinOp a)) return false;
        return left.equals(a.left) && right.equals(a.right) && op == a.op;
    }
}
