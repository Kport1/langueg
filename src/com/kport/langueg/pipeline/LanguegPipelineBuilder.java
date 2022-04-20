package com.kport.langueg.pipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LanguegPipelineBuilder<I, O> {
    private final ArrayList<Consumer<Object>> beforeProcess_ = new ArrayList<>();
    private final ArrayList<Consumer<Object>> afterProcess_ = new ArrayList<>();

    private LanguegPipeline<I, O> pipeline = new LanguegPipeline<>() {
        private final ArrayList<LanguegComponent> components = new ArrayList<>();

        private final ArrayList<Consumer<Object>> beforeProcess = beforeProcess_;
        private final ArrayList<Consumer<Object>> afterProcess = afterProcess_;

        private final HashMap<String, Object> additionalData = new HashMap<>();

        @Override
        public void addStep(LanguegComponent component) {
            components.add(component);
        }

        @Override
        public O evaluate(I input) {

            Object previousRepresentation = input;
            for (int i = 0; i < components.size(); i++) {
                beforeProcess.get(i).accept(previousRepresentation);
                previousRepresentation = components.get(i).process(previousRepresentation, this);
                additionalData.clear();
                afterProcess.get(i).accept(previousRepresentation);
            }

            return (O) previousRepresentation;
        }

        @Override
        public Object getAdditionalData(String key){
            return additionalData.get(key);
        }

        @Override
        public void putAdditionalData(String key, Object val){
            additionalData.put(key, val);
        }
    };

    public LanguegPipelineBuilder<I, O> addComponent(LanguegComponent component, Consumer<Object> before, Consumer<Object> after){
        pipeline.addStep(component);
        beforeProcess_.add(before);
        afterProcess_.add(after);

        return this;
    }

    public LanguegPipeline<I, O> get(){
        return pipeline;
    }

}
