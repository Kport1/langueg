package com.kport.langueg.codeGen;

import com.kport.langueg.pipeline.LanguegComponent;
import com.kport.langueg.pipeline.LanguegPipeline;

public interface CodeGenerator extends LanguegComponent {
    @Override
    byte[] process(Object input, LanguegPipeline<?, ?> pipeline);
}
