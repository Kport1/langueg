package com.kport.langueg.codeGen.languegVmCodeGen;

public enum LanguegVmValSize {
    //NONE,
    _8,
    _16,
    _32,
    _64;

    public static byte codeOf(LanguegVmValSize size){
        return switch (size){
            case null -> 0;
            case _8 -> 1;
            case _16 -> 2;
            case _32 -> 3;
            case _64 -> 4;
        };
    }
}
