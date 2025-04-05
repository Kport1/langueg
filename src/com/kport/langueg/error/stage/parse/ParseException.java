package com.kport.langueg.error.stage.parse;

import com.kport.langueg.error.Errors;
import com.kport.langueg.error.LanguegException;
import com.kport.langueg.util.Span;

public class ParseException extends LanguegException {
    public ParseException(Errors error_, Span location_, CharSequence source, Object... args) {
        super(error_, location_, source, args);
    }

    public ParseException(Errors error_, LanguegException reason_, Span location_, CharSequence source, Object... args) {
        super(error_, reason_, location_, source, args);
    }

    @Override
    public String format() {
        return "Parse error:\n" + super.format();
    }
}
