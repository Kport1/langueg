package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.nodes.expr.NBinOp;
import com.kport.langueg.typeCheck.types.PrimitiveType;

public enum Ops {
    NOP             (0x00),

    //Stack Ops
    PUSH8           (0x01, Generic.PUSH, LanguegVmValSize._8),
    PUSH16          (0x02, Generic.PUSH, LanguegVmValSize._16),
    PUSH32          (0x03, Generic.PUSH, LanguegVmValSize._32),
    PUSH64          (0x04, Generic.PUSH, LanguegVmValSize._64),

    DUP8            (0x05, Generic.DUP, LanguegVmValSize._8),
    DUP16           (0x06, Generic.DUP, LanguegVmValSize._16),
    DUP32           (0x07, Generic.DUP, LanguegVmValSize._32),
    DUP64           (0x08, Generic.DUP, LanguegVmValSize._64),

    POP8            (0x09, Generic.POP, LanguegVmValSize._8),
    POP16           (0x0A, Generic.POP, LanguegVmValSize._16),
    POP32           (0x0B, Generic.POP, LanguegVmValSize._32),
    POP64           (0x0C, Generic.POP, LanguegVmValSize._64),

    STORE8          (0x0D, Generic.STORE, LanguegVmValSize._8),
    STORE16         (0x0E, Generic.STORE, LanguegVmValSize._16),
    STORE32         (0x0F, Generic.STORE, LanguegVmValSize._32),
    STORE64         (0x10, Generic.STORE, LanguegVmValSize._64),

    LOAD8           (0x011, Generic.LOAD, LanguegVmValSize._8),
    LOAD16          (0x012, Generic.LOAD, LanguegVmValSize._16),
    LOAD32          (0x013, Generic.LOAD, LanguegVmValSize._32),
    LOAD64          (0x014, Generic.LOAD, LanguegVmValSize._64),



    //Binary Arithmetic
    ADD8            (0x15, Generic.ADD, LanguegVmValSize._8),
    ADD16           (0x16, Generic.ADD, LanguegVmValSize._16),
    ADD32           (0x17, Generic.ADD, LanguegVmValSize._32),
    ADD64           (0x18, Generic.ADD, LanguegVmValSize._64),

    SUB8            (0x19, Generic.SUB, LanguegVmValSize._8),
    SUB16           (0x1A, Generic.SUB, LanguegVmValSize._16),
    SUB32           (0x1B, Generic.SUB, LanguegVmValSize._32),
    SUB64           (0x1C, Generic.SUB, LanguegVmValSize._64),

    MUL8            (0x1D, Generic.MUL, LanguegVmValSize._8),
    MUL16           (0x1E, Generic.MUL, LanguegVmValSize._16),
    MUL32           (0x1F, Generic.MUL, LanguegVmValSize._32),
    MUL64           (0x20, Generic.MUL, LanguegVmValSize._64),

    DIV8            (0x21, Generic.DIV, LanguegVmValSize._8),
    DIV16           (0x22, Generic.DIV, LanguegVmValSize._16),
    DIV32           (0x23, Generic.DIV, LanguegVmValSize._32),
    DIV64           (0x24, Generic.DIV, LanguegVmValSize._64),

    MOD8            (0x25, Generic.MOD, LanguegVmValSize._8),
    MOD16           (0x26, Generic.MOD, LanguegVmValSize._16),
    MOD32           (0x27, Generic.MOD, LanguegVmValSize._32),
    MOD64           (0x28, Generic.MOD, LanguegVmValSize._64),

    POW8            (0x29, Generic.POW, LanguegVmValSize._8),
    POW16           (0x2A, Generic.POW, LanguegVmValSize._16),
    POW32           (0x2B, Generic.POW, LanguegVmValSize._32),
    POW64           (0x2C, Generic.POW, LanguegVmValSize._64),



    //Binary Bitwise
    SHIFTR8         (0x2D, Generic.SHIFTR, LanguegVmValSize._8),
    SHIFTR16        (0x2E, Generic.SHIFTR, LanguegVmValSize._16),
    SHIFTR32        (0x2F, Generic.SHIFTR, LanguegVmValSize._32),
    SHIFTR64        (0x30, Generic.SHIFTR, LanguegVmValSize._64),

    SHIFTL8         (0x31, Generic.SHIFTL, LanguegVmValSize._8),
    SHIFTL16        (0x32, Generic.SHIFTL, LanguegVmValSize._16),
    SHIFTL32        (0x33, Generic.SHIFTL, LanguegVmValSize._32),
    SHIFTL64        (0x34, Generic.SHIFTL, LanguegVmValSize._64),

    AND8            (0x35, Generic.AND, LanguegVmValSize._8),
    AND16           (0x36, Generic.AND, LanguegVmValSize._16),
    AND32           (0x37, Generic.AND, LanguegVmValSize._32),
    AND64           (0x38, Generic.AND, LanguegVmValSize._64),

