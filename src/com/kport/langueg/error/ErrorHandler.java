package com.kport.langueg.error;

import com.kport.langueg.parse.ast.AST;

public interface ErrorHandler {

    void error(ErrorType type, int line, Object... additional);
}
