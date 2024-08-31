package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.typeCheck.types.Type;

public class NCast extends NExpr {
    public Type type;
    public NExpr expr;

    public NCast(int offset_, Type type_, NExpr expr_) {
        super(offset_, expr_);
        type = type_;
        expr = expr_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{expr};
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    protected String nToString() {
        return type.toString();
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NCast a)) return false;
        return type.equals(a.type) && expr.equals(a.expr);
    }
}
