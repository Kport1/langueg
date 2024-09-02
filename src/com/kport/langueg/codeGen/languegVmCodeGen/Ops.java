package com.kport.langueg.codeGen.languegVmCodeGen;

public enum Ops {
    NOP(0x00),

    MOV(0x01), //bytes: u16, to: u16, from: u16
    MOV8(0x02), //to: u16, from: u16
    MOV16(0x03), //to: u16, from: u16
    MOV32(0x04), //to: u16, from: u16
    MOV64(0x05), //to: u16, from: u16

    LOAD(0x06), //bytes: u16, to: u16, data: [u8]
    LOAD8(0x07), //to: u16, data: u8
    LOAD16(0x08), //to: u16, data: u16
    LOAD32(0x09), //to: u16, data: u32
    LOAD64(0x0A), //to: u16, data: u64
    LOADP(0x0B), //to: u16, index: u16

    LOADFN(0x0C), //to: u16, fnIndex: u16
    CALL(0x0D), //fn: u16, paramsBegin: u16, retBegin: u16
    CALL_DIRECT(0x0E), //fnIndex: u16, paramsBegin: u16, retBegin: u16
    RET(0x0F), //retBegin: u16

    JMP_IF_FALSE(0x10), //byte: u16, jmp_delta: i16
    JMP(0x11), //jmpDelta: i16

    BRANCH(0x12), //index: u16, table: [u16]

    //-------------------------------------------

    ALLOC_HEAP(0x35), //to: u16, size: u16
    ALLOC_HEAP_DIRECT(0x36), //to: u16, size: u32

    MARK_HEAP_REF_USED(0x37), //ref: u16
    MARK_HEAP_REF_UNUSED(0x38), //ref: u16

    MOV_WITH_REF_MARK(0x39), //bytes: u16, to: u16, from: u16
    MOV_REF(0x3A), //to: u16, from: u16

    MOV_TO_ARR(0x40), //bytes: u16, to_ref: u16, from: u16, to_index: u16
    MOV_TO_ARR8(0x41), //to_ref: u16, from: u16, to_index: u16
    MOV_TO_ARR16(0x42), //to_ref: u16, from: u16, to_index: u16
    MOV_TO_ARR32(0x43), //to_ref: u16, from: u16, to_index: u16
    MOV_TO_ARR64(0x44), //to_ref: u16, from: u16, to_index: u16

    MOV_FROM_ARR(0x45), //bytes: u16, from_ref: u16, to: u16, from_index: u16
    MOV_FROM_ARR8(0x46), //from_ref: u16, to: u16, from_index: u16
    MOV_FROM_ARR16(0x47), //from_ref: u16, to: u16, from_index: u16
    MOV_FROM_ARR32(0x48), //from_ref: u16, to: u16, from_index: u16
    MOV_FROM_ARR64(0x49), //from_ref: u16, to: u16, from_index: u16

    MOV_TO_ARR_DIRECT(0x4A), //bytes: u16, to_ref: u16, from: u16, to_index: u32
    MOV_TO_ARR_DIRECT8(0x4B), //to_ref: u16, from: u16, to_index: u32
    MOV_TO_ARR_DIRECT16(0x4C), //to_ref: u16, from: u16, to_index: u32
    MOV_TO_ARR_DIRECT32(0x4D), //to_ref: u16, from: u16, to_index: u32
    MOV_TO_ARR_DIRECT64(0x4E), //to_ref: u16, from: u16, to_index: u32

    MOV_FROM_ARR_DIRECT(0x4F), //bytes: u16, from_ref: u16, to: u32, from_index: u16
    MOV_FROM_ARR_DIRECT8(0x50), //from_ref: u16, to: u32, from_index: u16
    MOV_FROM_ARR_DIRECT16(0x51), //from_ref: u16, to: u32, from_index: u16
    MOV_FROM_ARR_DIRECT32(0x52), //from_ref: u16, to: u32, from_index: u16
    MOV_FROM_ARR_DIRECT64(0x53), //from_ref: u16, to: u32, from_index: u16

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

    ADDF32(0x17),
    SUBF32(0x18),
    MULF32(0x19),
    DIVF32(0x1A),
    POWF32(0x1B),

    GREATERF32(0x1C),
    LESSF32(0x1D),
    GREATEREQF32(0x1E),
    LESSEQF32(0x1F),

    ADDF64(0x20),
    SUBF64(0x21),
    MULF64(0x22),
    DIVF64(0x23),
    POWF64(0x24),

    GREATERF64(0x25),
    LESSF64(0x26),
    GREATEREQF64(0x27),
    LESSEQF64(0x28),
    ;

    public final int code;
    Ops(int code_){
        code = code_;
    }
}
