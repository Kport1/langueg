package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;

public abstract class NStatement extends AST {
    public NStatement(int offset_, AST... children) {
        super(offset_, children);
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit(this, context);
    }
}
