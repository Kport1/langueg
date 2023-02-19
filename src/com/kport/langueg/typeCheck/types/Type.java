package com.kport.langueg.typeCheck.types;

import com.kport.langueg.error.Errors;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTTypeE;

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

    default TokenType primitive() {
        return null;
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

    static Type[] of(AST... asts) throws TypeConversionException{
        if(asts == null) return null;

        final boolean[] err = {false};
        final AST[] errAST = {null};


        Type[] res =
                Arrays.stream(asts).map((ast) -> {
                    if(ast.type == Tuple){
                        try {
                            return new TupleType(of(ast.children));
                        } catch (TypeConversionException e) {
                            err[0] = true;
                        }
                    }
                    if (ast.type == Identifier) {
                        return new CustomType(ast.val.getStr());
                    }
                    if(ast.type == ASTTypeE.Type){
                        return ast.val.getType();
                    }

                    err[0] = true;
                    errAST[0] = ast;
                    return null;
                }).toArray(Type[]::new);

        if(err[0]) throw new TypeConversionException(errAST[0]);
        return res;
    }

    static Type of(AST ast) throws TypeConversionException {
        return of(new AST[]{ast})[0];
    }
}
