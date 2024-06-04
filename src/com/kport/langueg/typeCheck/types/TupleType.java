package com.kport.langueg.typeCheck.types;

import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NameTypePair;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class TupleType implements Type{
    private final NameTypePair[] nameTypePairs;

    public TupleType(NameTypePair... types){
        nameTypePairs = types;
    }

    public NameTypePair[] nameTypePairs(){
        return nameTypePairs;
    }

    public Type[] tupleTypes(){
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

    public int getStride(int index){
        if(index >= nameTypePairs.length) throw new Error();
        int stride = 0;
        for(int i = 0; i < index; i++){
            stride += nameTypePairs[i].type.getSize();
        }
        return stride;
    }

    @Override
    public byte[] serialize() {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        o.write(0x03);
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
        return Math.max(Arrays.stream(tupleTypes()).reduce(0, (i, type) -> i + type.getSize(), Integer::sum), 1);
    }

    @Override
    public String toString(){
        if(nameTypePairs == null || nameTypePairs.length == 0) return "( )";
        StringBuilder builder = new StringBuilder("( ");

        for (NameTypePair nameTypePair : nameTypePairs) {
            builder.append(nameTypePair.name == null? nameTypePair.type : nameTypePair);
            builder.append(", ");
        }

        builder.deleteCharAt(builder.length() - 2);
        builder.append(")");

        return builder.toString();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof TupleType t){
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
