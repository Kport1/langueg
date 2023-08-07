package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.sun.jdi.InvalidTypeException;

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
    public void setChild(int index, AST ast) throws InvalidTypeException {
        if(!(ast instanceof NExpr expr)) throw new InvalidTypeException();
        elements[index] = expr;
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
}
