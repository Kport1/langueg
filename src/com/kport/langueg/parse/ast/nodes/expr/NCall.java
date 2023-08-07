package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.Util;
import com.sun.jdi.InvalidTypeException;

import java.util.Arrays;

public class NCall extends NExpr {
    public NExpr callee;
    public NExpr[] args;

    public NCall(int line_, int column_, NExpr callee_, NExpr... args_){
        super(line_, column_, Util.concatArrays(new AST[]{callee_}, args_, AST[].class));
        callee = callee_;
        args = args_;
    }

    @Override
    public AST[] getChildren() {
        return Util.concatArrays(new AST[]{callee}, args, AST[].class);
    }

    @Override
    public void setChild(int index, AST ast) throws InvalidTypeException {
        if(!(ast instanceof NExpr expr)) throw new InvalidTypeException();
        if(index == 0) callee = expr;
        else {
            index--;
            args[index] = expr;
        }
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
        callee.accept(visitor, VisitorContext.tryClone(context));
        for (NExpr arg : args) {
            arg.accept(visitor, VisitorContext.tryClone(context));
        }
    }
}
