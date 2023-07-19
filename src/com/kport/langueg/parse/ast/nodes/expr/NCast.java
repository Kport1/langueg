package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.typeCheck.types.Type;

public class NCast extends NExpr {

    public Type type;
    public NExpr expr;

    public NCast(int line_, int column_, Type type_, NExpr expr_) {
        super(line_, column_, expr_);
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
}
