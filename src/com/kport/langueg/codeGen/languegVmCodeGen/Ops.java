package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.parse.ast.AST;

public enum Ops {
    NOP             (0x00),

    PUSH_INTC       (0x01),
    LOAD_INT        (0x02),
    STORE_INT       (0x03),

    NEG_INT         (0x04),
    ADD_INT         (0x05),
    SUB_INT         (0x06),
    MUL_INT         (0x07),
    DIV_INT         (0x08),
    MOD_INT         (0x09),
    AND_INT         (0x0A),
    OR_INT          (0x0B),
    XOR_INT         (0x0C),
    SHIFTR_INT      (0x0D),
    SHIFTL_INT      (0x0E),

    DUP_INT         (0x0F),
    POP_INT         (0x10),

    CMP_EQ_INT      (0x11);



    public byte code;
    Ops(int code_){
        code = (byte) code_;
    }

    public static Ops ofBinOp(AST op){
        return switch (op.returnType.primitive()){
            case Int -> switch(op.val.getTok()){
                case Plus -> ADD_INT;
                case Minus -> SUB_INT;
                case Mul -> MUL_INT;
                case Div -> DIV_INT;
                case Mod -> MOD_INT;
                case And -> AND_INT;
                case Or -> OR_INT;
                case XOr -> XOR_INT;
                case ShiftR -> SHIFTR_INT;
                case ShiftL -> SHIFTL_INT;
                default -> NOP;
            };


            default -> NOP;
        };
    }
}
