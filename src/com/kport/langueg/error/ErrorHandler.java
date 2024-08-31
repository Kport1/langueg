package com.kport.langueg.error;

public interface ErrorHandler {
    void error(Errors error, int offset, Object... additional);
    void warning(Warnings warning, int offset, Object... additional);
}
