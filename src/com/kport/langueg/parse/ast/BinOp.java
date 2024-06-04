package com.kport.langueg.parse.ast;

import com.kport.langueg.lex.TokenType;

import java.util.EnumMap;

public enum BinOp {
    Plus,
    Minus,
    Mul,
    Div,
    Mod,
    Pow,

    ShiftR,
    ShiftL,

    BitAnd,
    BitOr,
    BitXOr,

    Greater,
    Less,
    GreaterEq,
    LessEq,
    Eq,
    NotEq,

    And,
    Or,
    XOr;

    private static final EnumMap<TokenType, BinOp> tokToBinOpMap = new EnumMap<>(TokenType.class);
    static {
        tokToBinOpMap.put(TokenType.Plus, Plus);
        tokToBinOpMap.put(TokenType.Minus, Minus);
        tokToBinOpMap.put(TokenType.Mul, Mul);
        tokToBinOpMap.put(TokenType.Div, Div);
        tokToBinOpMap.put(TokenType.Mod, Mod);
        tokToBinOpMap.put(TokenType.Pow, Pow);

        tokToBinOpMap.put(TokenType.ShiftR, ShiftR);
        tokToBinOpMap.put(TokenType.ShiftL, ShiftL);

        tokToBinOpMap.put(TokenType.BitAnd, BitAnd);
        tokToBinOpMap.put(TokenType.BitOr, BitOr);
        tokToBinOpMap.put(TokenType.BitXOr, BitXOr);

        tokToBinOpMap.put(TokenType.Greater, Greater);
        tokToBinOpMap.put(TokenType.Less, Less);
        tokToBinOpMap.put(TokenType.GreaterEq, GreaterEq);
        tokToBinOpMap.put(TokenType.LessEq, LessEq);
        tokToBinOpMap.put(TokenType.Eq, Eq);
        tokToBinOpMap.put(TokenType.NotEq, NotEq);

        tokToBinOpMap.put(TokenType.And, And);
        tokToBinOpMap.put(TokenType.Or, Or);
        tokToBinOpMap.put(TokenType.XOr, XOr);
    }
    public static BinOp fromTokenType(TokenType tok){
        return tokToBinOpMap.get(tok);
    }

    private static final EnumMap<CompoundAssign, BinOp> compoundAssignBinOpMap = new EnumMap<>(CompoundAssign.class);
    static {
        compoundAssignBinOpMap.put(CompoundAssign.PlusAssign, Plus);
        compoundAssignBinOpMap.put(CompoundAssign.MinusAssign, Minus);
        compoundAssignBinOpMap.put(CompoundAssign.MulAssign, Mul);
        compoundAssignBinOpMap.put(CompoundAssign.DivAssign, Div);
        compoundAssignBinOpMap.put(CompoundAssign.ModAssign, Mod);
        compoundAssignBinOpMap.put(CompoundAssign.PowAssign, Pow);

        compoundAssignBinOpMap.put(CompoundAssign.ShiftRAssign, ShiftR);
        compoundAssignBinOpMap.put(CompoundAssign.ShiftLAssign, ShiftL);

        compoundAssignBinOpMap.put(CompoundAssign.BitAndAssign, BitAnd);
        compoundAssignBinOpMap.put(CompoundAssign.BitOrAssign, BitOr);
        compoundAssignBinOpMap.put(CompoundAssign.BitXOrAssign, BitXOr);

        compoundAssignBinOpMap.put(CompoundAssign.AndAssign, And);
        compoundAssignBinOpMap.put(CompoundAssign.OrAssign, Or);
        compoundAssignBinOpMap.put(CompoundAssign.XOrAssign, XOr);
    }
    public static BinOp fromCompoundAssign(CompoundAssign compoundAssign){
        return compoundAssignBinOpMap.get(compoundAssign);
    }

    public static boolean isBinOp(TokenType tok){
        return tokToBinOpMap.containsKey(tok);
    }
}
