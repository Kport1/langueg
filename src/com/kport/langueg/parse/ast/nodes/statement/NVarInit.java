package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.NStatement;
import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.Span;

public class NVarInit extends NStatement {

    public Type type;
    public String name;
    public NExpr init;

    public NVarInit(Span location_, Type type_, String name_, NExpr init_) {
        super(location_, init_);
        type = type_;
        name = name_;
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
        return "t: " + type + ", n: " + name;
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) throws LanguegException {
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NVarInit a)) return false;
        return type.equals(a.type) && name.equals(a.name) && init.equals(a.init);
    }
}