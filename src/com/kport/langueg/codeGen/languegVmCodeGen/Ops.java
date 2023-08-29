package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.typeCheck.types.PrimitiveType;

public enum Ops {
    NOP             (0x00),

    PUSH8           (0x01, LanguegVmValSize._8),
    DUP8            (0x02, LanguegVmValSize._8, Generic.DUP),
    POP8            (0x03, LanguegVmValSize._8, Generic.POP),
    STORE8          (0x04, LanguegVmValSize._8, Generic.STORE),
    LOAD8           (0x05, LanguegVmValSize._8, Generic.LOAD),

    ADD8            (0x06, LanguegVmValSize._8),
    SUB8            (0x07, LanguegVmValSize._8),
    MUL8            (0x08, LanguegVmValSize._8),
    DIV8            (0x09, LanguegVmValSize._8),
    MOD8            (0x0A, LanguegVmValSize._8),

    SHIFTR8         (0x0B, LanguegVmValSize._8),
    SHIFTL8         (0x0C, LanguegVmValSize._8),
    AND8            (0x0D, LanguegVmValSize._8),
    OR8             (0x0E, LanguegVmValSize._8),
    XOR8            (0x0F, LanguegVmValSize._8),

    GREATER8        (0x10, LanguegVmValSize._8),
    LESS8           (0x11, LanguegVmValSize._8),
    GREATEREQ8      (0x12, LanguegVmValSize._8),
    LESSEQ8         (0x13, LanguegVmValSize._8),
    EQ8             (0x14, LanguegVmValSize._8),
    NOTEQ8          (0x15, LanguegVmValSize._8),

    JMP_IF_FALSE    (0x16, LanguegVmValSize._8),

    //-----------------------------------------------------------

    PUSH16          (0x31, LanguegVmValSize._16),
    DUP16           (0x32, LanguegVmValSize._16, Generic.DUP),
    POP16           (0x33, LanguegVmValSize._16, Generic.POP),
    STORE16         (0x34, LanguegVmValSize._16, Generic.STORE),
    LOAD16          (0x35, LanguegVmValSize._16, Generic.LOAD),

    ADD16           (0x36, LanguegVmValSize._16),
    SUB16           (0x37, LanguegVmValSize._16),
    MUL16           (0x38, LanguegVmValSize._16),
    DIV16           (0x39, LanguegVmValSize._16),
    MOD16           (0x3A, LanguegVmValSize._16),

    SHIFTR16        (0x3B, LanguegVmValSize._16),
    SHIFTL16        (0x3C, LanguegVmValSize._16),
    AND16           (0x3D, LanguegVmValSize._16),
    OR16            (0x3E, LanguegVmValSize._16),
    XOR16           (0x3F, LanguegVmValSize._16),

    GREATER16       (0x40, LanguegVmValSize._16),
    LESS16          (0x41, LanguegVmValSize._16),
    GREATEREQ16     (0x42, LanguegVmValSize._16),
    LESSEQ16        (0x43, LanguegVmValSize._16),
    EQ16            (0x44, LanguegVmValSize._16),
    NOTEQ16         (0x45, LanguegVmValSize._16),

    //-----------------------------------------------------------

    PUSH32          (0x61, LanguegVmValSize._32),
    DUP32           (0x62, LanguegVmValSize._32, Generic.DUP),
    POP32           (0x63, LanguegVmValSize._32, Generic.POP),
    STORE32         (0x64, LanguegVmValSize._32, Generic.STORE),
    LOAD32          (0x65, LanguegVmValSize._32, Generic.LOAD),

    ADD32           (0x66, LanguegVmValSize._32),
    SUB32           (0x67, LanguegVmValSize._32),
    MUL32           (0x68, LanguegVmValSize._32),
    DIV32           (0x69, LanguegVmValSize._32),
    MOD32           (0x6A, LanguegVmValSize._32),

    SHIFTR32        (0x6B, LanguegVmValSize._32),
    SHIFTL32        (0x6C, LanguegVmValSize._32),
    AND32           (0x6D, LanguegVmValSize._32),
    OR32            (0x6E, LanguegVmValSize._32),
    XOR32           (0x6F, LanguegVmValSize._32),

