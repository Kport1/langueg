package com.kport.langueg.typeCheck.op;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTTypeE;
import com.kport.langueg.parse.ast.astVals.ASTType;
import com.kport.langueg.parse.ast.nodes.expr.NBinOp;
import com.kport.langueg.parse.ast.nodes.expr.NCast;
import com.kport.langueg.parse.ast.nodes.expr.NIdent;
import com.kport.langueg.typeCheck.types.PrimitiveType;
import com.kport.langueg.typeCheck.types.Type;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import static com.kport.langueg.lex.TokenType.*;

public enum DefaultBinOpTypeMappings implements BinOpTypeMappingSupplier{

    PLUS((left, right, op) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot add " + left + " and " + right);
        }

        return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, op);
    }, Plus),

    MINUS((left, right, op) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot subtract " + left + " and " + right);
        }

        return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, op);
    }, Minus),

    MUL((left, right, op) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot multiply " + left + " and " + right);
        }

        return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, op);
    }, Mul),

    DIV((left, right, op) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot divide " + left + " and " + right);
        }

        return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, op);
    }, Div),

    MOD((left, right, op) -> {
        if(!(arePrim(left, right) && isInteger(left) && isInteger(right))){
            throw new Error("Cannot apply operator % to " + left + " and " + right);
        }

        return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, op);
    }, Mod),

    POW((left, right, op) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot apply operator ** to " + left + " and " + right);
        }

        return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, op);
    }, Pow),

    SHIFTR((left, right, op) -> {
        if(!(arePrim(left, right) && isInteger(left) && isInteger(right))){
            throw new Error("Cannot apply operator >> to " + left + " and " + right);
        }

        return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, op);
    }, ShiftR),

    SHIFTL((left, right, op) -> {
        if(!(arePrim(left, right) && isInteger(left) && isInteger(right))){
            throw new Error("Cannot apply operator << to " + left + " and " + right);
        }

        return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, op);
    }, ShiftL),

    GREATER((left, right, op) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot apply operator < to " + left + " and " + right);
        }

        castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, op);
        return PrimitiveType.Bool;
    }, Greater),

    LESS((left, right, op) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot apply operator > to " + left + " and " + right);
        }

        castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, op);
        return PrimitiveType.Bool;
    }, Less),

    GREATEREQ((left, right, op) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot apply operator <= to " + left + " and " + right);
        }

        castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, op);
        return PrimitiveType.Bool;
    }, GreaterEq),

    LESSEQ((left, right, op) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot apply operator >= to " + left + " and " + right);
        }

        castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, op);
        return PrimitiveType.Bool;
    }, LessEq),

    EQ((left, right, op) -> {
        if(!(left.equals(right))){
            throw new Error("Cannot apply operator == to " + left + " and " + right);
        }

        return PrimitiveType.Bool;
    }, Eq),

    NOTEQ((left, right, op) -> {
        if(!(left.equals(right))){
            throw new Error("Cannot apply operator != to " + left + " and " + right);
        }

        return PrimitiveType.Bool;
    }, NotEq),

    AND((left, right, op) -> {
        if(!(arePrim(left, right) && isBool(left) && isBool(right))){
            if(isInteger(left) && isInteger(right)){
                return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, op);
            }
            throw new Error("Cannot apply operator & to " + left + " and " + right);
        }

        return left;
    }, And),

    ANDAND((left, right, op) -> {
        if(!(arePrim(left, right) && isBool(left) && isBool(right))){
            throw new Error("Cannot apply operator && to " + left + " and " + right);
        }

        return left;
    }, AndAnd),

    OR((left, right, op) -> {
        if(!(arePrim(left, right) && isBool(left) && isBool(right))){
            if(isInteger(left) && isInteger(right)){
                return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, op);
            }
            throw new Error("Cannot apply operator | to " + left + " and " + right);
        }

        return left;
    }, Or),

    OROR((left, right, op) -> {
        if(!(arePrim(left, right) && isBool(left) && isBool(right))){
            throw new Error("Cannot apply operator & to " + left + " and " + right);
        }

        return left;
    }, OrOr),

    XOR((left, right, op) -> {
        if(!(arePrim(left, right) && isBool(left) && isBool(right))){
            if(isInteger(left) && isInteger(right)){
                return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, op);
            }
            throw new Error("Cannot apply operator | to " + left + " and " + right);
        }

        return left;
    }, XOr),

    PLUSASSIGN((left, right, op) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot apply operator += to " + left + " and " + right);
        }

        if(!right.equals(left)){
            op.right = new NCast(op.right.line, op.right.column, left, op.right);
        }

        return left;
    }, PlusAssign),

    MINUSASSIGN((left, right, op) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot apply operator -= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            op.right = new NCast(op.right.line, op.right.column, left, op.right);
        }

        return left;
    }, MinusAssign),

    MULASSIGN((left, right, op) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot apply operator *= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            op.right = new NCast(op.right.line, op.right.column, left, op.right);
        }

        return left;
    }, MulAssign),

    DIVASSIGN((left, right, op) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot apply operator /= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            op.right = new NCast(op.right.line, op.right.column, left, op.right);
        }

        return left;
    }, DivAssign),

    MODASSIGN((left, right, op) -> {
        if(!(arePrim(left, right) && isInteger(left) && isInteger(right))){
            throw new Error("Cannot apply operator %= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            op.right = new NCast(op.right.line, op.right.column, left, op.right);
        }

        return left;
    }, ModAssign),

    POWASSIGN((left, right, op) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot apply operator **= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            op.right = new NCast(op.right.line, op.right.column, left, op.right);
        }

        return left;
    }, PowAssign),

    SHIFTRASSIGN((left, right, op) -> {
        if(!(arePrim(left, right) && isInteger(left) && isInteger(right))){
            throw new Error("Cannot apply operator >>= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            op.right = new NCast(op.right.line, op.right.column, left, op.right);
        }

        return left;
    }, ShiftRAssign),

    SHIFTLASSIGN((left, right, op) -> {
        if(!(arePrim(left, right) && isInteger(left) && isInteger(right))){
            throw new Error("Cannot apply operator <<= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            op.right = new NCast(op.right.line, op.right.column, left, op.right);
        }

        return left;
    }, ShiftLAssign),

    ANDASSIGN((left, right, op) -> {
        if(!(arePrim(left, right) && isInteger(left) && isInteger(right))){
            throw new Error("Cannot apply operator &= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            op.right = new NCast(op.right.line, op.right.column, left, op.right);
        }

        return left;
    }, AndAssign),

    ORASSIGN((left, right, op) -> {
        if(!(arePrim(left, right) && isInteger(left) && isInteger(right))){
            throw new Error("Cannot apply operator |= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            op.right = new NCast(op.right.line, op.right.column, left, op.right);
        }

        return left;
    }, AndAssign),

    XORASSIGN((left, right, op) -> {
        if(!(arePrim(left, right) && isInteger(left) && isInteger(right))){
            throw new Error("Cannot apply operator ^= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            op.right = new NCast(op.right.line, op.right.column, left, op.right);
        }

        return left;
    }, AndAssign),

    ASSIGN((left, right, op) -> {
        if(!left.equals(right)){
            throw new Error("Cannot assign value of type " + right + " to variable " + op.left + " of type " + left);
        }

        return left;
    }, Assign);


    private final BinOpTypeMap map;
    private final TokenType op;
    DefaultBinOpTypeMappings(BinOpTypeMap map_, TokenType op_){
        map = map_;
        op = op_;
    }

    private static final EnumMap<TokenType, BinOpTypeMap> opToTypeMapping = new EnumMap<>(TokenType.class);
    static {
        Arrays.stream(values()).forEach((m) -> opToTypeMapping.put(m.op, m.map));
    }

    @Override
    public BinOpTypeMap getFromOp(TokenType op){
        return opToTypeMapping.get(op);
    }

    private static boolean arePrim(Type... t){
        return Arrays.stream(t).allMatch(Type::isPrimitive);
    }

    private static final EnumMap<PrimitiveType, Integer> prec = new EnumMap<>(Map.of(
            PrimitiveType.U8,   0,
            PrimitiveType.I16,  1,
            PrimitiveType.I32,    2,
            PrimitiveType.I64,   3,
            PrimitiveType.F32,  4,
            PrimitiveType.F64, 5
    ));

    private static boolean isNum(Type t){
        if(t instanceof PrimitiveType) {
            return prec.containsKey(t);
        }
        return false;
    }

    private static boolean isBool(Type t){
        if(t instanceof PrimitiveType) {
            return t == PrimitiveType.Bool;
        }
        return false;
    }

    private static boolean isInteger(Type t){
        if(t instanceof PrimitiveType) {
            return t == PrimitiveType.U8 ||
                    t == PrimitiveType.I16 ||
                    t == PrimitiveType.I32 ||
                    t == PrimitiveType.I64;
        }
        return false;
    }

    private static boolean isFloat(Type t){
        if(t instanceof PrimitiveType) {
            return t == PrimitiveType.F32 ||
                    t == PrimitiveType.F64;
        }
        return false;
    }

    private static PrimitiveType castNonDominantGetDominant(PrimitiveType left, PrimitiveType right, NBinOp op){
        if(left == right){
            return left;
        }

        boolean leftDominant = prec.get(left) > prec.get(right);
        PrimitiveType dominant = leftDominant? left : right;

        if(leftDominant) op.right = new NCast(op.right.line, op.right.column, dominant, op.right);
        else op.left = new NCast(op.left.line, op.left.column, dominant, op.left);

        return dominant;
    }
}
