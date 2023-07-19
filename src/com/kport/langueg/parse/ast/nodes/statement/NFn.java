package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.FnParamDef;
import com.kport.langueg.parse.ast.nodes.NStatement;
import com.kport.langueg.typeCheck.types.Type;

import java.util.Arrays;

public class NFn extends NStatement {

    public Type returnType;
    public FnParamDef[] params;
    public AST block;
    public String name;

    public NFn(int line_, int column_, Type returnType_, String name_, FnParamDef[] params_, AST block_){
        super(line_, column_, block_);
        returnType = returnType_;
        params = params_;
        block = block_;
        name = name_;
    }

    public Type[] getParamTypes(){
        return Arrays.stream(params).map(p -> p.type).toArray(Type[]::new);
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{block};
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public String nToString(){
        return "r: " + returnType.toString() + ", n: " + name + ", p: " + Arrays.toString(params);
    }
}