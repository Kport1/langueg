package com.kport.langueg.codeGen.languegVmCodeGen.op;

import com.kport.langueg.parse.ast.BinOp;
import com.kport.langueg.typeCheck.types.Type;

public interface OpCodeGenSupplier {
    BinOpCodeGen binOpCodeGen(BinOp op, Type left, Type right);
}
