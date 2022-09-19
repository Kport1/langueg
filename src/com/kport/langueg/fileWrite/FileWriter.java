package com.kport.langueg.fileWrite;

import com.kport.langueg.pipeline.LanguegComponent;
import com.kport.langueg.pipeline.LanguegPipeline;

public interface FileWriter extends LanguegComponent {
    @Override
    Void process(Object input, LanguegPipeline<?, ?> pipeline);

    String getFilePath();
}
