package com.kport.langueg.parse.ast;

import com.kport.langueg.util.Span;

public interface CodeLocatable {
    Span location();
}