    OR8             (0x39, Generic.OR, LanguegVmValSize._8),
    OR16            (0x3A, Generic.OR, LanguegVmValSize._16),
    OR32            (0x3B, Generic.OR, LanguegVmValSize._32),
    OR64            (0x3C, Generic.OR, LanguegVmValSize._64),

    XOR8            (0x3D, Generic.XOR, LanguegVmValSize._8),
    XOR16           (0x3E, Generic.XOR, LanguegVmValSize._16),
    XOR32           (0x3F, Generic.XOR, LanguegVmValSize._32),
    XOR64           (0x40, Generic.XOR, LanguegVmValSize._64),



    //Binary Comparison
    GREATER8        (0x41, Generic.GREATER, LanguegVmValSize._8),
    GREATER16       (0x42, Generic.GREATER, LanguegVmValSize._16),
    GREATER32       (0x43, Generic.GREATER, LanguegVmValSize._32),
    GREATER64       (0x44, Generic.GREATER, LanguegVmValSize._64),

    LESS8           (0x45, Generic.LESS, LanguegVmValSize._8),
    LESS16          (0x46, Generic.LESS, LanguegVmValSize._16),
    LESS32          (0x47, Generic.LESS, LanguegVmValSize._32),
    LESS64          (0x48, Generic.LESS, LanguegVmValSize._64),

    GREATEREQ8      (0x49, Generic.GREATEREQ, LanguegVmValSize._8),
    GREATEREQ16     (0x4A, Generic.GREATEREQ, LanguegVmValSize._16),
    GREATEREQ32     (0x4B, Generic.GREATEREQ, LanguegVmValSize._32),
    GREATEREQ64     (0x4C, Generic.GREATEREQ, LanguegVmValSize._64),

    LESSEQ8         (0x4D, Generic.LESSEQ, LanguegVmValSize._8),
    LESSEQ16        (0x4E, Generic.LESSEQ, LanguegVmValSize._16),
    LESSEQ32        (0x4F, Generic.LESSEQ, LanguegVmValSize._32),
    LESSEQ64        (0x50, Generic.LESSEQ, LanguegVmValSize._64),

    EQ8             (0x51, Generic.EQ, LanguegVmValSize._8),
    EQ16            (0x52, Generic.EQ, LanguegVmValSize._16),
    EQ32            (0x53, Generic.EQ, LanguegVmValSize._32),
    EQ64            (0x54, Generic.EQ, LanguegVmValSize._64),

    NOTEQ8          (0x55, Generic.NOTEQ, LanguegVmValSize._8),
    NOTEQ16         (0x56, Generic.NOTEQ, LanguegVmValSize._16),
    NOTEQ32         (0x57, Generic.NOTEQ, LanguegVmValSize._32),
    NOTEQ64         (0x58, Generic.NOTEQ, LanguegVmValSize._64),

    //Branching
    JMP_IF_FALSE    (0x59),
    JMP             (0x5A);

    public enum Generic {
        PUSH,
        DUP,
        POP,
        STORE,
        LOAD,

        ADD,
        SUB,
        MUL,
        DIV,
        MOD,
        POW,

        SHIFTR,
        SHIFTL,
        AND,
        OR,
        XOR,

        GREATER,
        LESS,
        GREATEREQ,
        LESSEQ,
        EQ,
        NOTEQ,


        NONE
    }


    public final byte code;
    private final Generic generic;
    private final LanguegVmValSize size;
    Ops(int code_, Generic generic_, LanguegVmValSize size_){
        code = (byte) code_;
        generic = generic_;
        size = size_;
    }

    Ops(int code_){
        code = (byte) code_;
        generic = Generic.NONE;
        size = LanguegVmValSize.NONE;
    }

    public static Ops ofGeneric(Generic genericOp, LanguegVmValSize size){
        for (Ops op : Ops.values()) {
            if(op.generic == genericOp && op.size == size){
                return op;
            }
        }
        return NOP;
    }

    public static Ops ofOP(NBinOp op){
        return ofOP((PrimitiveType) op.exprType, op.op);
    }

    public static Ops ofOP(PrimitiveType type, TokenType op){
        return ofGeneric(switch(op){
            case Plus -> Generic.ADD;
            case Minus -> Generic.SUB;
            case Mul -> Generic.MUL;
            case Div -> Generic.DIV;
            case Mod -> Generic.MOD;
            case Pow -> Generic.POW;
            case ShiftR -> Generic.SHIFTR;
            case ShiftL -> Generic.SHIFTL;
            case Greater -> Generic.GREATER;
            case Less -> Generic.LESS;
            case GreaterEq -> Generic.GREATEREQ;
            case LessEq -> Generic.LESSEQ;
            case Eq -> Generic.EQ;
            case NotEq -> Generic.NOTEQ;
            case BAnd -> Generic.AND;
            case And -> null;
            case BOr -> Generic.OR;
            case Or -> null;
            case BXOr -> Generic.XOR;
            case XOr -> null;
            case Inc -> null;
            case Dec -> null;
            case Not -> null;

            default -> throw new Error();
        }, type.getSize());
    }
}
