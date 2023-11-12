package com.kport.langueg.parse.ast.nodes.statement;

import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.FnParamDef;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.NFn;
import com.kport.langueg.parse.ast.nodes.NStatement;
import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.FnHeader;
import com.kport.langueg.util.FnIdentifier;
import com.kport.langueg.util.Scope;
import com.sun.jdi.InvalidTypeException;

import java.util.Arrays;

public class NNamedFn extends NStatement implements NFn {

    public FnHeader header;
    public AST block;

    public NNamedFn(int line_, int column_, Type returnType_, String name_, FnParamDef[] params_, AST block_){
        super(line_, column_, block_);
        header = new FnHeader(returnType_, params_, name_);
        block = block_;
    }

    @Override
    public FnParamDef[] getParams(){
        return header.params();
    }

    @Override
    public Type[] getParamTypes(){
        return header.getParamTypes();
    }

    @Override
    public Type getReturnType(){
        return header.returnType();
    }

    public String getName(){
        return header.name();
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
        if(id == null) id = new FnIdentifier(scope, header.name(), getParamTypes());
        return id;
    }

    public FnHeader getHeader(){
        return header;
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
        return "r: " + header.returnType().toString() + ", n: " + header.name() + ", p: " + Arrays.toString(header.params());
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context){
        super.accept(visitor, context);
        visitor.visit((NFn) this, context);
        visitor.visit(this, context);
        block.accept(visitor, VisitorContext.tryClone(context));
    }
}