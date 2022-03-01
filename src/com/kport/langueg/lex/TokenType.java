package com.kport.langueg.lex;

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
        ShiftR,
        ShiftL,

        //Bool
        Greater,
        Less,
        GreaterEq,
        LessEq,
        Eq,
        NotEq,
        And,
        AndAnd,
        Or,
        OrOr,
        XOR,
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

    //Class---------
        Class,
        New,
        Dot,
    //--------------

    //Modifiers-----
        Public,
        Private,
        Protected,
        Static,

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
        Boolean,
        Byte,
        Int,
        Long,
        Float,
        Double,

        FnType,
        Void,
        Null,
    //---------------


    Undefined
}
