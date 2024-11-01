package com.kport.langueg.error.stage.typecheck;

import com.kport.langueg.error.Errors;

public class TypeSynthesisException extends Exception {
    private final String error;
    private final TypeSynthesisException reason;

    public TypeSynthesisException(Errors error_, int offset_, CharSequence source, Object... args) {
        super();
        error = Errors.formatError(error_.format, source, offset_, args);
        reason = null;
    }

    public TypeSynthesisException(Errors error_, TypeSynthesisException reason_, int offset_, CharSequence source, Object... args) {
        super();
        error = Errors.formatError(error_.format, source, offset_, args);
        reason = reason_;
    }

    public String format() {
        StringBuilder str = new StringBuilder("Type synthesis Error:\n");
        str.append(error).append("\n");

        TypeSynthesisException reason = this.reason;
        int i = 1;
        while (reason != null) {
            str.append("| ".repeat(i)).append("Reason:\n");
            str.append("| ".repeat(i)).append(reason.error).append("\n");
            reason = reason.reason;
            i++;
        }
        return str.toString();
    }
}
