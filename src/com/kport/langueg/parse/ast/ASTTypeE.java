package com.kport.langueg.parse.ast;

public enum ASTTypeE {
    Prog,

    //Type
    Type,
    Cast,

    Fn,
    FnArg,
    Tuple,
    Class,

    Str,
    Float,
    Double,
    Byte,
    Short,
    Int,
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

    //Op
    Var,
    BinOp,
    UnaryOpBefore,
    UnaryOpAfter,

    Modifier,

    Identifier,
}