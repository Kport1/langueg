package com.kport.langueg.util;

import com.kport.langueg.typeCheck.types.Type;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public record FnIdentifier(int depth, int count, String name, Type[] args) {

    @Override
    public boolean equals(Object o){
        if(o instanceof FnIdentifier i){
            return  i.depth == depth &&
                    i.count == count &&
                    i.name.equals(name) &&
                    Arrays.equals(i.args, args);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return 31 * (31 * (31 * (31 + depth) + count) + name.hashCode()) + Arrays.hashCode(args);
    }

    public boolean equalsIgnoreArgs(Object o){
        if(o instanceof FnIdentifier i){
            return  i.depth == depth &&
                    i.count == count &&
                    i.name.equals(name);
        }
        return false;
    }

    public boolean isAnon(){
        return name == null;
    }

    @Override
    public String toString(){
        return "fnId(d = " + depth + ", c = " + count + ", n = " + name + ", a = " + Arrays.toString(args) + ")";
    }
}
