package com.kport.langueg.parse.typeCheck.typedAST;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.typeCheck.Type;

import java.util.Arrays;

public class TypedAST extends AST {
    public Type exprType;

    public TypedAST(AST ast, Type type_) {
        super(ast.type, ast.val,
                Arrays.stream(ast.children).map(TypedAST::new).toArray(TypedAST[]::new));
        exprType = type_;
    }

    public TypedAST(AST ast){
        super(ast.type, ast.val, ast.children != null?
                Arrays.stream(ast.children).map(TypedAST::new).toArray(TypedAST[]::new)
                : null);
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder(
                (exprType != null? ("[ " + exprType + " ]") : "") +
                type.name() + (val != null ? "( " + val + " )" : ""));
        if(children == null){
            return str.toString();
        }
        str.append("{ ");

        for (AST child : children) {
            str.append(child.toString());
            str.append(", ");
        }
        return str.substring(0, str.length() - 2) + " }";
    }
}