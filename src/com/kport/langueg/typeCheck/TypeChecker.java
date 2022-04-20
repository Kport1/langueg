package com.kport.langueg.typeCheck;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.pipeline.LanguegComponent;
import com.kport.langueg.pipeline.LanguegPipeline;

public interface TypeChecker extends LanguegComponent {
    @Override
    AST process(Object program, LanguegPipeline<?, ?> pipeline);
}
