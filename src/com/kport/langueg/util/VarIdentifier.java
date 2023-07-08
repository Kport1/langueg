package com.kport.langueg.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public record VarIdentifier(int depth, int count, String name) {

    @Override
    public boolean equals(Object o){
        if(o instanceof VarIdentifier i){
            return  i.depth == depth &&
                    i.count == count &&
                    i.name.equals(name);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return 31 * (31 * depth + count) + name.hashCode();
    }

    @Override
    public String toString(){
        return "varId(d = " + depth + ", c = " + count + ", n = " + name + ")";
    }
}
