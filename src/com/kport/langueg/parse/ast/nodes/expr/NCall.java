package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.expr.integer.NInt8;
import com.kport.langueg.util.Util;

import java.util.Arrays;

public class NCall extends NExpr {
    public NExpr callee;
    public NExpr[] args;

    public NCall(int offset_, NExpr callee_, NExpr... args_){
        super(offset_, Util.concatArrays(new AST[]{callee_}, args_, AST[].class));
        callee = callee_;
        args = args_;
    }

    @Override
    public AST[] getChildren() {
        return Util.concatArrays(new AST[]{callee}, args, AST[].class);
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public String nToString(){
        return "";
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NCall a)) return false;
        return callee.equals(a.callee) && Arrays.deepEquals(args, a.args);
    }
}
