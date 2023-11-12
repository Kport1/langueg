package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.util.Pair;

import java.util.EnumMap;
import java.util.Map;

import static com.kport.langueg.codeGen.languegVmCodeGen.LanguegVmValSize.*;

public enum Ops {
    NOP             (0x00),

    PUSH8           (0x01, _8, new Pair<>(_8, 1)),
    DUP8            (0x02, _8, Generic.DUP, new Pair<>(_8, 1)),
    POP8            (0x03, _8, Generic.POP, new Pair<>(_8, -1)),
    STORE8          (0x04, _8, Generic.STORE, new Pair<>(_8, -1)),
    LOAD8           (0x05, _8, Generic.LOAD, new Pair<>(_8, 1)),

    ADD8            (0x06, _8, new Pair<>(_8, -1)),
    SUB8            (0x07, _8, new Pair<>(_8, -1)),
    MUL8            (0x08, _8, new Pair<>(_8, -1)),
    DIV8            (0x09, _8, new Pair<>(_8, -1)),
    MOD8            (0x0A, _8, new Pair<>(_8, -1)),

    SHIFTR8         (0x0B, _8, new Pair<>(_8, -1)),
    SHIFTL8         (0x0C, _8, new Pair<>(_8, -1)),
    AND8            (0x0D, _8, new Pair<>(_8, -1)),
    OR8             (0x0E, _8, new Pair<>(_8, -1)),
    XOR8            (0x0F, _8, new Pair<>(_8, -1)),

    GREATER8        (0x10, _8, new Pair<>(_8, -1)),
    LESS8           (0x11, _8, new Pair<>(_8, -1)),
    GREATEREQ8      (0x12, _8, new Pair<>(_8, -1)),
    LESSEQ8         (0x13, _8, new Pair<>(_8, -1)),
    EQ8             (0x14, _8, new Pair<>(_8, -1)),
    NOTEQ8          (0x15, _8, new Pair<>(_8, -1)),

    JMP_IF_FALSE    (0x16, _8, new Pair<>(_8, -1)),

    //-----------------------------------------------------------

    PUSH16          (0x31, _16, new Pair<>(_16, 1)),
    DUP16           (0x32, _16, Generic.DUP, new Pair<>(_16, 1)),
    POP16           (0x33, _16, Generic.POP, new Pair<>(_16, -1)),
    STORE16         (0x34, _16, Generic.STORE, new Pair<>(_16, -1)),
    LOAD16          (0x35, _16, Generic.LOAD, new Pair<>(_16, 1)),

    ADD16           (0x36, _16, new Pair<>(_16, -1)),
    SUB16           (0x37, _16, new Pair<>(_16, -1)),
    MUL16           (0x38, _16, new Pair<>(_16, -1)),
    DIV16           (0x39, _16, new Pair<>(_16, -1)),
    MOD16           (0x3A, _16, new Pair<>(_16, -1)),

    SHIFTR16        (0x3B, _16, new Pair<>(_8, -1)),
    SHIFTL16        (0x3C, _16, new Pair<>(_8, -1)),
    AND16           (0x3D, _16, new Pair<>(_16, -1)),
    OR16            (0x3E, _16, new Pair<>(_16, -1)),
    XOR16           (0x3F, _16, new Pair<>(_16, -1)),

    GREATER16       (0x40, _16, new Pair<>(_16, -2), new Pair<>(_8, 1)),
    LESS16          (0x41, _16, new Pair<>(_16, -2), new Pair<>(_8, 1)),
    GREATEREQ16     (0x42, _16, new Pair<>(_16, -2), new Pair<>(_8, 1)),
    LESSEQ16        (0x43, _16, new Pair<>(_16, -2), new Pair<>(_8, 1)),
    EQ16            (0x44, _16, new Pair<>(_16, -2), new Pair<>(_8, 1)),
    NOTEQ16         (0x45, _16, new Pair<>(_16, -2), new Pair<>(_8, 1)),

    //-----------------------------------------------------------

    PUSH32          (0x61, _32, new Pair<>(_32, 1)),
    DUP32           (0x62, _32, Generic.DUP, new Pair<>(_32, 1)),
    POP32           (0x63, _32, Generic.POP, new Pair<>(_32, -1)),
    STORE32         (0x64, _32, Generic.STORE, new Pair<>(_32, -1)),
    LOAD32          (0x65, _32, Generic.LOAD, new Pair<>(_32, 1)),

    ADD32           (0x66, _32, new Pair<>(_32, -1)),
    SUB32           (0x67, _32, new Pair<>(_32, -1)),
    MUL32           (0x68, _32, new Pair<>(_32, -1)),
    DIV32           (0x69, _32, new Pair<>(_32, -1)),
    MOD32           (0x6A, _32, new Pair<>(_32, -1)),

    SHIFTR32        (0x6B, _32, new Pair<>(_8, -1)),
    SHIFTL32        (0x6C, _32, new Pair<>(_8, -1)),
    AND32           (0x6D, _32, new Pair<>(_32, -1)),
    OR32            (0x6E, _32, new Pair<>(_32, -1)),
    XOR32           (0x6F, _32, new Pair<>(_32, -1)),

    GREATER32       (0x70, _32, new Pair<>(_32, -2), new Pair<>(_8, 1)),
    LESS32          (0x71, _32, new Pair<>(_32, -2), new Pair<>(_8, 1)),
    GREATEREQ32     (0x72, _32, new Pair<>(_32, -2), new Pair<>(_8, 1)),
    LESSEQ32        (0x73, _32, new Pair<>(_32, -2), new Pair<>(_8, 1)),
    EQ32            (0x74, _32, new Pair<>(_32, -2), new Pair<>(_8, 1)),
    NOTEQ32         (0x75, _32, new Pair<>(_32, -2), new Pair<>(_8, 1)),

