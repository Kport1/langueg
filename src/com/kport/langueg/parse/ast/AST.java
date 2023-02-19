package com.kport.langueg.parse.ast;

import com.kport.langueg.parse.ast.astVals.ASTValue;
import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.FnIdentifier;

public class AST {
    public final AST[] children;
    public AST parent;
    public ASTValue val;
    public ASTTypeE type;

    public int line;
    public int column;

    public Type returnType = null;

    public int depth;
    public int count;

    public FnIdentifier enclosingFn;

    public AST(ASTTypeE type_){
        type = type_;
        children = new AST[0];
    }

    public AST(ASTTypeE type_, int line_, int column_){
        type = type_;
        line = line_;
        column = column_;
        children = new AST[0];
    }

    public AST(ASTTypeE type_, int line_, int column_, AST... children_){
        children = children_;
        type = type_;
        line = line_;
        column = column_;
        for (AST child : children) {
            child.parent = this;
        }
    }

    public AST(ASTTypeE type_, ASTValue val_, int line_, int column_){
        val = val_;
        type = type_;
        line = line_;
        column = column_;
        children = new AST[0];
    }

    public AST(ASTTypeE type_, ASTValue val_, int line_, int column_, AST... children_){
        children = children_;
        val = val_;
        type = type_;
        line = line_;
        column = column_;
        for (AST child : children) {
            child.parent = this;
        }
    }

    public void accept(ASTVisitor visitor, VisitorContext context){
        visitor.visit(this, context);
        for (AST child : children) {
            try {
                child.accept(visitor, context == null? null : (VisitorContext) context.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean hasChildren(){
        return children.length != 0;
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
                .append(", c: ")
                .append(column)
                .append(", ")
                .append(returnType != null ? "t: " + returnType + ", " : "")
                .append("d: ")
                .append(depth)
                .append(", c: ")
                .append(count)
                .append(", fn: ")
                .append(enclosingFn)
                .append(" ]");
        str.append(val != null ? " ( " + val + " )" : "");
        if(!hasChildren()){
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
