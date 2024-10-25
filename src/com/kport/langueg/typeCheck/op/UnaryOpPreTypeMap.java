package com.kport.langueg.typeCheck.op;

import com.kport.langueg.parse.ast.nodes.expr.operators.NUnaryOpPre;
import com.kport.langueg.typeCheck.types.Type;

public interface UnaryOpPreTypeMap {
    Type getType(Type operand, NUnaryOpPre op);
}
