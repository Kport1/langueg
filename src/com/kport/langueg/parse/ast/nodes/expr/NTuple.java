package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;

import java.util.Arrays;

public class NTuple extends NExpr {
    public NExpr[] elements;

    public NTuple(int offset_, NExpr... elements_){
        super(offset_, elements_);
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

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit(this, context);
        for (NExpr element : elements) {
            element.accept(visitor, VisitorContext.tryClone(context));
        }
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NTuple a)) return false;
        return Arrays.deepEquals(elements, a.elements);
    }
}
