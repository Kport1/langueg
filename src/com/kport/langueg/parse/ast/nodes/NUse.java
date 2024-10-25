package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NIdent;

public class NUse extends AST {

    public NIdent usedModPath;

    public NUse(int offset_, NIdent usedModPath_){
        super(offset_);
        usedModPath = usedModPath_;
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
        return usedModPath.toString();
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NUse a)) return false;
        return usedModPath.equals(a.usedModPath);
    }
}
