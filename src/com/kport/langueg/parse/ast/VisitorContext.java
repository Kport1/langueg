package com.kport.langueg.parse.ast;

import java.util.HashMap;
import java.util.Map;

public class VisitorContext implements Cloneable{
    private HashMap<String, Object> values = new HashMap<>();

    public VisitorContext(Map<String, Object> initValues){
        values.putAll(initValues);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object clone() throws CloneNotSupportedException {
        VisitorContext c = (VisitorContext) super.clone();
        c.values = (HashMap<String, Object>) c.values.clone();
        return c;
    }

    public Object get(String s){
        return values.get(s);
    }

    public void put(String s, Object val){
        values.put(s, val);
    }

    public static VisitorContext clone(VisitorContext context){
        try {
            return context == null? null : (VisitorContext) context.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
