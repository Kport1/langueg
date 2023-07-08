package com.kport.langueg.typeCheck.types;

import java.util.Arrays;
import java.util.Objects;

public class FnType implements Type{
    private final Type[] fnArgs;
    private final Type fnReturn;

    public FnType(Type fnReturn_, Type... fnArgs_){
        fnReturn = fnReturn_;
        fnArgs = fnArgs_;
    }

    @Override
    public boolean isFn(){
        return true;
    }

    @Override
    public Type getFnReturn(){
        return fnReturn;
    }

    @Override
    public Type[] getFnArgs() {
        return fnArgs;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder("( ");
        for (Type arg : fnArgs) {
            builder.append(arg);
            builder.append(", ");
        }

        if(fnArgs.length > 0) {
            builder.deleteCharAt(builder.length() - 2);
        }
        builder.append(") -> ");
        builder.append(fnReturn);

        return builder.toString();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof FnType t){
            return  Arrays.equals(t.fnArgs, fnArgs) &&
                    t.fnReturn.equals(fnReturn);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return Objects.hash(Arrays.hashCode(fnArgs), fnReturn);
    }
}
