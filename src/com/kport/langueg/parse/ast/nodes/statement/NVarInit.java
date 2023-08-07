package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.NStatement;
import com.kport.langueg.typeCheck.types.Type;
import com.sun.jdi.InvalidTypeException;

public class NVarInit extends NStatement {

    public Type type;
    public String name;
    public NExpr init;

    public NVarInit(int line_, int column_, Type type_, String name_, NExpr init_) {
        super(line_, column_, init_);
        type = type_;
        name = name_;
        init = init_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{init};
    }

    @Override
    public void setChild(int index, AST ast) throws InvalidTypeException {
        if(index != 0) throw new ArrayIndexOutOfBoundsException();
        if(!(ast instanceof NExpr expr)) throw new InvalidTypeException();
        init = expr;
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    protected String nToString() {
        return "t: " + type + ", n: " + name;
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit(this, context);
        init.accept(visitor, VisitorContext.tryClone(context));
    }
}