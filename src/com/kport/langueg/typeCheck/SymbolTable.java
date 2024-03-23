package com.kport.langueg.typeCheck;

import com.kport.langueg.parse.ast.nodes.FnHeader;
import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.Scope;
import com.kport.langueg.util.Identifier;

import java.util.*;

public class SymbolTable {
    private final Map<Identifier, Identifiable> identifiers = new HashMap<>();

    public SymbolTable(){

    }

    public static sealed abstract class Identifiable {
        public static final class Variable extends Identifiable {
            public final Type varType;
            public Variable(Type varType_){
                varType = varType_;
            }
        }

        public static final class Function extends Identifiable {
            public final FnHeader fnHeader;
            public Function(FnHeader fnHeader_){
                fnHeader = fnHeader_;
            }
        }

        public static final class NamedType extends Identifiable {
            public final Type definition;
            public NamedType(Type definition_){
                definition = definition_;
            }
        }
    }

    public boolean fnExistsInScope(Identifier id){
        return identifiers.get(id) instanceof Identifiable.Function;
    }

    public boolean varExistsInScope(Identifier id){
        return identifiers.get(id) instanceof Identifiable.Variable;
    }

    public boolean fnExists(Identifier id){
        return getById(id) instanceof Identifiable.Function;
    }

    public boolean varExists(Identifier id){
        return getById(id) instanceof Identifiable.Variable;
    }

    public Identifiable getById(Identifier id){
        Scope parentScope = id.scope();
        while (parentScope != null){
            Identifiable identifiable = identifiers.get(new Identifier(parentScope, id.name()));
            if (identifiable != null) return identifiable;
            parentScope = parentScope.parent;
        }
        return null;
    }

    public boolean registerVar(Identifier id, Type type){
        if(identifiers.containsKey(id)) return false;
        identifiers.put(id, new Identifiable.Variable(type));
        return true;
    }

    public boolean registerFn(Identifier id, FnHeader header){
        if(identifiers.containsKey(id)) return false;
        identifiers.put(id, new Identifiable.Function(header));
        return true;
    }

}
