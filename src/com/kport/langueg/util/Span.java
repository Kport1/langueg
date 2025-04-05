package com.kport.langueg.util;

public record Span(int begin, int end) {
    public static Span union(Span a, Span b){
        return new Span(Math.min(a.begin, b.begin), Math.max(a.end, b.end));
    }
}
