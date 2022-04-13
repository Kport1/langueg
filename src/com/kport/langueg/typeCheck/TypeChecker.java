package com.kport.langueg.typeCheck;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.pipeline.LanguegComponent;

public interface TypeChecker extends LanguegComponent {
    @Override
    AST process(Object program);
}
