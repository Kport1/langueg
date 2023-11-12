package com.kport.langueg.typeCheck.types;

import java.util.*;

public class FnType implements Type{
    private final Type[] fnParams;
    private final Type fnReturn;

    public FnType(Type fnReturn_, Type... fnArgs_){
        fnReturn = fnReturn_;
        fnParams = fnArgs_;
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
    public Type[] getFnParams() {
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
        int retArrSize = 1;

        byte[] retBytes = fnReturn.serialize();
        retArrSize += retBytes.length;

        retArrSize += 1;
        List<byte[]> paramBytesList = new ArrayList<>();
        for (Type fnParam : fnParams) {
            byte[] paramBytes = fnParam.serialize();
            retArrSize += paramBytes.length;
            paramBytesList.add(paramBytes);
        }

        byte[] ret = new byte[retArrSize];
        ret[0] = 0x02;

        System.arraycopy(retBytes, 0, ret, 1, retBytes.length);
        ret[retBytes.length] = (byte)fnParams.length;

        int dest = fnParams.length + 1;
        for (byte[] bytes : paramBytesList) {
            System.arraycopy(bytes, 0, ret, dest, bytes.length);
            dest += bytes.length;
        }

        return ret;
    }
}
