package com.kport.langueg.lex;

import java.util.EnumMap;
import java.util.EnumSet;

public enum TokenType {

    Assign,
    Var,

    //Binary Ops----------
        //Num
        Plus,
        Minus,
        Mul,
        Div,
        Mod,
        Pow,

        //Bit shift
        ShiftR,
        ShiftL,

        //Bitwise
        BAnd,
        BOr,
        BXOr,

        PlusAssign,
        MinusAssign,
        MulAssign,
        DivAssign,
        ModAssign,
        PowAssign,
        ShiftRAssign,
        ShiftLAssign,
        AndAssign,
        OrAssign,
        XOrAssign,

        //Comparison
        Greater,
        Less,
        GreaterEq,
        LessEq,
        Eq,
        NotEq,

        //Bool
        And,
        Or,
        XOr,
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
        Switch,
        While,
        For,
    //-----------------

    //Func----------
        Fn,
        SingleArrow,
    //--------------

    //Struct----------
        LParen,
        RParen,
        LBrack,
        RBrack,
        LCurl,
        RCurl,
        Semicolon,
        Comma,
    //----------------

    //Literals----------
        StringL,
        NumberL,
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

        Void,
        Null,
    //---------------


    Undefined;


    private static final EnumMap<TokenType, String> expandedNames = new EnumMap<>(TokenType.class);
    static {
        expandedNames.put(Assign, "assign");
        expandedNames.put(Var, "var");
        expandedNames.put(Plus, "plus");
        expandedNames.put(Minus, "minus");
        expandedNames.put(Mul, "multiply");
        expandedNames.put(Div, "divide");
        expandedNames.put(Mod, "modulo");
        expandedNames.put(Pow, "power");
        expandedNames.put(ShiftR, "right shift");
        expandedNames.put(ShiftL, "left shift");

        expandedNames.put(PlusAssign, "plus assign");
        expandedNames.put(MinusAssign, "minus assign");
        expandedNames.put(MulAssign, "multiply assign");
        expandedNames.put(DivAssign, "divide assign");
        expandedNames.put(ModAssign, "modulo assign");
        expandedNames.put(PowAssign, "power assign");
        expandedNames.put(ShiftRAssign, "right shift assign");
        expandedNames.put(ShiftLAssign, "left shift assign");
        expandedNames.put(AndAssign, "and assign");
        expandedNames.put(OrAssign, "or assign");
        expandedNames.put(XOrAssign, "xor assign");

        expandedNames.put(Greater, "greater than");
        expandedNames.put(Less, "less than");
        expandedNames.put(GreaterEq, "greater than or equals");
        expandedNames.put(LessEq, "less than or equals");
        expandedNames.put(Eq, "equals");
        expandedNames.put(NotEq, "not equals");
        expandedNames.put(BAnd, "bitwise and");
        expandedNames.put(And, "and");
        expandedNames.put(BOr, "bitwise or");
        expandedNames.put(Or, "or");
        expandedNames.put(BXOr, "bitwise xor");
        expandedNames.put(XOr, "xor");

        expandedNames.put(Inc, "increment");
        expandedNames.put(Dec, "decrement");

        expandedNames.put(Not, "not");

        expandedNames.put(If, "if");
        expandedNames.put(Else, "else");
        expandedNames.put(Return, "return");
        expandedNames.put(Break, "break");
        expandedNames.put(Continue, "continue");
        expandedNames.put(Switch, "switch");
        expandedNames.put(While, "while");
        expandedNames.put(For, "for");

        expandedNames.put(Fn, "function");
        expandedNames.put(SingleArrow, "single arrow");

        expandedNames.put(LParen, "left parenthesis");
        expandedNames.put(RParen, "right parenthesis");
        expandedNames.put(LBrack, "left bracket");
        expandedNames.put(RBrack, "right bracket");
        expandedNames.put(LCurl, "left curly brace");
        expandedNames.put(RCurl, "right curly brace");
        expandedNames.put(Semicolon, "semicolon");
        expandedNames.put(Comma, "comma");

        expandedNames.put(StringL, "string");
        expandedNames.put(NumberL, "number");
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

        expandedNames.put(Void, "void");
        expandedNames.put(Null, "null");

        expandedNames.put(Undefined, "undefined");
    }
    private static final EnumMap<TokenType, TokenType> opAssignToOp = new EnumMap<>(TokenType.class);
    static{
        opAssignToOp.put(PlusAssign, Plus);
        opAssignToOp.put(MinusAssign, Minus);
        opAssignToOp.put(MulAssign, Mul);
        opAssignToOp.put(DivAssign, Div);
        opAssignToOp.put(ModAssign, Mod);
        opAssignToOp.put(PowAssign, Pow);
        opAssignToOp.put(ShiftRAssign, ShiftR);
        opAssignToOp.put(ShiftLAssign, ShiftL);
        opAssignToOp.put(AndAssign, BAnd);
        opAssignToOp.put(OrAssign, BOr);
        opAssignToOp.put(XOrAssign, BXOr);
    }
    private static final EnumSet<TokenType> binOps = EnumSet.of(
            Assign,
            Plus,
            Minus,
            Mul,
            Div,
            Mod,
            Pow,
            ShiftR,
            ShiftL,
            Greater,
            Less,
            GreaterEq,
            LessEq,
            Eq,
            NotEq,
            BAnd,
            And,
            BOr,
            Or,
            BXOr,
            XOr
    );
    private static final EnumSet<TokenType> unaryOpsPost = EnumSet.of(
            Inc,
            Dec
    );

    public String expandedName(){
        return expandedNames.get(this);
    }

    public boolean isOpAssign(){
        return opAssignToOp.containsKey(this);
    }

    public boolean isBinOp(){
        return binOps.contains(this);
    }

    public boolean isUnaryOpPost(){
        return unaryOpsPost.contains(this);
    }

    public TokenType getOpOfOpAssign(){
        return opAssignToOp.get(this);
    }

}
