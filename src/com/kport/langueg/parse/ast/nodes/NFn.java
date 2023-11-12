package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.Scope;

public interface NFn {
    Type[] getParamTypes();
    Type getReturnType();
    FnParamDef[] getParams();
    Scope getBlockScope();
    void setBlockScope(Scope scope);
}
