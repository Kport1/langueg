package com.kport.langueg.pipeline;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LanguegPipelineBuilder<I, O> {
    private final ArrayList<BiConsumer<Object, Object>> beforeProcess_ = new ArrayList<>();
    private final ArrayList<BiConsumer<Object, Object>> afterProcess_ = new ArrayList<>();

    private LanguegPipeline<I, O> pipeline = new LanguegPipeline<>() {
        private final ArrayList<LanguegComponent> components = new ArrayList<>();
        public final ArrayList<BiConsumer<Object, Object>> beforeProcess = beforeProcess_;
        private final ArrayList<BiConsumer<Object, Object>> afterProcess = afterProcess_;
        @Override
        public void addStep(LanguegComponent component) {
            components.add(component);
        }

        @Override
        public O evaluate(I input) {

            Object previousRepresentation = input;
            for (LanguegComponent component : components) {
                System.out.println(previousRepresentation);
                previousRepresentation = component.process(previousRepresentation);
            }

            return (O) previousRepresentation;
        }
    };

    public LanguegPipelineBuilder<I, O> addComponent(LanguegComponent component, BiConsumer<Object, Object> before, BiConsumer<Object, Object> after){
        pipeline.addStep(component);
        beforeProcess_.add(before);
        afterProcess_.add(after);

        return this;
    }

    public LanguegPipeline<I, O> get(){
        return pipeline;
    }

}
