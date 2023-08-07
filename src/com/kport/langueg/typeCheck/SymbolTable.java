package com.kport.langueg.typeCheck;

import com.kport.langueg.parse.ast.nodes.FnParamDef;
import com.kport.langueg.parse.ast.nodes.expr.NAnonFn;
import com.kport.langueg.parse.ast.nodes.statement.NNamedFn;
import com.kport.langueg.typeCheck.types.FnType;
import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.FnIdentifier;
import com.kport.langueg.util.Scope;
import com.kport.langueg.util.VarIdentifier;

import java.util.Arrays;
import java.util.HashMap;

public class SymbolTable {

    private final HashMap<FnIdentifier, Type> fnTypes = new HashMap<>();
    private final HashMap<VarIdentifier, Type> varTypes = new HashMap<>();

    public SymbolTable(){

    }

    public Type getFnType(FnIdentifier id){
        Type inScope = fnTypes.get(id);
        if(inScope != null) return inScope;

        if(id.scope().parent == null) return null;

        return getFnType(new FnIdentifier(id.scope().parent, id.name(), id.params()));
    }

    public boolean fnExists(FnIdentifier id){
        return getFnType(id) != null;
    }

    public Type[] getAllFnTypes(String name, Scope scope){
        Type[] inScope = fnTypes.keySet().stream().filter((id) -> id.name().equals(name) && id.scope().equals(scope))
                .map((id) -> new FnType(fnTypes.get(id), id.params())).toArray(Type[]::new);

        if(scope.parent == null) return inScope;

        Type[] parentScopes = getAllFnTypes(name, scope.parent);
        Type[] out = Arrays.copyOfRange(inScope, 0, inScope.length + parentScopes.length);
        System.arraycopy(parentScopes, 0, out, inScope.length, parentScopes.length);
        return out;
    }

    public boolean anyFnExists(String name, Scope scope){
        return getAllFnTypes(name, scope).length != 0;
    }

    public Type getVarType(VarIdentifier id){
        Type inScope = varTypes.get(id);
        if(inScope != null) return inScope;

        if(id.scope().parent == null){
            return null;
        }

        return getVarType(new VarIdentifier(id.scope().parent, id.name()));
    }

    public boolean varExists(VarIdentifier id){
        return getVarType(id) != null;
    }

    public boolean varExistsInScope(VarIdentifier id){
        return varTypes.containsKey(id);
    }

    public boolean registerFn(NNamedFn fn){
        FnIdentifier id = fn.getId();

        if(fnTypes.containsKey(id)) return false;
        fnTypes.put(id, fn.returnType);
        for (FnParamDef param : fn.params) {
            if(!registerVar(new VarIdentifier(fn.getBlockScope(), param.name), param.type))
                return false;
        }
        return true;
    }

    public boolean registerAnonFn(NAnonFn fn){
        for (FnParamDef param : fn.params) {
            if(!registerVar(new VarIdentifier(fn.getBlockScope(), param.name), param.type))
                return false;
        }
        return true;
    }

    public boolean registerVar(VarIdentifier id, Type type){
        if(varTypes.containsKey(id)) return false;
        varTypes.put(id, type);
        return true;
    }

}
