package com.kport.langueg.parse.ast;

public enum ASTType {
    Prog,

    //Data literal
    Str,
    Dub,
    Int,

    //Control
    If,
    Switch,
    Call,
    Block,

    //Data type
    Func,
    Tuple,

    //Op
    Var,
    BinOp,
    UnaryOpBefore,
    UnaryOpAfter,

    Identifier,

}
