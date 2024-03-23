package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NStatement;
import com.kport.langueg.parse.ast.nodes.expr.integer.NInt8;

import java.util.Arrays;

public class NBlock extends NStatement {

    public AST[] statements;

    public NBlock(int offset_, AST... statements_) {
        super(offset_, statements_);
        statements = statements_;
    }

    @Override
    public AST[] getChildren() {
        return statements;
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

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NBlock a)) return false;
        return Arrays.deepEquals(statements, a.statements);
    }
}
