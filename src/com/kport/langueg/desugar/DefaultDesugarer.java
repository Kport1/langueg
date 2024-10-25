package com.kport.langueg.desugar;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.pipeline.LanguegPipeline;

public class DefaultDesugarer implements Desugarer {

    @Override
    public AST process(Object ast_, LanguegPipeline<?, ?> pipeline) {
        return (AST) ast_;
    }
}
