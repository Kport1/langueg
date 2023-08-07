package com.kport.langueg.typeCheck.op;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.nodes.expr.NCast;
import com.kport.langueg.typeCheck.types.PrimitiveType;

import java.util.Map;

public class DefaultBinOpTypeMappings implements BinOpTypeMappingSupplier{

    private static final Map<TokenType, BinOpTypeMap> binOpTypeMappings = Map.ofEntries(
            Map.entry(TokenType.Plus, primitiveArithmeticOp(TokenType.Plus)),
            Map.entry(TokenType.Minus, primitiveArithmeticOp(TokenType.Minus)),
            Map.entry(TokenType.Mul, primitiveArithmeticOp(TokenType.Mul)),
            Map.entry(TokenType.Div, primitiveArithmeticOp(TokenType.Div)),
            Map.entry(TokenType.Mod, primitiveArithmeticOp(TokenType.Mod)),
            Map.entry(TokenType.Pow, primitiveArithmeticOp(TokenType.Pow)),

            Map.entry(TokenType.ShiftR, primitiveBitShiftOp(TokenType.ShiftR)),
            Map.entry(TokenType.ShiftL, primitiveBitShiftOp(TokenType.ShiftL)),

            Map.entry(TokenType.BAnd, primitiveBitwiseOp(TokenType.BAnd)),
            Map.entry(TokenType.BOr, primitiveBitwiseOp(TokenType.BOr)),
            Map.entry(TokenType.BXOr, primitiveBitwiseOp(TokenType.BXOr)),

            Map.entry(TokenType.Greater, primitiveComparisonOp(TokenType.Greater)),
            Map.entry(TokenType.Less, primitiveComparisonOp(TokenType.Less)),
            Map.entry(TokenType.GreaterEq, primitiveComparisonOp(TokenType.GreaterEq)),
            Map.entry(TokenType.LessEq, primitiveComparisonOp(TokenType.LessEq)),
            Map.entry(TokenType.Eq, primitiveComparisonOp(TokenType.Eq)),
            Map.entry(TokenType.NotEq, primitiveComparisonOp(TokenType.NotEq)),

            Map.entry(TokenType.And, primitiveBoolOp(TokenType.And)),
            Map.entry(TokenType.Or, primitiveBoolOp(TokenType.Or)),
            Map.entry(TokenType.XOr, primitiveBoolOp(TokenType.XOr))
    );


    private static BinOpTypeMap primitiveArithmeticOp(TokenType op){
        return (leftType, rightType, binOp) -> {
            if(!(leftType instanceof PrimitiveType lt && rightType instanceof PrimitiveType rt && lt.isNumeric() && rt.isNumeric())){
                throw new Error("Cannot apply operator \"" + op.expandedName() + "\" to left value of type \"" + leftType + "\" and right value of type \"" + rightType + "\"");
            }

            if(lt.isFloating() && !rt.isFloating()){
                binOp.right = new NCast(binOp.right.line, binOp.right.column, leftType, binOp.right);
                return leftType;
            }
            else if (rt.isFloating() && !lt.isFloating()) {
                binOp.left = new NCast(binOp.left.line, binOp.left.column, rightType, binOp.left);
                return rightType;
            }

            int sizeCmp = leftType.getSize().compareTo(rightType.getSize());
            if(sizeCmp > 0) {
                binOp.right = new NCast(binOp.right.line, binOp.right.column, leftType, binOp.right);
                return leftType;
            }
            else if(sizeCmp < 0){
                binOp.left = new NCast(binOp.left.line, binOp.left.column, rightType, binOp.left);
                return rightType;
            }

            return leftType;
        };
    }

    private static BinOpTypeMap primitiveBitShiftOp(TokenType op){
        return (leftType, rightType, binOp) -> {
            if(!(leftType instanceof PrimitiveType lt && rightType instanceof PrimitiveType rt && lt.isInteger() && rt.isInteger())){
                throw new Error("Cannot apply operator \"" + op.expandedName() + "\" to left value of type \"" + leftType + "\" and right value of type \"" + rightType + "\"");
            }
            if(rt != PrimitiveType.U8){
                binOp.right = new NCast(binOp.right.line, binOp.right.column, PrimitiveType.U8, binOp.right);
            }
            return leftType;
        };
    }

    private static BinOpTypeMap primitiveBitwiseOp(TokenType op){
        return (leftType, rightType, binOp) -> {
            if(!(leftType instanceof PrimitiveType lt && rightType instanceof PrimitiveType rt && lt.isInteger() && rt.isInteger())){
                throw new Error("Cannot apply operator \"" + op.expandedName() + "\" to left value of type \"" + leftType + "\" and right value of type \"" + rightType + "\"");
            }

            int sizeCmp = leftType.getSize().compareTo(rightType.getSize());
            if(sizeCmp > 0) {
                binOp.right = new NCast(binOp.right.line, binOp.right.column, leftType, binOp.right);
                return leftType;
            }
            else if(sizeCmp < 0){
                binOp.left = new NCast(binOp.left.line, binOp.left.column, rightType, binOp.left);
                return rightType;
            }

            return leftType;
        };
    }

    private static BinOpTypeMap primitiveComparisonOp(TokenType op){
        return (leftType, rightType, binOp) -> {
            if(!(leftType instanceof PrimitiveType lt && rightType instanceof PrimitiveType rt && lt.isNumeric() && rt.isNumeric())){
                throw new Error("Cannot apply operator \"" + op.expandedName() + "\" to left value of type \"" + leftType + "\" and right value of type \"" + rightType + "\"");
            }

            if(lt.isFloating() && !rt.isFloating()){
                binOp.right = new NCast(binOp.right.line, binOp.right.column, leftType, binOp.right);
            }
            else if (rt.isFloating() && !lt.isFloating()) {
                binOp.left = new NCast(binOp.left.line, binOp.left.column, rightType, binOp.left);
            }

            int sizeCmp = leftType.getSize().compareTo(rightType.getSize());
            if(sizeCmp > 0) {
                binOp.right = new NCast(binOp.right.line, binOp.right.column, leftType, binOp.right);
            }
            else if(sizeCmp < 0){
                binOp.left = new NCast(binOp.left.line, binOp.left.column, rightType, binOp.left);
            }

            return PrimitiveType.Bool;
        };
    }

    private static BinOpTypeMap primitiveBoolOp(TokenType op){
        return (leftType, rightType, binOp) -> {
            if(!(leftType instanceof PrimitiveType lt && rightType instanceof PrimitiveType rt && lt == PrimitiveType.Bool && rt == PrimitiveType.Bool)){
                throw new Error("Cannot apply operator \"" + op.expandedName() + "\" to left value of type \"" + leftType + "\" and right value of type \"" + rightType + "\"");
            }

            return PrimitiveType.Bool;
        };
    }

    @Override
    public BinOpTypeMap getFromOp(TokenType op) {
        return binOpTypeMappings.get(op);
    }
}
