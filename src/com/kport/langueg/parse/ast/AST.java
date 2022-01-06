package com.kport.langueg.parse.ast;

public class AST {
    public AST[] children;
    public ASTValue val;
    public ASTType type;

    public AST(ASTType type_){
        type = type_;
    }

    public AST(ASTType type_, AST... children_){
        children = children_;
        type = type_;
    }

    public AST(ASTType type_, ASTValue val_){
        val = val_;
        type = type_;
    }

    public AST(ASTType type_, ASTValue val_, AST... children_){
        children = children_;
        val = val_;
        type = type_;
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder(type.name() + (val != null ? "( " + val + " )" : ""));
        if(children == null){
            return str.toString();
        }
        str.append("{ ");

        for (AST child : children) {
            str.append(child.toString());
            str.append(", ");
        }
        return str.substring(0, str.length() - 2) + " }";
    }
}
