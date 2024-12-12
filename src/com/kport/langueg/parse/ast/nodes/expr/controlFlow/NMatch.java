package com.kport.langueg.parse.ast.nodes.expr.controlFlow;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.util.Either;
import com.kport.langueg.util.Pair;
import com.kport.langueg.util.Util;

import java.util.Arrays;
import java.util.Objects;

public class NMatch extends NExpr {

    public NExpr value;
    public Pair<Pattern, NExpr>[] branches;

    public NMatch(int offset_, NExpr value_, Pair<Pattern, NExpr>... branches_){
        super(offset_, value_);
        value = value_;
        branches = branches_;
    }

    @Override
    public AST[] getChildren() {
        AST[] branchExprs = new AST[branches.length];
        for (int i = 0; i < branches.length; i++) {
            branchExprs[i] = branches[i].right;
        }
        return Util.concatArrays(new AST[]{value}, branchExprs, AST[].class);
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
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NMatch a)) return false;
        return Objects.equals(value, a.value) && Arrays.deepEquals(branches, a.branches);
    }

    public sealed static class Pattern {
        public static final class Union extends Pattern {
            public final Either<Integer, String> element;
            public final String elementVarName;

            public Union(Either<Integer, String> element_, String elementVarName_){
                element = element_;
                elementVarName = elementVarName_;
            }
        }

        public static final class Default extends Pattern {
            public Default(){

            }
        }
    }
}
