package com.kport.langueg.parse.ast.nodes.expr.operators;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.util.Span;

public class NUnaryOpPost extends NExpr {
    public NExpr operand;
    public TokenType op;

    public NUnaryOpPost(Span location_, NExpr operand_, TokenType op_) {
        super(location_, operand_);
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
    public String nToString() {
        return op.name();
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NUnaryOpPost a)) return false;
        return operand.equals(a.operand) && op == a.op;
    }
}
