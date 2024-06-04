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
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.Plus),       (state) -> state.writeOp(Ops.ADD));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.Minus),      (state) -> state.writeOp(Ops.SUB));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.Mul),        (state) -> state.writeOp(Ops.MUL));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.Div),        (state) -> state.writeOp(Ops.DIV));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.Mod),        (state) -> state.writeOp(Ops.MOD));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.BitAnd),     (state) -> state.writeOp(Ops.AND));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.BitOr),      (state) -> state.writeOp(Ops.OR));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.BitXOr),     (state) -> state.writeOp(Ops.XOR));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.Greater),    (state) -> state.writeOp(Ops.GREATER));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.Less),       (state) -> state.writeOp(Ops.LESS));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.GreaterEq),  (state) -> state.writeOp(Ops.GREATEREQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.LessEq),     (state) -> state.writeOp(Ops.LESSEQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.Eq),         (state) -> state.writeOp(Ops.EQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, BinOp.NotEq),      (state) -> state.writeOp(Ops.NOTEQ));

        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.Plus),      (state) -> state.writeOp(Ops.ADD));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.Minus),     (state) -> state.writeOp(Ops.SUB));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.Mul),       (state) -> state.writeOp(Ops.MUL));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.Div),       (state) -> state.writeOp(Ops.DIV));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.Mod),       (state) -> state.writeOp(Ops.MOD));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.BitAnd),    (state) -> state.writeOp(Ops.AND));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.BitOr),     (state) -> state.writeOp(Ops.OR));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.BitXOr),    (state) -> state.writeOp(Ops.XOR));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.Greater),   (state) -> state.writeOp(Ops.GREATER));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.Less),      (state) -> state.writeOp(Ops.LESS));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.GreaterEq), (state) -> state.writeOp(Ops.GREATEREQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.LessEq),    (state) -> state.writeOp(Ops.LESSEQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.Eq),        (state) -> state.writeOp(Ops.EQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, BinOp.NotEq),     (state) -> state.writeOp(Ops.NOTEQ));

        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.Plus),      (state) -> state.writeOp(Ops.ADD));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.Minus),     (state) -> state.writeOp(Ops.SUB));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.Mul),       (state) -> state.writeOp(Ops.MUL));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.Div),       (state) -> state.writeOp(Ops.DIV));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.Mod),       (state) -> state.writeOp(Ops.MOD));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.BitAnd),    (state) -> state.writeOp(Ops.AND));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.BitOr),     (state) -> state.writeOp(Ops.OR));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.BitXOr),    (state) -> state.writeOp(Ops.XOR));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.Greater),   (state) -> state.writeOp(Ops.GREATER));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.Less),      (state) -> state.writeOp(Ops.LESS));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.GreaterEq), (state) -> state.writeOp(Ops.GREATEREQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.LessEq),    (state) -> state.writeOp(Ops.LESSEQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.Eq),        (state) -> state.writeOp(Ops.EQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, BinOp.NotEq),     (state) -> state.writeOp(Ops.NOTEQ));

        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.Plus),      (state) -> state.writeOp(Ops.ADD));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.Minus),     (state) -> state.writeOp(Ops.SUB));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.Mul),       (state) -> state.writeOp(Ops.MUL));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.Div),       (state) -> state.writeOp(Ops.DIV));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.Mod),       (state) -> state.writeOp(Ops.MOD));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.BitAnd),    (state) -> state.writeOp(Ops.AND));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.BitOr),     (state) -> state.writeOp(Ops.OR));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.BitXOr),    (state) -> state.writeOp(Ops.XOR));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.Greater),   (state) -> state.writeOp(Ops.GREATER));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.Less),      (state) -> state.writeOp(Ops.LESS));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.GreaterEq), (state) -> state.writeOp(Ops.GREATEREQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.LessEq),    (state) -> state.writeOp(Ops.LESSEQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.Eq),        (state) -> state.writeOp(Ops.EQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, BinOp.NotEq),     (state) -> state.writeOp(Ops.NOTEQ));

        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.Plus),       (state) -> state.writeOp(Ops.ADD));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.Minus),      (state) -> state.writeOp(Ops.SUB));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.Mul),        (state) -> state.writeOp(Ops.MUL));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.Div),        (state) -> state.writeOp(Ops.DIV));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.Mod),        (state) -> state.writeOp(Ops.MOD));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.BitAnd),     (state) -> state.writeOp(Ops.AND));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.BitOr),      (state) -> state.writeOp(Ops.OR));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.BitXOr),     (state) -> state.writeOp(Ops.XOR));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.Greater),    (state) -> state.writeOp(Ops.GREATER));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.Less),       (state) -> state.writeOp(Ops.LESS));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.GreaterEq),  (state) -> state.writeOp(Ops.GREATEREQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.LessEq),     (state) -> state.writeOp(Ops.LESSEQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.Eq),         (state) -> state.writeOp(Ops.EQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, BinOp.NotEq),      (state) -> state.writeOp(Ops.NOTEQ));

        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.Plus),      (state) -> state.writeOp(Ops.ADD));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.Minus),     (state) -> state.writeOp(Ops.SUB));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.Mul),       (state) -> state.writeOp(Ops.MUL));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.Div),       (state) -> state.writeOp(Ops.DIV));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.Mod),       (state) -> state.writeOp(Ops.MOD));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.BitAnd),    (state) -> state.writeOp(Ops.AND));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.BitOr),     (state) -> state.writeOp(Ops.OR));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.BitXOr),    (state) -> state.writeOp(Ops.XOR));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.Greater),   (state) -> state.writeOp(Ops.GREATER));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.Less),      (state) -> state.writeOp(Ops.LESS));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.GreaterEq), (state) -> state.writeOp(Ops.GREATEREQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.LessEq),    (state) -> state.writeOp(Ops.LESSEQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.Eq),        (state) -> state.writeOp(Ops.EQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, BinOp.NotEq),     (state) -> state.writeOp(Ops.NOTEQ));

        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.Plus),      (state) -> state.writeOp(Ops.ADD));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.Minus),     (state) -> state.writeOp(Ops.SUB));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.Mul),       (state) -> state.writeOp(Ops.MUL));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.Div),       (state) -> state.writeOp(Ops.DIV));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.Mod),       (state) -> state.writeOp(Ops.MOD));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.BitAnd),    (state) -> state.writeOp(Ops.AND));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.BitOr),     (state) -> state.writeOp(Ops.OR));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.BitXOr),    (state) -> state.writeOp(Ops.XOR));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.Greater),   (state) -> state.writeOp(Ops.GREATER));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.Less),      (state) -> state.writeOp(Ops.LESS));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.GreaterEq), (state) -> state.writeOp(Ops.GREATEREQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.LessEq),    (state) -> state.writeOp(Ops.LESSEQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.Eq),        (state) -> state.writeOp(Ops.EQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, BinOp.NotEq),     (state) -> state.writeOp(Ops.NOTEQ));

        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.Plus),      (state) -> state.writeOp(Ops.ADD));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.Minus),     (state) -> state.writeOp(Ops.SUB));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.Mul),       (state) -> state.writeOp(Ops.MUL));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.Div),       (state) -> state.writeOp(Ops.DIV));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.Mod),       (state) -> state.writeOp(Ops.MOD));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.BitAnd),    (state) -> state.writeOp(Ops.AND));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.BitOr),     (state) -> state.writeOp(Ops.OR));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.BitXOr),    (state) -> state.writeOp(Ops.XOR));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.Greater),   (state) -> state.writeOp(Ops.GREATER));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.Less),      (state) -> state.writeOp(Ops.LESS));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.GreaterEq), (state) -> state.writeOp(Ops.GREATEREQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.LessEq),    (state) -> state.writeOp(Ops.LESSEQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.Eq),        (state) -> state.writeOp(Ops.EQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, BinOp.NotEq),     (state) -> state.writeOp(Ops.NOTEQ));

        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.Plus),      (state) -> state.writeOp(Ops.ADDF32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.Minus),     (state) -> state.writeOp(Ops.SUBF32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.Mul),       (state) -> state.writeOp(Ops.MULF32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.Div),       (state) -> state.writeOp(Ops.DIVF32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.Pow),       (state) -> state.writeOp(Ops.POWF32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.Greater),   (state) -> state.writeOp(Ops.GREATERF32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.Less),      (state) -> state.writeOp(Ops.LESSF32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.GreaterEq), (state) -> state.writeOp(Ops.GREATEREQF32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.LessEq),    (state) -> state.writeOp(Ops.LESSEQF32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.Eq),        (state) -> state.writeOp(Ops.EQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, BinOp.NotEq),     (state) -> state.writeOp(Ops.NOTEQ));

        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.Plus),      (state) -> state.writeOp(Ops.ADDF64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.Minus),     (state) -> state.writeOp(Ops.SUBF64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.Mul),       (state) -> state.writeOp(Ops.MULF64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.Div),       (state) -> state.writeOp(Ops.DIVF64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.Pow),       (state) -> state.writeOp(Ops.POWF64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.Greater),   (state) -> state.writeOp(Ops.GREATERF64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.Less),      (state) -> state.writeOp(Ops.LESSF64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.GreaterEq), (state) -> state.writeOp(Ops.GREATEREQF64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.LessEq),    (state) -> state.writeOp(Ops.LESSEQF64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.Eq),        (state) -> state.writeOp(Ops.EQ));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, BinOp.NotEq),     (state) -> state.writeOp(Ops.NOTEQ));
    }

    @Override
    public BinOpCodeGen binOpCodeGen(BinOp op, Type left, Type right) {
        if(left instanceof PrimitiveType lt && right instanceof PrimitiveType rt && lt == rt){
            return primitiveBinOps.get(new Pair<>(lt, op));
        }
        throw new Error();
    }
}
