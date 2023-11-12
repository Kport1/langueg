package com.kport.langueg.parse.ast;

import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.util.FnIdentifier;
import com.kport.langueg.util.Scope;
import com.sun.jdi.InvalidTypeException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class AST {
    public AST parent;

    public int line;
    public int column;

    public Scope scope;

    public AST(int line_, int column_, AST... children){
        line = line_;
        column = column_;
        for (AST child : children) {
            child.parent = this;
        }
    }

    public abstract AST[] getChildren();

    public abstract void setChild(int index, AST ast) throws InvalidTypeException;

    public abstract boolean hasChildren();

    protected abstract String nToString();

    /*
        Calls all applicable visitor.visit methods in the order of superclasses, then interfaces and last the class of this object itself.
     */
    public void accept(ASTVisitor visitor, VisitorContext context){
        visitor.visit(this, context);
    }

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
                .append(scope != null? ", s: " + scope : "")
                .append(this instanceof NExpr expr && expr.exprType != null? ", t: " + expr.exprType : "")
                .append(this instanceof NExpr expr? ", isExpStmnt: " + expr.isExprStmnt : "")
                .append(" ]")
                .append("( ")
                .append(nToString())
                .append(" )");

        if(!hasChildren() || getChildren().length == 0){
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
