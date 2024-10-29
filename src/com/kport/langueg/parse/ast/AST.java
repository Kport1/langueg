package com.kport.langueg.parse.ast;

import com.kport.langueg.parse.Visitable;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.util.Scope;

public abstract class AST implements Visitable, CodeLocatable {
    public AST parent;

    private final int offset;

    public Scope scope = null;

    public AST(int offset_, AST... children){
        offset = offset_;
        for (AST child : children) {
            child.parent = this;
        }
    }

    @Override
    public int codeOffset(){
        return offset;
    }

    public abstract AST[] getChildren();

    public abstract boolean hasChildren();

    protected abstract String nToString();

    /*
        Calls all applicable visitor.visit methods in the order of superclasses, then interfaces and last the class of this object itself.
     */
    @Override
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
                .append("offset: ")
                .append(offset)
                .append(scope != null? ", scope: " + scope : "")
                .append(this instanceof NExpr expr && expr.exprType != null? ", exprType: " + expr.exprType : "")
                .append(this instanceof NExpr expr? ", isExprStmnt: " + expr.isExprStmnt : "")
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
