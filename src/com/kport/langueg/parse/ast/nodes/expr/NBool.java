package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.sun.jdi.InvalidTypeException;

public class NBool extends NExpr {

    public boolean bool;

    public NBool(int line_, int column_, boolean bool_) {
        super(line_, column_);
        bool = bool_;
    }

    @Override
    public AST[] getChildren() {
        return null;
    }

    @Override
    public void setChild(int index, AST ast) {
        throw new ArrayIndexOutOfBoundsException();
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    protected String nToString() {
        return Boolean.toString(bool);
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit(this, context);
    }
}
