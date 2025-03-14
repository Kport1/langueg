package com.kport.langueg.error.stage.typecheck;

import com.kport.langueg.error.Errors;
import com.kport.langueg.error.LanguegException;

public class TypeCheckException extends LanguegException {
    public TypeCheckException(Errors error_, int offset_, CharSequence source, Object... args) {
        super(error_, offset_, source, args);
    }

    public TypeCheckException(Errors error_, LanguegException reason_, int offset_, CharSequence source, Object... args) {
        super(error_, reason_, offset_, source, args);
    }

    @Override
    public String format() {
        return "Type check error:\n" + super.format();
    }
}
