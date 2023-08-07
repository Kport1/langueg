package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.typeCheck.types.Type;
import com.sun.jdi.InvalidTypeException;

public class NCast extends NExpr {

    public Type type;
    public NExpr expr;

    public NCast(int line_, int column_, Type type_, NExpr expr_) {
        super(line_, column_, expr_);
        type = type_;
        expr = expr_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{expr};
    }

    @Override
    public void setChild(int index, AST ast) throws InvalidTypeException {
        if(index != 0) throw new ArrayIndexOutOfBoundsException();
        if(!(ast instanceof NExpr expr_)) throw new InvalidTypeException();
        expr = expr_;
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    protected String nToString() {
        return type.toString();
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit(this, context);
        expr.accept(visitor, VisitorContext.tryClone(context));
    }
}
