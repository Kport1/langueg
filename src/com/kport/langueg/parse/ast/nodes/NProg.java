package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.statement.NNamedFn;
import com.kport.langueg.util.Util;

public class NProg extends AST {

    public AST[] statements;
    public NNamedFn moduleInterface = null;

    public NProg(int line_, int column_, NNamedFn moduleInterface_, AST... children) {
        super(line_, column_, children);
        moduleInterface = moduleInterface_;
        statements = children;
    }

    public NProg(int line_, int column_, AST... children) {
        super(line_, column_, children);
        statements = children;
    }

    @Override
    public AST[] getChildren() {
        return Util.concatArrays(statements, new AST[]{moduleInterface}, AST[].class);
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
        moduleInterface.accept(visitor, VisitorContext.tryClone(context));
        for (AST statement : statements) {
            statement.accept(visitor, VisitorContext.tryClone(context));
        }
    }
}
