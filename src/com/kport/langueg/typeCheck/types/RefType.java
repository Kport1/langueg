package com.kport.langueg.typeCheck.types;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;

public final class RefType implements Type {
    public static final int REF_BYTES = 8;

    public final Type referentType;

    public RefType(Type referentType_) {
        referentType = referentType_;
    }

    public Type referentType() {
        return referentType;
    }

    @Override
    public int getSize() {
        return REF_BYTES;
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        Type.super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public String toString() {
        return "&" + referentType;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RefType t) {
            return referentType.equals(t.referentType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return referentType.hashCode() + 31;
    }
}
