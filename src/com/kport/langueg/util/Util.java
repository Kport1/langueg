package com.kport.langueg.util;

import java.util.Arrays;

public abstract class Util {
    public static <T> T[] concatArrays(T[] a, T[] b, Class<T[]> clazz){
        T[] res = Arrays.copyOf(a, a.length + b.length, clazz);
        System.arraycopy(b, 0, res, a.length, b.length);
        return res;
    }

    public static <T> T[] downcastArray(Object[] a, Class<T[]> clazz){
        return clazz.cast(a);
    }
}
