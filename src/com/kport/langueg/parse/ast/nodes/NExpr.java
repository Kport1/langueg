package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.Span;

public abstract class NExpr extends AST {

    public Type exprType = null;
    public boolean isExprStmnt = false;

    public NExpr(Span location_, AST... children) {
        super(location_, children);
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        super.accept(visitor, context);
        visitor.visit(this, context);
    }
}
