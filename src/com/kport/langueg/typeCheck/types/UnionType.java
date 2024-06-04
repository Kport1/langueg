package com.kport.langueg.typeCheck.types;

import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NameTypePair;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class UnionType implements Type{
    public static final int UNION_TAG_BYTES = 2;

    private final NameTypePair[] nameTypePairs;

    public UnionType(NameTypePair... types){
        nameTypePairs = types;
    }

    public NameTypePair[] nameTypePairs(){
        return nameTypePairs;
    }

    public Type[] unionTypes(){
        return Arrays.stream(nameTypePairs).map(p -> p.type).toArray(Type[]::new);
    }

    public Type typeByName(String name){
        for (NameTypePair nameTypePair : nameTypePairs) {
            if (nameTypePair.name.equals(name)) {
                return nameTypePair.type;
            }
        }
        return null;
    }

    public int indexOfName(String name){
        for (int i = 0; i < nameTypePairs.length; i++) {
            if (nameTypePairs[i].name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public byte[] serialize() {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        o.write(0x04);
        o.write(nameTypePairs.length);
        for (NameTypePair nameTypePair : nameTypePairs) {
            o.writeBytes(nameTypePair.type.serialize());
            byte[] nameBytes = nameTypePair.name.getBytes(StandardCharsets.UTF_8);
            o.write(nameBytes.length);
            o.writeBytes(nameBytes);
        }
        return o.toByteArray();
    }

    @Override
    public int getSize() {
        return UNION_TAG_BYTES + Arrays.stream(unionTypes()).reduce(0, (i, type) -> Math.max(i, type.getSize()), Integer::max);
    }

    @Override
    public String toString(){
        if(nameTypePairs == null || nameTypePairs.length == 0) return "{ }";
        StringBuilder builder = new StringBuilder("{ ");

        for (NameTypePair nameTypePair : nameTypePairs) {
            builder.append(nameTypePair.name == null? nameTypePair.type : nameTypePair);
            builder.append(" | ");
        }

        builder.delete(builder.length() - 2, builder.length());
        builder.append("}");

        return builder.toString();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof UnionType t){
            return Arrays.equals(nameTypePairs, t.nameTypePairs);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return Arrays.hashCode(nameTypePairs);
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) {
        Type.super.accept(visitor, context);
        visitor.visit(this, context);
        for (NameTypePair nameTypePair : nameTypePairs)
            nameTypePair.type.accept(visitor, VisitorContext.tryClone(context));
    }
}
