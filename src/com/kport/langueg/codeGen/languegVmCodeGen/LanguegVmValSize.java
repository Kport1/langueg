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
            case Bool, U8 -> _8;
            case Char, I16 -> _16;
            case I32, F32 -> _32;
            case I64, F64 -> _64;
        };
    }

    public static LanguegVmValSize ofType(Type type){
        if(type.isPrimitive())
            return ofPrimitive((PrimitiveType) type);

        return _64;
    }
}
