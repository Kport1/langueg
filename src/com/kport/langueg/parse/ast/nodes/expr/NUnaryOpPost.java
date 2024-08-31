package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.expr.integer.NInt8;

public class NUnaryOpPost extends NExpr {
    public NExpr operand;
    public TokenType op;

    public NUnaryOpPost(int offset_, NExpr operand_, TokenType op_){
        super(offset_, operand_);
        operand = operand_;
        op = op_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{operand};
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
        if(!(o instanceof NUnaryOpPost a)) return false;
        return operand.equals(a.operand) && op == a.op;
    }
}
