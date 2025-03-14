package com.kport.langueg.typeCheck.types;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;

import java.util.Objects;

public final class FnType implements Type {
    public static final int FN_REF_BYTES = 8;

    private final Type fnParam;
    private final Type fnReturn;

    public FnType(Type fnReturn_, Type fnParam_) {
        fnReturn = fnReturn_;
        fnParam = fnParam_;
    }

    public Type fnReturn() {
        return fnReturn;
    }

    public Type fnParam() {
        return fnParam;
    }

    @Override
    public String toString() {
        return fnParam + " -> " + fnReturn;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FnType t) {
            return Objects.equals(t.fnParam, fnParam) &&
                    Objects.equals(t.fnReturn, fnReturn);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fnParam, fnReturn);
    }

    @Override
    public int getSize() {
        return FN_REF_BYTES;
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        Type.super.accept(visitor, context);
        visitor.visit(this, context);
    }
}
