package com.kport.langueg.typeCheck.types;

import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;

import java.io.ByteArrayOutputStream;
import java.util.*;

public final class FnType implements Type {
    public static final int FN_REF_BYTES = 8;

    public final Type[] fnParams;
    public final Type fnReturn;

    public FnType(Type fnReturn_, Type... fnParams_){
        fnReturn = fnReturn_;
        fnParams = fnParams_;
    }

    public Type fnReturn(){
        return fnReturn;
    }

    public Type[] fnParams() {
        return fnParams;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder("( ");
        for (Type arg : fnParams) {
            builder.append(arg);
            builder.append(", ");
        }

        if(fnParams.length > 0) {
            builder.deleteCharAt(builder.length() - 2);
        }
        builder.append(") -> ( ");
        builder.append(fnReturn);
        builder.append(" )");

        return builder.toString();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof FnType t){
            return  Arrays.equals(t.fnParams, fnParams) &&
                    t.fnReturn.equals(fnReturn);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return Objects.hash(Arrays.hashCode(fnParams), fnReturn);
    }

    @Override
    public byte[] serialize(){
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        o.write(0x02);
        o.writeBytes(fnReturn.serialize());
        o.write(fnParams.length);
        for (Type fnParam : fnParams) {
            o.writeBytes(fnParam.serialize());
        }
        return o.toByteArray();
    }

    @Override
    public int getSize() {
        return FN_REF_BYTES;
    }

    @Override
    public void accept(ASTVisitor visitor, VisitorContext context) {
        Type.super.accept(visitor, context);
        visitor.visit(this, context);
    }
}
