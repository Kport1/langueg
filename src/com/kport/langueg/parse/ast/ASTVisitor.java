package com.kport.langueg.parse.ast;

import com.kport.langueg.parse.ast.nodes.*;
import com.kport.langueg.parse.ast.nodes.expr.*;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NAssignable;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NDeRef;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NIdent;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NDotAccess;
import com.kport.langueg.parse.ast.nodes.expr.controlFlow.*;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.*;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.floating.NFloat32;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.floating.NFloat64;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.integer.*;
import com.kport.langueg.parse.ast.nodes.expr.operators.*;
import com.kport.langueg.parse.ast.nodes.statement.*;
import com.kport.langueg.typeCheck.types.*;
import com.kport.langueg.util.Pair;

import java.util.Arrays;

public interface ASTVisitor {
    default void visit(AST ast, VisitorContext context){}


    default void visit(NExpr expr, VisitorContext context){}
    default void visit(NStatement stmnt, VisitorContext context){}
    default void visit(NProg prog, VisitorContext context){
        for (AST statement : prog.statements) {
            statement.accept(this, VisitorContext.clone(context));
        }
    }


    default void visit(NAssignable assignable, VisitorContext context){}
    default void visit(NDeRef deRef, VisitorContext context){
        deRef.reference.accept(this, VisitorContext.clone(context));
    }
    default void visit(NDotAccess access, VisitorContext context){
        access.accessed.accept(this, VisitorContext.clone(context));
    }
    default void visit(NIdent ident, VisitorContext context){}


    default void visit(NInt8 int8, VisitorContext context){}
    default void visit(NInt16 int16, VisitorContext context){}
    default void visit(NInt32 int32, VisitorContext context){}
    default void visit(NInt64 int64, VisitorContext context){}
    default void visit(NUInt8 uint8, VisitorContext context){}
    default void visit(NUInt16 uint16, VisitorContext context){}
    default void visit(NUInt32 uint32, VisitorContext context){}
    default void visit(NUInt64 uint64, VisitorContext context){}

    default void visit(NBool bool, VisitorContext context){}
    default void visit(NChar char_, VisitorContext context){}
    default void visit(NFloat32 float32, VisitorContext context){}
    default void visit(NFloat64 float64, VisitorContext context){}

    default void visit(NStr str, VisitorContext context){}


    default void visit(NAssign assign, VisitorContext context){
        assign.left.accept(this, VisitorContext.clone(context));
        assign.right.accept(this, VisitorContext.clone(context));
    }
    default void visit(NAssignCompound assignCompound, VisitorContext context){
        assignCompound.left.accept(this, VisitorContext.clone(context));
        assignCompound.right.accept(this, VisitorContext.clone(context));
    }
    default void visit(NBinOp binOp, VisitorContext context){
        binOp.left.accept(this, VisitorContext.clone(context));
        binOp.right.accept(this, VisitorContext.clone(context));
    }
    default void visit(NBlock block, VisitorContext context){
        for (AST statement : block.statements) {
            statement.accept(this, VisitorContext.clone(context));
        }
    }
    default void visit(NBlockYielding blockYielding, VisitorContext context){
        for (AST statement : blockYielding.statements) {
            statement.accept(this, VisitorContext.clone(context));
        }
        blockYielding.value.accept(this, VisitorContext.clone(context));
    }
    default void visit(NCall call, VisitorContext context){
        call.callee.accept(this, VisitorContext.clone(context));
        for (NExpr arg : call.args) {
            arg.accept(this, VisitorContext.clone(context));
        }
    }
    default void visit(NCast cast, VisitorContext context){
        cast.type.accept(this, VisitorContext.clone(context));
        cast.expr.accept(this, VisitorContext.clone(context));
    }
    default void visit(NIf if_, VisitorContext context){
        if_.cond.accept(this, VisitorContext.clone(context));
        if_.ifBlock.accept(this, VisitorContext.clone(context));
    }
    default void visit(NIfElse ifElse, VisitorContext context){
        ifElse.cond.accept(this, VisitorContext.clone(context));
        ifElse.ifBlock.accept(this, VisitorContext.clone(context));
        ifElse.elseBlock.accept(this, VisitorContext.clone(context));
    }
    default void visit(NMatch match, VisitorContext context){
        match.value.accept(this, VisitorContext.clone(context));
        for (Pair<NMatch.Pattern, NExpr> branch : match.branches) {
            branch.right.accept(this, VisitorContext.clone(context));
        }
    }
    default void visit(NRef ref, VisitorContext context){
        ref.referent.accept(this, VisitorContext.clone(context));
    }
    default void visit(NReturn return_, VisitorContext context){
        return_.expr.accept(this, VisitorContext.clone(context));
    }
    default void visit(NTuple tuple, VisitorContext context){
        for (NExpr element : Arrays.stream(tuple.elements).map(p -> p.right).toList()) {
            element.accept(this, VisitorContext.clone(context));
        }
    }
    default void visit(NArray array, VisitorContext context){
        for (NExpr element : array.elements) {
            element.accept(this, VisitorContext.clone(context));
        }
    }
    default void visit(NUnaryOpPost unaryOpPost, VisitorContext context){
        unaryOpPost.operand.accept(this, VisitorContext.clone(context));
    }
    default void visit(NUnaryOpPre unaryOpPre, VisitorContext context){
        unaryOpPre.operand.accept(this, VisitorContext.clone(context));
    }
    default void visit(NUnion union, VisitorContext context){
        union.initializedElement.accept(this, VisitorContext.clone(context));
    }
    default void visit(NWhile while_, VisitorContext context){
        while_.cond.accept(this, VisitorContext.clone(context));
        while_.block.accept(this, VisitorContext.clone(context));
    }


