package com.kport.langueg.parse;

import com.kport.langueg.error.stage.parse.ParseException;

import java.util.Objects;

@FunctionalInterface
public interface FunctionThrowsParseException<T, R> {
    R apply(T var1) throws ParseException;

    default <V> FunctionThrowsParseException<V, R> compose(FunctionThrowsParseException<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (v) -> this.apply(before.apply(v));
    }

    default <V> FunctionThrowsParseException<T, V> andThen(FunctionThrowsParseException<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (t) -> after.apply(this.apply(t));
    }

    static <T> FunctionThrowsParseException<T, T> identity() {
        return (t) -> t;
    }
}
