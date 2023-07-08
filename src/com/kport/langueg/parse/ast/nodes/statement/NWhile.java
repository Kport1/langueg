package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NStatement;

public class NWhile extends NStatement {

    public AST condition, block;

    public NWhile(int line_, int column_, AST condition_, AST block_){
        super(line_, column_, condition_, block_);
        condition = condition_;
        block = block_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{condition, block};
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
