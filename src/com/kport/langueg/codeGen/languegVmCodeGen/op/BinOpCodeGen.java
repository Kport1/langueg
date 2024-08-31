package com.kport.langueg.codeGen.languegVmCodeGen.op;

import com.kport.langueg.codeGen.languegVmCodeGen.CodeGenState;

public interface BinOpCodeGen {
    void gen(CodeGenState state, short to, short op1, short op2);
}
