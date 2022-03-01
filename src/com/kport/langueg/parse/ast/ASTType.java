package com.kport.langueg.parse.ast;

public enum ASTType {
    Prog,
    Type,

    //Data literal
    Str,
    Double,
    Float,
    Int,
    Byte,
    Long,
    Bool,

    //Control
    If,
    Switch,
    While,
    For,
    Call,
    Block,
    Return,

    //Data type
    Fn,
    FnArg,
    Tuple,
    Class,

    //Op
    Var,
    BinOp,
    UnaryOpBefore,
    UnaryOpAfter,

    Modifier,

    Identifier,
}