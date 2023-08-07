package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.sun.jdi.InvalidTypeException;

public class NUnaryOpPre extends NExpr {
    public NExpr operand;
    public TokenType op;

    public NUnaryOpPre(int line_, int column_, NExpr operand_, TokenType op_){
        super(line_, column_, operand_);
        operand = operand_;
        op = op_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{operand};
    }

    @Override
    public void setChild(int index, AST ast) throws InvalidTypeException {
        if(index != 0) throw new ArrayIndexOutOfBoundsException();
        if(!(ast instanceof NExpr expr)) throw new InvalidTypeException();
        operand = expr;
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public String nToString(){
        return op.name();
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit(this, context);
        operand.accept(visitor, VisitorContext.tryClone(context));
    }
}
