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

    public static byte[] trimByteArr(byte[] arr){
        int trimTo = 1;
        for (int i = arr.length - 1; i >= 0; i--) {
            if(arr[i] != 0){
                trimTo = i + 1;
                break;
            }
        }
        byte[] res = new byte[trimTo];
        System.arraycopy(arr, 0, res, 0, trimTo);
        return res;
    }
}
