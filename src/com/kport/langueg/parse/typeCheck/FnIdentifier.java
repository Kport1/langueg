package com.kport.langueg.parse.typeCheck;

import java.util.Arrays;
import java.util.Map;

public record FnIdentifier(Map.Entry<Integer, Integer> depthCount, String name, Type[] args) {

    @Override
    public boolean equals(Object o){
        if(o instanceof FnIdentifier other){
            return other.depthCount.equals(depthCount) &&
                    other.name.equals(name) &&
                    Arrays.equals(other.args, args);
        }
        return false;
    }

    @Override
    public String toString(){
        return "fnId(d = " + depthCount.getKey() + ", c = " + depthCount.getValue() + ", n = " + name + ", a = " + Arrays.toString(args) + ")";
    }
}
