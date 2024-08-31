package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.parse.Visitable;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.typeCheck.types.Type;

import java.util.Objects;

public final class NameTypePair implements Visitable {
    public Type type;
    public String name;

    public NameTypePair(Type type_, String name_){
        type = type_;
        name = name_;
    }

    @Override
    public String toString(){
        return name + " : " + type;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NameTypePair a)) return false;
        return Objects.equals(type, a.type) && Objects.equals(name, a.name);
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) {
        visitor.visit(this, context);
    }
}
