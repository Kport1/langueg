package com.kport.langueg.typeCheck.op;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.BinOp;
import com.kport.langueg.typeCheck.types.PrimitiveType;

import java.util.Map;

public class DefaultOpTypeMappings implements OpTypeMappingSupplier {

    private static final Map<BinOp, BinOpTypeMap> binOpTypeMappings = Map.ofEntries(
            Map.entry(BinOp.Plus, primitiveArithmeticOp(BinOp.Plus)),
            Map.entry(BinOp.Minus, primitiveArithmeticOp(BinOp.Minus)),
            Map.entry(BinOp.Mul, primitiveArithmeticOp(BinOp.Mul)),
            Map.entry(BinOp.Div, primitiveArithmeticOp(BinOp.Div)),
            Map.entry(BinOp.Mod, primitiveArithmeticOp(BinOp.Mod)),
            Map.entry(BinOp.Pow, primitiveFloatingOp(BinOp.Pow)),

            Map.entry(BinOp.ShiftR, primitiveBitShiftOp(BinOp.ShiftR)),
            Map.entry(BinOp.ShiftL, primitiveBitShiftOp(BinOp.ShiftL)),

            Map.entry(BinOp.BitAnd, primitiveBitwiseOp(BinOp.BitAnd)),
            Map.entry(BinOp.BitOr, primitiveBitwiseOp(BinOp.BitOr)),
            Map.entry(BinOp.BitXOr, primitiveBitwiseOp(BinOp.BitXOr)),

            Map.entry(BinOp.Greater, primitiveComparisonOp(BinOp.Greater)),
            Map.entry(BinOp.Less, primitiveComparisonOp(BinOp.Less)),
            Map.entry(BinOp.GreaterEq, primitiveComparisonOp(BinOp.GreaterEq)),
            Map.entry(BinOp.LessEq, primitiveComparisonOp(BinOp.LessEq)),
            Map.entry(BinOp.Eq, primitiveComparisonOp(BinOp.Eq)),
            Map.entry(BinOp.NotEq, primitiveComparisonOp(BinOp.NotEq)),

            Map.entry(BinOp.And, primitiveBoolOp(BinOp.And)),
            Map.entry(BinOp.Or, primitiveBoolOp(BinOp.Or)),
            Map.entry(BinOp.XOr, primitiveBoolOp(BinOp.XOr))
    );

    private static final Map<TokenType, UnaryOpPreTypeMap> unaryOpPreTypeMappings = Map.ofEntries(
            Map.entry(TokenType.Inc, primitiveIntegerUnaryOpPre(TokenType.Inc)),
            Map.entry(TokenType.Dec, primitiveIntegerUnaryOpPre(TokenType.Dec)),
            Map.entry(TokenType.Minus, (operand, op) -> {
                if (!(operand instanceof PrimitiveType t && t.isNumeric())) {
                    throw new Error("Cannot apply prefix operator \"" + TokenType.Minus.expandedName() + "\" to value of type \"" + operand + "\"");
                }
                return operand;
            }),
            Map.entry(TokenType.Not, (operand, op) -> {
                if (!(operand instanceof PrimitiveType t && t == PrimitiveType.Bool)) {
                    throw new Error("Cannot apply prefix operator \"" + TokenType.Not.expandedName() + "\" to value of type \"" + operand + "\"");
                }
                return operand;
            })
    );

    private static final Map<TokenType, UnaryOpPostTypeMap> unaryOpPostTypeMappings = Map.ofEntries(
            Map.entry(TokenType.Inc, primitiveIntegerUnaryOpPost(TokenType.Inc)),
            Map.entry(TokenType.Dec, primitiveIntegerUnaryOpPost(TokenType.Dec))
    );


    private static BinOpTypeMap primitiveArithmeticOp(BinOp op) {
        return (leftType, rightType) -> {
            if (!(leftType instanceof PrimitiveType lt && lt.isNumeric() && leftType.equals(rightType))) {
                throw new Error("Cannot apply operator \"" + op + "\" to left value of type \"" + leftType + "\" and right value of type \"" + rightType + "\"");
            }
            return leftType;
        };
    }

    private static BinOpTypeMap primitiveFloatingOp(BinOp op) {
        return (leftType, rightType) -> {
            if (!(leftType instanceof PrimitiveType lt && lt.isFloating() && leftType.equals(rightType))) {
                throw new Error("Cannot apply operator \"" + op + "\" to left value of type \"" + leftType + "\" and right value of type \"" + rightType + "\"");
            }
            return leftType;
        };
    }

    private static BinOpTypeMap primitiveBitShiftOp(BinOp op) {
        return (leftType, rightType) -> {
            if (!(leftType instanceof PrimitiveType lt && rightType instanceof PrimitiveType rt && lt.isInteger() && rt == PrimitiveType.U8)) {
                throw new Error("Cannot apply operator \"" + op + "\" to left value of type \"" + leftType + "\" and right value of type \"" + rightType + "\"");
            }
            return leftType;
        };
    }

    private static BinOpTypeMap primitiveBitwiseOp(BinOp op) {
        return (leftType, rightType) -> {
            if (!(leftType instanceof PrimitiveType lt && lt.isInteger() && leftType.equals(rightType))) {
                throw new Error("Cannot apply operator \"" + op + "\" to left value of type \"" + leftType + "\" and right value of type \"" + rightType + "\"");
            }
            return leftType;
        };
    }

    private static BinOpTypeMap primitiveComparisonOp(BinOp op) {
        return (leftType, rightType) -> {
            if (!(leftType instanceof PrimitiveType lt && lt.isNumeric() && leftType.equals(rightType))) {
                throw new Error("Cannot apply operator \"" + op + "\" to left value of type \"" + leftType + "\" and right value of type \"" + rightType + "\"");
            }
            return PrimitiveType.Bool;
        };
    }

    private static BinOpTypeMap primitiveBoolOp(BinOp op) {
        return (leftType, rightType) -> {
            if (!(leftType instanceof PrimitiveType lt && lt == PrimitiveType.Bool && leftType.equals(rightType))) {
                throw new Error("Cannot apply operator \"" + op + "\" to left value of type \"" + leftType + "\" and right value of type \"" + rightType + "\"");
            }
            return PrimitiveType.Bool;
        };
    }

    private static UnaryOpPreTypeMap primitiveIntegerUnaryOpPre(TokenType op) {
        return (operand, _op) -> {
            if (!(operand instanceof PrimitiveType t && t.isInteger())) {
                throw new Error("Cannot apply prefix operator \"" + op.expandedName() + "\" to value of type \"" + operand + "\"");
            }
            return operand;
        };
    }

    private static UnaryOpPostTypeMap primitiveIntegerUnaryOpPost(TokenType op) {
        return (operand, _op) -> {
            if (!(operand instanceof PrimitiveType t && t.isInteger())) {
                throw new Error("Cannot apply postfix operator \"" + op.expandedName() + "\" to value of type \"" + operand + "\"");
            }
            return operand;
        };
    }

    @Override
    public BinOpTypeMap binOpTypeMap(BinOp op) {
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
