package com.kport.langueg.util;

import com.kport.langueg.typeCheck.types.Type;

import java.util.Arrays;
import java.util.Objects;

public record FnIdentifier(Scope scope, String name, Type[] params) {

    @Override
    public boolean equals(Object o){
        if(o instanceof FnIdentifier i){
            return  i.scope == scope &&
                    i.name.equals(name) &&
                    Arrays.equals(i.params, params);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return Objects.hash(scope, name, Arrays.hashCode(params));
    }

    public boolean equalsIgnoreArgs(Object o){
        if(o instanceof FnIdentifier i){
            return  i.scope == scope &&
                    i.name.equals(name);
        }
        return false;
    }

    @Override
    public String toString(){
        return "fnId(s = " + scope + ", n = " + name + ", a = " + Arrays.toString(params) + ")";
    }
}
