package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;

public class NStr extends NExpr {

    public String str;

    public NStr(int line_, int column_, String str_) {
        super(line_, column_);
        str = str_;
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
        return str;
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit(this, context);
    }
}
