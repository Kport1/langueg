package com.kport.langueg.parse;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.pipeline.LanguegComponent;
import com.kport.langueg.pipeline.LanguegPipeline;

public interface Parser extends LanguegComponent {

    @Override
    AST process(Object tokens, LanguegPipeline<?, ?> pipeline);

}
