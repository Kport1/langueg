package com.kport.langueg.parse.ast;

import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.util.FnIdentifier;

public abstract class AST {
    //public final AST[] children;
    public AST parent;
    //public ASTValue val;
    //public ASTTypeE type;

    public int line;
    public int column;

    public int depth;
    public int count;

    public FnIdentifier enclosingFn;

    public AST(int line_, int column_, AST... children){
        line = line_;
        column = column_;
        for (AST child : children) {
            child.parent = this;
        }
    }

    /*public AST(ASTTypeE type_){
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
    }*/

    public abstract AST[] getChildren();

    public void accept(ASTVisitor visitor, VisitorContext context){
        visitor.visit(this, context);
        if(!hasChildren()) return;
        for (AST child : getChildren()) {
            try {
                child.accept(visitor, context == null? null : (VisitorContext) context.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }

    public abstract boolean hasChildren();

    protected abstract String nToString();

    @Override
    public String toString(){
        return toStringPretty(1);
    }

    private String toStringPretty(int indent){
        StringBuilder str = new StringBuilder(this.getClass().getSimpleName());
        str.append(" [ ")
                .append("l: ")
                .append(line)
                .append(", c: ")
                .append(column)
                .append(", d: ")
                .append(depth)
                .append(", c: ")
                .append(count)
                .append(this instanceof NExpr expr && expr.exprType != null? ", r: " + expr.exprType : "")
                .append(", fn: ")
                .append(enclosingFn)
                .append(" ]")
                .append("( ")
                .append(nToString())
                .append(" )");

        if(!hasChildren()){
            return str.toString();
        }
        str.append(" {\n");
        str.append("   ".repeat(indent));

        for (AST child : getChildren()) {
            str.append(child.toStringPretty(indent + 1));
            str.append(",\n");
            str.append("   ".repeat(indent));
        }
        str.delete(str.length() - 3 * indent - 2, str.length());
        str.append("\n");
        return str + "   ".repeat(indent - 1) + "}";
    }
}
