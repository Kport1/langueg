package com.kport.langueg.typeCheck.types;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.Visitable;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;

public sealed interface Type extends Visitable permits ArrayType, FnType, NamedType, PrimitiveType, RefType, TupleType, UnionType {
    Type UNIT = new TupleType();
    Type ZERO = new UnionType();

    int getSize();

    @Override
    default void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        visitor.visit(this, context);
    }
}