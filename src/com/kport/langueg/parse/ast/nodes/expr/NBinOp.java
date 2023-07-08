package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NExpr;

public class NBinOp extends NExpr {
    public NExpr left, right;
    public TokenType op;

    public NBinOp(int line_, int column_, NExpr left_, NExpr right_, TokenType op_){
        super(line_, column_, left_, right_);
        left = left_;
        right = right_;
        op = op_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{left, right};
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public String nToString(){
        return op.name();
    }
}
