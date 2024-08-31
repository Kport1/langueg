package com.kport.langueg.lex;

import java.util.EnumMap;
import java.util.EnumSet;

public enum TokenType {

    LineComment,
    BlockComment,

    Assign,
    Var,

    //Binary Ops----------
        //Num
        Plus,
        PlusAssign,
        Minus,
        MinusAssign,
        Mul,
        MulAssign,
        Div,
        DivAssign,
        Mod,
        ModAssign,
        Pow,
        PowAssign,

        //Bit shift
        ShiftR,
        ShiftRAssign,
        ShiftL,
        ShiftLAssign,

        //Bitwise
        BitAnd,
        BitAndAssign,
        BitOr,
        BitOrAssign,
        BitXOr,
        BitXOrAssign,

        //Comparison
        Greater,
        Less,
        GreaterEq,
        LessEq,
        Eq,
        NotEq,

        //Bool
        And,
        AndAssign,
        Or,
        OrAssign,
        XOr,
        XOrAssign,
    //--------------------

    //Unary Ops----------
        //Num
        Inc,
        Dec,

        //Bool
        Not,
    //-------------------

    //Control----------
        If,
        Else,

        Return,

        Break,
        Continue,

        Match,
        Case,

        While,
        For,
    //-----------------

    //Func----------
        Fn,
        SingleArrow,
    //--------------

    //Mod-----------
        Module,
        Use,
    //--------------

    //Struct----------
        LParen,
        RParen,
        LBrack,
        RBrack,
        LCurl,
        RCurl,
        Colon,
        Semicolon,
        Comma,
        Dot,
        DoubleArrow,
    //----------------

    //Literals----------
        StringL,
        IntL,
        FloatL,
        Identifier,
        True,
        False,
    //------------------

    //Types----------
        //Primitive
        Bool,
        Char,

        U8,
        U16,
        U32,
        U64,

        I8,
        I16,
        I32,
        I64,

        F32,
        F64,

        //Typedef
        TypeDef,

        //Cast
        As,
    //---------------


    Undefined;


    private static final EnumMap<TokenType, String> expandedNames = new EnumMap<>(TokenType.class);
    static {
        expandedNames.put(Assign, "assign");
        expandedNames.put(Var, "var");

        expandedNames.put(Plus, "plus");
        expandedNames.put(PlusAssign, "plus assign");
        expandedNames.put(Minus, "minus");
        expandedNames.put(MinusAssign, "minus assign");
        expandedNames.put(Mul, "multiply");
        expandedNames.put(MulAssign, "multiply assign");
        expandedNames.put(Div, "divide");
        expandedNames.put(DivAssign, "divide assign");
        expandedNames.put(Mod, "modulo");
        expandedNames.put(ModAssign, "modulo assign");
        expandedNames.put(Pow, "power");
        expandedNames.put(PowAssign, "power assign");

        expandedNames.put(ShiftR, "right shift");
        expandedNames.put(ShiftRAssign, "right shift assign");
        expandedNames.put(ShiftL, "left shift");
        expandedNames.put(ShiftLAssign, "left shift assign");

        expandedNames.put(BitAnd, "bitwise and");
        expandedNames.put(BitAndAssign, "bitwise and assign");
        expandedNames.put(BitOr, "bitwise or assign");
        expandedNames.put(BitOrAssign, "bitwise and");
        expandedNames.put(BitXOr, "bitwise xor assign");
        expandedNames.put(BitXOrAssign, "bitwise and");

        expandedNames.put(Greater, "greater than");
        expandedNames.put(Less, "less than");
        expandedNames.put(GreaterEq, "greater than or equals");
        expandedNames.put(LessEq, "less than or equals");
        expandedNames.put(Eq, "equals");
        expandedNames.put(NotEq, "not equals");

        expandedNames.put(And, "and");
        expandedNames.put(AndAssign, "and assign");
        expandedNames.put(Or, "or");
        expandedNames.put(OrAssign, "or assign");
        expandedNames.put(XOr, "xor");
        expandedNames.put(XOrAssign, "xor assign");

        expandedNames.put(Inc, "increment");
        expandedNames.put(Dec, "decrement");

        expandedNames.put(Not, "not");

        expandedNames.put(If, "if");
        expandedNames.put(Else, "else");
        expandedNames.put(Return, "return");
        expandedNames.put(Break, "break");
        expandedNames.put(Continue, "continue");
        expandedNames.put(Match, "match");
        expandedNames.put(Case, "case");
        expandedNames.put(While, "while");
        expandedNames.put(For, "for");

        expandedNames.put(Fn, "function");
        expandedNames.put(SingleArrow, "single arrow");

        expandedNames.put(Module, "module");
        expandedNames.put(Use, "use");

        expandedNames.put(LParen, "left parenthesis");
        expandedNames.put(RParen, "right parenthesis");
        expandedNames.put(LBrack, "left bracket");
        expandedNames.put(RBrack, "right bracket");
        expandedNames.put(LCurl, "left curly brace");
        expandedNames.put(RCurl, "right curly brace");
        expandedNames.put(Colon, "colon");
        expandedNames.put(Semicolon, "semicolon");
        expandedNames.put(Comma, "comma");
        expandedNames.put(Dot, "dot");
        expandedNames.put(DoubleArrow, "double arrow");

        expandedNames.put(StringL, "string");
        expandedNames.put(IntL, "integer");
        expandedNames.put(FloatL, "floating point number");
        expandedNames.put(Identifier, "identifier");
        expandedNames.put(True, "true");
        expandedNames.put(False, "false");

        expandedNames.put(Bool, "boolean");
        expandedNames.put(U8, "u8");
        expandedNames.put(U16, "u16");
        expandedNames.put(U32, "u32");
        expandedNames.put(U64, "u64");
        expandedNames.put(I8, "i8");
        expandedNames.put(I16, "i16");
        expandedNames.put(I32, "i32");
        expandedNames.put(I64, "i64");
        expandedNames.put(F32, "f32");
        expandedNames.put(F64, "f64");

        expandedNames.put(TypeDef, "type definition");
        expandedNames.put(As, "as");

        expandedNames.put(Undefined, "undefined");
    }

    private static final EnumSet<TokenType> unaryOpsPost = EnumSet.of(
            Inc,
            Dec
    );

    public String expandedName(){
        return expandedNames.get(this);
    }

    public boolean isUnaryOpPost(){
        return unaryOpsPost.contains(this);
    }
}
