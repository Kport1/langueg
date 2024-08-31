package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.BinOp;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;

public class NBinOp extends NExpr {
    public NExpr left, right;
    public BinOp op;

    public NBinOp(int offset_, NExpr left_, NExpr right_, TokenType op_){
        super(offset_, left_, right_);
        left = left_;
        right = right_;
        op = BinOp.fromTokenType(op_);
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
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NBinOp a)) return false;
        return left.equals(a.left) && right.equals(a.right) && op == a.op;
    }
}
