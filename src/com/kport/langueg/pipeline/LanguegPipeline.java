package com.kport.langueg.pipeline;

import com.kport.langueg.error.ErrorHandler;

public interface LanguegPipeline<I, O> {

    void addStep(LanguegComponent component);
    O evaluate(I input);
    Object getAdditionalData(String key);
    void putAdditionalData(String key, Object val);
    ErrorHandler getErrorHandler();
}
