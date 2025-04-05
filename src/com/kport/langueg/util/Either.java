package com.kport.langueg.util;

import java.util.Objects;
import java.util.function.Function;

public sealed interface Either<A, B> permits Either.Left, Either.Right {
    <R> R match(Function<A, R> left, Function<B, R> right);

    static <A, B> Left<A, B> left(A value) {
        return new Left<>(value);
    }

    static <A, B> Right<A, B> right(B value) {
        return new Right<>(value);
    }

    record Left<A, B>(A value) implements Either<A, B> {

        @Override
        public <R> R match(Function<A, R> left, Function<B, R> right) {
            return left.apply(value());
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Left<?, ?>(Object v))) return false;
            return Objects.equals(value, v);
        }
    }

    record Right<A, B>(B value) implements Either<A, B> {

        @Override
        public <R> R match(Function<A, R> left, Function<B, R> right) {
            return right.apply(value());
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Right<?, ?>(Object v))) return false;
            return Objects.equals(value, v);
        }
    }
}



