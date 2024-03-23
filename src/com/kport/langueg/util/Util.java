package com.kport.langueg.util;

import java.util.Arrays;
import java.util.function.Function;

public abstract class Util {
    public static <T> T[] concatArrays(T[] a, T[] b, Class<T[]> clazz){
        T[] res = Arrays.copyOf(a, a.length + b.length, clazz);
        System.arraycopy(b, 0, res, a.length, b.length);
        return res;
    }

    @SuppressWarnings("unchecked")
    public static <T, U> U[] mapArray(T[] a, Function<T, U> fn){
        return (U[]) Arrays.stream(a).map(fn).toArray();
    }

    public static <T> T[] downcastArray(Object[] a, Class<T[]> clazz){
        return clazz.cast(a);
    }
}