    GREATER32       (0x70, LanguegVmValSize._32),
    LESS32          (0x71, LanguegVmValSize._32),
    GREATEREQ32     (0x72, LanguegVmValSize._32),
    LESSEQ32        (0x73, LanguegVmValSize._32),
    EQ32            (0x74, LanguegVmValSize._32),
    NOTEQ32         (0x75, LanguegVmValSize._32),

    ADDF32          (0x76, LanguegVmValSize._32),
    SUBF32          (0x77, LanguegVmValSize._32),
    MULF32          (0x78, LanguegVmValSize._32),
    DIVF32          (0x79, LanguegVmValSize._32),
    POWF32          (0x7A, LanguegVmValSize._32),

    GREATERF32      (0x7B, LanguegVmValSize._32),
    LESSF32         (0x7C, LanguegVmValSize._32),
    GREATEREQF32    (0x7D, LanguegVmValSize._32),
    LESSEQF32       (0x7E, LanguegVmValSize._32),
    EQF32           (0x7F, LanguegVmValSize._32),
    NOTEQF32        (0x80, LanguegVmValSize._32),

    //-----------------------------------------------------------

    PUSH64          (0x91, LanguegVmValSize._64),
    DUP64           (0x92, LanguegVmValSize._64, Generic.DUP),
    POP64           (0x93, LanguegVmValSize._64, Generic.POP),
    STORE64         (0x94, LanguegVmValSize._64, Generic.STORE),
    LOAD64          (0x95, LanguegVmValSize._64, Generic.LOAD),

    ADD64           (0x96, LanguegVmValSize._64),
    SUB64           (0x97, LanguegVmValSize._64),
    MUL64           (0x98, LanguegVmValSize._64),
    DIV64           (0x99, LanguegVmValSize._64),
    MOD64           (0x9A, LanguegVmValSize._64),

    SHIFTR64        (0x9B, LanguegVmValSize._64),
    SHIFTL64        (0x9C, LanguegVmValSize._64),
    AND64           (0x9D, LanguegVmValSize._64),
    OR64            (0x9E, LanguegVmValSize._64),
    XOR64           (0x9F, LanguegVmValSize._64),

    GREATER64       (0xA0, LanguegVmValSize._64),
    LESS64          (0xA1, LanguegVmValSize._64),
    GREATEREQ64     (0xA2, LanguegVmValSize._64),
    LESSEQ64        (0xA3, LanguegVmValSize._64),
    EQ64            (0xA4, LanguegVmValSize._64),
    NOTEQ64         (0xA5, LanguegVmValSize._64),

    ADDF64          (0xA6, LanguegVmValSize._64),
    SUBF64          (0xA7, LanguegVmValSize._64),
    MULF64          (0xA8, LanguegVmValSize._64),
    DIVF64          (0xA9, LanguegVmValSize._64),
    POWF64          (0xAA, LanguegVmValSize._64),

    GREATERF64      (0xAB, LanguegVmValSize._64),
    LESSF64         (0xAC, LanguegVmValSize._64),
    GREATEREQF64    (0xAD, LanguegVmValSize._64),
    LESSEQF64       (0xAE, LanguegVmValSize._64),
    EQF64           (0xAF, LanguegVmValSize._64),
    NOTEQF64        (0xB0, LanguegVmValSize._64),

    PUSHFN          (0xB1, LanguegVmValSize._64),

    //---------------------------------------------------------------------

    JMP             (0xC1);

    public enum Generic {
        DUP,
        POP,
        STORE,
        LOAD,
        NONE
    }

    public final byte code;
    private final LanguegVmValSize size;
    private final Generic generic;

    Ops(int code_, LanguegVmValSize size_, Generic generic_){
        code = (byte) code_;
        size = size_;
        generic = generic_;
    }

    Ops(int code_, LanguegVmValSize size_){
        code = (byte) code_;
        size = size_;
        generic = Generic.NONE;
    }

    Ops(int code_){
        code = (byte) code_;
        size = null;
        generic = Generic.NONE;
    }

    public static Ops ofGeneric(LanguegVmValSize size, Generic generic){
        for (Ops op : values()) {
            if(op.size == size && op.generic == generic) return op;
        }
        return NOP;
    }
}
