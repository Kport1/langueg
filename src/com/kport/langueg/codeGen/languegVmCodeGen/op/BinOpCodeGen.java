package com.kport.langueg.codeGen.languegVmCodeGen.op;

import com.kport.langueg.codeGen.languegVmCodeGen.CodeGenState;
import com.kport.langueg.codeGen.languegVmCodeGen.LanguegVmCodeGenerator;
import com.kport.langueg.parse.ast.nodes.expr.NBinOp;

public interface BinOpCodeGen {
    void gen(NBinOp op, CodeGenState state);
}
