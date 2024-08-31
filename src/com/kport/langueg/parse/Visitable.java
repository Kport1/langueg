package com.kport.langueg.parse;

import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;

public interface Visitable {
    void accept(ASTVisitor visitor, VisitorContext context);
}
