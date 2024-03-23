package com.kport.langueg.typeCheck.types;

import com.kport.langueg.codeGen.languegVmCodeGen.LanguegVmValSize;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class CustomType implements Type{
    private final String typeName;

    public CustomType(String typeName_){
        typeName = typeName_;
    }

    public String customTypeName(){
        return typeName;
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
    public LanguegVmValSize getSize() {
        throw new Error("Custom type size is unknown");
    }

    @Override
    public String toString(){
        return "\"" + typeName + "\"";
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof CustomType t){
            return t.typeName.equals(typeName);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return typeName.hashCode();
    }
}
