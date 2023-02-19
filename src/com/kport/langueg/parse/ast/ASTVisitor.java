package com.kport.langueg.parse.ast;

public interface ASTVisitor {
    void visit(AST ast, VisitorContext context);
}
