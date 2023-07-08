package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NStatement;
import com.kport.langueg.typeCheck.types.Type;

import java.util.Objects;

public class NVar extends NStatement {

    public Type type;
    public String name;

    public NVar(int line_, int column_, Type type_, String name_) {
        super(line_, column_);
        type = type_;
        name = name_;
    }

    @Override
    public AST[] getChildren() {
        return null;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    protected String nToString() {
        return "t: " + type + ", n: " + name;
    }
}
