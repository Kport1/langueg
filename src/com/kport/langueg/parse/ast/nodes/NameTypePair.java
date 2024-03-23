package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.typeCheck.types.Type;

public final class NameTypePair {
    public Type type;
    public String name;

    public NameTypePair(Type type_, String name_){
        type = type_;
        name = name_;
    }

    @Override
    public String toString(){
        return name + " : " + type;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NameTypePair a)) return false;
        return type.equals(a.type) && name.equals(a.name);
    }
}
