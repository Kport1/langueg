package com.kport.langueg.parse.ast;

import com.kport.langueg.error.LanguegException;
import com.kport.langueg.parse.ast.nodes.*;
import com.kport.langueg.parse.ast.nodes.expr.NAssign;
import com.kport.langueg.parse.ast.nodes.expr.NBlock;
import com.kport.langueg.parse.ast.nodes.expr.NBlockYielding;
import com.kport.langueg.parse.ast.nodes.expr.NCast;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NAssignable;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NDeRef;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NDotAccess;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NIdent;
import com.kport.langueg.parse.ast.nodes.expr.controlFlow.*;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.*;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.floating.NFloat32;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.floating.NFloat64;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.integer.*;
import com.kport.langueg.parse.ast.nodes.expr.operators.*;
import com.kport.langueg.parse.ast.nodes.statement.NTypeDef;
import com.kport.langueg.parse.ast.nodes.statement.NVarInit;
import com.kport.langueg.typeCheck.types.*;
import com.kport.langueg.util.Pair;

public interface ASTVisitor {
    default void visit(AST ast, VisitorContext context) throws LanguegException {
    }


    default void visit(NExpr ignoredExpr, VisitorContext ignoredContext) throws LanguegException {
    }

    default void visit(NStatement ignoredStmnt, VisitorContext ignoredContext) throws LanguegException {
    }

    default void visit(NProg prog, VisitorContext context) throws LanguegException {
        for (AST statement : prog.statements) {
            statement.accept(this, VisitorContext.clone(context));
        }
    }


    default void visit(NAssignable ignoredAssignable, VisitorContext ignoredContext) throws LanguegException {
    }

    default void visit(NDeRef deRef, VisitorContext context) throws LanguegException {
        deRef.reference.accept(this, VisitorContext.clone(context));
    }

    default void visit(NDotAccess access, VisitorContext context) throws LanguegException {
        access.accessed.accept(this, VisitorContext.clone(context));
    }

    default void visit(NIdent ignoredIdent, VisitorContext ignoredContext) throws LanguegException {
    }


    default void visit(NInt8 ignoredInt8, VisitorContext ignoredContext) throws LanguegException {
    }

    default void visit(NInt16 ignoredInt16, VisitorContext ignoredContext) throws LanguegException {
    }

    default void visit(NInt32 ignoredInt32, VisitorContext ignoredContext) throws LanguegException {
    }

    default void visit(NInt64 ignoredInt64, VisitorContext ignoredContext) throws LanguegException {
    }

    default void visit(NUInt8 ignoredUint8, VisitorContext ignoredContext) throws LanguegException {
    }

    default void visit(NUInt16 ignoredUint16, VisitorContext ignoredContext) throws LanguegException {
    }

    default void visit(NUInt32 ignoredUint32, VisitorContext ignoredContext) throws LanguegException {
    }

    default void visit(NUInt64 ignoredUint64, VisitorContext ignoredContext) throws LanguegException {
    }

    default void visit(NBool ignoredBool, VisitorContext ignoredContext) throws LanguegException {
    }

    default void visit(NChar ignoredChar, VisitorContext ignoredContext) throws LanguegException {
    }

    default void visit(NFloat32 ignoredFloat32, VisitorContext ignoredContext) throws LanguegException {
    }

    default void visit(NFloat64 ignoredFloat64, VisitorContext ignoredContext) throws LanguegException {
    }

    default void visit(NStr ignoredStr, VisitorContext ignoredContext) throws LanguegException {
    }


    default void visit(NAssign assign, VisitorContext context) throws LanguegException {
        assign.left.accept(this, VisitorContext.clone(context));
        assign.right.accept(this, VisitorContext.clone(context));
    }

    default void visit(NAssignCompound assignCompound, VisitorContext context) throws LanguegException {
        assignCompound.left.accept(this, VisitorContext.clone(context));
        assignCompound.right.accept(this, VisitorContext.clone(context));
    }

    default void visit(NBinOp binOp, VisitorContext context) throws LanguegException {
        binOp.left.accept(this, VisitorContext.clone(context));
        binOp.right.accept(this, VisitorContext.clone(context));
    }

    default void visit(NBlock block, VisitorContext context) throws LanguegException {
        for (AST statement : block.statements) {
            statement.accept(this, VisitorContext.clone(context));
        }
    }

    default void visit(NBlockYielding blockYielding, VisitorContext context) throws LanguegException {
        for (AST statement : blockYielding.statements) {
            statement.accept(this, VisitorContext.clone(context));
        }
        blockYielding.value.accept(this, VisitorContext.clone(context));
    }

    default void visit(NCall call, VisitorContext context) throws LanguegException {
        call.callee.accept(this, VisitorContext.clone(context));
        call.arg.accept(this, VisitorContext.clone(context));
    }

    default void visit(NCast cast, VisitorContext context) throws LanguegException {
        cast.type.accept(this, VisitorContext.clone(context));
        cast.expr.accept(this, VisitorContext.clone(context));
    }

