package com.kport.langueg.parse.typeCheck;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public record VarIdentifier(Map.Entry<Integer, Integer> depthCount, String name) {

    @Override
    public boolean equals(Object o){
        if(o instanceof VarIdentifier other){
            return other.depthCount.equals(depthCount) &&
                    other.name.equals(name);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return Objects.hash(depthCount.getKey(), depthCount.getValue(), name);
    }

    @Override
    public String toString(){
        return "varId(d = " + depthCount.getKey() + ", c = " + depthCount.getValue() + ", n = " + name + ")";
    }
}
