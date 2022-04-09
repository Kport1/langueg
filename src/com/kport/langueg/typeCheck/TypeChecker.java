package com.kport.langueg.typeCheck;

import com.kport.langueg.parse.ast.AST;

public interface TypeChecker {
    void check(AST program);
}
