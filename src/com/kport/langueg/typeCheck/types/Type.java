package com.kport.langueg.typeCheck.types;

import com.kport.langueg.codeGen.languegVmCodeGen.LanguegVmValSize;
import com.kport.langueg.error.Errors;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTTypeE;
import com.kport.langueg.parse.ast.nodes.expr.NIdent;
import com.kport.langueg.parse.ast.nodes.expr.NTuple;

import java.util.Arrays;
import java.util.Objects;

import static com.kport.langueg.parse.ast.ASTTypeE.Identifier;
import static com.kport.langueg.parse.ast.ASTTypeE.Tuple;

public interface Type {

    default boolean isPrimitive(){
        return false;
    }

    default boolean isCustom(){
        return false;
    }

    default boolean isFn(){
        return false;
    }

    default boolean isTuple(){
        return false;
    }

    default String name(){
        return null;
    }

    default Type getFnReturn(){
        return null;
    }

    default Type[] getFnArgs() {
        return null;
    }

    default Type[] getTupleTypes(){
        return null;
    }

    default LanguegVmValSize getSize(){
        return LanguegVmValSize._64;
    }
}
