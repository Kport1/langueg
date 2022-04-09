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

    //Op
    Var,
    BinOp,
    UnaryOpBefore,
    UnaryOpAfter,

    Modifier,

    Identifier,
}