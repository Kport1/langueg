package com.kport.langueg.codeGen.languegVmCodeGen;

public enum Ops {
    NOP(0x00),

    MOV(0x01), //bytes: u16, to: u16, from: u16, flags: u8(zero: u7, contains_ref: bool)
    MOV8(0x02), //to: u16, from: u16
    MOV16(0x03), //to: u16, from: u16
    MOV32(0x04), //to: u16, from: u16
    MOV64(0x05), //to: u16, from: u16
    MOV_REF(0x06), //to: u16, from: u16

    LOAD(0x11), //bytes: u16, to: u16, data: [u8]
    LOAD8(0x12), //to: u16, data: u8
    LOAD16(0x13), //to: u16, data: u16
    LOAD32(0x14), //to: u16, data: u32
    LOAD64(0x15), //to: u16, data: u64
    LOADP(0x16), //to: u16, index: u16

    LOADFN(0x1A), //to: u16, fnIndex: u16
    CALL(0x1B), //fn: u16, paramsBegin: u16, retBegin: u16
    CALL_DIRECT(0x1C), //fnIndex: u16, paramsBegin: u16, retBegin: u16
    RET(0x1D), //retBegin: u16

    JMP_IF_FALSE(0x21), //byte: u16, jmp_delta: i16
    JMP(0x22), //jmpDelta: i16

    BRANCH(0x23), //index: u16, table: [u16]

    //-------------------------------------------

    ALLOC(0x31), //ref_index: u16, size_index: u16
    ALLOC_DIRECT(0x32), //ref_index: u16, size: u32

    MOV_TO_HEAP(0x35), //bytes: u16, to_index: u16, from: u16, ref_index: u16, flags: u8(zero: u7, contains_ref: bool)
    MOV_FROM_HEAP(0x36), //bytes: u16, to: u16, from_index: u16, ref_index: u16, flags: u8(zero: u7, contains_ref: bool)
    MOV_TO_HEAP_DIRECT(0x37), //bytes: u16, to: u32, from: u16, ref_index: u16, flags: u8(zero: u7, contains_ref: bool)
    MOV_FROM_HEAP_DIRECT(0x38), //bytes: u16, to: u16, from: u32, ref_index: u16, flags: u8(zero: u7, contains_ref: bool)
    MOV_TO_HEAP_ZERO(0x39), //bytes: u16, from: u16, ref_index: u16, flags: u8(zero: u7, contains_ref: bool)
    MOV_FROM_HEAP_ZERO(0x3A), //bytes: u16, to: u16, ref_index: u16, flags: u8(zero: u7, contains_ref: bool)

    //-------------------------------------------

    ADD8(0x81), //to: u16, op1: u16, op2: u16
    SUB8(0x82), //to: u16, op1: u16, op2: u16
    MUL8(0x83), //to: u16, op1: u16, op2: u16
    DIV8(0x84), //to: u16, op1: u16, op2: u16
    MOD8(0x85), //to: u16, op1: u16, op2: u16

    ADD16(0x86), //to: u16, op1: u16, op2: u16
    SUB16(0x87), //to: u16, op1: u16, op2: u16
    MUL16(0x88), //to: u16, op1: u16, op2: u16
    DIV16(0x89), //to: u16, op1: u16, op2: u16
    MOD16(0x8A), //to: u16, op1: u16, op2: u16

    ADD32(0x8B), //to: u16, op1: u16, op2: u16
    SUB32(0x8C), //to: u16, op1: u16, op2: u16
    MUL32(0x8D), //to: u16, op1: u16, op2: u16
    DIV32(0x8E), //to: u16, op1: u16, op2: u16
    MOD32(0x8F), //to: u16, op1: u16, op2: u16

    ADD64(0x90), //to: u16, op1: u16, op2: u16
    SUB64(0x91), //to: u16, op1: u16, op2: u16
    MUL64(0x92), //to: u16, op1: u16, op2: u16
    DIV64(0x93), //to: u16, op1: u16, op2: u16
    MOD64(0x94), //to: u16, op1: u16, op2: u16


    AND8(0x95), //to: u16, op1: u16, op2: u16
    OR8(0x96), //to: u16, op1: u16, op2: u16
    XOR8(0x97), //to: u16, op1: u16, op2: u16

    AND16(0x98), //to: u16, op1: u16, op2: u16
    OR16(0x99), //to: u16, op1: u16, op2: u16
    XOR16(0x9A), //to: u16, op1: u16, op2: u16

    AND32(0x9B), //to: u16, op1: u16, op2: u16
    OR32(0x9C), //to: u16, op1: u16, op2: u16
    XOR32(0x9D), //to: u16, op1: u16, op2: u16

    AND64(0x9E), //to: u16, op1: u16, op2: u16
    OR64(0x9F), //to: u16, op1: u16, op2: u16
    XOR64(0xA0), //to: u16, op1: u16, op2: u16


    SHIFTR8(0xA1), //to: u16, op1: u16, op2: u16
    SHIFTL8(0xA2), //to: u16, op1: u16, op2: u16

    SHIFTR16(0xA3), //to: u16, op1: u16, op2: u16
    SHIFTL16(0xA4), //to: u16, op1: u16, op2: u16

    SHIFTR32(0xA5), //to: u16, op1: u16, op2: u16
    SHIFTL32(0xA6), //to: u16, op1: u16, op2: u16

    SHIFTR64(0xA7), //to: u16, op1: u16, op2: u16
    SHIFTL64(0xA8), //to: u16, op1: u16, op2: u16


    GREATER8(0xA9), //to: u16, op1: u16, op2: u16
    LESS8(0xAA), //to: u16, op1: u16, op2: u16
    GREATEREQ8(0xAB), //to: u16, op1: u16, op2: u16
    LESSEQ8(0xAC), //to: u16, op1: u16, op2: u16
    EQ8(0xAD), //to: u16, op1: u16, op2: u16
    NOTEQ8(0xAE), //to: u16, op1: u16, op2: u16

    GREATER16(0xAF), //to: u16, op1: u16, op2: u16
    LESS16(0xB0), //to: u16, op1: u16, op2: u16
    GREATEREQ16(0xB1), //to: u16, op1: u16, op2: u16
    LESSEQ16(0xB2), //to: u16, op1: u16, op2: u16
    EQ16(0xB3), //to: u16, op1: u16, op2: u16
    NOTEQ16(0xB4), //to: u16, op1: u16, op2: u16

    GREATER32(0xB5), //to: u16, op1: u16, op2: u16
    LESS32(0xB6), //to: u16, op1: u16, op2: u16
    GREATEREQ32(0xB7), //to: u16, op1: u16, op2: u16
    LESSEQ32(0xB8), //to: u16, op1: u16, op2: u16
    EQ32(0xB9), //to: u16, op1: u16, op2: u16
    NOTEQ32(0xBA), //to: u16, op1: u16, op2: u16

    GREATER64(0xBB), //to: u16, op1: u16, op2: u16
    LESS64(0xBC), //to: u16, op1: u16, op2: u16
    GREATEREQ64(0xBD), //to: u16, op1: u16, op2: u16
    LESSEQ64(0xBE), //to: u16, op1: u16, op2: u16
    EQ64(0xBF), //to: u16, op1: u16, op2: u16
    NOTEQ64(0xC0), //to: u16, op1: u16, op2: u16

    //-------------------------------------------
    ;

    public final int code;
    Ops(int code_){
        code = code_;
    }
}
