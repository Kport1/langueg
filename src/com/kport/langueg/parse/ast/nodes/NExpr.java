package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.typeCheck.types.Type;

public abstract class NExpr extends AST {

    public Type exprType = null;
    public boolean isExprStmnt = false;

    public NExpr(int line_, int column_, AST... children) {
        super(line_, column_, children);
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit(this, context);
    }
}
