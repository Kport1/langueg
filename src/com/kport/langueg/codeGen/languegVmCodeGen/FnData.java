package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.util.CodeOutputStream;
import com.kport.langueg.util.Identifier;
import com.kport.langueg.util.Scope;

import java.util.*;

public class FnData {
    public final Stack<ScopeData> scopeData;
    public final Map<Identifier, Integer> localOffsets;
    public int stackDepth;

    public final CodeOutputStream code = new CodeOutputStream();


    public final byte[] paramSizes;
    public int localsSize;
    public int maxStackDepth;

    public FnData(byte[] paramSizes_){
        scopeData = new Stack<>();
        scopeData.push(new ScopeData());
        localOffsets = new HashMap<>();
        maxStackDepth = 0;

        paramSizes = paramSizes_;
        localsSize = 0;
        stackDepth = 0;
    }

    public short allocateLocal(Identifier id, byte size){
        int offset = scopeData.peek().nextUnallocatedLocalByte;
        if(offset + size - 1 >= 2 << 16) throw new Error();

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

    public short allocateTempLocal(byte size){
        int offset = scopeData.peek().nextUnallocatedLocalByte;
        if(offset + size - 1 >= 2 << 16) throw new Error();

        int newLocalsSize = offset + size;
        localsSize = Math.max(localsSize, newLocalsSize);
        return (short) offset;
    }

    @Override
    public String toString(){
        return  "paramSizes: " + Arrays.toString(paramSizes) +
                "\nlocalsSize: " + localsSize +
                "\nmaxStackDepth: " + maxStackDepth +
                "\nCode :\n" + code;
    }
}
