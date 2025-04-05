package com.kport.langueg.parse.ast.nodes.expr.dataTypes;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NDotAccessSpecifier;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.util.Pair;
import com.kport.langueg.util.Span;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NTuple extends NExpr {
    public Pair<NDotAccessSpecifier, NExpr>[] elements;

    public NTuple(Span location_, Pair<NDotAccessSpecifier, NExpr>... elements_) {
        super(location_, Arrays.stream(elements_).mapMulti((BiConsumer<Pair<NDotAccessSpecifier, NExpr>, Consumer<AST>>) (pair, consumer) -> {
            if(pair.left != null)
                consumer.accept(pair.left);
            consumer.accept(pair.right);
        }).toArray(AST[]::new));
        elements = elements_;
    }

    @Override
    public AST[] getChildren() {
        return Arrays.stream(elements).mapMulti((BiConsumer<Pair<NDotAccessSpecifier, NExpr>, Consumer<AST>>) (pair, consumer) -> {
            if(pair.left != null)
                consumer.accept(pair.left);
            consumer.accept(pair.right);
        }).toArray(AST[]::new);
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public String nToString() {
        return "";
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NTuple a)) return false;
        return Arrays.deepEquals(elements, a.elements);
    }
}
