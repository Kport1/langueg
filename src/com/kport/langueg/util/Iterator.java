package com.kport.langueg.util;

import java.util.List;

public class Iterator<T> {
    private final List<T> list;
    private int index = 0;

    public Iterator(List<T> list_){
        list = list_;
    }

    public T next(){
        inc();
        return get(index);
    }

    public T previous(){
        dec();
        return get(index);
    }

    public void inc(){
        index++;
    }

    public void dec(){
        index = Math.max(index - 1, 0);
    }

    public T current(){
        return get(index);
    }

    public T peek(){
        return get(index + 1);
    }

    public T get(int i){
        return list.get(Math.max(Math.min(i, list.size() - 1), 0));
    }

    public void setIndex(int i){
        index = i;
    }

    public int getIndex(){
        return index;
    }

    public boolean isEOF(){
        return index >= list.size();
    }

}
