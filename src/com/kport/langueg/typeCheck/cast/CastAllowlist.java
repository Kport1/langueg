package com.kport.langueg.typeCheck.cast;

import com.kport.langueg.typeCheck.SymbolTable;
import com.kport.langueg.typeCheck.types.Type;

public interface CastAllowlist {
    boolean allowCastImplicit(Type from, Type to, SymbolTable symbolTable);

    boolean allowCastExplicit(Type from, Type to, SymbolTable symbolTable);
}
