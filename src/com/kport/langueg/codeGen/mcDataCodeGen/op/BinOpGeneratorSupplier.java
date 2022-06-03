package com.kport.langueg.codeGen.mcDataCodeGen.op;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.typeCheck.types.Type;

public interface BinOpGeneratorSupplier {
    BinOpGenerator getFromOp(TokenType op, TokenType type);
}
