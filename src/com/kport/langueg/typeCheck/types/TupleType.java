package com.kport.langueg.typeCheck.types;

import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NameTypePair;
import com.kport.langueg.util.Either;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public final class TupleType implements Type{
    public final NameTypePair[] nameTypePairs;

    public TupleType(NameTypePair... types){
        nameTypePairs = types;
    }

    public NameTypePair[] nameTypePairs(){
        return nameTypePairs;
    }

    public Type[] tupleTypes(){
        return Arrays.stream(nameTypePairs).map(p -> p.type).toArray(Type[]::new);
    }

    public int indexByName(String name){
        for (int i = 0; i < nameTypePairs.length; i++) {
            if (Objects.equals(nameTypePairs[i].name, name)) return i;
        }
        return -1;
    }

    public Type typeByName(String name){
        int index = indexByName(name);
        if(index == -1) return null;
        return nameTypePairs[index].type;
    }

    public boolean hasElement(Either<Integer, String> position){
        return position.match(i -> i < nameTypePairs.length, str -> indexByName(str) != -1);
    }

    public int resolveElementIndex(Either<Integer, String> position){
        return position.match(i -> i, this::indexByName);
    }

    public Type resolveElementType(Either<Integer, String> position){
        return position.match(i -> nameTypePairs[i].type, this::typeByName);
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
        return Arrays.stream(tupleTypes()).reduce(0, (i, type) -> i + type.getSize(), Integer::sum);
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
    }
}
