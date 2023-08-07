package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.NStatement;
import com.sun.jdi.InvalidTypeException;

//TODO fuckin for loops
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
    public void setChild(int index, AST ast) throws InvalidTypeException {

    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public String nToString(){
        return "";
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit(this, context);
        //
    }
}
