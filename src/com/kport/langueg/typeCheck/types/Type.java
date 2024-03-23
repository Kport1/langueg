package com.kport.langueg.typeCheck.types;

import com.kport.langueg.codeGen.languegVmCodeGen.LanguegVmValSize;

public interface Type {
    /*
    * 0x01 Primitive    CODE
    * 0x02 Fn           RetT ParamTLen [ParamT]
    * 0x03 Tuple        TupTLen [TupT TupTNameLen [TupTName]]
    * 0x04 Union        UnionTLen [UnionT UnionTNameLen [UnionTName]]
    * 0x05 Array        ArrayT
    *
    * 0x10 Custom       NameLen [Name]
    * */
    byte[] serialize();

    LanguegVmValSize getSize();
}
