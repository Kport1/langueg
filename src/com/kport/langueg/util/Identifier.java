package com.kport.langueg.util;

public record Identifier(Scope scope, String name) {

    @Override
    public boolean equals(Object o){
        if(o instanceof Identifier i){
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
        return "Id(s = " + scope + ", n = " + name + ")";
    }
}
