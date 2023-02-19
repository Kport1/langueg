package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.typeCheck.types.PrimitiveType;
import com.kport.langueg.typeCheck.types.Type;

public enum LanguegVmValSize {
    NONE,
    _8,
    _16,
    _32,
    _64;

    public static LanguegVmValSize ofPrimitive(PrimitiveType type){
        return switch (type){
            case Void -> NONE;
            case Boolean, Byte -> _8;
            case Char, Short -> _16;
            case Int, Float -> _32;
            case Long, Double -> _64;
        };
    }

    public static LanguegVmValSize ofType(Type type){
        if(type.isPrimitive())
            return ofPrimitive((PrimitiveType) type);

        return _64;
    }
}
