package com.kport.langueg.typeCheck.op;

import com.kport.langueg.parse.ast.nodes.expr.NUnaryOpPost;
import com.kport.langueg.typeCheck.types.Type;

public interface UnaryOpPostTypeMap {
    Type getType(Type operand, NUnaryOpPost op);
}
