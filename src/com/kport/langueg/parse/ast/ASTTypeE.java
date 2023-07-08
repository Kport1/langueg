package com.kport.langueg.parse.ast;

import java.util.EnumMap;

public enum ASTTypeE {
    Prog,

    //Type
    Type,
    Cast,

    Fn,
    AnonFn,
    FnArg,
    Tuple,
    Class,

    Str,
    Float,
    Double,
    Byte,
    Char,
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
    VarDestruct,
    AssignDestruct,
    BinOp,
    UnaryOpBefore,
    UnaryOpAfter,
    Index,

    Modifier,

    Identifier;

    private final static EnumMap<ASTTypeE, String> expandedNames = new EnumMap<>(ASTTypeE.class);
    static {
        expandedNames.put(Prog, "program");

        expandedNames.put(Type, "type");
        expandedNames.put(Cast, "cast");

        expandedNames.put(Fn, "function");
        expandedNames.put(FnArg, "function parameter");
        expandedNames.put(Tuple, "tuple");
        expandedNames.put(Class, "class");

        expandedNames.put(Str, "string");
        expandedNames.put(Float, "float");
        expandedNames.put(Double, "double");
        expandedNames.put(Byte, "byte");
        expandedNames.put(Short, "short");
        expandedNames.put(Int, "int");
        expandedNames.put(Long, "long");
        expandedNames.put(Bool, "bool");

        expandedNames.put(If, "if");
        expandedNames.put(Switch, "switch");
        expandedNames.put(While, "while");
        expandedNames.put(For, "for");
        expandedNames.put(Call, "call");
        expandedNames.put(Block, "block");
        expandedNames.put(Return, "return");

        expandedNames.put(Var, "variable");
        expandedNames.put(BinOp, "binary operator");
        expandedNames.put(UnaryOpBefore, "unary operator");
        expandedNames.put(UnaryOpAfter, "unary operator");
        expandedNames.put(Index, "index");

        expandedNames.put(Modifier, "modifier");

        expandedNames.put(Identifier, "identifier");
    }

    public String expandedName(){
        return expandedNames.get(this);
    }
}