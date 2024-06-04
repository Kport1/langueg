package com.kport.langueg.codeGen.languegVmCodeGen;

public enum Ops {
    NOP         (0x00),

    PUSH        (0x01, 1), //stack_data: u128
    DUP         (0x02, 1),
    POP         (0x03, -1),
    STORE       (0x04, -1), //offset: u16, bytes: u8
    LOAD        (0x05, 1), //offset: u16, bytes: u8

    //-------------------------------------------

    ADD         (0x06, -1),
    SUB         (0x07, -1),
    MUL         (0x08, -1),
    DIV         (0x09, -1),
    MOD         (0x0A, -1),

    SHIFTR      (0x0B, -1),
    SHIFTL      (0x0C, -1),
    AND         (0x0D, -1),
    OR          (0x0E, -1),
    XOR         (0x0F, -1),

    GREATER     (0x10, -1),
    LESS        (0x11, -1),
    GREATEREQ   (0x12, -1),
    LESSEQ      (0x13, -1),
    EQ          (0x14, -1),
    NOTEQ       (0x15, -1),

    //-------------------------------------------

    JMP_IF_FALSE(0x16, -1), //jmp_delta: i16

    //-------------------------------------------

    ADDF32      (0x17, -1),
    SUBF32      (0x18, -1),
    MULF32      (0x19, -1),
    DIVF32      (0x1A, -1),
    POWF32      (0x1B, -1),

    GREATERF32  (0x1C, -1),
    LESSF32     (0x1D, -1),
    GREATEREQF32(0x1E, -1),
    LESSEQF32   (0x1F, -1),

    ADDF64      (0x20, -1),
    SUBF64      (0x21, -1),
    MULF64      (0x22, -1),
    DIVF64      (0x23, -1),
    POWF64      (0x24, -1),

    GREATERF64  (0x25, -1),
    LESSF64     (0x26, -1),
    GREATEREQF64(0x27, -1),
    LESSEQF64   (0x28, -1),

    //-------------------------------------------

    PUSHFN          (0x29, 1), //fn_index: u16
    CALL            (0x2A, -1),

    //-------------------------------------------

    JMP             (0x30), //jmp_delta: i16
    RET             (0x31),

    //-------------------------------------------
    ALLOC           (0x35, -1, 1), //refs_mult: u16, data_mult: u16

    DUP_REF         (0x36, 0, 1),
    POP_REF         (0x37, 0, -1),
    STORE_REF       (0x38, 0, -1), //offset: u16
    LOAD_REF        (0x39, 0, 1), //offset: u16

    //-------------------------------------------

    PUSH8           (0x40, 1), //stack_data: u8
    PUSH16          (0x41, 1), //stack_data: u16
    PUSHP           (0x42, 1), //constant_pool_index: u16
    ;

    public final byte code;
    public final int stackEffect;
    public final int stackEffectRef;

    Ops(int code_, int stackEffect_, int stackEffectRef_){
        code = (byte) code_;
        stackEffect = stackEffect_;
        stackEffectRef = stackEffectRef_;
    }

    Ops(int code_, int stackEffect_){
        code = (byte) code_;
        stackEffect = stackEffect_;
        stackEffectRef = 0;
    }

    Ops(int code_){
        code = (byte) code_;
        stackEffect = 0;
        stackEffectRef = 0;
    }
}
