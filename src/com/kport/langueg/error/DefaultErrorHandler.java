package com.kport.langueg.error;

import java.util.ArrayList;
import java.util.function.Predicate;

public class DefaultErrorHandler implements ErrorHandler{
    private final ArrayList<ErrorIntercept> errorIntercepts = new ArrayList<>();


    @Override
    public void error(Errors error, Object... additional) {
        if(!errorIntercepts.stream().noneMatch(i -> i.intercept(error, additional)))
            return;

        System.err.println("Error:");
        System.err.printf(error.format + "%n", additional);
        if(!error.suggestion.isEmpty())
            System.err.println(error.suggestion);

        Thread.dumpStack();

        System.exit(1);
    }

    @Override
    public void warning(Warnings warning, Object... additional) {
        System.out.println("Warning:");
        System.out.printf(warning.format + "%n", additional);
    }

    @Override
    public void addErrorIntercept(ErrorIntercept intercept) {
        errorIntercepts.add(intercept);
    }

    @Override
    public void removeErrorIntercept(ErrorIntercept intercept) {
        errorIntercepts.remove(intercept);
    }
}
