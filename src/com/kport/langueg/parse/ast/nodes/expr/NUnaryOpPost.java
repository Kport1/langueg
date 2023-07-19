package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NExpr;

public class NUnaryOpPost extends NExpr {
    public NExpr operand;
    public TokenType op;

    public NUnaryOpPost(int line_, int column_, NExpr operand_, TokenType op_){
        super(line_, column_, operand_);
        operand = operand_;
        op = op_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{operand};
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
