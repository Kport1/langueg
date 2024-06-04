package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.NStatement;
import com.kport.langueg.parse.ast.nodes.expr.integer.NInt8;
import com.kport.langueg.typeCheck.types.Type;

import java.util.Arrays;

public class NVarDestruct extends NStatement {

    public Type[] types;
    public String[] names;
    public NExpr init;

    public NVarDestruct(int offset_, Type[] types_, String[] names_, NExpr init_) {
        super(offset_, init_);
        types = types_;
        names = names_;
        init = init_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{init};
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
        for (Type type : types)
            type.accept(visitor, VisitorContext.tryClone(context));
        init.accept(visitor, VisitorContext.tryClone(context));
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NVarDestruct a)) return false;
        return Arrays.deepEquals(types, a.types) && Arrays.deepEquals(names, a.names) && init.equals(a.init);
    }
}
