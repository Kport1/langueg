package com.kport.langueg.error.stage.parse;

import com.kport.langueg.error.Errors;

public class ParseException extends Exception {
    private final String error;
    private final int position;
    private final ParseException reason;

    public ParseException(Errors error_, int offset_, CharSequence source, Object... args) {
        super();
        error = Errors.formatError(error_.format, source, offset_, args);
        position = offset_;
        reason = null;
    }

    public ParseException(Errors error_, ParseException reason_, int offset_, CharSequence source, Object... args) {
        super();
        error = Errors.formatError(error_.format, source, offset_, args);
        position = offset_;
        reason = reason_;
    }

    public String format() {
        StringBuilder str = new StringBuilder("Parse Error:\n");
        str.append(error).append("\n");

        ParseException reason = this.reason;
        int i = 0;
        while (reason != null) {
            str.append(" ".repeat(i * 2)).append("Reason:\n");
            str.append(" ".repeat(i * 2)).append(reason.error).append("\n");
            reason = reason.reason;
            i++;
        }
        return str.toString();
    }
}
