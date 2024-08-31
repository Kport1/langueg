package com.kport.langueg.fileWrite;

import com.kport.langueg.pipeline.LanguegComponent;
import com.kport.langueg.pipeline.LanguegPipeline;

import java.nio.file.Path;

public interface FileWriter extends LanguegComponent {
    @Override
    Void process(Object input, LanguegPipeline<?, ?> pipeline);

    Path getFilePath();
}
