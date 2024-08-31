package com.kport.langueg.codeGen.languegVmCodeGen.op;

import com.kport.langueg.codeGen.languegVmCodeGen.Ops;
import com.kport.langueg.parse.ast.BinOp;
import com.kport.langueg.typeCheck.types.PrimitiveType;
import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class LanguegVmOpCodeGen implements OpCodeGenSupplier{
    private static final Map<Pair<PrimitiveType, BinOp>, BinOpCodeGen> primitiveBinOps = new HashMap<>();
    static {
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.Plus),       (state, to, op1, op2) -> state.writeOp(Ops.ADD8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.Minus),      (state, to, op1, op2) -> state.writeOp(Ops.SUB8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.Mul),        (state, to, op1, op2) -> state.writeOp(Ops.MUL8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.Div),        (state, to, op1, op2) -> state.writeOp(Ops.DIV8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.Mod),        (state, to, op1, op2) -> state.writeOp(Ops.MOD8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.BitAnd),     (state, to, op1, op2) -> state.writeOp(Ops.AND8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.BitOr),      (state, to, op1, op2) -> state.writeOp(Ops.OR8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.BitXOr),     (state, to, op1, op2) -> state.writeOp(Ops.XOR8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.Greater),    (state, to, op1, op2) -> state.writeOp(Ops.GREATER8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.Less),       (state, to, op1, op2) -> state.writeOp(Ops.LESS8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.GreaterEq),  (state, to, op1, op2) -> state.writeOp(Ops.GREATEREQ8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.LessEq),     (state, to, op1, op2) -> state.writeOp(Ops.LESSEQ8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.Eq),         (state, to, op1, op2) -> state.writeOp(Ops.EQ8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.NotEq),      (state, to, op1, op2) -> state.writeOp(Ops.NOTEQ8, to, op1, op2));

        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.Plus),      (state, to, op1, op2) -> state.writeOp(Ops.ADD16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.Minus),     (state, to, op1, op2) -> state.writeOp(Ops.SUB16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.Mul),       (state, to, op1, op2) -> state.writeOp(Ops.MUL16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.Div),       (state, to, op1, op2) -> state.writeOp(Ops.DIV16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.Mod),       (state, to, op1, op2) -> state.writeOp(Ops.MOD16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.BitAnd),    (state, to, op1, op2) -> state.writeOp(Ops.AND16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.BitOr),     (state, to, op1, op2) -> state.writeOp(Ops.OR16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.BitXOr),    (state, to, op1, op2) -> state.writeOp(Ops.XOR16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.Greater),   (state, to, op1, op2) -> state.writeOp(Ops.GREATER16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.Less),      (state, to, op1, op2) -> state.writeOp(Ops.LESS16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.GreaterEq), (state, to, op1, op2) -> state.writeOp(Ops.GREATEREQ16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.LessEq),    (state, to, op1, op2) -> state.writeOp(Ops.LESSEQ16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.Eq),        (state, to, op1, op2) -> state.writeOp(Ops.EQ16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.NotEq),     (state, to, op1, op2) -> state.writeOp(Ops.NOTEQ16, to, op1, op2));

        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.Plus),      (state, to, op1, op2) -> state.writeOp(Ops.ADD32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.Minus),     (state, to, op1, op2) -> state.writeOp(Ops.SUB32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.Mul),       (state, to, op1, op2) -> state.writeOp(Ops.MUL32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.Div),       (state, to, op1, op2) -> state.writeOp(Ops.DIV32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.Mod),       (state, to, op1, op2) -> state.writeOp(Ops.MOD32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.BitAnd),    (state, to, op1, op2) -> state.writeOp(Ops.AND32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.BitOr),     (state, to, op1, op2) -> state.writeOp(Ops.OR32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.BitXOr),    (state, to, op1, op2) -> state.writeOp(Ops.XOR32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.Greater),   (state, to, op1, op2) -> state.writeOp(Ops.GREATER32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.Less),      (state, to, op1, op2) -> state.writeOp(Ops.LESS32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.GreaterEq), (state, to, op1, op2) -> state.writeOp(Ops.GREATEREQ32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.LessEq),    (state, to, op1, op2) -> state.writeOp(Ops.LESSEQ32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.Eq),        (state, to, op1, op2) -> state.writeOp(Ops.EQ32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.NotEq),     (state, to, op1, op2) -> state.writeOp(Ops.NOTEQ32, to, op1, op2));

        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.Plus),      (state, to, op1, op2) -> state.writeOp(Ops.ADD64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.Minus),     (state, to, op1, op2) -> state.writeOp(Ops.SUB64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.Mul),       (state, to, op1, op2) -> state.writeOp(Ops.MUL64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.Div),       (state, to, op1, op2) -> state.writeOp(Ops.DIV64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.Mod),       (state, to, op1, op2) -> state.writeOp(Ops.MOD64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.BitAnd),    (state, to, op1, op2) -> state.writeOp(Ops.AND64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.BitOr),     (state, to, op1, op2) -> state.writeOp(Ops.OR64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.BitXOr),    (state, to, op1, op2) -> state.writeOp(Ops.XOR64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.Greater),   (state, to, op1, op2) -> state.writeOp(Ops.GREATER64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.Less),      (state, to, op1, op2) -> state.writeOp(Ops.LESS64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.GreaterEq), (state, to, op1, op2) -> state.writeOp(Ops.GREATEREQ64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.LessEq),    (state, to, op1, op2) -> state.writeOp(Ops.LESSEQ64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.Eq),        (state, to, op1, op2) -> state.writeOp(Ops.EQ64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.NotEq),     (state, to, op1, op2) -> state.writeOp(Ops.NOTEQ64, to, op1, op2));

        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.Plus),       (state, to, op1, op2) -> state.writeOp(Ops.ADD8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.Minus),      (state, to, op1, op2) -> state.writeOp(Ops.SUB8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.Mul),        (state, to, op1, op2) -> state.writeOp(Ops.MUL8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.Div),        (state, to, op1, op2) -> state.writeOp(Ops.DIV8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.Mod),        (state, to, op1, op2) -> state.writeOp(Ops.MOD8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.BitAnd),     (state, to, op1, op2) -> state.writeOp(Ops.AND8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.BitOr),      (state, to, op1, op2) -> state.writeOp(Ops.OR8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.BitXOr),     (state, to, op1, op2) -> state.writeOp(Ops.XOR8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.Greater),    (state, to, op1, op2) -> state.writeOp(Ops.GREATER8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.Less),       (state, to, op1, op2) -> state.writeOp(Ops.LESS8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.GreaterEq),  (state, to, op1, op2) -> state.writeOp(Ops.GREATEREQ8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.LessEq),     (state, to, op1, op2) -> state.writeOp(Ops.LESSEQ8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.Eq),         (state, to, op1, op2) -> state.writeOp(Ops.EQ8, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.NotEq),      (state, to, op1, op2) -> state.writeOp(Ops.NOTEQ8, to, op1, op2));

        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.Plus),      (state, to, op1, op2) -> state.writeOp(Ops.ADD16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.Minus),     (state, to, op1, op2) -> state.writeOp(Ops.SUB16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.Mul),       (state, to, op1, op2) -> state.writeOp(Ops.MUL16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.Div),       (state, to, op1, op2) -> state.writeOp(Ops.DIV16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.Mod),       (state, to, op1, op2) -> state.writeOp(Ops.MOD16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.BitAnd),    (state, to, op1, op2) -> state.writeOp(Ops.AND16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.BitOr),     (state, to, op1, op2) -> state.writeOp(Ops.OR16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.BitXOr),    (state, to, op1, op2) -> state.writeOp(Ops.XOR16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.Greater),   (state, to, op1, op2) -> state.writeOp(Ops.GREATER16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.Less),      (state, to, op1, op2) -> state.writeOp(Ops.LESS16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.GreaterEq), (state, to, op1, op2) -> state.writeOp(Ops.GREATEREQ16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.LessEq),    (state, to, op1, op2) -> state.writeOp(Ops.LESSEQ16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.Eq),        (state, to, op1, op2) -> state.writeOp(Ops.EQ16, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.NotEq),     (state, to, op1, op2) -> state.writeOp(Ops.NOTEQ16, to, op1, op2));

        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.Plus),      (state, to, op1, op2) -> state.writeOp(Ops.ADD32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.Minus),     (state, to, op1, op2) -> state.writeOp(Ops.SUB32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.Mul),       (state, to, op1, op2) -> state.writeOp(Ops.MUL32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.Div),       (state, to, op1, op2) -> state.writeOp(Ops.DIV32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.Mod),       (state, to, op1, op2) -> state.writeOp(Ops.MOD32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.BitAnd),    (state, to, op1, op2) -> state.writeOp(Ops.AND32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.BitOr),     (state, to, op1, op2) -> state.writeOp(Ops.OR32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.BitXOr),    (state, to, op1, op2) -> state.writeOp(Ops.XOR32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.Greater),   (state, to, op1, op2) -> state.writeOp(Ops.GREATER32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.Less),      (state, to, op1, op2) -> state.writeOp(Ops.LESS32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.GreaterEq), (state, to, op1, op2) -> state.writeOp(Ops.GREATEREQ32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.LessEq),    (state, to, op1, op2) -> state.writeOp(Ops.LESSEQ32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.Eq),        (state, to, op1, op2) -> state.writeOp(Ops.EQ32, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.NotEq),     (state, to, op1, op2) -> state.writeOp(Ops.NOTEQ32, to, op1, op2));

        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.Plus),      (state, to, op1, op2) -> state.writeOp(Ops.ADD64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.Minus),     (state, to, op1, op2) -> state.writeOp(Ops.SUB64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.Mul),       (state, to, op1, op2) -> state.writeOp(Ops.MUL64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.Div),       (state, to, op1, op2) -> state.writeOp(Ops.DIV64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.Mod),       (state, to, op1, op2) -> state.writeOp(Ops.MOD64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.BitAnd),    (state, to, op1, op2) -> state.writeOp(Ops.AND64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.BitOr),     (state, to, op1, op2) -> state.writeOp(Ops.OR64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.BitXOr),    (state, to, op1, op2) -> state.writeOp(Ops.XOR64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.Greater),   (state, to, op1, op2) -> state.writeOp(Ops.GREATER64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.Less),      (state, to, op1, op2) -> state.writeOp(Ops.LESS64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.GreaterEq), (state, to, op1, op2) -> state.writeOp(Ops.GREATEREQ64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.LessEq),    (state, to, op1, op2) -> state.writeOp(Ops.LESSEQ64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.Eq),        (state, to, op1, op2) -> state.writeOp(Ops.EQ64, to, op1, op2));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.NotEq),     (state, to, op1, op2) -> state.writeOp(Ops.NOTEQ64, to, op1, op2));

        //primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.Plus),      (state, to, op1, op2) -> state.writeOp(Ops.ADDF32));
        //primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.Minus),     (state, to, op1, op2) -> state.writeOp(Ops.SUBF32));
        //primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.Mul),       (state, to, op1, op2) -> state.writeOp(Ops.MULF32));
        //primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.Div),       (state, to, op1, op2) -> state.writeOp(Ops.DIVF32));
        //primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.Pow),       (state, to, op1, op2) -> state.writeOp(Ops.POWF32));
        //primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.Greater),   (state, to, op1, op2) -> state.writeOp(Ops.GREATERF32));
        //primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.Less),      (state, to, op1, op2) -> state.writeOp(Ops.LESSF32));
        //primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.GreaterEq), (state, to, op1, op2) -> state.writeOp(Ops.GREATEREQF32));
        //primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.LessEq),    (state, to, op1, op2) -> state.writeOp(Ops.LESSEQF32));
        //primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.Eq),        (state, to, op1, op2) -> state.writeOp(Ops.EQ));
        //primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.NotEq),     (state, to, op1, op2) -> state.writeOp(Ops.NOTEQ));

        //primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.Plus),      (state, to, op1, op2) -> state.writeOp(Ops.ADDF64));
        //primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.Minus),     (state, to, op1, op2) -> state.writeOp(Ops.SUBF64));
        //primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.Mul),       (state, to, op1, op2) -> state.writeOp(Ops.MULF64));
        //primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.Div),       (state, to, op1, op2) -> state.writeOp(Ops.DIVF64));
        //primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.Pow),       (state, to, op1, op2) -> state.writeOp(Ops.POWF64));
        //primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.Greater),   (state, to, op1, op2) -> state.writeOp(Ops.GREATERF64));
        //primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.Less),      (state, to, op1, op2) -> state.writeOp(Ops.LESSF64));
        //primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.GreaterEq), (state, to, op1, op2) -> state.writeOp(Ops.GREATEREQF64));
        //primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.LessEq),    (state, to, op1, op2) -> state.writeOp(Ops.LESSEQF64));
        //primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.Eq),        (state, to, op1, op2) -> state.writeOp(Ops.EQ));
        //primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.NotEq),     (state, to, op1, op2) -> state.writeOp(Ops.NOTEQ));
    }

    @Override
    public BinOpCodeGen binOpCodeGen(BinOp op, Type left, Type right) {
        if(left instanceof PrimitiveType lt && right instanceof PrimitiveType rt && lt == rt){
            return primitiveBinOps.get(new Pair<>(lt, op));
        }
        throw new Error();
    }
}
