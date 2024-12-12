package com.kport.langueg.typeCheck.types;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.util.Scope;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class NamedType implements Type {
    private final String typeName;
    public final Type[] typeArgs;

    public Scope scope = null;
    public int size;

    public NamedType(String typeName_, Type... typeArgs_){
        typeName = typeName_;
        typeArgs = typeArgs_;
    }

    public NamedType(String typeName_){
        typeName = typeName_;
        typeArgs = new Type[0];
    }

    public String name(){
        return typeName;
    }

    public Type[] typeArgs(){
        return typeArgs;
    }

    @Override
    public byte[] serialize() {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        byte[] nameBytes = typeName.getBytes(StandardCharsets.UTF_8);
        o.write(nameBytes.length);
        o.writeBytes(nameBytes);
        return o.toByteArray();
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        Type.super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public String toString(){
        return typeName + (typeArgs.length == 0? "" : Arrays.toString(typeArgs));
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof NamedType t){
            return t.typeName.equals(typeName) && Arrays.equals(t.typeArgs, typeArgs) && t.scope.equals(scope);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return typeName.hashCode();
    }
}
