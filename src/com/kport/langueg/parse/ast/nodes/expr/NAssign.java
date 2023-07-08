package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NExpr;

public class NAssign extends NExpr {
    public NAssignable left;
    public NExpr right;
    public TokenType op;

    public NAssign(int line_, int column_, NAssignable left_, NExpr right_, TokenType op_){
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
