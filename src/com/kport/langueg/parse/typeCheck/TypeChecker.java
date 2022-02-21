package com.kport.langueg.parse.typeCheck;

import com.kport.langueg.parse.ast.AST;

public interface TypeChecker {
    void check(AST program);
}
