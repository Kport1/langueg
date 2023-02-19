package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.typeCheck.types.PrimitiveType;

import java.util.EnumMap;

public enum Ops {
    NOP             (0x00),

    //Byte
    PUSH_BYTE       (0x01, Generic.PUSHC, PrimitiveType.Byte),
    LOAD_BYTE       (0x02, Generic.LOAD, PrimitiveType.Byte),
    STORE_BYTE      (0x03, Generic.STORE, PrimitiveType.Byte),

    AND_BYTE        (0x04),
    OR_BYTE         (0x05),
    XOR_BYTE        (0x06),

    //Short
    PUSH_SHORT      (0x07, Generic.PUSHC, PrimitiveType.Short),
    LOAD_SHORT      (0x08, Generic.LOAD, PrimitiveType.Short),
    STORE_SHORT     (0x09, Generic.STORE, PrimitiveType.Short),

    NEG_SHORT       (0x0A),
    ADD_SHORT       (0x0B),
    SUB_SHORT       (0x0C),
    MUL_SHORT       (0x0D),
    DIV_SHORT       (0x0E),
    MOD_SHORT       (0x0F),
    AND_SHORT       (0x10),
    OR_SHORT        (0x11),
    XOR_SHORT       (0x12),
    SHIFTR_SHORT    (0x13),
    SHIFTL_SHORT    (0x14),

    GREATER_SHORT   (0x15),
    LESS_SHORT      (0x16),
    GREATEREQ_SHORT (0x17),
    LESSEQ_SHORT    (0x18),
    EQ_SHORT        (0x19),
    NOTEQ_SHORT     (0x1A),

    //Int
    PUSH_INTC       (0x1B, Generic.PUSHC, PrimitiveType.Int),
    LOAD_INT        (0x1C, Generic.LOAD, PrimitiveType.Int),
    STORE_INT       (0x1D, Generic.STORE, PrimitiveType.Int),

    NEG_INT         (0x1E),
    ADD_INT         (0x1F),
    SUB_INT         (0x20),
    MUL_INT         (0x21),
    DIV_INT         (0x22),
    MOD_INT         (0x23),
    AND_INT         (0x24),
    OR_INT          (0x25),
    XOR_INT         (0x26),
    SHIFTR_INT      (0x27),
    SHIFTL_INT      (0x28),

    GREATER_INT     (0x29),
    LESS_INT        (0x2A),
    GREATEREQ_INT   (0x2B),
    LESSEQ_INT      (0x2C),
    EQ_INT          (0x2D),
    NOTEQ_INT       (0x2E),

    DUP_INT         (0x2F, Generic.DUP, PrimitiveType.Int),

    //Long
    PUSH_LONGC      (0x30, Generic.PUSHC, PrimitiveType.Long),
    LOAD_LONG       (0x31, Generic.LOAD, PrimitiveType.Long),
    STORE_LONG      (0x32, Generic.STORE, PrimitiveType.Long),

    NEG_LONG        (0x33),
    ADD_LONG        (0x34),
    SUB_LONG        (0x35),
    MUL_LONG        (0x36),
    DIV_LONG        (0x37),
    MOD_LONG        (0x38),
    AND_LONG        (0x39),
    OR_LONG         (0x3A),
    XOR_LONG        (0x3B),
    SHIFTR_LONG     (0x3C),
    SHIFTL_LONG     (0x3D),

    GREATER_LONG    (0x3E),
    LESS_LONG       (0x3F),
    GREATEREQ_LONG  (0x40),
    LESSEQ_LONG     (0x41),
    EQ_LONG         (0x42),
    NOTEQ_LONG      (0x43),

    //Float
    PUSH_FLOATC     (0x44, Generic.PUSHC, PrimitiveType.Float),
    LOAD_FLOAT      (0x45, Generic.LOAD, PrimitiveType.Float),
    STORE_FLOAT     (0x46, Generic.STORE, PrimitiveType.Float),

    NEG_FLOAT       (0x47),
    ADD_FLOAT       (0x48),
    SUB_FLOAT       (0x49),
    MUL_FLOAT       (0x4A),
    DIV_FLOAT       (0x4B),

    GREATER_FLOAT   (0x4C),
    LESS_FLOAT      (0x4D),
    GREATEREQ_FLOAT (0x4E),
    LESSEQ_FLOAT    (0x4F),
    EQ_FLOAT        (0x50),
    NOTEQ_FLOAT     (0x51),

    //Double
    PUSH_DOUBLEC    (0x52, Generic.PUSHC, PrimitiveType.Float),
    LOAD_DOUBLE     (0x53, Generic.LOAD, PrimitiveType.Float),
    STORE_DOUBLE    (0x54, Generic.STORE, PrimitiveType.Float),

    NEG_DOUBLE      (0x55),
    ADD_DOUBLE      (0x56),
    SUB_DOUBLE      (0x57),
    MUL_DOUBLE      (0x58),
    DIV_DOUBLE      (0x59),

    GREATER_DOUBLE  (0x5A),
    LESS_DOUBLE     (0x5B),
    GREATEREQ_DOUBLE(0x5C),
    LESSEQ_DOUBLE   (0x5D),
    EQ_DOUBLE       (0x5E),
    NOTEQ_DOUBLE    (0x5F),

    JMP             (0x60),
    JMP_IF_FALSE    (0x61);

    public enum Generic {
        PUSHC,
        LOAD,
        STORE,
        DUP,

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
        return ofBinOp((PrimitiveType) op.children[0].returnType, op.val.getTok());
    }

    public static Ops ofBinOp(PrimitiveType type, TokenType op){
        return switch (type){
            case Int -> switch(op){
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
                case Greater -> GREATER_INT;
                case Less -> LESS_INT;
                case GreaterEq -> GREATEREQ_INT;
                case LessEq -> LESSEQ_INT;
                case Eq -> EQ_INT;
                case NotEq -> NOTEQ_INT;
                default -> NOP;
            };


            default -> NOP;
        };
    }
}
