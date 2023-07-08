package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NExpr;

public class NUInt16 extends NExpr {

    public short val;

    public NUInt16(int line_, int column_, short val_) {
        super(line_, column_);
        val = val_;
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
        return Long.toUnsignedString(val);
    }
}
