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

        tokToBinOpMap.put(TokenType.BitAnd, BitAnd);
        tokToBinOpMap.put(TokenType.BitOr, BitOr);
        tokToBinOpMap.put(TokenType.BitXOr, BitXOr);

        tokToBinOpMap.put(TokenType.Greater, Greater);
        tokToBinOpMap.put(TokenType.Less, Less);
        tokToBinOpMap.put(TokenType.NotEq, NotEq);

        tokToBinOpMap.put(TokenType.And, And);
        tokToBinOpMap.put(TokenType.Or, Or);
        tokToBinOpMap.put(TokenType.XOr, XOr);
    }
    public static BinOp fromTokenType(TokenType tok){
        return tokToBinOpMap.get(tok);
    }

    public static boolean isBinOp(TokenType tok){
        return tokToBinOpMap.containsKey(tok);
    }
}
