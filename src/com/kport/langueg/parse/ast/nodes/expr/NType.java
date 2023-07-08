package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.typeCheck.types.Type;

public class NType extends NExpr {

    public Type type;

    public NType(int line_, int column_, Type type_) {
        super(line_, column_);
        type = type_;
    }

    @Override
    public AST[] getChildren() {
        return null;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    protected String nToString() {
        return type.toString();
    }
}
