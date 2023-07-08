package com.kport.langueg.pipeline;

import com.kport.langueg.error.DefaultErrorHandler;
import com.kport.langueg.error.ErrorHandler;
import com.sun.jdi.InvalidTypeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;

public class LanguegPipelineBuilder<I, O> {
    private final String source;

    private final ArrayList<LanguegComponent> components_ = new ArrayList<>();

    private final ArrayList<BiConsumer<Object, LanguegPipeline<I, O>>> beforeProcess_ = new ArrayList<>();
    private final ArrayList<BiConsumer<Object, LanguegPipeline<I, O>>> afterProcess_ = new ArrayList<>();

    public LanguegPipelineBuilder(String source_){
        source = source_;
    }

    public LanguegPipelineBuilder<I, O> addComponent(LanguegComponent component, BiConsumer<Object, LanguegPipeline<I, O>> before, BiConsumer<Object, LanguegPipeline<I, O>> after){
        components_.add(component);
        beforeProcess_.add(before);
        afterProcess_.add(after);

        return this;
    }

    public LanguegPipeline<I, O> get(){
        return new LanguegPipeline<I, O>() {
            private final ArrayList<LanguegComponent> components = components_;

            private final ArrayList<BiConsumer<Object, LanguegPipeline<I, O>>> beforeProcess = beforeProcess_;
            private final ArrayList<BiConsumer<Object, LanguegPipeline<I, O>>> afterProcess = afterProcess_;

            private final HashMap<String, Object> additionalData = new HashMap<>();

            private final ErrorHandler errorHandler = new DefaultErrorHandler(source);

            public void addStep(LanguegComponent component) {
                components.add(component);
            }

            @Override
            public O evaluate(I input) {

                Object previousRepresentation = input;
                for (int i = 0; i < components.size(); i++) {
                    beforeProcess.get(i).accept(previousRepresentation, this);

                    previousRepresentation = components.get(i).process(previousRepresentation, this);

                    afterProcess.get(i).accept(previousRepresentation, this);
                }

                return (O) previousRepresentation;
            }

            @Override
            public <T> T getAdditionalData(String key, Class<T> valType) throws InvalidTypeException {
                Object data = additionalData.get(key);
                if(!valType.isInstance(data)){
                    throw new InvalidTypeException("Data \"" + key + "\" does not have the type " + valType.getTypeName());
                }
                return valType.cast(data);
            }

            @Override
            public void putAdditionalData(String key, Object val){
                additionalData.put(key, val);
            }

            @Override
            public ErrorHandler getErrorHandler(){
                return errorHandler;
            }
        };
    }

}
