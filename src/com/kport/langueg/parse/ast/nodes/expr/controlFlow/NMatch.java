package com.kport.langueg.parse.ast.nodes.expr.controlFlow;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NDotAccessSpecifier;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.util.Pair;
import com.kport.langueg.util.Span;
import com.kport.langueg.util.Util;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NMatch extends NExpr {

    public NExpr value;
    public Pair<Pattern, NExpr>[] branches;

    public NMatch(Span location_, NExpr value_, Pair<Pattern, NExpr>... branches_) {
        super(location_, Util.concatArrays(new AST[]{value_}, Arrays.stream(branches_).mapMulti((BiConsumer<Pair<Pattern, NExpr>, Consumer<AST>>) (pair, consumer) -> {
            if (pair.left instanceof Pattern.Union unionPattern)
                consumer.accept(unionPattern.specifier);
            consumer.accept(pair.right);
        }).toArray(AST[]::new)));
        value = value_;
        branches = branches_;
    }

    @Override
    public AST[] getChildren() {
        return Util.concatArrays(new AST[]{value}, Arrays.stream(branches).mapMulti((BiConsumer<Pair<Pattern, NExpr>, Consumer<AST>>) (pair, consumer) -> {
            if (pair.left instanceof Pattern.Union unionPattern)
                consumer.accept(unionPattern.specifier);
            consumer.accept(pair.right);
        }).toArray(AST[]::new));
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
        if (!(o instanceof NMatch a)) return false;
        return Objects.equals(value, a.value) && Arrays.deepEquals(branches, a.branches);
    }

    public sealed static class Pattern {
        public static final class Union extends Pattern {
            public final NDotAccessSpecifier specifier;
            public final String elementVarName;

            public Union(NDotAccessSpecifier specifier_, String elementVarName_) {
                specifier = specifier_;
                elementVarName = elementVarName_;
            }
        }

        public static final class Default extends Pattern {
            public Default() {

            }
        }
    }
}
