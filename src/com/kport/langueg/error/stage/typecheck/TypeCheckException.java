package com.kport.langueg.error.stage.typecheck;

import com.kport.langueg.error.Errors;

public class TypeCheckException extends Exception {
    private final String error;
    private final int position;
    private final TypeCheckException reason;

    public TypeCheckException(Errors error_, int offset_, CharSequence source, Object... args) {
        super();
        error = Errors.formatError(error_.format, source, offset_, args);
        position = offset_;
        reason = null;
    }

    public TypeCheckException(Errors error_, TypeCheckException reason_, int offset_, CharSequence source, Object... args) {
        super();
        error = Errors.formatError(error_.format, source, offset_, args);
        position = offset_;
        reason = reason_;
    }

    public String format() {
        StringBuilder str = new StringBuilder("Type checking Error:\n");
        str.append(error).append("\n");

        TypeCheckException reason = this.reason;
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
