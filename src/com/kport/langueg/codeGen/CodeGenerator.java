package com.kport.langueg.codeGen;

import com.kport.langueg.pipeline.LanguegComponent;
import com.kport.langueg.pipeline.LanguegPipeline;

public interface CodeGenerator extends LanguegComponent {
    @Override
    Object process(Object input, LanguegPipeline<?, ?> pipeline);
}
