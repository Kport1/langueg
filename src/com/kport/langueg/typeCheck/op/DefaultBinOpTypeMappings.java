package com.kport.langueg.typeCheck.op;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTTypeE;
import com.kport.langueg.parse.ast.astVals.ASTType;
import com.kport.langueg.typeCheck.types.PrimitiveType;
import com.kport.langueg.typeCheck.types.Type;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static com.kport.langueg.lex.TokenType.*;

public enum DefaultBinOpTypeMappings implements BinOpTypeMappingSupplier{

    PLUS((left, right, ast) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot add " + left + " and " + right);
        }

        return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, ast);
    }, Plus),

    MINUS((left, right, ast) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot subtract " + left + " and " + right);
        }

        return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, ast);
    }, Minus),

    MUL((left, right, ast) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot multiply " + left + " and " + right);
        }

        return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, ast);
    }, Mul),

    DIV((left, right, ast) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot divide " + left + " and " + right);
        }

        return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, ast);
    }, Div),

    MOD((left, right, ast) -> {
        if(!(arePrim(left, right) && isInteger(left) && isInteger(right))){
            throw new Error("Cannot apply operator % to " + left + " and " + right);
        }

        return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, ast);
    }, Mod),

    POW((left, right, ast) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot apply operator ** to " + left + " and " + right);
        }

        return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, ast);
    }, Pow),

    SHIFTR((left, right, ast) -> {
        if(!(arePrim(left, right) && isInteger(left) && isInteger(right))){
            throw new Error("Cannot apply operator >> to " + left + " and " + right);
        }

        return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, ast);
    }, ShiftR),

    SHIFTL((left, right, ast) -> {
        if(!(arePrim(left, right) && isInteger(left) && isInteger(right))){
            throw new Error("Cannot apply operator << to " + left + " and " + right);
        }

        return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, ast);
    }, ShiftL),

    GREATER((left, right, ast) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot apply operator < to " + left + " and " + right);
        }

        castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, ast);
        return PrimitiveType.Boolean;
    }, Greater),

    LESS((left, right, ast) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot apply operator > to " + left + " and " + right);
        }

        castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, ast);
        return PrimitiveType.Boolean;
    }, Less),

    GREATEREQ((left, right, ast) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot apply operator <= to " + left + " and " + right);
        }

        castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, ast);
        return PrimitiveType.Boolean;
    }, GreaterEq),

    LESSEQ((left, right, ast) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot apply operator >= to " + left + " and " + right);
        }

        castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, ast);
        return PrimitiveType.Boolean;
    }, LessEq),

    EQ((left, right, ast) -> {
        if(!(left.equals(right))){
            throw new Error("Cannot apply operator == to " + left + " and " + right);
        }

        return PrimitiveType.Boolean;
    }, Eq),

    NOTEQ((left, right, ast) -> {
        if(!(left.equals(right))){
            throw new Error("Cannot apply operator != to " + left + " and " + right);
        }

        return PrimitiveType.Boolean;
    }, NotEq),

    AND((left, right, ast) -> {
        if(!(arePrim(left, right) && isBool(left) && isBool(right))){
            if(isInteger(left) && isInteger(right)){
                return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, ast);
            }
            throw new Error("Cannot apply operator & to " + left + " and " + right);
        }

        return left;
    }, And),

    ANDAND((left, right, ast) -> {
        if(!(arePrim(left, right) && isBool(left) && isBool(right))){
            throw new Error("Cannot apply operator && to " + left + " and " + right);
        }

        return left;
    }, AndAnd),

    OR((left, right, ast) -> {
        if(!(arePrim(left, right) && isBool(left) && isBool(right))){
            if(isInteger(left) && isInteger(right)){
                return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, ast);
            }
            throw new Error("Cannot apply operator | to " + left + " and " + right);
        }

        return left;
    }, Or),

    OROR((left, right, ast) -> {
        if(!(arePrim(left, right) && isBool(left) && isBool(right))){
            throw new Error("Cannot apply operator & to " + left + " and " + right);
        }

        return left;
    }, OrOr),

    XOR((left, right, ast) -> {
        if(!(arePrim(left, right) && isBool(left) && isBool(right))){
            if(isInteger(left) && isInteger(right)){
                return castNonDominantGetDominant((PrimitiveType) left, (PrimitiveType) right, ast);
            }
            throw new Error("Cannot apply operator | to " + left + " and " + right);
        }

        return left;
    }, XOr),

    PLUSASSIGN((left, right, ast) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot apply operator += to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1].line, ast.children[1].column, ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, PlusAssign),

    MINUSASSIGN((left, right, ast) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot apply operator -= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1].line, ast.children[1].column, ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, MinusAssign),

    MULASSIGN((left, right, ast) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot apply operator *= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1].line, ast.children[1].column, ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, MulAssign),

    DIVASSIGN((left, right, ast) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot apply operator /= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1].line, ast.children[1].column, ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, DivAssign),

    MODASSIGN((left, right, ast) -> {
        if(!(arePrim(left, right) && isInteger(left) && isInteger(right))){
            throw new Error("Cannot apply operator %= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1].line, ast.children[1].column, ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, ModAssign),

    POWASSIGN((left, right, ast) -> {
        if(!(arePrim(left, right) && isNum(left) && isNum(right))){
            throw new Error("Cannot apply operator **= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1].line, ast.children[1].column, ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, PowAssign),

    SHIFTRASSIGN((left, right, ast) -> {
        if(!(arePrim(left, right) && isInteger(left) && isInteger(right))){
            throw new Error("Cannot apply operator >>= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1].line, ast.children[1].column, ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, ShiftRAssign),

    SHIFTLASSIGN((left, right, ast) -> {
        if(!(arePrim(left, right) && isInteger(left) && isInteger(right))){
            throw new Error("Cannot apply operator <<= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1].line, ast.children[1].column, ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, ShiftLAssign),

    ANDASSIGN((left, right, ast) -> {
        if(!(arePrim(left, right) && isInteger(left) && isInteger(right))){
            throw new Error("Cannot apply operator &= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1].line, ast.children[1].column, ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, AndAssign),

    ORASSIGN((left, right, ast) -> {
        if(!(arePrim(left, right) && isInteger(left) && isInteger(right))){
            throw new Error("Cannot apply operator |= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1].line, ast.children[1].column, ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, AndAssign),

    XORASSIGN((left, right, ast) -> {
        if(!(arePrim(left, right) && isInteger(left) && isInteger(right))){
            throw new Error("Cannot apply operator ^= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1].line, ast.children[1].column, ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, AndAssign),

    ASSIGN((left, right, ast) -> {
        if(!left.equals(right)){
            throw new Error("Cannot assign value of type " + right + " to variable " + ast.children[0].val.getStr() + " of type " + left);
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
            PrimitiveType.Byte,   0,
            PrimitiveType.Short,  1,
            PrimitiveType.Int,    2,
            PrimitiveType.Long,   3,
            PrimitiveType.Float,  4,
            PrimitiveType.Double, 5
    ));

    private static boolean isNum(Type t){
        if(t instanceof PrimitiveType) {
            return prec.containsKey(t);
        }
        return false;
    }

    private static boolean isBool(Type t){
        if(t instanceof PrimitiveType) {
            return t == PrimitiveType.Boolean;
        }
        return false;
    }

    private static boolean isInteger(Type t){
        if(t instanceof PrimitiveType) {
            return t == PrimitiveType.Byte ||
                    t == PrimitiveType.Short ||
                    t == PrimitiveType.Int ||
                    t == PrimitiveType.Long;
        }
        return false;
    }

    private static boolean isFloat(Type t){
        if(t instanceof PrimitiveType) {
            return t == PrimitiveType.Float ||
                    t == PrimitiveType.Double;
        }
        return false;
    }

    private static PrimitiveType castNonDominantGetDominant(PrimitiveType left, PrimitiveType right, AST ast){
        if(left == right){
            return left;
        }

        boolean leftDominant = prec.get(left) > prec.get(right);
        PrimitiveType dominant = leftDominant? left : right;

        AST unCast = ast.children[leftDominant? 1 : 0];
        ast.children[leftDominant? 1 : 0] = new AST(ASTTypeE.Cast, new ASTType(dominant), unCast.line, unCast.column, unCast);
        return dominant;
    }
}
