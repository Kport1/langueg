package com.kport.langueg.parse.ast;

import com.kport.langueg.parse.ast.nodes.*;
import com.kport.langueg.parse.ast.nodes.expr.*;
import com.kport.langueg.parse.ast.nodes.statement.*;

public interface ASTVisitor {
    default void visit(AST ast, VisitorContext context){}
    default void visit(NExpr expr, VisitorContext context){}
    default void visit(NStatement stmnt, VisitorContext context){}


    default void visit(NProg prog, VisitorContext context){}

    default void visit(NAnonFn anonFn, VisitorContext context){}

    default void visit(NAssign assign, VisitorContext context){}

    default void visit(NAssignable assignable, VisitorContext context){}

    default void visit(NBinOp binOp, VisitorContext context){}

    default void visit(NBool bool, VisitorContext context){}

    default void visit(NCall call, VisitorContext context){}

    default void visit(NCast cast, VisitorContext context){}

    default void visit(NChar char_, VisitorContext context){}

    default void visit(NFloat32 float32, VisitorContext context){}

    default void visit(NFloat64 float64, VisitorContext context){}

    default void visit(NFn fn, VisitorContext context){}

    default void visit(NIdent ident, VisitorContext context){}

    default void visit(NInt8 int8, VisitorContext context){}

    default void visit(NInt16 int16, VisitorContext context){}

    default void visit(NInt32 int32, VisitorContext context){}

    default void visit(NInt64 int64, VisitorContext context){}

    default void visit(NStr str, VisitorContext context){}

    default void visit(NTuple tuple, VisitorContext context){}

    default void visit(NUInt8 uint8, VisitorContext context){}

    default void visit(NUInt16 uint16, VisitorContext context){}

    default void visit(NUInt32 uint32, VisitorContext context){}

    default void visit(NUInt64 uint64, VisitorContext context){}

    default void visit(NUnaryOpPost unaryOpPost, VisitorContext context){}

    default void visit(NUnaryOpPre unaryOpPre, VisitorContext context){}



    default void visit(NBlock block, VisitorContext context){}

    default void visit(NFor for_, VisitorContext context){}

    default void visit(NIf if_, VisitorContext context){}

    default void visit(NIfElse ifElse, VisitorContext context){}

    default void visit(NReturn return_, VisitorContext context){}

    default void visit(NReturnVoid returnVoid, VisitorContext context){}

    default void visit(NVar var, VisitorContext context){}

    default void visit(NVarDestruct varDestruct, VisitorContext context){}

    default void visit(NVarInit varInit, VisitorContext context){}

    default void visit(NWhile while_, VisitorContext context){}
}
