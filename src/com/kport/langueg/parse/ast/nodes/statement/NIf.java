package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NStatement;

public class NIf extends NStatement {

    public AST cond, ifBlock;

    public NIf(int line_, int column_, AST cond_, AST ifBlock_){
        super(line_, column_, cond_, ifBlock_);
        cond = cond_;
        ifBlock = ifBlock_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{cond, ifBlock};
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public String nToString(){
        return "";
    }
}
