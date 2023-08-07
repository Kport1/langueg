package com.kport.langueg.desugar;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.pipeline.LanguegComponent;
import com.kport.langueg.pipeline.LanguegPipeline;

public interface Desugarer extends LanguegComponent {
    @Override
    AST process(Object ast, LanguegPipeline<?, ?> pipeline);
}
