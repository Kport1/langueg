package com.kport.langueg.error.stage.typecheck;

import com.kport.langueg.error.Errors;
import com.kport.langueg.error.LanguegException;
import com.kport.langueg.util.Span;

public class TypeCheckException extends LanguegException {
    public TypeCheckException(Errors error_, Span location_, CharSequence source, Object... args) {
        super(error_, location_, source, args);
    }

    public TypeCheckException(Errors error_, LanguegException reason_, Span location_, CharSequence source, Object... args) {
        super(error_, reason_, location_, source, args);
    }

    @Override
    public String format() {
        return "Type check error:\n" + super.format();
    }
}
