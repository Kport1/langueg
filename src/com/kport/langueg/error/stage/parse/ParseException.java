package com.kport.langueg.error.stage.parse;

import com.kport.langueg.error.Errors;

public class ParseException extends Exception {
    private final String error;
    private final ParseException reason;

    public ParseException(Errors error_, int offset_, CharSequence source, Object... args) {
        super();
        error = Errors.formatError(error_.format, source, offset_, args) +
                (error_.suggestion.isEmpty()? "" : "\nSuggestion: " + error_.suggestion);
        reason = null;
    }

    public ParseException(Errors error_, ParseException reason_, int offset_, CharSequence source, Object... args) {
        super();
        error = Errors.formatError(error_.format, source, offset_, args) +
                (error_.suggestion.isEmpty()? "" : "\nSuggestion: " + error_.suggestion);
        reason = reason_;
    }

    public String format() {
        StringBuilder str = new StringBuilder("Parse Error:\n");
        str.append(error).append("\n");

        ParseException reason = this.reason;
        int i = 1;
        while (reason != null) {
            str.append("| ".repeat(i)).append("Reason:\n");
            String[] lines = reason.error.split("\n");
            for(String l : lines){
                str.append("| ".repeat(i)).append(l).append("\n");
            }
            reason = reason.reason;
            i++;
        }
        return str.toString();
    }
}