    default void visit(NIf if_, VisitorContext context) throws LanguegException {
        if_.cond.accept(this, VisitorContext.clone(context));
        if_.ifBlock.accept(this, VisitorContext.clone(context));
    }

    default void visit(NIfElse ifElse, VisitorContext context) throws LanguegException {
        ifElse.cond.accept(this, VisitorContext.clone(context));
        ifElse.ifBlock.accept(this, VisitorContext.clone(context));
        ifElse.elseBlock.accept(this, VisitorContext.clone(context));
    }

    default void visit(NMatch match, VisitorContext context) throws LanguegException {
        match.value.accept(this, VisitorContext.clone(context));
        for (Pair<NMatch.Pattern, NExpr> branch : match.branches) {
            branch.right.accept(this, VisitorContext.clone(context));
        }
    }

    default void visit(NRef ref, VisitorContext context) throws LanguegException {
        ref.referent.accept(this, VisitorContext.clone(context));
    }

    default void visit(NReturn return_, VisitorContext context) throws LanguegException {
        return_.expr.accept(this, VisitorContext.clone(context));
    }

    default void visit(NTuple tuple, VisitorContext context) throws LanguegException {
        for (Pair<NDotAccessSpecifier, NExpr> element : tuple.elements) {
            if(element.left != null)
                element.left.accept(this, VisitorContext.clone(context));
            element.right.accept(this, VisitorContext.clone(context));
        }
    }

    default void visit(NArray array, VisitorContext context) throws LanguegException {
        for (NExpr element : array.elements) {
            element.accept(this, VisitorContext.clone(context));
        }
    }

    default void visit(NUnaryOpPost unaryOpPost, VisitorContext context) throws LanguegException {
        unaryOpPost.operand.accept(this, VisitorContext.clone(context));
    }

    default void visit(NUnaryOpPre unaryOpPre, VisitorContext context) throws LanguegException {
        unaryOpPre.operand.accept(this, VisitorContext.clone(context));
    }

    default void visit(NUnion union, VisitorContext context) throws LanguegException {
        union.initElement.accept(this, VisitorContext.clone(context));
        union.specifier.accept(this, VisitorContext.clone(context));
    }

    default void visit(NWhile while_, VisitorContext context) throws LanguegException {
        while_.cond.accept(this, VisitorContext.clone(context));
        while_.block.accept(this, VisitorContext.clone(context));
    }


    default void visit(NTypeDef typeDef, VisitorContext ignoredContext) throws LanguegException {
    }

    default void visit(NVarInit varInit, VisitorContext context) throws LanguegException {
        if (varInit.type != null) varInit.type.accept(this, VisitorContext.clone(context));
        varInit.init.accept(this, VisitorContext.clone(context));
    }

    default void visit(NDotAccessSpecifier ignoredSpecifier, VisitorContext ignoredContext) throws LanguegException {
    }


    default void visit(NAnonFn anonFn, VisitorContext context) throws LanguegException {
        anonFn.type.accept(this, VisitorContext.clone(context));
        anonFn.body.accept(this, VisitorContext.clone(context));
    }

    default void visit(NNamedFn namedFn, VisitorContext context) throws LanguegException {
        namedFn.type.accept(this, VisitorContext.clone(context));
        namedFn.body.accept(this, VisitorContext.clone(context));
    }

    default void visit(NFn fn, VisitorContext ignoredContext) throws LanguegException {
    }


    default void visit(Type ignoredType, VisitorContext ignoredContext) throws LanguegException {
    }

    default void visit(ArrayType arrayType, VisitorContext context) throws LanguegException {
        arrayType.type.accept(this, VisitorContext.clone(context));
    }

    default void visit(FnType fnType, VisitorContext context) throws LanguegException {
        fnType.fnParam().accept(this, VisitorContext.clone(context));
        fnType.fnReturn().accept(this, VisitorContext.clone(context));
    }

    default void visit(NamedType namedType, VisitorContext context) throws LanguegException {
        for (Type typeArg : namedType.typeArgs) {
            typeArg.accept(this, VisitorContext.clone(context));
        }
    }

    default void visit(PrimitiveType ignoredPrimitiveType, VisitorContext ignoredContext) throws LanguegException {
    }

    default void visit(RefType refType, VisitorContext context) throws LanguegException {
        refType.referentType.accept(this, VisitorContext.clone(context));
    }

    default void visit(TupleType tupleType, VisitorContext context) throws LanguegException {
        for (NameTypePair nameTypePair : tupleType.nameTypePairs) {
            nameTypePair.type.accept(this, VisitorContext.clone(context));
        }
    }

    default void visit(UnionType unionType, VisitorContext context) throws LanguegException {
        for (NameTypePair nameTypePair : unionType.nameTypePairs) {
            nameTypePair.type.accept(this, VisitorContext.clone(context));
        }
    }

    default void visit(NameTypePair nameTypePair, VisitorContext context) throws LanguegException {
        nameTypePair.type.accept(this, VisitorContext.clone(context));
    }
}
