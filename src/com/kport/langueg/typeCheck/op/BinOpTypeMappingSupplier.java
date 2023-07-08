package com.kport.langueg.typeCheck.op;

import com.kport.langueg.lex.TokenType;

public interface BinOpTypeMappingSupplier {
    BinOpTypeMap getFromOp(TokenType op);
}
