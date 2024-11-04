package com.kport.langueg.error.stage.parse;

import com.kport.langueg.error.Errors;
import com.kport.langueg.error.LanguegException;

public class ParseException extends LanguegException {
    public ParseException(Errors error_, int offset_, CharSequence source, Object... args) {
        super(error_, offset_, source, args);
    }

    public ParseException(Errors error_, LanguegException reason_, int offset_, CharSequence source, Object... args) {
        super(error_, reason_, offset_, source, args);
    }

    @Override
    public String format() {
        return "Parse error:\n" + super.format();
    }
}