    ADDF32          (0x76, _32, new Pair<>(_32, -1)),
    SUBF32          (0x77, _32, new Pair<>(_32, -1)),
    MULF32          (0x78, _32, new Pair<>(_32, -1)),
    DIVF32          (0x79, _32, new Pair<>(_32, -1)),
    POWF32          (0x7A, _32, new Pair<>(_32, -1)),

    GREATERF32      (0x7B, _32, new Pair<>(_32, -2), new Pair<>(_8, 1)),
    LESSF32         (0x7C, _32, new Pair<>(_32, -2), new Pair<>(_8, 1)),
    GREATEREQF32    (0x7D, _32, new Pair<>(_32, -2), new Pair<>(_8, 1)),
    LESSEQF32       (0x7E, _32, new Pair<>(_32, -2), new Pair<>(_8, 1)),

    //-----------------------------------------------------------

    PUSH64          (0x91, _64, new Pair<>(_64, 1)),
    DUP64           (0x92, _64, Generic.DUP, new Pair<>(_64, 1)),
    POP64           (0x93, _64, Generic.POP, new Pair<>(_64, -1)),
    STORE64         (0x94, _64, Generic.STORE, new Pair<>(_64, -1)),
    LOAD64          (0x95, _64, Generic.LOAD, new Pair<>(_64, 1)),

    ADD64           (0x96, _64, new Pair<>(_64, -1)),
    SUB64           (0x97, _64, new Pair<>(_64, -1)),
    MUL64           (0x98, _64, new Pair<>(_64, -1)),
    DIV64           (0x99, _64, new Pair<>(_64, -1)),
    MOD64           (0x9A, _64, new Pair<>(_64, -1)),

    SHIFTR64        (0x9B, _64, new Pair<>(_8, -1)),
    SHIFTL64        (0x9C, _64, new Pair<>(_8, -1)),
    AND64           (0x9D, _64, new Pair<>(_64, -1)),
    OR64            (0x9E, _64, new Pair<>(_64, -1)),
    XOR64           (0x9F, _64, new Pair<>(_64, -1)),

    GREATER64       (0xA0, _64, new Pair<>(_64, -2), new Pair<>(_8, 1)),
    LESS64          (0xA1, _64, new Pair<>(_64, -2), new Pair<>(_8, 1)),
    GREATEREQ64     (0xA2, _64, new Pair<>(_64, -2), new Pair<>(_8, 1)),
    LESSEQ64        (0xA3, _64, new Pair<>(_64, -2), new Pair<>(_8, 1)),
    EQ64            (0xA4, _64, new Pair<>(_64, -2), new Pair<>(_8, 1)),
    NOTEQ64         (0xA5, _64, new Pair<>(_64, -2), new Pair<>(_8, 1)),

    ADDF64          (0xA6, _64, new Pair<>(_64, -1)),
    SUBF64          (0xA7, _64, new Pair<>(_64, -1)),
    MULF64          (0xA8, _64, new Pair<>(_64, -1)),
    DIVF64          (0xA9, _64, new Pair<>(_64, -1)),
    POWF64          (0xAA, _64, new Pair<>(_64, -1)),

    GREATERF64      (0xAB, _64, new Pair<>(_64, -2), new Pair<>(_8, 1)),
    LESSF64         (0xAC, _64, new Pair<>(_64, -2), new Pair<>(_8, 1)),
    GREATEREQF64    (0xAD, _64, new Pair<>(_64, -2), new Pair<>(_8, 1)),
    LESSEQF64       (0xAE, _64, new Pair<>(_64, -2), new Pair<>(_8, 1)),

    PUSHFN          (0xB1, _64, new Pair<>(_64, 1)),
    CALL            (0xB2, _64, new Pair<>(_64, -1)),

    //---------------------------------------------------------------------

    JMP             (0xC1),
    RET             (0xC2),

    ;

    public enum Generic {
        DUP,
        POP,
        STORE,
        LOAD
    }

    public final byte code;
    private final LanguegVmValSize size;
    private final Generic generic;
    public final Map<LanguegVmValSize, Integer> stackSizeEffects = new EnumMap<>(LanguegVmValSize.class);

    @SafeVarargs
    Ops(int code_, LanguegVmValSize size_, Generic generic_, Pair<LanguegVmValSize, Integer>... stackSizeEffects_){
        code = (byte) code_;
        size = size_;
        generic = generic_;
        for (Pair<LanguegVmValSize, Integer> effect : stackSizeEffects_) {
            stackSizeEffects.put(effect.left, effect.right);
        }
    }

    @SafeVarargs
    Ops(int code_, LanguegVmValSize size_, Pair<LanguegVmValSize, Integer>... stackSizeEffects_){
        code = (byte) code_;
        size = size_;
        generic = null;
        for (Pair<LanguegVmValSize, Integer> effect : stackSizeEffects_) {
            stackSizeEffects.put(effect.left, effect.right);
        }
    }

    @SafeVarargs
    Ops(int code_, Pair<LanguegVmValSize, Integer>... stackSizeEffects_){
        code = (byte) code_;
        size = null;
        generic = null;
        for (Pair<LanguegVmValSize, Integer> effect : stackSizeEffects_) {
            stackSizeEffects.put(effect.left, effect.right);
        }

    }

    public static Ops ofGeneric(LanguegVmValSize size, Generic generic){
        for (Ops op : values()) {
            if(op.size == size && op.generic == generic) return op;
        }
        return NOP;
    }
}
