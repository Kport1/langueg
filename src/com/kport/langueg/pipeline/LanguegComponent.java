package com.kport.langueg.pipeline;

public interface LanguegComponent {

    Object process(Object input, LanguegPipeline<?, ?> pipeline);

}
