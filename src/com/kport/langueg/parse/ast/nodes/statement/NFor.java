package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NStatement;

public class NFor extends NStatement {

    public AST init, cond, inc, block;

    public NFor(int line_, int column_, AST init_, AST cond_, AST inc_, AST block_){
        super(line_, column_, init_, cond_, inc_, block_);
        init = init_;
        cond = cond_;
        inc = inc_;
        block = block_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{init, cond, inc, block};
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
