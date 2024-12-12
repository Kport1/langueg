package com.kport.langueg.typeCheck.types;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;

import java.io.ByteArrayOutputStream;

public final class RefType implements Type {
    public static final int REF_BYTES = 8;

    public final Type referentType;

    public RefType(Type referentType_){
        referentType = referentType_;
    }

    public Type referentType(){
        return referentType;
    }

    @Override
    public byte[] serialize() {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        o.write(0x06);
        o.writeBytes(referentType.serialize());
        return o.toByteArray();
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
    public String toString(){
        return "&" + referentType;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof RefType t){
            return referentType.equals(t.referentType);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return referentType.hashCode() + 31;
    }
}
