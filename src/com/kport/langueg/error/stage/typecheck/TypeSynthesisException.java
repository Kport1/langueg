package com.kport.langueg.error.stage.typecheck;

import com.kport.langueg.error.Errors;
import com.kport.langueg.error.LanguegException;
import com.kport.langueg.util.Span;

public class TypeSynthesisException extends LanguegException {
    public TypeSynthesisException(Errors error_, Span location_, CharSequence source, Object... args) {
        super(error_, location_, source, args);
    }

    public TypeSynthesisException(Errors error_, LanguegException reason_, Span location_, CharSequence source, Object... args) {
        super(error_, reason_, location_, source, args);
    }

    @Override
    public String format() {
        return "Type synthesis error:\n" + super.format();
    }
}
