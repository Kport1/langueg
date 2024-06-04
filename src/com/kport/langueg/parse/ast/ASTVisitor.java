package com.kport.langueg.parse.ast;

import com.kport.langueg.parse.ast.nodes.*;
import com.kport.langueg.parse.ast.nodes.expr.*;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NAssignable;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NIdent;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NDotAccess;
import com.kport.langueg.parse.ast.nodes.expr.integer.*;
import com.kport.langueg.parse.ast.nodes.statement.*;
import com.kport.langueg.typeCheck.types.*;

public interface ASTVisitor {
    default void visit(AST ast, VisitorContext context){}


    default void visit(NExpr expr, VisitorContext context){}
    default void visit(NStatement stmnt, VisitorContext context){}
    default void visit(NProg prog, VisitorContext context){}


    default void visit(NAssignable assignable, VisitorContext context){}

    default void visit(NDotAccess access, VisitorContext context){}

    default void visit(NIdent ident, VisitorContext context){}

    default void visit(NInt8 int8, VisitorContext context){}

    default void visit(NInt16 int16, VisitorContext context){}

    default void visit(NInt32 int32, VisitorContext context){}

    default void visit(NInt64 int64, VisitorContext context){}

    default void visit(NUInt8 uint8, VisitorContext context){}

    default void visit(NUInt16 uint16, VisitorContext context){}

    default void visit(NUInt32 uint32, VisitorContext context){}

    default void visit(NUInt64 uint64, VisitorContext context){}

    default void visit(NAssign assign, VisitorContext context){}

    default void visit(NAssignCompound assignCompound, VisitorContext context){}

    default void visit(NBinOp binOp, VisitorContext context){}

    default void visit(NBool bool, VisitorContext context){}

    default void visit(NCall call, VisitorContext context){}

    default void visit(NCast cast, VisitorContext context){}

    default void visit(NChar char_, VisitorContext context){}

    default void visit(NFloat32 float32, VisitorContext context){}

    default void visit(NFloat64 float64, VisitorContext context){}

    default void visit(NStr str, VisitorContext context){}

    default void visit(NTuple tuple, VisitorContext context){}

    default void visit(NUnaryOpPost unaryOpPost, VisitorContext context){}

    default void visit(NUnaryOpPre unaryOpPre, VisitorContext context){}



    default void visit(NBlock block, VisitorContext context){}

    default void visit(NIf if_, VisitorContext context){}

    default void visit(NIfElse ifElse, VisitorContext context){}

    default void visit(NReturn return_, VisitorContext context){}

    default void visit(NTypeDef typeDef, VisitorContext context){}

    default void visit(NVar var, VisitorContext context){}

    default void visit(NVarDestruct varDestruct, VisitorContext context){}

    default void visit(NVarInit varInit, VisitorContext context){}

    default void visit(NWhile while_, VisitorContext context){}


    default void visit(NAnonFn anonFn, VisitorContext context){}
    default void visit(NNamedFn namedFn, VisitorContext context){}
    default void visit(NFn fn, VisitorContext context){}
    default void visit(FnHeader fnHeader, VisitorContext context){}

    default void visit(Type type, VisitorContext context){}
    default void visit(ArrayType arrayType, VisitorContext context){}
    default void visit(FnType fnType, VisitorContext context){}
    default void visit(NamedType namedType, VisitorContext context){}
    default void visit(PrimitiveType primitiveType, VisitorContext context){}
    default void visit(RefType refType, VisitorContext context){}
    default void visit(TupleType tupleType, VisitorContext context){}
    default void visit(UnionType unionType, VisitorContext context){}

    default void visit(NameTypePair nameTypePair, VisitorContext context){}
}
