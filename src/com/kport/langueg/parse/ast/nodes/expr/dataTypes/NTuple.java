package com.kport.langueg.parse.ast.nodes.expr.dataTypes;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.util.Either;
import com.kport.langueg.util.Pair;

import java.util.Arrays;

public class NTuple extends NExpr {
    public Pair<Either<Integer, String>, NExpr>[] elements;
    //public NExpr[] elements;

    public NTuple(int offset_, Pair<Either<Integer, String>, NExpr>... elements_){
        super(offset_, Arrays.stream(elements_).map(p -> p.right).toArray(NExpr[]::new));
        elements = elements_;
    }

    @Override
    public AST[] getChildren() {
        return Arrays.stream(elements).map(p -> p.right).toArray(NExpr[]::new);
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
        if(!(o instanceof NTuple a)) return false;
        return Arrays.deepEquals(elements, a.elements);
    }
}
