package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.nodes.NStatement;
import com.kport.langueg.typeCheck.types.Type;

import java.util.Arrays;

public class NVarDestruct extends NStatement {

    public Type[] types;
    public String[] names;
    public AST init;

    public NVarDestruct(int line_, int column_, Type[] types_, String[] names_, AST init_) {
        super(line_, column_, init_);
        types = types_;
        names = names_;
        init = init_;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{init};
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    protected String nToString() {
        return "t: " + Arrays.toString(types) + ", n: " + Arrays.toString(names);
    }
}
