package com.kport.langueg.parse.typeCheck;

import com.kport.langueg.lex.TokenType;

import java.util.Arrays;
import java.util.Objects;

public class Type {
    private TokenType primitiveType = null;
    private String typeName = null;

    private Type[] fnArgs = null;
    private Type fnReturn = null;
    private boolean isFn;

    public Type(TokenType primitive){
        primitiveType = primitive;
    }

    public Type(String typeName_){
        typeName = typeName_;
    }

    public Type(Type fnReturn_, Type... fnArgs_){
        fnReturn = fnReturn_;
        fnArgs = fnArgs_;
        isFn = true;
    }

    public boolean isPrimitive(){
        return primitiveType != null;
    }

    public boolean isCustom(){
        return typeName != null;
    }

    public boolean isFn(){
        return isFn;
    }

    public TokenType primitive() {
        return primitiveType;
    }

    public String name(){
        return typeName;
    }

    public Type getFnReturn(){
        return fnReturn;
    }

    public Type[] getFnArgs() {
        return fnArgs;
    }

    @Override
    public String toString(){
        if(isPrimitive()){
            return primitiveType.name();
        }
        if(isCustom()){
            return typeName;
        }
        if(isFn()){
            StringBuilder sb = new StringBuilder("Fn[(");

            for (int i = 0; i < fnArgs.length; i++) {
                sb.append(fnArgs[i]);
                if(i != fnArgs.length - 1){
                    sb.append(", ");
                }
            }

            sb.append(") -> ");
            sb.append(fnReturn);
            sb.append("]");

            return sb.toString();
        }
        return null;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Type t){
            return t.primitiveType == primitiveType &&
                    Objects.equals(t.typeName, typeName) &&
                    Arrays.equals(t.fnArgs, fnArgs) &&
                    t.fnReturn.equals(fnReturn) &&
                    t.isFn == isFn;
        }
        return false;
    }
}
