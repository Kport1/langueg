package com.kport.langueg.parse.ast.nodes.expr.operators;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.*;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NAssignable;

public class NAssignCompound extends NExpr {
    public NAssignable left;
    public NExpr right;
    public CompoundAssign op;

    public NAssignCompound(int offset_, NAssignable left_, NExpr right_, TokenType op_){
        super(offset_, left_, right_);
        left = left_;
        right = right_;
        op = CompoundAssign.fromTokenType(op_);
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
        if(!(o instanceof NAssignCompound a)) return false;
        return left.equals(a.left) && right.equals(a.right) && op == a.op;
    }
}
