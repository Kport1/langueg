package com.kport.langueg.parse.ast.nodes.expr.dataTypes;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.util.Either;

import java.util.Objects;

public class NUnion extends NExpr {
    public Either<Integer, String> initializedElementPosition;
    public NExpr initializedElement;

    public NUnion(int offset_, NExpr initializedElement_, Either<Integer, String> initializedElementPosition_) {
        super(offset_, initializedElement_);
        initializedElement = initializedElement_;
        initializedElementPosition = initializedElementPosition_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{initializedElement};
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public String nToString() {
        return "." + initializedElementPosition.match(String::valueOf, str -> str);
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NUnion a)) return false;
        return a.initializedElementPosition.equals(initializedElementPosition) && Objects.equals(a.initializedElement, initializedElement);
    }
}
