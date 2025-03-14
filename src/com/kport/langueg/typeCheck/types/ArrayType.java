package com.kport.langueg.typeCheck.types;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;

public final class ArrayType implements Type {
    public static final int ARRAY_REF_BYTES = 8;

    public final Type type;

    public ArrayType(Type type_) {
        type = type_;
    }

    public Type arrayType() {
        return type;
    }

    @Override
    public int getSize() {
        return ARRAY_REF_BYTES;
    }

    @Override
    public String toString() {
        return type.toString() + "[]";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ArrayType a)) return false;
        return type.equals(a.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode() ^ 0x6f3a05d9;
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        Type.super.accept(visitor, context);
        visitor.visit(this, context);
    }
}
