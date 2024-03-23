package com.kport.langueg.typeCheck.types;

import com.kport.langueg.codeGen.languegVmCodeGen.LanguegVmValSize;

public enum PrimitiveType implements Type{
    Void(0),

    Bool(1),
    Char(2),

    U8(3),
    I8(4),
    U16(5),
    I16(6),
    U32(7),
    I32(8),
    U64(9),
    I64(10),

    F32(11),
    F64(12);

    private final byte code;

    PrimitiveType(int code_) {
        code = (byte)code_;
    }

    public boolean isNumeric(){
        return switch (this) {
            case Void, Bool, Char -> false;
            case U8, I8, U16, I16, U32, I32, U64, I64, F32, F64 -> true;
        };
    }

    public boolean isFloating(){
        return switch (this) {
            case U8, I8, U16, I16, U32, I32, U64, I64, Void, Bool, Char -> false;
            case F32, F64 -> true;
        };
    }

    public boolean isInteger(){
        return switch (this) {
            case Void, Bool, Char, F32, F64 -> false;
            case U8, I8, U16, I16, U32, I32, U64, I64 -> true;
        };
    }

    public boolean isUnsigned(){
        return switch (this) {
            case I8, I16, I32, I64, F32, F64, Void, Bool, Char-> false;
            case U8, U16, U32, U64 -> true;
        };
    }

    @Override
    public LanguegVmValSize getSize(){
        return switch (this) {
            case Void -> null;
            case Bool, U8, I8 -> LanguegVmValSize._8;
            case Char, U16, I16 -> LanguegVmValSize._16;
            case U32, I32, F32 -> LanguegVmValSize._32;
            case U64, I64, F64 -> LanguegVmValSize._64;
        };
    }

    @Override
    public String toString(){
        return this.name();
    }

    @Override
    public byte[] serialize(){
        return new byte[]{0x01, code};
    }
}
