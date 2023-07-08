package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NExpr;

public class NTuple extends NExpr {
    public NExpr[] elements;

    public NTuple(int line_, int column_, NExpr... elements_){
        super(line_, column_, elements_);
        elements = elements_;
    }

    @Override
    public AST[] getChildren() {
        return elements;
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
