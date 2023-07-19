package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.NStatement;

public class NIfElse extends NStatement {

    public NExpr cond;
    public AST ifBlock, elseBlock;

    public NIfElse(int line_, int column_, NExpr cond_, AST ifBlock_, AST elseBlock_){
        super(line_, column_, cond_, ifBlock_, elseBlock_);
        cond = cond_;
        ifBlock = ifBlock_;
        elseBlock = elseBlock_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{cond, ifBlock, elseBlock};
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
