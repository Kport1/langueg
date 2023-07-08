package com.kport.langueg.parse.ast.nodes;

import com.kport.langueg.parse.ast.AST;

public abstract class NStatement extends AST {
    public NStatement(int line_, int column_, AST... children) {
        super(line_, column_, children);
    }
}
