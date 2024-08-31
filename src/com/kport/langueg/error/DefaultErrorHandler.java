package com.kport.langueg.error;

public class DefaultErrorHandler implements ErrorHandler{
    private final CharSequence source;

    public DefaultErrorHandler(CharSequence source_){
        source = source_;
    }

    @Override
    public void error(Errors error, int offset, Object... additional) {
        String errorString = String.format(error.format, additional);

        //%%CL Code line
        int indexOfPrevNewline = offset;
        while (source.charAt(indexOfPrevNewline) != '\n' && indexOfPrevNewline > 0) indexOfPrevNewline--;
        int indexOfNextNewline = offset;
        while (source.charAt(indexOfNextNewline) != '\n' && indexOfNextNewline < source.length()) indexOfNextNewline++;
        String line = source.subSequence(indexOfPrevNewline + 1, indexOfNextNewline).toString();

        errorString = errorString.replaceAll("%CL", line);

        System.err.println("Error:");
        System.err.println(errorString);
        if(!error.suggestion.isEmpty())
            System.err.println(error.suggestion);

        Thread.dumpStack();
        System.exit(1);
    }

    @Override
    public void warning(Warnings warning, int offset, Object... additional) {
        System.out.println("Warning:");
        System.out.printf(warning.format + "%n", additional);
    }
}
