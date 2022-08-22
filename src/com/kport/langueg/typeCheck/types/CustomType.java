package com.kport.langueg.typeCheck.types;

public class CustomType extends Type{
    private final String typeName;

    public CustomType(String typeName_){
        typeName = typeName_;
    }

    @Override
    public boolean isCustom(){
        return true;
    }

    @Override
    public String name(){
        return typeName;
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
