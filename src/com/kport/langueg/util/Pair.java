package com.kport.langueg.util;

import java.util.Objects;

public class Pair<L, R> {
    public final L left;
    public final R right;

    public Pair(L left_, R right_){
        left = left_;
        right = right_;
    }

    @Override
    public int hashCode(){
        return Objects.hash(left, right);
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof Pair<?,?> p)) return false;
        return p.left.equals(left) && p.right.equals(right);
    }
}
