package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.util.CodeOutputStream;
import com.kport.langueg.util.Identifier;
import com.kport.langueg.util.Scope;

import java.util.*;

public class FnData {
    private final Stack<ScopeData> scopeData;
    private final Map<Identifier, Integer> localOffsets;

    public final CodeOutputStream code = new CodeOutputStream();

    public final short paramLocalsSize;
    public final short retLocalsSize;
    public int localsSize;

    public FnData(short paramLocalsSize_, short retLocalsSize_){
        scopeData = new Stack<>();
        scopeData.push(new ScopeData());
        localOffsets = new HashMap<>();

        paramLocalsSize = paramLocalsSize_;
        retLocalsSize = retLocalsSize_;
        localsSize = 0;
    }

    public void enterScope(){
        try {
            scopeData.push(scopeData.peek().clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public void exitScope(){
        scopeData.pop();
    }

    public short allocateLocal(Identifier id, int size){
        int offset = scopeData.peek().nextUnallocatedLocalByte;
        if(offset + size > 2 << 16) throw new Error();

        localOffsets.put(id, offset);

        int nextOffset = offset + size;
        scopeData.peek().nextUnallocatedLocalByte = nextOffset;
        localsSize = Math.max(localsSize, nextOffset);
        return (short) offset;
    }

    public short getLocalOffset(Identifier id){
        Scope parentScope = id.scope();
        while (parentScope != null){
            if(localOffsets.containsKey(new Identifier(parentScope, id.name())))
                return localOffsets.get(new Identifier(parentScope, id.name())).shortValue();
            parentScope = parentScope.parent;
        }
        throw new Error();
    }

    public short allocateTempLocal(int size){
        int offset = scopeData.peek().nextUnallocatedLocalByte;
        if(offset + size > 2 << 16) throw new Error();

        int newLocalsSize = offset + size;
        localsSize = Math.max(localsSize, newLocalsSize);
        return (short) offset;
    }

    public short allocateAnonLocal(int size){
        int offset = scopeData.peek().nextUnallocatedLocalByte;
        if(offset + size > 2 << 16) throw new Error();

        int nextOffset = offset + size;
        scopeData.peek().nextUnallocatedLocalByte = nextOffset;
        localsSize = Math.max(localsSize, nextOffset);
        return (short) offset;
    }

    public void popStack(int size){
        scopeData.peek().nextUnallocatedLocalByte -= size;
    }

    public void rewindLocalsTo(int index){
        if(index > scopeData.peek().nextUnallocatedLocalByte) throw new Error();
        scopeData.peek().nextUnallocatedLocalByte = index;
    }

    public short nextUnallocatedByte(){
        return (short)scopeData.peek().nextUnallocatedLocalByte;
    }

    @Override
    public String toString(){
        return  "paramLocalsSize: " + paramLocalsSize +
                "\nretLocalsSize: " + retLocalsSize +
                "\nlocalsSize: " + localsSize +
                "\nCode :\n" + code;
    }
}
