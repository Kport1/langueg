package com.kport.langueg.typeCheck.types;

import com.kport.langueg.codeGen.languegVmCodeGen.LanguegVmValSize;
import com.kport.langueg.parse.ast.nodes.NameTypePair;
import com.kport.langueg.util.Util;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TupleType implements Type{
    private final NameTypePair[] nameTypePairs;

    public TupleType(NameTypePair... types){
        nameTypePairs = types;
    }

    public Type[] tupleTypes(){
        return Arrays.stream(nameTypePairs).map(p -> p.type).toArray(Type[]::new);
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
    public LanguegVmValSize getSize() {
        return LanguegVmValSize._64;
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
            return Arrays.equals(Util.mapArray(nameTypePairs, (p) -> p.type), Util.mapArray(t.nameTypePairs, (p) -> p.type));
        }
        return false;
    }

    @Override
    public int hashCode(){
        return Arrays.hashCode(nameTypePairs);
    }
}
