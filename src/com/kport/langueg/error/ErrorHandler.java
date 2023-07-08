package com.kport.langueg.error;

public interface ErrorHandler {

    void error(Errors error, Object... additional);
    void warning(Warnings warning, Object... additional);

    void addErrorIntercept(ErrorIntercept intercept);
    void removeErrorIntercept(ErrorIntercept intercept);
}
