package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.NStatement;
import com.sun.jdi.InvalidTypeException;

public class NBlock extends NStatement {

    public AST[] statements;

    public NBlock(int line_, int column_, AST... statements_) {
        super(line_, column_, statements_);
        statements = statements_;
    }

    @Override
    public AST[] getChildren() {
        return statements;
    }

    @Override
    public void setChild(int index, AST ast) {
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
