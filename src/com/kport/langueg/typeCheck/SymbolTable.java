package com.kport.langueg.typeCheck;

import com.kport.langueg.parse.ast.nodes.FnHeader;
import com.kport.langueg.parse.ast.nodes.NameTypePair;
import com.kport.langueg.parse.ast.nodes.statement.NTypeDef;
import com.kport.langueg.typeCheck.types.*;
import com.kport.langueg.util.Identifier;
import com.kport.langueg.util.Scope;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<Identifier, Identifiable> identifiers = new HashMap<>();

    public SymbolTable() {

    }

    public static sealed abstract class Identifiable {
        public static final class Variable extends Identifiable {
            public final Type varType;

            public Variable(Type varType_) {
                varType = varType_;
            }
        }

        public static final class Function extends Identifiable {
            public final FnHeader fnHeader;

            public Function(FnHeader fnHeader_) {
                fnHeader = fnHeader_;
            }
        }

        public static final class NamedType extends Identifiable {
            public final Type definition;
            public final String[] typeParameters;

            public NamedType(Type definition_, String[] typeParameters_) {
                definition = definition_;
                typeParameters = typeParameters_;
            }
        }
    }

    public boolean fnExistsInScope(Identifier id) {
        return identifiers.get(id) instanceof Identifiable.Function;
    }

    public boolean varExistsInScope(Identifier id) {
        return identifiers.get(id) instanceof Identifiable.Variable;
    }

    public boolean typeExistsInScope(Identifier id) {
        return identifiers.get(id) instanceof Identifiable.NamedType;
    }

    public boolean fnExists(Identifier id) {
        return getById(id) instanceof Identifiable.Function;
    }

    public boolean varExists(Identifier id) {
        return getById(id) instanceof Identifiable.Variable;
    }

    public boolean typeExists(Identifier id) {
        return getById(id) instanceof Identifiable.NamedType;
    }

    public boolean anyExists(Identifier id) {
        return getById(id) != null;
    }

    public Identifiable getById(Identifier id) {
        Scope parentScope = id.scope();
        while (parentScope != null) {
            Identifiable identifiable = identifiers.get(new Identifier(parentScope, id.name()));
            if (identifiable != null) return identifiable;
            parentScope = parentScope.parent;
        }
        return null;
    }

    public boolean identifiersReferToSameThing(Identifier a, Identifier b) {
        return getById(a) == getById(b);
    }

    public boolean registerVar(Identifier id, Type type) {
        if (identifiers.containsKey(id)) return false;
        identifiers.put(id, new Identifiable.Variable(type));
        return true;
    }

    public boolean registerFn(Identifier id, FnHeader header) {
        if (identifiers.containsKey(id)) return false;
        identifiers.put(id, new Identifiable.Function(header));
        return true;
    }

    public boolean registerType(Identifier id, NTypeDef typeDef) {
        if (identifiers.containsKey(id)) return false;
        identifiers.put(id, new Identifiable.NamedType(typeDef.definition, typeDef.typeParameters));
        return true;
    }

    public Type tryInstantiateType(Type type) {
        if (!(type instanceof NamedType namedType)) return type;
        return tryInstantiateType(instantiateType(namedType));
    }

    public Type instantiateType(NamedType namedType) {
        if (!typeExists(new Identifier(namedType.scope, namedType.name())))
            throw new Error("Type " + namedType.name() + " does not exist");

        SymbolTable.Identifiable.NamedType registeredType = (SymbolTable.Identifiable.NamedType) getById(new Identifier(namedType.scope, namedType.name()));
        if (namedType.typeArgs().length != registeredType.typeParameters.length)
            throw new Error("Type " + namedType.name() + " does not have the same amount of type parameters as its definition");

        NameTypePair[] args = new NameTypePair[namedType.typeArgs().length];
        for (int i = 0; i < args.length; i++)
            args[i] = new NameTypePair(namedType.typeArgs()[i], registeredType.typeParameters[i]);

        return instantiateTypeShallow(registeredType.definition, args);
    }

    private Type instantiateTypeShallow(Type definition, NameTypePair... arguments) {
        return switch (definition) {
            case ArrayType arrayType -> new ArrayType(instantiateTypeShallow(arrayType.arrayType(), arguments));

            case FnType fnType -> new FnType(
                    instantiateTypeShallow(fnType.fnReturn(), arguments),
                    Arrays.stream(fnType.fnParams()).map((t) -> instantiateTypeShallow(t, arguments)).toArray(Type[]::new)
            );

            case NamedType namedType -> {
                for (NameTypePair argument : arguments) {
                    if (argument.name.equals(namedType.name())) {
                        if (namedType.typeArgs().length != 0)
                            throw new Error("Type parameter " + namedType.name() + " cannot be instantiated");
                        yield argument.type;
                    }
                }
                NamedType ret = new NamedType(namedType.name(), Arrays.stream(namedType.typeArgs()).map(t -> instantiateTypeShallow(t, arguments)).toArray(Type[]::new));
                ret.scope = namedType.scope;
                yield ret;
            }

            case PrimitiveType primitiveType -> primitiveType;

            case RefType refType -> new RefType(instantiateTypeShallow(refType.referentType(), arguments));

            case TupleType tupleType -> new TupleType(
                    Arrays.stream(tupleType.nameTypePairs()).map(nameTypePair -> new NameTypePair(instantiateTypeShallow(nameTypePair.type, arguments), nameTypePair.name)).toArray(NameTypePair[]::new)
            );

            case UnionType unionType -> new UnionType(
                    Arrays.stream(unionType.nameTypePairs()).map(nameTypePair -> new NameTypePair(instantiateTypeShallow(nameTypePair.type, arguments), nameTypePair.name)).toArray(NameTypePair[]::new)
            );
        };
    }

    public int getNamedTypeSize(NamedType namedType) {
        if (!typeExists(new Identifier(namedType.scope, namedType.name())))
            throw new Error("Type " + namedType + " does not exist");

        SymbolTable.Identifiable.NamedType registeredType = (SymbolTable.Identifiable.NamedType) getById(new Identifier(namedType.scope, namedType.name()));
        if (namedType.typeArgs().length != registeredType.typeParameters.length)
            throw new Error("Type " + namedType.name() + " does not have the same amount of type parameters as its definition");

        NameTypePair[] args = new NameTypePair[namedType.typeArgs().length];
        for (int i = 0; i < args.length; i++)
            args[i] = new NameTypePair(namedType.typeArgs()[i], registeredType.typeParameters[i]);

        try {
            return getTypeSize(registeredType.definition, args);
        } catch (StackOverflowError err) {
            throw new Error("Type " + namedType + " is too deeply recursive");
        }
    }

    private int getTypeSize(Type definition, NameTypePair... arguments) {
        return switch (definition) {
            case ArrayType ignored -> ArrayType.ARRAY_REF_BYTES;

            case FnType ignored -> FnType.FN_REF_BYTES;

            case NamedType namedType -> {
                for (NameTypePair argument : arguments) {
                    if (argument.name.equals(namedType.name())) {
                        if (namedType.typeArgs().length != 0)
                            throw new Error("Type parameter " + namedType.name() + " cannot be instantiated");
                        yield getTypeSize(argument.type);
                    }
                }
                NamedType namedTypeIntermediate = new NamedType(namedType.name(), Arrays.stream(namedType.typeArgs()).map(t -> instantiateTypeShallow(t, arguments)).toArray(Type[]::new));
                namedTypeIntermediate.scope = namedType.scope;
                yield getNamedTypeSize(namedTypeIntermediate);
            }

            case PrimitiveType primitiveType -> primitiveType.getSize();

            case RefType ignored -> RefType.REF_BYTES;

            case TupleType tupleType ->
                    Arrays.stream(tupleType.tupleTypes()).reduce(0, (i, t) -> i + getTypeSize(t, arguments), Integer::sum);

            case UnionType unionType ->
                    UnionType.UNION_TAG_BYTES + Arrays.stream(unionType.unionTypes()).reduce(0, (i, t) -> Math.max(i, getTypeSize(t, arguments)), Integer::max);
        };
    }

}
