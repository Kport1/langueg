package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.Util;

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
        return new AST[]{callee};
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public String nToString(){
        return "";
    }
}
