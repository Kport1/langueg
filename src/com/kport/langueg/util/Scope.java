package com.kport.langueg.util;

import com.kport.langueg.parse.ast.AST;

public class Scope {
    public final Scope parent;
    public final AST scopeOpeningNode;
    public final boolean fnScope;

    public Scope(Scope parent_, AST scopeOpeningNode_, boolean fnScope_){
        parent = parent_;
        scopeOpeningNode = scopeOpeningNode_;
        fnScope = fnScope_;
    }

    public Scope enclosingFnScope(){
        Scope scope = this;
        while (!scope.fnScope){
            scope = scope.parent;
            if(scope == null) return null;
        }
        return scope;
    }

    @Override
    public String toString(){
        return Integer.toHexString(hashCode());
    }

    @Override
    public int hashCode(){
        return super.hashCode();
    }

    @Override
    public boolean equals(Object other){
        return this == other;
    }

}
