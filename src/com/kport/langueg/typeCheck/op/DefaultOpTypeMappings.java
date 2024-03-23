package com.kport.langueg.typeCheck.op;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.typeCheck.types.PrimitiveType;

import java.util.Map;

public class DefaultOpTypeMappings implements OpTypeMappingSupplier {

    private static final Map<TokenType, BinOpTypeMap> binOpTypeMappings = Map.ofEntries(
            Map.entry(TokenType.Plus, primitiveArithmeticOp(TokenType.Plus)),
            Map.entry(TokenType.Minus, primitiveArithmeticOp(TokenType.Minus)),
            Map.entry(TokenType.Mul, primitiveArithmeticOp(TokenType.Mul)),
            Map.entry(TokenType.Div, primitiveArithmeticOp(TokenType.Div)),
            Map.entry(TokenType.Mod, primitiveArithmeticOp(TokenType.Mod)),
            Map.entry(TokenType.Pow, primitiveFloatingOp(TokenType.Pow)),

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

    private static final Map<TokenType, UnaryOpPreTypeMap> unaryOpPreTypeMappings = Map.ofEntries(
            Map.entry(TokenType.Inc, primitiveIntegerUnaryOpPre(TokenType.Inc)),
            Map.entry(TokenType.Dec, primitiveIntegerUnaryOpPre(TokenType.Dec)),
            Map.entry(TokenType.Minus, (operand, op) -> {
                if(!(operand instanceof PrimitiveType t && t.isNumeric())){
                    throw new Error("Cannot apply prefix operator \"" + TokenType.Minus.expandedName() + "\" to value of type \"" + operand + "\"");
                }
                return operand;
            }),
            Map.entry(TokenType.Not, (operand, op) -> {
                if(!(operand instanceof PrimitiveType t && t == PrimitiveType.Bool)){
                    throw new Error("Cannot apply prefix operator \"" + TokenType.Not.expandedName() + "\" to value of type \"" + operand + "\"");
                }
                return operand;
            })
    );

    private static final Map<TokenType, UnaryOpPostTypeMap> unaryOpPostTypeMappings = Map.ofEntries(
            Map.entry(TokenType.Inc, primitiveIntegerUnaryOpPost(TokenType.Inc)),
            Map.entry(TokenType.Dec, primitiveIntegerUnaryOpPost(TokenType.Dec))
    );


    private static BinOpTypeMap primitiveArithmeticOp(TokenType op){
        return (leftType, rightType, binOp) -> {
            if(!(leftType instanceof PrimitiveType lt && lt.isNumeric() && leftType.equals(rightType))){
                throw new Error("Cannot apply operator \"" + op.expandedName() + "\" to left value of type \"" + leftType + "\" and right value of type \"" + rightType + "\"");
            }
            return leftType;
        };
    }

    private static BinOpTypeMap primitiveFloatingOp(TokenType op){
        return (leftType, rightType, binOp) -> {
            if(!(leftType instanceof PrimitiveType lt && lt.isFloating() && leftType.equals(rightType))){
                throw new Error("Cannot apply operator \"" + op.expandedName() + "\" to left value of type \"" + leftType + "\" and right value of type \"" + rightType + "\"");
            }
            return leftType;
        };
    }

    private static BinOpTypeMap primitiveBitShiftOp(TokenType op){
        return (leftType, rightType, binOp) -> {
            if(!(leftType instanceof PrimitiveType lt && rightType instanceof PrimitiveType rt && lt.isInteger() && rt == PrimitiveType.U8)){
                throw new Error("Cannot apply operator \"" + op.expandedName() + "\" to left value of type \"" + leftType + "\" and right value of type \"" + rightType + "\"");
            }
            return leftType;
        };
    }

    private static BinOpTypeMap primitiveBitwiseOp(TokenType op){
        return (leftType, rightType, binOp) -> {
            if(!(leftType instanceof PrimitiveType lt && lt.isInteger() && leftType.equals(rightType))){
                throw new Error("Cannot apply operator \"" + op.expandedName() + "\" to left value of type \"" + leftType + "\" and right value of type \"" + rightType + "\"");
            }
            return leftType;
        };
    }

    private static BinOpTypeMap primitiveComparisonOp(TokenType op){
        return (leftType, rightType, binOp) -> {
            if(!(leftType instanceof PrimitiveType lt && lt.isNumeric() && leftType.equals(rightType))){
                throw new Error("Cannot apply operator \"" + op.expandedName() + "\" to left value of type \"" + leftType + "\" and right value of type \"" + rightType + "\"");
            }
            return PrimitiveType.Bool;
        };
    }

    private static BinOpTypeMap primitiveBoolOp(TokenType op){
        return (leftType, rightType, binOp) -> {
            if(!(leftType instanceof PrimitiveType lt && lt == PrimitiveType.Bool && leftType.equals(rightType))){
                throw new Error("Cannot apply operator \"" + op.expandedName() + "\" to left value of type \"" + leftType + "\" and right value of type \"" + rightType + "\"");
            }
            return PrimitiveType.Bool;
        };
    }

    private static UnaryOpPreTypeMap primitiveIntegerUnaryOpPre(TokenType op){
        return (operand, _op) -> {
            if(!(operand instanceof PrimitiveType t && t.isInteger())){
                throw new Error("Cannot apply prefix operator \"" + op.expandedName() + "\" to value of type \"" + operand + "\"");
            }
            return operand;
        };
    }

    private static UnaryOpPostTypeMap primitiveIntegerUnaryOpPost(TokenType op){
        return (operand, _op) -> {
            if(!(operand instanceof PrimitiveType t && t.isInteger())){
                throw new Error("Cannot apply postfix operator \"" + op.expandedName() + "\" to value of type \"" + operand + "\"");
            }
            return operand;
        };
    }

    @Override
    public BinOpTypeMap binOpTypeMap(TokenType op) {
        return binOpTypeMappings.get(op);
    }

    @Override
    public UnaryOpPreTypeMap unaryOpPreTypeMap(TokenType op) {
        return unaryOpPreTypeMappings.get(op);
    }

    @Override
    public UnaryOpPostTypeMap unaryOpPostTypeMap(TokenType op) {
        return unaryOpPostTypeMappings.get(op);
    }
}
