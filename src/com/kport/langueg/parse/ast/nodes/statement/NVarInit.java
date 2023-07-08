package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.typeCheck.types.Type;

public class NVarInit extends NVar {

    public NExpr init;

    public NVarInit(int line_, int column_, Type type_, String name_, NExpr init_) {
        super(line_, column_, type_, name_);
        init = init_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{init};
    }

    @Override
    public boolean hasChildren() {
        return true;
    }
}