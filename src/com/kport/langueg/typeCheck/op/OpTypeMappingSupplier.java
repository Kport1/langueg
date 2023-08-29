package com.kport.langueg.typeCheck.op;

import com.kport.langueg.lex.TokenType;

public interface OpTypeMappingSupplier {
    BinOpTypeMap binOpTypeMap(TokenType op);
    UnaryOpPreTypeMap unaryOpPreTypeMap(TokenType op);
    UnaryOpPostTypeMap unaryOpPostTypeMap(TokenType op);
}