    default void visit(NTypeDef typeDef, VisitorContext context){}
    default void visit(NVar var, VisitorContext context){
        if(var.type != null) var.type.accept(this, VisitorContext.clone(context));
    }
    default void visit(NVarInit varInit, VisitorContext context){
        if(varInit.type != null) varInit.type.accept(this, VisitorContext.clone(context));
        varInit.init.accept(this, VisitorContext.clone(context));
    }


    default void visit(NAnonFn anonFn, VisitorContext context){
        anonFn.header.accept(this, VisitorContext.clone(context));
        anonFn.body.accept(this, VisitorContext.clone(context));
    }
    default void visit(NNamedFn namedFn, VisitorContext context){
        namedFn.header.accept(this, VisitorContext.clone(context));
        namedFn.body.accept(this, VisitorContext.clone(context));
    }
    default void visit(NFn fn, VisitorContext context){}
    default void visit(FnHeader fnHeader, VisitorContext context){
        for (NameTypePair param : fnHeader.params) {
            param.type.accept(this, VisitorContext.clone(context));
        }
        fnHeader.returnType.accept(this, VisitorContext.clone(context));
    }



    default void visit(Type type, VisitorContext context){}
    default void visit(ArrayType arrayType, VisitorContext context){
        arrayType.type.accept(this, VisitorContext.clone(context));
    }
    default void visit(FnType fnType, VisitorContext context){
        for (Type fnParam : fnType.fnParams) {
            fnParam.accept(this, VisitorContext.clone(context));
        }
        fnType.fnReturn.accept(this, VisitorContext.clone(context));
    }
    default void visit(NamedType namedType, VisitorContext context){
        for (Type typeArg : namedType.typeArgs) {
            typeArg.accept(this, VisitorContext.clone(context));
        }
    }
    default void visit(PrimitiveType primitiveType, VisitorContext context){}
    default void visit(RefType refType, VisitorContext context){
        refType.referentType.accept(this, VisitorContext.clone(context));
    }
    default void visit(TupleType tupleType, VisitorContext context){
        for (NameTypePair nameTypePair : tupleType.nameTypePairs) {
            nameTypePair.type.accept(this, VisitorContext.clone(context));
        }
    }
    default void visit(UnionType unionType, VisitorContext context){
        for (NameTypePair nameTypePair : unionType.nameTypePairs) {
            nameTypePair.type.accept(this, VisitorContext.clone(context));
        }
    }

    default void visit(NameTypePair nameTypePair, VisitorContext context){
        nameTypePair.type.accept(this, VisitorContext.clone(context));
    }
}
