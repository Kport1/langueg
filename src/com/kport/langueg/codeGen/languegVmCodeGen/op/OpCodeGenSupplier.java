package com.kport.langueg.codeGen.languegVmCodeGen.op;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.typeCheck.types.Type;

public interface OpCodeGenSupplier {
    BinOpCodeGen binOpCodeGen(TokenType op, Type left, Type right);
}
