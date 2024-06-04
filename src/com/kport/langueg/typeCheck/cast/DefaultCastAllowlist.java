package com.kport.langueg.typeCheck.cast;

import com.kport.langueg.typeCheck.SymbolTable;
import com.kport.langueg.typeCheck.types.*;
import com.kport.langueg.util.Identifier;

public class DefaultCastAllowlist implements CastAllowlist {
    @Override
    public boolean allowCastImplicit(Type from, Type to, SymbolTable symbolTable) {
        if(from instanceof NamedType namedTypeFrom && to instanceof NamedType namedTypeTo &&
                symbolTable.identifiersReferToSameThing(
                        new Identifier(namedTypeFrom.scope, namedTypeFrom.name()),
                        new Identifier(namedTypeTo.scope, namedTypeTo.name())
                )
            ) return true;
        from = symbolTable.tryInstantiateType(from);
        to = symbolTable.tryInstantiateType(to);

        return switch(from){
            case ArrayType arrayTypeFrom -> to instanceof ArrayType arrayTypeTo && allowCastImplicit(arrayTypeFrom.arrayType(), arrayTypeTo.arrayType(), symbolTable);
            case FnType fnTypeFrom -> {
                if(!(to instanceof FnType fnTypeTo)) yield false;
                if(!allowCastImplicit(fnTypeFrom.fnReturn(), fnTypeTo.fnReturn(), symbolTable)) yield false;
                if(fnTypeFrom.fnParams().length != fnTypeTo.fnParams().length) yield false;
                for(int i = 0; i < fnTypeFrom.fnParams().length; i++)
                    if(!allowCastImplicit(fnTypeFrom.fnParams()[i], fnTypeTo.fnParams()[i], symbolTable)) yield false;
                yield true;
            }
            case PrimitiveType primitiveTypeFrom -> to instanceof PrimitiveType primitiveTypeTo && primitiveTypeFrom == primitiveTypeTo;
            case RefType refTypeFrom -> to instanceof RefType refTypeTo && allowCastImplicit(refTypeFrom.referentType(), refTypeTo.referentType(), symbolTable);
            case TupleType tupleTypeFrom -> {
                if(!(to instanceof TupleType tupleTypeTo)) yield false;
                if(tupleTypeFrom.tupleTypes().length != tupleTypeTo.tupleTypes().length) yield false;
                for(int i = 0; i < tupleTypeFrom.tupleTypes().length; i++)
                    if(!allowCastImplicit(tupleTypeFrom.tupleTypes()[i], tupleTypeTo.tupleTypes()[i], symbolTable)) yield false;
                yield true;
            }
            case UnionType unionTypeFrom -> {
                if(!(to instanceof UnionType unionTypeTo)) yield false;
                if(unionTypeFrom.unionTypes().length != unionTypeTo.unionTypes().length) yield false;
                for(int i = 0; i < unionTypeFrom.unionTypes().length; i++)
                    if(!allowCastImplicit(unionTypeFrom.unionTypes()[i], unionTypeTo.unionTypes()[i], symbolTable)) yield false;
                yield true;
            }
            case NamedType ignored -> false;
        };
    }

    @Override
    public boolean allowCastExplicit(Type from, Type to, SymbolTable symbolTable) {
        return false;
    }
}
