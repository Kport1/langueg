package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;

public class NProg extends AST {

    public AST[] statements;

    public NProg(int line_, int column_, AST... children) {
        super(line_, column_, children);
        statements = children;
    }

    @Override
    public AST[] getChildren() {
        return statements;
    }

    @Override
    public void setChild(int index, AST ast){
        statements[index] = ast;
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    protected String nToString() {
        return "";
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit(this, context);
        for (AST statement : statements) {
            statement.accept(visitor, VisitorContext.tryClone(context));
        }
    }
}
