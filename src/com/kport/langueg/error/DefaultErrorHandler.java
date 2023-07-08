package com.kport.langueg.error;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;

public class DefaultErrorHandler implements ErrorHandler{
    private final String source;
    private final ArrayList<ErrorIntercept> errorIntercepts = new ArrayList<>();


    public DefaultErrorHandler(String source_){
        source = source_;
    }


    @Override
    public void error(Errors error, Object... additional) {
        if(!errorIntercepts.stream().noneMatch(i -> i.intercept(error, additional)))
            return;

        String errorString = String.format(error.format, additional);

        //%%Cl.c.
        int lastCodeInsertIndex = -1;
        while((lastCodeInsertIndex = errorString.indexOf("%C", lastCodeInsertIndex)) != -1){

            int dot1Index = errorString.indexOf('.', lastCodeInsertIndex);
            String lineStr = errorString.substring(lastCodeInsertIndex + 2, dot1Index);
            int line = Integer.parseInt(lineStr);

            int dot2Index = errorString.indexOf('.', dot1Index + 1);
            String columnStr = errorString.substring(dot1Index + 1, dot2Index);
            int column = Integer.parseInt(columnStr);

            String[] sourceLines = source.split("\r?\n", line + 1);
            if(line > sourceLines.length || line == 0) throw new Error("Line out of bounds");
            String sourceLine = sourceLines[sourceLines.length - (sourceLines.length == line? 1 : 2)];
            String columnPointer = " ".repeat(column) + "^";

            errorString = errorString.replaceFirst("%C[0-9]+.[0-9]+.", sourceLine + "\n" + columnPointer);
        }

        System.err.println("Error:");
        System.err.println(errorString);
        if(!error.suggestion.isEmpty())
            System.err.println(error.suggestion);

        //Thread.dumpStack();

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
