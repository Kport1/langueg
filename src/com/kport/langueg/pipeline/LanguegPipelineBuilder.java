package com.kport.langueg.pipeline;

import com.sun.jdi.InvalidTypeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;

public class LanguegPipelineBuilder<O> {

    private final ArrayList<LanguegComponent> components_ = new ArrayList<>();

    private final ArrayList<BiConsumer<Object, LanguegPipeline<String, O>>> beforeProcess_ = new ArrayList<>();
    private final ArrayList<BiConsumer<Object, LanguegPipeline<String, O>>> afterProcess_ = new ArrayList<>();

    public LanguegPipelineBuilder() {

    }

    public LanguegPipelineBuilder<O> addComponent(LanguegComponent component, BiConsumer<Object, LanguegPipeline<String, O>> before, BiConsumer<Object, LanguegPipeline<String, O>> after) {
        components_.add(component);
        beforeProcess_.add(before);
        afterProcess_.add(after);

        return this;
    }

    public LanguegPipeline<String, O> get() {
        return new LanguegPipeline<>() {
            private final ArrayList<LanguegComponent> components = components_;

            private final ArrayList<BiConsumer<Object, LanguegPipeline<String, O>>> beforeProcess = beforeProcess_;
            private final ArrayList<BiConsumer<Object, LanguegPipeline<String, O>>> afterProcess = afterProcess_;

            private final HashMap<String, Object> additionalData = new HashMap<>();

            private CharSequence source;

            @Override
            @SuppressWarnings("unchecked")
            public O evaluate(String input) {
                source = input;
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
                if (!valType.isInstance(data)) {
                    throw new InvalidTypeException("Data \"" + key + "\" does not have the type " + valType.getTypeName());
                }
                return valType.cast(data);
            }

            @Override
            public void putAdditionalData(String key, Object val) {
                additionalData.put(key, val);
            }

            @Override
            public CharSequence getSource() {
                return source;
            }
        };
    }

}
