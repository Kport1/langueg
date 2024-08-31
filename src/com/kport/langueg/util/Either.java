package com.kport.langueg.util;

import java.util.function.Consumer;
import java.util.function.Function;

public sealed interface Either<A, B> permits Either.Left, Either.Right {
    <R> R match(Function<A, R> left, Function<B, R> right);

    default void consume(Consumer<A> left, Consumer<B> right){
        match(
                (a) -> {
                    left.accept(a);
                    return null;
                },
                (b) -> {
                    right.accept(b);
                    return null;
                }
        );
    }

    static <A, B> Left<A, B> left(A value){
        return new Left<>(value);
    }
    static <A, B> Right<A, B> right(B value){
        return new Right<>(value);
    }

    record Left<A, B>(A value) implements Either<A, B> {

        @Override
        public <R> R match(Function<A, R> left, Function<B, R> right) {
            return left.apply(value());
        }

        @Override
        public boolean equals(Object o){
            if(!(o instanceof Either.Left<?,?> l)) return false;
            return value.equals(l.value);
        }
    }

    record Right<A, B>(B value) implements Either<A, B> {

        @Override
        public <R> R match(Function<A, R> left, Function<B, R> right) {
            return right.apply(value());
        }

        @Override
        public boolean equals(Object o){
            if(!(o instanceof Either.Right<?,?> r)) return false;
            return value.equals(r.value);
        }
    }
}



