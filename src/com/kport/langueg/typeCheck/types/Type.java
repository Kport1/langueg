package com.kport.langueg.typeCheck.types;

import com.kport.langueg.parse.Visitable;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;

public sealed interface Type extends Visitable permits ArrayType, FnType, NamedType, PrimitiveType, RefType, TupleType, UnionType {
    Type UNIT = new TupleType();
    Type ZERO = new UnionType();

    /*
    * 0x01 Primitive    CODE
    * 0x02 Fn           RetT ParamTLen [ParamT]
    * 0x03 Tuple        TupTLen [TupT TupTNameLen [TupTName]]
    * 0x04 Union        UnionTLen [UnionT UnionTNameLen [UnionTName]]
    * 0x05 Array        ArrayT
    * 0x06 Ref          ReferentT
    *
    * 0x10 Custom       NameLen [Name]
    * */
    byte[] serialize();

    int getSize();

    @Override
    default void accept(ASTVisitor visitor, VisitorContext context) {
        visitor.visit(this, context);
    }
}
