package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.util.Util;

import java.util.Arrays;

public class NBlockYielding extends NExpr {

    public AST[] statements;
    public NExpr value;

    public NBlockYielding(int offset_, NExpr value_, AST... statements_) {
        super(offset_, statements_);
        statements = statements_;
        value = value_;
    }

    @Override
    public AST[] getChildren() {
        return Util.concatArrays(statements, new AST[]{value}, AST[].class);
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
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NBlockYielding a)) return false;
        return Arrays.deepEquals(statements, a.statements);
    }
}
