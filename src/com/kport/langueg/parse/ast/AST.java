package com.kport.langueg.parse.ast;

import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.util.FnIdentifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public abstract class AST {
    public AST parent;

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

    public abstract AST[] getChildren();

    public abstract boolean hasChildren();

    protected abstract String nToString();

    public void accept(ASTVisitor visitor, VisitorContext context){
        ArrayList<Class<?>> thisSuperClasses = new ArrayList<>();
        Class<?> clazz = this.getClass();
        while(!clazz.equals(Object.class)){
            thisSuperClasses.add(clazz);
            clazz = clazz.getSuperclass();
        }


        try {
            for(int i = thisSuperClasses.size() - 1; i >= 0; i--){
                Method m = visitor.getClass().getMethod("visit", thisSuperClasses.get(i), VisitorContext.class);
                m.setAccessible(true);
                m.invoke(visitor, this, context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!hasChildren()) return;
        for (AST child : getChildren()) {
            try {
                child.accept(visitor, context == null? null : (VisitorContext) context.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
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
