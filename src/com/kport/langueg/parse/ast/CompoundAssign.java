package com.kport.langueg.parse.ast;

import com.kport.langueg.lex.TokenType;

import java.util.EnumMap;

public enum CompoundAssign {
    PlusAssign,
    MinusAssign,
    MulAssign,
    DivAssign,
    ModAssign,
    PowAssign,

    ShiftRAssign,
    ShiftLAssign,

    BitAndAssign,
    BitOrAssign,
    BitXOrAssign,

    AndAssign,
    OrAssign,
    XOrAssign;

    private static final EnumMap<TokenType, CompoundAssign> tokToCompoundAssignMap = new EnumMap<>(TokenType.class);
    static {
        tokToCompoundAssignMap.put(TokenType.PlusAssign, PlusAssign);
        tokToCompoundAssignMap.put(TokenType.MinusAssign, MinusAssign);
        tokToCompoundAssignMap.put(TokenType.MulAssign, MulAssign);
        tokToCompoundAssignMap.put(TokenType.DivAssign, DivAssign);
        tokToCompoundAssignMap.put(TokenType.ModAssign, ModAssign);
        tokToCompoundAssignMap.put(TokenType.PowAssign, PowAssign);

        tokToCompoundAssignMap.put(TokenType.ShiftRAssign, ShiftRAssign);
        tokToCompoundAssignMap.put(TokenType.ShiftLAssign, ShiftLAssign);

        tokToCompoundAssignMap.put(TokenType.AndAssign, AndAssign);
        tokToCompoundAssignMap.put(TokenType.OrAssign, OrAssign);
        tokToCompoundAssignMap.put(TokenType.XOrAssign, XOrAssign);

        tokToCompoundAssignMap.put(TokenType.BitAndAssign, BitAndAssign);
        tokToCompoundAssignMap.put(TokenType.BitOrAssign, BitOrAssign);
        tokToCompoundAssignMap.put(TokenType.BitXOrAssign, BitXOrAssign);
    }
    public static CompoundAssign fromTokenType(TokenType tok){
        return tokToCompoundAssignMap.get(tok);
    }

    public static boolean isCompoundAssign(TokenType tok){
        return tokToCompoundAssignMap.containsKey(tok);
    }
}
