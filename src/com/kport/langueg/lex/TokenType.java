package com.kport.langueg.lex;

public enum TokenType {

    Assign,
    Var,

    //Binary Ops

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
    Or,
    XOR,

    //UnaryOps

    //Num
    Inc,
    Dec,

    //Bool
    Not,

    //Control
    If,
    Else,
    Return,
    Break,
    Continue,
    Switch,
    While,
    For,

    //Func

    //Struct
    LParen,
    RParen,
    LBrack,
    RBrack,
    LCurl,
    RCurl,
    Semicolon,
    Comma,


    String,
    Number,
    Identifier,
    True,
    False,


    Undefined
}
