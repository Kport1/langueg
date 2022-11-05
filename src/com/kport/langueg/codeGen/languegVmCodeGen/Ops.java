package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.typeCheck.types.PrimitiveType;

import java.util.EnumMap;

public enum Ops {
    NOP             (0x00),

    PUSH_BYTE       (0x01, Generic.PUSHC, PrimitiveType.Byte),
    LOAD_BYTE       (0x02, Generic.LOAD, PrimitiveType.Byte),
    STORE_BYTE      (0x03, Generic.STORE, PrimitiveType.Byte),

    PUSH_INTC       (0x04, Generic.PUSHC, PrimitiveType.Int),
    LOAD_INT        (0x05, Generic.LOAD, PrimitiveType.Int),
    STORE_INT       (0x06, Generic.STORE, PrimitiveType.Int),

    NEG_INT         (0x07),
    ADD_INT         (0x08),
    SUB_INT         (0x09),
    MUL_INT         (0x0A),
    DIV_INT         (0x0B),
    MOD_INT         (0x0C),
    AND_INT         (0x0D),
    OR_INT          (0x0E),
    XOR_INT         (0x0F),
    SHIFTR_INT      (0x10),
    SHIFTL_INT      (0x11),

    DUP_INT         (0x12),
    POP_INT         (0x13),

    JMP             (0x14),
    JMP_IF_FALSE    (0x15);

    public enum Generic {
        PUSHC,
        LOAD,
        STORE,

        NONE
    }



    public final byte code;
    private final Generic generic;
    private final PrimitiveType type;
    Ops(int code_, Generic generic_, PrimitiveType type_){
        code = (byte) code_;
        generic = generic_;
        type = type_;
    }

    Ops(int code_){
        code = (byte) code_;
        generic = Generic.NONE;
        type = PrimitiveType.Void;
    }

    public static Ops ofGeneric(Generic genericOp, PrimitiveType type){
        for (Ops op : Ops.values()) {
            if(op.generic == genericOp && op.type == type){
                return op;
            }
        }
        return NOP;
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
