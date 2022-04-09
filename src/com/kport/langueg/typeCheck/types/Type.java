package com.kport.langueg.typeCheck.types;

import com.kport.langueg.lex.TokenType;

import java.util.Arrays;
import java.util.Objects;

public class Type {
    private TokenType primitiveType = null;
    private String typeName = null;

    private Type[] fnArgs = null;
    private Type fnReturn = null;
    private boolean isFn;

    protected Type[] overloadedFns = null;
    protected boolean isOverloaded;

    protected Type[] tupleTypes = null;
    protected boolean isTuple;

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

    protected Type(){}

    public boolean isPrimitive(){
        return primitiveType != null;
    }

    public boolean isCustom(){
        return typeName != null;
    }

    public boolean isFn(){
        return isFn;
    }

    public boolean isOverloaded(){
        return isOverloaded;
    }

    public boolean isTuple(){
        return isTuple;
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

    public Type[] getOverloadedFns(){
        return overloadedFns;
    }

    public boolean anyOverloadedFnMatches(Type t){
        if(!isOverloaded){
            return false;
        }
        return Arrays.asList(overloadedFns).contains(t);
    }

    public Type[] getTupleTypes(){
        return tupleTypes;
    }

    @Override
    public String toString(){
        if(isPrimitive()){
            return primitiveType.name();
        }
        if(isCustom()){
            return "\"" + typeName + "\"";
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
        if(isOverloaded()){
            StringBuilder sb = new StringBuilder("Overload[ ");

            for (int i = 0; i < overloadedFns.length; i++) {
                sb.append(overloadedFns[i]);
                if(i != overloadedFns.length - 1){
                    sb.append("; ");
                }
            }

            sb.append(" ]");
            return sb.toString();
        }
        if(isTuple()){
            StringBuilder sb = new StringBuilder("Tup[ ");

            for (int i = 0; i < tupleTypes.length; i++) {
                sb.append(tupleTypes[i]);
                if(i != tupleTypes.length - 1){
                    sb.append(", ");
                }
            }

            sb.append(" ]");
            return sb.toString();
        }
        return "";
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Type t){
            return t.primitiveType == primitiveType &&
                    Objects.equals(t.typeName, typeName) &&
                    Arrays.equals(t.fnArgs, fnArgs) &&
                    Objects.equals(t.fnReturn, fnReturn) &&
                    t.isFn == isFn &&
                    Arrays.equals(t.overloadedFns, overloadedFns) &&
                    t.isOverloaded == isOverloaded &&
                    Arrays.equals(t.tupleTypes, tupleTypes) &&
                    t.isTuple == isTuple;
        }
        return false;
    }

    @Override
    public int hashCode(){
        return Objects.hash(primitiveType, typeName, Arrays.hashCode(fnArgs), fnReturn, isFn, Arrays.hashCode(overloadedFns), isOverloaded, Arrays.hashCode(tupleTypes), isTuple);
    }
}
