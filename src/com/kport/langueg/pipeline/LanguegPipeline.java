package com.kport.langueg.pipeline;

import com.sun.jdi.InvalidTypeException;

public interface LanguegPipeline<I, O> {

    O evaluate(I input);
    <T> T getAdditionalData(String key, Class<T> valType) throws InvalidTypeException;
    void putAdditionalData(String key, Object val);
    CharSequence getSource();
}
