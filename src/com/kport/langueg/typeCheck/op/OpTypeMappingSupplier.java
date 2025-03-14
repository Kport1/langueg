package com.kport.langueg.typeCheck.op;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.BinOp;

public interface OpTypeMappingSupplier {
    BinOpTypeMap binOpTypeMap(BinOp op);

    UnaryOpPreTypeMap unaryOpPreTypeMap(TokenType op);

    UnaryOpPostTypeMap unaryOpPostTypeMap(TokenType op);
}
