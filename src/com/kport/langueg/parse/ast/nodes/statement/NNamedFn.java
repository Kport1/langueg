package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.FnParamDef;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.NFn;
import com.kport.langueg.parse.ast.nodes.NStatement;
import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.FnIdentifier;
import com.kport.langueg.util.Scope;
import com.sun.jdi.InvalidTypeException;

import java.util.Arrays;

public class NNamedFn extends NStatement implements NFn {

    public Type returnType;
    public FnParamDef[] params;
    public AST block;
    public String name;

    public NNamedFn(int line_, int column_, Type returnType_, String name_, FnParamDef[] params_, AST block_){
        super(line_, column_, block_);
        returnType = returnType_;
        params = params_;
        block = block_;
        name = name_;
    }

    @Override
    public Type[] getParamTypes(){
        return Arrays.stream(params).map(p -> p.type).toArray(Type[]::new);
    }

    @Override
    public Type getReturnType(){
        return returnType;
    }

    @Override
    public FnParamDef[] getParams(){
        return params;
    }

    @Override
    public AST getBlock(){
        return block;
    }

    private Scope blockScope = null;
    @Override
    public Scope getBlockScope(){
        return blockScope;
    }

    @Override
    public void setBlockScope(Scope scope_){
        blockScope = scope_;
    }


    private FnIdentifier id;
    public FnIdentifier getId(){
        if(id == null) id = new FnIdentifier(scope, name, getParamTypes());
        return id;
    }

    @Override
    public AST[] getChildren() {
        return new AST[]{block};
    }

    @Override
    public void setChild(int index, AST ast){
        if(index != 0) throw new ArrayIndexOutOfBoundsException();
        block = ast;
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public String nToString(){
        return "r: " + returnType.toString() + ", n: " + name + ", p: " + Arrays.toString(params);
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit((NFn) this, context);
        visitor.visit(this, context);
        block.accept(visitor, VisitorContext.tryClone(context));
    }
}