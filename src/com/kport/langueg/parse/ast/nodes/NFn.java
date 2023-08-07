package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.Scope;

public interface NFn {
    Type[] getParamTypes();
    Type getReturnType();
    FnParamDef[] getParams();
    AST getBlock();
    Scope getBlockScope();
    void setBlockScope(Scope scope);
}
