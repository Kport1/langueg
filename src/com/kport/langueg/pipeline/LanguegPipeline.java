package com.kport.langueg.pipeline;

import com.kport.langueg.error.ErrorHandler;
import com.sun.jdi.InvalidTypeException;

public interface LanguegPipeline<I, O> {

    void addStep(LanguegComponent component);
    O evaluate(I input);
    <T> T getAdditionalData(String key, Class<T> valType) throws InvalidTypeException;
    void putAdditionalData(String key, Object val);
    ErrorHandler getErrorHandler();
}
