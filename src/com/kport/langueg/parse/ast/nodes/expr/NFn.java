package com.kport.langueg.parse.ast.nodes.expr;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.statement.NVar;
import com.kport.langueg.typeCheck.types.Type;

import java.util.Arrays;

public class NFn extends NAnonFn {

    public String name;

    public NFn(int line_, int column_, Type returnType_, String name_, NVar[] params_, AST block_){
        super(line_, column_, returnType_, params_, block_);
        name = name_;
    }

    @Override
    public String nToString(){
        return "r: " + returnType.toString() + ", n: " + name + ", p: " + Arrays.toString(params);
    }
}