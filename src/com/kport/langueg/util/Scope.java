package com.kport.langueg.util;

import java.util.ArrayList;

public class Scope {
    public final Scope parent;
    public final ArrayList<Scope> children = new ArrayList<>();
    public final boolean fnScope;

    public Scope(Scope parent_, boolean fnScope_){
        parent = parent_;
        fnScope = fnScope_;
    }

    @Override
    public String toString(){
        return Integer.toHexString(hashCode());
    }

    @Override
    public int hashCode(){
        return super.hashCode();
    }

}
