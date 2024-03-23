package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.util.Scope;

public sealed interface NFn permits NNamedFn, NAnonFn
{
    FnHeader getFnHeader();
    AST getBody();
    Scope getBodyScope();
    void setBodyScope(Scope scope);
}
