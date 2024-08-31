package com.kport.langueg.typeCheck.types;

import com.kport.langueg.parse.Visitable;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;

import java.util.Arrays;

public sealed interface Type extends Visitable permits ArrayType, FnType, NamedType, PrimitiveType, RefType, TupleType, UnionType {
    Type UNIT = new TupleType();
    Type ZERO = new UnionType();

    /*
    * 0x01 Primitive    CODE
    * 0x02 Fn           RetT ParamTLen [ParamT]
    * 0x03 Tuple        TupTLen [TupT TupTNameLen [TupTName]]
    * 0x04 Union        UnionTLen [UnionT UnionTNameLen [UnionTName]]
    * 0x05 Array        ArrayT
    * 0x06 Ref          ReferentT
    *
    * 0x10 Custom       NameLen [Name]
    * */
    byte[] serialize();

    int getSize();

    @Override
    default void accept(ASTVisitor visitor, VisitorContext context) {
        visitor.visit(this, context);
    }

    sealed interface InitTracker {
        boolean isInit();
        void init();

        static InitTracker getInitTracker(Type type){
            return switch (type){
                case ArrayType ignored -> new SimpleInitTracker();
                case FnType ignored-> new SimpleInitTracker();
                case NamedType ignored -> new SimpleInitTracker();
                case PrimitiveType ignored -> new SimpleInitTracker();
                case RefType ignored -> new SimpleInitTracker();
                case UnionType ignored -> new SimpleInitTracker();

                case TupleType tupleType -> new TupleInitTracker(tupleType);
            };
        }

        static InitTracker intersect(InitTracker a, InitTracker b){
            switch (a){
                case TupleInitTracker ta -> {
                    if(!(b instanceof TupleInitTracker tb)) throw new Error();
                    for (int i = 0; i < ta.elementInit.length; i++) {
                        ta.elementInit[i] = intersect(ta.elementInit[i], tb.elementInit[i]);
                    }
                    return ta;
                }

                case SimpleInitTracker sa -> {
                    if(!(b instanceof SimpleInitTracker sb)) throw new Error();
                    if(sa.init && sb.init) return sa;
                    return new SimpleInitTracker();
                }
            }
        }

        final class SimpleInitTracker implements InitTracker {
            private boolean init;

            @Override
            public boolean isInit() {
                return init;
            }

            @Override
            public void init() {
                init = true;
            }
        }

        final class TupleInitTracker implements InitTracker {
            final InitTracker[] elementInit;
            public TupleInitTracker(TupleType type){
                elementInit = new InitTracker[type.tupleTypes().length];
                for (int i = 0; i < type.tupleTypes().length; i++) {
                    elementInit[i] = getInitTracker(type.tupleTypes()[i]);
                }
            }

            public InitTracker getElement(int index){
                return elementInit[index];
            }

            @Override
            public boolean isInit() {
                return Arrays.stream(elementInit).allMatch(InitTracker::isInit);
            }

            @Override
            public void init() {
                for (InitTracker initTracker : elementInit) {
                    initTracker.init();
                }
            }
        }
    }
}
