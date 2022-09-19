package com.kport.langueg.parse.ast;

import com.kport.langueg.parse.ast.astVals.ASTValue;
import com.kport.langueg.typeCheck.types.Type;

public class AST {
    public AST[] children;
    public ASTValue val;
    public ASTTypeE type;

    public int line;

    public Type returnType = null;
    public int depth;
    public int count;

    public AST(ASTTypeE type_){
        type = type_;
    }

    public AST(ASTTypeE type_, int line_){
        type = type_;
        line = line_;
    }

    public AST(ASTTypeE type_, int line_, AST... children_){
        children = children_;
        type = type_;
        line = line_;
    }

    public AST(ASTTypeE type_, ASTValue val_, int line_){
        val = val_;
        type = type_;
        line = line_;
    }

    public AST(ASTTypeE type_, ASTValue val_, int line_, AST... children_){
        children = children_;
        val = val_;
        type = type_;
        line = line_;
    }

    @Override
    public String toString(){
        return toStringPretty(1);
    }

    private String toStringPretty(int indent){
        StringBuilder str = new StringBuilder(type.name());
        str.append(" [ ")
                .append("l: ")
                .append(line)
                .append(", ")
                .append(returnType != null ? "t: " + returnType + ", " : "")
                .append("d: ")
                .append(depth)
                .append(", c: ")
                .append(count)
                .append(" ]");
        str.append(val != null ? " ( " + val + " )" : "");
        if(children == null){
            return str.toString();
        }
        str.append(" {\n");
        str.append("   ".repeat(indent));

        for (AST child : children) {
            str.append(child.toStringPretty(indent + 1));
            str.append(",\n");
            str.append("   ".repeat(indent));
        }
        str.delete(str.length() - 3 * indent - 2, str.length());
        str.append("\n");
        return str + "   ".repeat(indent - 1) + "}";
    }
}
