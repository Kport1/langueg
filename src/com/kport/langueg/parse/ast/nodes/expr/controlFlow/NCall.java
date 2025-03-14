package com.kport.langueg.parse.ast.nodes.expr.controlFlow;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;

import java.util.Objects;

public class NCall extends NExpr {
    public NExpr callee;
    public NExpr arg;

    public NCall(int offset_, NExpr callee_, NExpr arg_) {
        super(offset_, callee_, arg_);
        callee = callee_;
        arg = arg_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{callee, arg};
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public String nToString() {
        return "";
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NCall a)) return false;
        return Objects.equals(callee, a.callee) && Objects.equals(arg, a.arg);
    }
}
