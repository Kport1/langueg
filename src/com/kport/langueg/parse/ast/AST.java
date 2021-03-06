package com.kport.langueg.parse.ast;

import com.kport.langueg.parse.ast.astVals.ASTValue;
import com.kport.langueg.typeCheck.types.Type;

public class AST {
    public AST[] children;
    public ASTValue val;
    public ASTTypeE type;

    public Type returnType = null;
    public int depth;
    public int count;

    public AST(ASTTypeE type_){
        type = type_;
    }

    public AST(ASTTypeE type_, AST... children_){
        children = children_;
        type = type_;
    }

    public AST(ASTTypeE type_, ASTValue val_){
        val = val_;
        type = type_;
    }

    public AST(ASTTypeE type_, ASTValue val_, AST... children_){
        children = children_;
        val = val_;
        type = type_;
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder(type.name());
        str.append("[")
                .append(returnType != null ? returnType + ", " : "")
                .append(depth)
                .append(", ")
                .append(count)
                .append("]");
        str.append(val != null ? "( " + val + " )" : "");
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
