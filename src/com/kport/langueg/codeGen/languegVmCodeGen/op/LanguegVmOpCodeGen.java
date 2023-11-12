package com.kport.langueg.codeGen.languegVmCodeGen.op;

import com.kport.langueg.codeGen.languegVmCodeGen.LanguegVmCodeGenerator;
import com.kport.langueg.codeGen.languegVmCodeGen.Ops;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.typeCheck.types.PrimitiveType;
import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class LanguegVmOpCodeGen implements OpCodeGenSupplier{
    private static final Map<Pair<PrimitiveType, TokenType>, BinOpCodeGen> primitiveBinOps = new HashMap<>();
    static {
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, TokenType.Plus), (binOp, state) -> state.writeOp(Ops.ADD8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, TokenType.Minus), (binOp, state) -> state.writeOp(Ops.SUB8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, TokenType.Mul), (binOp, state) -> state.writeOp(Ops.MUL8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, TokenType.Div), (binOp, state) -> state.writeOp(Ops.DIV8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, TokenType.Mod), (binOp, state) -> state.writeOp(Ops.MOD8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, TokenType.BAnd), (binOp, state) -> state.writeOp(Ops.AND8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, TokenType.BOr), (binOp, state) -> state.writeOp(Ops.OR8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, TokenType.BXOr), (binOp, state) -> state.writeOp(Ops.XOR8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, TokenType.Greater), (binOp, state) -> state.writeOp(Ops.GREATER8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, TokenType.Less), (binOp, state) -> state.writeOp(Ops.LESS8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, TokenType.GreaterEq), (binOp, state) -> state.writeOp(Ops.GREATEREQ8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, TokenType.LessEq), (binOp, state) -> state.writeOp(Ops.LESSEQ8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, TokenType.Eq), (binOp, state) -> state.writeOp(Ops.EQ8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U8, TokenType.NotEq), (binOp, state) -> state.writeOp(Ops.NOTEQ8));

        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, TokenType.Plus), (binOp, state) -> state.writeOp(Ops.ADD16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, TokenType.Minus), (binOp, state) -> state.writeOp(Ops.SUB16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, TokenType.Mul), (binOp, state) -> state.writeOp(Ops.MUL16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, TokenType.Div), (binOp, state) -> state.writeOp(Ops.DIV16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, TokenType.Mod), (binOp, state) -> state.writeOp(Ops.MOD16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, TokenType.BAnd), (binOp, state) -> state.writeOp(Ops.AND16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, TokenType.BOr), (binOp, state) -> state.writeOp(Ops.OR16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, TokenType.BXOr), (binOp, state) -> state.writeOp(Ops.XOR16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, TokenType.Greater), (binOp, state) -> state.writeOp(Ops.GREATER16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, TokenType.Less), (binOp, state) -> state.writeOp(Ops.LESS16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, TokenType.GreaterEq), (binOp, state) -> state.writeOp(Ops.GREATEREQ16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, TokenType.LessEq), (binOp, state) -> state.writeOp(Ops.LESSEQ16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, TokenType.Eq), (binOp, state) -> state.writeOp(Ops.EQ16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U16, TokenType.NotEq), (binOp, state) -> state.writeOp(Ops.NOTEQ16));

        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, TokenType.Plus), (binOp, state) -> state.writeOp(Ops.ADD32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, TokenType.Minus), (binOp, state) -> state.writeOp(Ops.SUB32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, TokenType.Mul), (binOp, state) -> state.writeOp(Ops.MUL32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, TokenType.Div), (binOp, state) -> state.writeOp(Ops.DIV32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, TokenType.Mod), (binOp, state) -> state.writeOp(Ops.MOD32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, TokenType.BAnd), (binOp, state) -> state.writeOp(Ops.AND32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, TokenType.BOr), (binOp, state) -> state.writeOp(Ops.OR32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, TokenType.BXOr), (binOp, state) -> state.writeOp(Ops.XOR32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, TokenType.Greater), (binOp, state) -> state.writeOp(Ops.GREATER32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, TokenType.Less), (binOp, state) -> state.writeOp(Ops.LESS32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, TokenType.GreaterEq), (binOp, state) -> state.writeOp(Ops.GREATEREQ32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, TokenType.LessEq), (binOp, state) -> state.writeOp(Ops.LESSEQ32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, TokenType.Eq), (binOp, state) -> state.writeOp(Ops.EQ32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U32, TokenType.NotEq), (binOp, state) -> state.writeOp(Ops.NOTEQ32));

        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, TokenType.Plus), (binOp, state) -> state.writeOp(Ops.ADD64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, TokenType.Minus), (binOp, state) -> state.writeOp(Ops.SUB64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, TokenType.Mul), (binOp, state) -> state.writeOp(Ops.MUL64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, TokenType.Div), (binOp, state) -> state.writeOp(Ops.DIV64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, TokenType.Mod), (binOp, state) -> state.writeOp(Ops.MOD64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, TokenType.BAnd), (binOp, state) -> state.writeOp(Ops.AND64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, TokenType.BOr), (binOp, state) -> state.writeOp(Ops.OR64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, TokenType.BXOr), (binOp, state) -> state.writeOp(Ops.XOR64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, TokenType.Greater), (binOp, state) -> state.writeOp(Ops.GREATER64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, TokenType.Less), (binOp, state) -> state.writeOp(Ops.LESS64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, TokenType.GreaterEq), (binOp, state) -> state.writeOp(Ops.GREATEREQ64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, TokenType.LessEq), (binOp, state) -> state.writeOp(Ops.LESSEQ64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, TokenType.Eq), (binOp, state) -> state.writeOp(Ops.EQ64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.U64, TokenType.NotEq), (binOp, state) -> state.writeOp(Ops.NOTEQ64));

        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, TokenType.Plus), (binOp, state) -> state.writeOp(Ops.ADD8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, TokenType.Minus), (binOp, state) -> state.writeOp(Ops.SUB8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, TokenType.Mul), (binOp, state) -> state.writeOp(Ops.MUL8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, TokenType.Div), (binOp, state) -> state.writeOp(Ops.DIV8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, TokenType.Mod), (binOp, state) -> state.writeOp(Ops.MOD8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, TokenType.BAnd), (binOp, state) -> state.writeOp(Ops.AND8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, TokenType.BOr), (binOp, state) -> state.writeOp(Ops.OR8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, TokenType.BXOr), (binOp, state) -> state.writeOp(Ops.XOR8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, TokenType.Greater), (binOp, state) -> state.writeOp(Ops.GREATER8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, TokenType.Less), (binOp, state) -> state.writeOp(Ops.LESS8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, TokenType.GreaterEq), (binOp, state) -> state.writeOp(Ops.GREATEREQ8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, TokenType.LessEq), (binOp, state) -> state.writeOp(Ops.LESSEQ8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, TokenType.Eq), (binOp, state) -> state.writeOp(Ops.EQ8));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I8, TokenType.NotEq), (binOp, state) -> state.writeOp(Ops.NOTEQ8));

        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, TokenType.Plus), (binOp, state) -> state.writeOp(Ops.ADD16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, TokenType.Minus), (binOp, state) -> state.writeOp(Ops.SUB16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, TokenType.Mul), (binOp, state) -> state.writeOp(Ops.MUL16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, TokenType.Div), (binOp, state) -> state.writeOp(Ops.DIV16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, TokenType.Mod), (binOp, state) -> state.writeOp(Ops.MOD16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, TokenType.BAnd), (binOp, state) -> state.writeOp(Ops.AND16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, TokenType.BOr), (binOp, state) -> state.writeOp(Ops.OR16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, TokenType.BXOr), (binOp, state) -> state.writeOp(Ops.XOR16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, TokenType.Greater), (binOp, state) -> state.writeOp(Ops.GREATER16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, TokenType.Less), (binOp, state) -> state.writeOp(Ops.LESS16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, TokenType.GreaterEq), (binOp, state) -> state.writeOp(Ops.GREATEREQ16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, TokenType.LessEq), (binOp, state) -> state.writeOp(Ops.LESSEQ16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, TokenType.Eq), (binOp, state) -> state.writeOp(Ops.EQ16));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I16, TokenType.NotEq), (binOp, state) -> state.writeOp(Ops.NOTEQ16));

        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, TokenType.Plus), (binOp, state) -> state.writeOp(Ops.ADD32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, TokenType.Minus), (binOp, state) -> state.writeOp(Ops.SUB32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, TokenType.Mul), (binOp, state) -> state.writeOp(Ops.MUL32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, TokenType.Div), (binOp, state) -> state.writeOp(Ops.DIV32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, TokenType.Mod), (binOp, state) -> state.writeOp(Ops.MOD32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, TokenType.BAnd), (binOp, state) -> state.writeOp(Ops.AND32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, TokenType.BOr), (binOp, state) -> state.writeOp(Ops.OR32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, TokenType.BXOr), (binOp, state) -> state.writeOp(Ops.XOR32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, TokenType.Greater), (binOp, state) -> state.writeOp(Ops.GREATER32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, TokenType.Less), (binOp, state) -> state.writeOp(Ops.LESS32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, TokenType.GreaterEq), (binOp, state) -> state.writeOp(Ops.GREATEREQ32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, TokenType.LessEq), (binOp, state) -> state.writeOp(Ops.LESSEQ32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, TokenType.Eq), (binOp, state) -> state.writeOp(Ops.EQ32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I32, TokenType.NotEq), (binOp, state) -> state.writeOp(Ops.NOTEQ32));

        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, TokenType.Plus), (binOp, state) -> state.writeOp(Ops.ADD64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, TokenType.Minus), (binOp, state) -> state.writeOp(Ops.SUB64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, TokenType.Mul), (binOp, state) -> state.writeOp(Ops.MUL64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, TokenType.Div), (binOp, state) -> state.writeOp(Ops.DIV64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, TokenType.Mod), (binOp, state) -> state.writeOp(Ops.MOD64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, TokenType.BAnd), (binOp, state) -> state.writeOp(Ops.AND64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, TokenType.BOr), (binOp, state) -> state.writeOp(Ops.OR64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, TokenType.BXOr), (binOp, state) -> state.writeOp(Ops.XOR64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, TokenType.Greater), (binOp, state) -> state.writeOp(Ops.GREATER64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, TokenType.Less), (binOp, state) -> state.writeOp(Ops.LESS64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, TokenType.GreaterEq), (binOp, state) -> state.writeOp(Ops.GREATEREQ64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, TokenType.LessEq), (binOp, state) -> state.writeOp(Ops.LESSEQ64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, TokenType.Eq), (binOp, state) -> state.writeOp(Ops.EQ64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.I64, TokenType.NotEq), (binOp, state) -> state.writeOp(Ops.NOTEQ64));

        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, TokenType.Plus), (binOp, state) -> state.writeOp(Ops.ADDF32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, TokenType.Minus), (binOp, state) -> state.writeOp(Ops.SUBF32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, TokenType.Mul), (binOp, state) -> state.writeOp(Ops.MULF32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, TokenType.Div), (binOp, state) -> state.writeOp(Ops.DIVF32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, TokenType.Pow), (binOp, state) -> state.writeOp(Ops.POWF32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, TokenType.Greater), (binOp, state) -> state.writeOp(Ops.GREATERF32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, TokenType.Less), (binOp, state) -> state.writeOp(Ops.LESSF32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, TokenType.GreaterEq), (binOp, state) -> state.writeOp(Ops.GREATEREQF32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, TokenType.LessEq), (binOp, state) -> state.writeOp(Ops.LESSEQF32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, TokenType.Eq), (binOp, state) -> state.writeOp(Ops.EQ32));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F32, TokenType.NotEq), (binOp, state) -> state.writeOp(Ops.NOTEQ32));

        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, TokenType.Plus), (binOp, state) -> state.writeOp(Ops.ADDF64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, TokenType.Minus), (binOp, state) -> state.writeOp(Ops.SUBF64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, TokenType.Mul), (binOp, state) -> state.writeOp(Ops.MULF64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, TokenType.Div), (binOp, state) -> state.writeOp(Ops.DIVF64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, TokenType.Pow), (binOp, state) -> state.writeOp(Ops.POWF64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, TokenType.Greater), (binOp, state) -> state.writeOp(Ops.GREATERF64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, TokenType.Less), (binOp, state) -> state.writeOp(Ops.LESSF64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, TokenType.GreaterEq), (binOp, state) -> state.writeOp(Ops.GREATEREQF64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, TokenType.LessEq), (binOp, state) -> state.writeOp(Ops.LESSEQF64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, TokenType.Eq), (binOp, state) -> state.writeOp(Ops.EQ64));
        primitiveBinOps.put(new Pair<>(PrimitiveType.F64, TokenType.NotEq), (binOp, state) -> state.writeOp(Ops.NOTEQ64));
    }

    @Override
    public BinOpCodeGen binOpCodeGen(TokenType op, Type left, Type right) {
        if(left instanceof PrimitiveType lt && right instanceof PrimitiveType rt && lt == rt){
            return primitiveBinOps.get(new Pair<>(lt, op));
        }
        throw new Error();
    }
}
