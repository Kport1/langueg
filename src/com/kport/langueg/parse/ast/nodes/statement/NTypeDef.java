package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NStatement;
import com.kport.langueg.typeCheck.types.Type;

import java.util.Arrays;

public class NTypeDef extends NStatement {
    public String name;
    public Type definition;
    public String[] typeParameters;

    public NTypeDef(int offset_, String name_, Type definition_, String... typeParameters_){
        super(offset_);
        name = name_;
        definition = definition_;
        typeParameters = typeParameters_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{};
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public String nToString(){
        return "type" + Arrays.toString(typeParameters) + " " + name + " = " + definition;
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit(this, context);
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof NTypeDef a)) return false;
        return definition.equals(a.definition) && Arrays.equals(typeParameters, a.typeParameters);
    }
}
