package com.kport.langueg.typeCheck.op;

import com.kport.langueg.typeCheck.types.Type;

@FunctionalInterface
public interface BinOpTypeMap {
    Type getType(Type left, Type right);
}
