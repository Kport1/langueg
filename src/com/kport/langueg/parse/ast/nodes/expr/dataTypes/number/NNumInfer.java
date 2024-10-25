package com.kport.langueg.parse.ast.nodes.expr.dataTypes.number;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;

import java.util.Objects;

public class NNumInfer extends NExpr {

    public String valString;

    public NNumInfer(int offset_, String valString_) {
        super(offset_);
        valString = valString_;
    }

    @Override
    public AST[] getChildren() {
        return null;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    protected String nToString() {
        return valString;
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NNumInfer a)) return false;
        return Objects.equals(valString, a.valString);
    }
}

