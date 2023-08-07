package com.kport.langueg.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public record VarIdentifier(Scope scope, String name) {

    @Override
    public boolean equals(Object o){
        if(o instanceof VarIdentifier i){
            return  i.scope == scope &&
                    i.name.equals(name);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return 31 * scope.hashCode() + name.hashCode();
    }

    @Override
    public String toString(){
        return "varId(s = " + scope + ", n = " + name + ")";
    }
}
