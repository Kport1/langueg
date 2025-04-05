package com.kport.langueg.util;

import java.util.Arrays;
import java.util.function.Function;

public abstract class Util {
    public static <T> T[] concatArrays(T[] a, T[] b) {
        T[] res = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, res, a.length, b.length);
        return res;
    }

    @SuppressWarnings("unchecked")
    public static <T, U> U[] mapArray(T[] a, Function<T, U> fn) {
        return (U[]) Arrays.stream(a).map(fn).toArray();
    }

    public static <T> T[] downcastArray(Object[] a, Class<T[]> clazz) {
        return clazz.cast(a);
    }

    public static byte[] trimByteArr(byte[] arr) {
        int trimTo = 1;
        for (int i = arr.length - 1; i >= 0; i--) {
            if (arr[i] != 0) {
                trimTo = i + 1;
                break;
            }
        }
        byte[] res = new byte[trimTo];
        System.arraycopy(arr, 0, res, 0, trimTo);
        return res;
    }

    public static short zeroExtendS(byte b) {
        return (short) ((short) b & ((short) 0xFF));
    }

    public static int zeroExtendI(byte b) {
        return b & 0xFF;
    }

    public static long zeroExtendL(byte b) {
        return b & 0xFFL;
    }

    public static short fromBytesS(byte[] bytes) {
        return (short) (zeroExtendS(bytes[0]) | zeroExtendS(bytes[1]) << ((short) 8));
    }

    public static int fromBytesI(byte[] bytes) {
        return zeroExtendI(bytes[0]) | zeroExtendI(bytes[1]) << 8 |
                zeroExtendI(bytes[2]) << 16 | zeroExtendI(bytes[3]) << 24;
    }

    public static long fromBytesL(byte[] bytes) {
        return zeroExtendL(bytes[0]) | zeroExtendL(bytes[1]) << 8L |
                zeroExtendL(bytes[2]) << 16L | zeroExtendL(bytes[3]) << 24L |
                zeroExtendL(bytes[4]) << 32L | zeroExtendL(bytes[5]) << 40L |
                zeroExtendL(bytes[6]) << 48L | zeroExtendL(bytes[7]) << 56L;
    }
}
