package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.NStatement;
import com.kport.langueg.typeCheck.types.Type;
import com.sun.jdi.InvalidTypeException;

import java.util.Arrays;

public class NVarDestruct extends NStatement {

    public Type[] types;
    public String[] names;
    public NExpr init;

    public NVarDestruct(int line_, int column_, Type[] types_, String[] names_, NExpr init_) {
        super(line_, column_, init_);
        types = types_;
        names = names_;
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
        return "t: " + Arrays.toString(types) + ", n: " + Arrays.toString(names);
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit(this, context);
        init.accept(visitor, VisitorContext.tryClone(context));
    }
}
