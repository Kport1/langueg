package com.kport.langueg.codeGen.mcDataCodeGen.op;

import com.kport.langueg.parse.ast.AST;

public interface BinOpGenerator {
    void gen(AST op, StringBuilder output);
}
