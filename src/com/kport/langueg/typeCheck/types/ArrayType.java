package com.kport.langueg.typeCheck.types;

import com.kport.langueg.codeGen.languegVmCodeGen.LanguegVmValSize;

import java.io.ByteArrayOutputStream;

public class ArrayType implements Type{
    private final Type type;

    public ArrayType(Type type_){
        type = type_;
    }

    public Type arrayType(){
        return type;
    }

    @Override
    public byte[] serialize() {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        o.write(0x05);
        o.writeBytes(type.serialize());
        return o.toByteArray();
    }

    @Override
    public LanguegVmValSize getSize() {
        return LanguegVmValSize._64;
    }

    @Override
    public String toString(){
        return type.toString() + "[]";
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof ArrayType a)) return false;
        return type.equals(a.type);
    }

    @Override
    public int hashCode(){
        return type.hashCode() ^ 0x6f3a05d9;
    }
}
