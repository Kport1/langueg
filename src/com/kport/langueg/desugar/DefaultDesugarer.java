package com.kport.langueg.desugar;

import com.kport.langueg.error.ErrorHandler;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.pipeline.LanguegPipeline;

public class DefaultDesugarer implements Desugarer {

    private ErrorHandler errorHandler;
    @Override
    public AST process(Object ast_, LanguegPipeline<?, ?> pipeline) {
        AST ast = (AST) ast_;
        errorHandler = pipeline.getErrorHandler();

        return ast;
    }
}
