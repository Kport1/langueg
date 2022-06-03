package com.kport.langueg.typeCheck.op;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTTypeE;
import com.kport.langueg.parse.ast.astVals.ASTType;
import com.kport.langueg.typeCheck.types.Type;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static com.kport.langueg.lex.TokenType.*;

public enum DefaultBinOpTypeMappings implements BinOpTypeMappingSupplier{

    PLUS((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isNum(lTok) && isNum(rTok))){
            throw new Error("Cannot add " + left + " and " + right);
        }

        return new Type(castNonDominantGetDominant(lTok, rTok, ast));
    }, Plus),

    MINUS((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isNum(lTok) && isNum(rTok))){
            throw new Error("Cannot subtract " + left + " and " + right);
        }

        return new Type(castNonDominantGetDominant(lTok, rTok, ast));
    }, Minus),

    MUL((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isNum(lTok) && isNum(rTok))){
            throw new Error("Cannot multiply " + left + " and " + right);
        }

        return new Type(castNonDominantGetDominant(lTok, rTok, ast));
    }, Mul),

    DIV((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isNum(lTok) && isNum(rTok))){
            throw new Error("Cannot divide " + left + " and " + right);
        }

        return new Type(castNonDominantGetDominant(lTok, rTok, ast));
    }, Div),

    MOD((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isInteger(lTok) && isInteger(rTok))){
            throw new Error("Cannot apply operator % to " + left + " and " + right);
        }

        return new Type(castNonDominantGetDominant(lTok, rTok, ast));
    }, Mod),

    POW((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isNum(lTok) && isNum(rTok))){
            throw new Error("Cannot apply operator ** to " + left + " and " + right);
        }

        return new Type(castNonDominantGetDominant(lTok, rTok, ast));
    }, Pow),

    SHIFTR((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isInteger(lTok) && isInteger(rTok))){
            throw new Error("Cannot apply operator >> to " + left + " and " + right);
        }

        return new Type(castNonDominantGetDominant(lTok, rTok, ast));
    }, ShiftR),

    SHIFTL((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isInteger(lTok) && isInteger(rTok))){
            throw new Error("Cannot apply operator << to " + left + " and " + right);
        }

        return new Type(castNonDominantGetDominant(lTok, rTok, ast));
    }, ShiftL),

    GREATER((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isNum(lTok) && isNum(rTok))){
            throw new Error("Cannot apply operator < to " + left + " and " + right);
        }

        castNonDominantGetDominant(lTok, rTok, ast);
        return new Type(Boolean);
    }, Greater),

    LESS((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isNum(lTok) && isNum(rTok))){
            throw new Error("Cannot apply operator > to " + left + " and " + right);
        }

        castNonDominantGetDominant(lTok, rTok, ast);
        return new Type(Boolean);
    }, Less),

    GREATEREQ((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isNum(lTok) && isNum(rTok))){
            throw new Error("Cannot apply operator <= to " + left + " and " + right);
        }

        castNonDominantGetDominant(lTok, rTok, ast);
        return new Type(Boolean);
    }, GreaterEq),

    LESSEQ((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isNum(lTok) && isNum(rTok))){
            throw new Error("Cannot apply operator >= to " + left + " and " + right);
        }

        castNonDominantGetDominant(lTok, rTok, ast);
        return new Type(Boolean);
    }, LessEq),

    EQ((left, right, ast) -> {
        if(!(left.equals(right))){
            throw new Error("Cannot apply operator == to " + left + " and " + right);
        }

        return new Type(Boolean);
    }, Eq),

    NOTEQ((left, right, ast) -> {
        if(!(left.equals(right))){
            throw new Error("Cannot apply operator != to " + left + " and " + right);
        }

        return new Type(Boolean);
    }, NotEq),

    AND((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isBool(lTok) && isBool(rTok))){
            if(isInteger(lTok) && isInteger(rTok)){
                return new Type(castNonDominantGetDominant(lTok, rTok, ast));
            }
            throw new Error("Cannot apply operator & to " + left + " and " + right);
        }

        return left;
    }, And),

    ANDAND((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isBool(lTok) && isBool(rTok))){
            throw new Error("Cannot apply operator && to " + left + " and " + right);
        }

        return left;
    }, AndAnd),

    OR((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isBool(lTok) && isBool(rTok))){
            if(isInteger(lTok) && isInteger(rTok)){
                return new Type(castNonDominantGetDominant(lTok, rTok, ast));
            }
            throw new Error("Cannot apply operator | to " + left + " and " + right);
        }

        return left;
    }, Or),

    OROR((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isBool(lTok) && isBool(rTok))){
            throw new Error("Cannot apply operator & to " + left + " and " + right);
        }

        return left;
    }, OrOr),

    XOR((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isBool(lTok) && isBool(rTok))){
            if(isInteger(lTok) && isInteger(rTok)){
                return new Type(castNonDominantGetDominant(lTok, rTok, ast));
            }
            throw new Error("Cannot apply operator | to " + left + " and " + right);
        }

        return left;
    }, XOr),

    PLUSASSIGN((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isNum(lTok) && isNum(rTok))){
            throw new Error("Cannot apply operator += to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, PlusAssign),

    MINUSASSIGN((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isNum(lTok) && isNum(rTok))){
            throw new Error("Cannot apply operator -= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, MinusAssign),

    MULASSIGN((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isNum(lTok) && isNum(rTok))){
            throw new Error("Cannot apply operator *= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, MulAssign),

    DIVASSIGN((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isNum(lTok) && isNum(rTok))){
            throw new Error("Cannot apply operator /= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, DivAssign),

    MODASSIGN((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isInteger(lTok) && isInteger(rTok))){
            throw new Error("Cannot apply operator %= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, ModAssign),

    POWASSIGN((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isNum(lTok) && isNum(rTok))){
            throw new Error("Cannot apply operator **= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, PowAssign),

    SHIFTRASSIGN((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isInteger(lTok) && isInteger(rTok))){
            throw new Error("Cannot apply operator >>= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, ShiftRAssign),

    SHIFTLASSIGN((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isInteger(lTok) && isInteger(rTok))){
            throw new Error("Cannot apply operator <<= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, ShiftLAssign),

    ANDASSIGN((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isInteger(lTok) && isInteger(rTok))){
            throw new Error("Cannot apply operator &= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, AndAssign),

    ORASSIGN((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isInteger(lTok) && isInteger(rTok))){
            throw new Error("Cannot apply operator |= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1]);
            ast.children[1] = cast;
        }

        return left;
    }, AndAssign),

    XORASSIGN((left, right, ast) -> {
        TokenType lTok = left.primitive();
        TokenType rTok = right.primitive();
        if(!(arePrim(left, right) && isInteger(lTok) && isInteger(rTok))){
            throw new Error("Cannot apply operator ^= to " + left + " and " + right);
        }

        if(!right.equals(left)){
            AST cast = new AST(ASTTypeE.Cast, new ASTType(left), ast.children[1]);
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

    private static final EnumMap<TokenType, Integer> prec = new EnumMap<>(Map.of(
            Byte, 0,
            Int, 1,
            Long, 2,
            Float, 3,
            Double, 4
    ));

    private static boolean isNum(TokenType t){
        return prec.containsKey(t);
    }

    private static boolean isBool(TokenType t){
        return t == Boolean;
    }

    private static boolean isInteger(TokenType t){
        return  t == Int ||
                t == Long ||
                t == Byte;
    }

    private static boolean isFloat(TokenType t){
        return  t == Float ||
                t == Double;
    }

    private static TokenType castNonDominantGetDominant(TokenType left, TokenType right, AST ast){
        if(left == right){
            return left;
        }

        boolean leftDominant = prec.get(left) > prec.get(right);
        TokenType dominant = leftDominant? left : right;

        AST unCast = ast.children[leftDominant? 1 : 0];
        ast.children[leftDominant? 1 : 0] = new AST(ASTTypeE.Cast, new ASTType(new Type(dominant)), unCast);
        return dominant;
    }
}
