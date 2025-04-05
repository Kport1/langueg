package com.kport.langueg.error;

import com.kport.langueg.util.Span;

public abstract class LanguegException extends Exception {
    private final String error;
    private final LanguegException reason;

    public LanguegException(Errors error_, Span location_, CharSequence source, Object... args) {
        super();
        error = Errors.formatError(error_, source, location_, args);
        reason = null;
    }

    public LanguegException(Errors error_, LanguegException reason_, Span location_, CharSequence source, Object... args) {
        super();
        error = Errors.formatError(error_, source, location_, args);
        reason = reason_;
    }

    public String format() {
        StringBuilder str = new StringBuilder();
        String[] lines = error.split("\n");
        for (String l : lines) {
            str.append("| ").append(l).append("\n");
        }

        LanguegException reason = this.reason;
        int i = 2;
        while (reason != null) {
            str.append("| ".repeat(i)).append("Reason:\n");
            String[] reasonLines = reason.error.split("\n");
            for (String l : reasonLines) {
                str.append("| ".repeat(i)).append(l).append("\n");
            }
            reason = reason.reason;
            i++;
        }
        return str.toString();
    }
}
