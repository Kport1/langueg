package com.kport.langueg.parse;

import com.kport.langueg.error.Errors;
import com.kport.langueg.error.stage.parse.ParseException;
import com.kport.langueg.lex.Token;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.BinOp;
import com.kport.langueg.parse.ast.CompoundAssign;
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
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.NBool;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.NStr;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.NTuple;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.NUnion;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.NNumInfer;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.floating.NFloat32;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.floating.NFloat64;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.integer.*;
import com.kport.langueg.parse.ast.nodes.expr.operators.*;
import com.kport.langueg.parse.ast.nodes.statement.NTypeDef;
import com.kport.langueg.parse.ast.nodes.statement.NVar;
import com.kport.langueg.parse.ast.nodes.statement.NVarInit;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.typeCheck.types.*;
import com.kport.langueg.util.Either;
import com.kport.langueg.util.Iterator;
import com.kport.langueg.util.Pair;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class DefaultParser implements Parser {

    private LanguegPipeline<?, ?> pipeline;

    private Iterator<Token> iterator;
    private static final HashMap<TokenType, Integer> opPrecedence = new HashMap<>();

    static {
        opPrecedence.put(TokenType.Assign, 0);

        opPrecedence.put(TokenType.PlusAssign, 0);
        opPrecedence.put(TokenType.MinusAssign, 0);
        opPrecedence.put(TokenType.MulAssign, 0);
        opPrecedence.put(TokenType.DivAssign, 0);
        opPrecedence.put(TokenType.ModAssign, 0);
        opPrecedence.put(TokenType.PowAssign, 0);
        opPrecedence.put(TokenType.ShiftRAssign, 0);
        opPrecedence.put(TokenType.ShiftLAssign, 0);
        opPrecedence.put(TokenType.AndAssign, 0);
        opPrecedence.put(TokenType.OrAssign, 0);
        opPrecedence.put(TokenType.XOrAssign, 0);
        opPrecedence.put(TokenType.BitAndAssign, 0);
        opPrecedence.put(TokenType.BitOrAssign, 0);
        opPrecedence.put(TokenType.BitXOrAssign, 0);

        opPrecedence.put(TokenType.And, 1);
        opPrecedence.put(TokenType.Or, 1);
        opPrecedence.put(TokenType.XOr, 1);

        opPrecedence.put(TokenType.Greater, 2);
        opPrecedence.put(TokenType.Less, 2);
        opPrecedence.put(TokenType.GreaterEq, 2);
        opPrecedence.put(TokenType.LessEq, 2);
        opPrecedence.put(TokenType.Eq, 2);
        opPrecedence.put(TokenType.NotEq, 2);

        opPrecedence.put(TokenType.Plus, 3);
        opPrecedence.put(TokenType.Minus, 3);
        opPrecedence.put(TokenType.Mul, 4);
        opPrecedence.put(TokenType.Div, 4);
        opPrecedence.put(TokenType.Mod, 4);
        opPrecedence.put(TokenType.Pow, 5);

        opPrecedence.put(TokenType.BitAnd, 6);
        opPrecedence.put(TokenType.BitOr, 6);
        opPrecedence.put(TokenType.BitXOr, 6);

        opPrecedence.put(TokenType.ShiftR, 7);
        opPrecedence.put(TokenType.ShiftL, 7);
    }

    @Override
    @SuppressWarnings("unchecked")
    public AST process(Object tokens_, LanguegPipeline<?, ?> pipeline_) {
        ArrayList<Token> tokens = (ArrayList<Token>) tokens_;
        iterator = new Iterator<>(tokens);
        pipeline = pipeline_;

        try {
            return parseProg();
        } catch (ParseException e) {
            System.err.println(e.format());
            System.exit(1);
        }
        return null;
    }

    private NProg parseProg() throws ParseException {
        ArrayList<AST> stmnts = new ArrayList<>();
        while (!iterator.isEOF()) {
            stmnts.add(parseStatement());
        }
        return new NProg(0, stmnts.toArray(AST[]::new));
    }

    private AST parseStatement() throws ParseException {
        Token cur = iterator.current();

        return switch (cur.tok) {

            case Var -> parseVar();

            case Fn -> parseFn();

            case TypeDef -> parseTypeDef();

            default -> parseExpr();
        };
    }

    private NExpr parseExpr() throws ParseException {
        if (iterator.current().tok == TokenType.Semicolon) {
            iterator.inc();
            return new NBlock(iterator.current().offset);
        }

        return parseBinaryOp(
                parseCast(
                        parseUnaryOp(
                                parseDotAccess(
                                        parseCall(parseAtom())
                                )
                        )
                ),
                -1);
    }

    private NExpr parseAtom() throws ParseException {

        if (iterator.isEOF())
            throw new ParseException(Errors.PARSE_ATOM_REACHED_EOF, iterator.previous().offset, pipeline.getSource());

        Token cur = iterator.current();

        switch (cur.tok) {

            case LParen -> {
                return parseTuple();
            }

            case LCurl -> {
                if (iterator.peek().tok == TokenType.Dot)
                    return parseUnion();

                return parseBlock();
            }

            case String -> {
                iterator.inc();
                return new NStr(cur.offset, cur.val);
            }

            case Number -> {
                return parseNumber();
            }

            /*case IntL -> {
                return parseInt();
            }

            case FloatL -> {
                return parseFloat();
            }*/

            case Identifier -> {
                iterator.inc();
                return new NIdent(cur.offset, cur.val);
            }

            case True -> {
                iterator.inc();
                return new NBool(cur.offset, true);
            }

            case False -> {
                iterator.inc();
                return new NBool(cur.offset, false);
            }

            case If -> {
                return parseIf();
            }

            case Match -> {
                return parseMatch();
            }

            case While -> {
                return parseWhile();
            }

            case Fn -> {
                return parseAnonFn();
            }

            case Return -> {
                iterator.inc();
                return new NReturn(cur.offset, parseExpr());
            }

            case Minus, Not, Inc, Dec -> {
                iterator.inc();
                return new NUnaryOpPre(cur.offset, parseUnaryOp(parseDotAccess(parseCall(parseAtom()))), cur.tok);
            }

            case And -> {
                iterator.inc();
                return new NRef(cur.offset, parseUnaryOp(parseDotAccess(parseCall(parseAtom()))));
            }

            case Mul -> {
                iterator.inc();
                return new NDeRef(cur.offset, parseUnaryOp(parseDotAccess(parseCall(parseAtom()))));
            }
        }

        throw new ParseException(Errors.PARSE_ATOM_UNEXPECTED_TOKEN, cur.offset, pipeline.getSource(), cur.tok.expandedName());
    }

    private NExpr parseBinaryOp(NExpr left, int lastPrec) throws ParseException {
        Token cur = iterator.current();
        if (!(BinOp.isBinOp(cur.tok) || CompoundAssign.isCompoundAssign(cur.tok) || cur.tok == TokenType.Assign))
            return left;

        int currentPrec = opPrecedence.get(cur.tok);
        if (currentPrec <= lastPrec) return left;

        iterator.inc();
        NExpr right = parseBinaryOp(parseCast(parseUnaryOp(parseDotAccess(parseCall(parseAtom())))), currentPrec);

        if (cur.tok == TokenType.Assign) {
            if (!(left instanceof NAssignable assignable)) throw new Error("Cannot assign a value to:\n" + left);
            return parseBinaryOp(new NAssign(cur.offset, assignable, right), lastPrec);
        }
        if (CompoundAssign.isCompoundAssign(cur.tok)) {
            if (!(left instanceof NAssignable assignable)) throw new Error("Cannot assign a value to:\n" + left);
            return parseBinaryOp(new NAssignCompound(cur.offset, assignable, right, cur.tok), lastPrec);
        }
        if (BinOp.isBinOp(cur.tok)) {
            return parseBinaryOp(new NBinOp(cur.offset, left, right, cur.tok), lastPrec);
        }
        throw new Error();
    }

    private NExpr parseUnaryOp(NExpr left) {
        Token cur = iterator.current();
        if (!cur.tok.isUnaryOpPost()) return left;

        iterator.inc();
        return new NUnaryOpPost(cur.offset, left, cur.tok);
    }

    private NExpr parseDotAccess(NExpr left) throws ParseException {
        Token cur = iterator.current();
        if (cur.tok != TokenType.Dot) return left;
        iterator.inc();

        Either<Integer, String> accessor = parseDotAccessor();
        iterator.inc();
        if (accessor == null) throw new ParseException(Errors.PLACEHOLDER, cur.offset + 1, pipeline.getSource());

        return parseDotAccess(new NDotAccess(cur.offset, left, accessor));
    }

    private Either<Integer, String> parseDotAccessor() throws ParseException {
        Token cur = iterator.current();
        iterator.inc();
        return switch (cur.tok) {
            case TokenType.Number -> Either.left(Integer.parseInt(cur.val));

            case TokenType.Identifier -> Either.right(cur.val);

            default -> throw new ParseException(Errors.PLACEHOLDER, cur.offset, pipeline.getSource());
        };
    }

    private NExpr parseCall(NExpr left) throws ParseException {
        Token cur = iterator.current();
        if (cur.tok != TokenType.LParen) return left;

        NExpr[] args = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);
        return parseCall(new NCall(cur.offset, left, args));
    }

    private NExpr parseCast(NExpr left) throws ParseException {
        Token cur = iterator.current();
        if (cur.tok != TokenType.As) return left;

        iterator.inc();
        return new NCast(cur.offset, parseType(), left);
    }

    @SuppressWarnings("unchecked")
    private NExpr parseTuple() throws ParseException {
        Token cur = iterator.current();

        final boolean[] trailingComma = {false};
        Pair<Either<Integer, String>, NExpr>[] exprs = parseDelimAs(TokenType.LParen, TokenType.RParen, TokenType.Comma, i -> {
            trailingComma[0] = false;

            Either<Integer, String> position = null;
            if (iterator.current().tok == TokenType.Dot) {
                iterator.inc();
                position = parseDotAccessor();
                if (iterator.current().tok != TokenType.Assign)
                    throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
                iterator.inc();
            }

            NExpr expr = parseExpr();
            if (iterator.current().tok == TokenType.Comma)
                trailingComma[0] = true;
            return new Pair<>(position, expr);
        }, Pair.class);

        if (!trailingComma[0] && exprs.length == 1 && exprs[0].left == null) return exprs[0].right;

        boolean[] elemIsSet = new boolean[exprs.length];
        HashSet<String> usedNames = new HashSet<>();
        for (int i = 0; i < exprs.length; i++) {
            if (exprs[i].left == null) {
                if (elemIsSet[i])
                    throw new ParseException(Errors.PLACEHOLDER, exprs[i].right.codeOffset(), pipeline.getSource());
                elemIsSet[i] = true;
                continue;
            }

            switch (exprs[i].left) {
                case Either.Left<Integer, String> index -> {
                    if (index.value() >= exprs.length)
                        throw new ParseException(Errors.PLACEHOLDER, 0, pipeline.getSource());
                    if (elemIsSet[index.value()])
                        throw new ParseException(Errors.PLACEHOLDER, 0, pipeline.getSource());
                    elemIsSet[index.value()] = true;
                }
                case Either.Right<Integer, String> name -> {
                    if (usedNames.contains(name.value()))
                        throw new ParseException(Errors.PLACEHOLDER, 0, pipeline.getSource());
                    usedNames.add(name.value());
                }
            }
        }

        return new NTuple(cur.offset, exprs);
    }

    private NUnion parseUnion() throws ParseException {
        Token cur = iterator.current();
        if (iterator.current().tok != TokenType.LCurl)
            throw new ParseException(Errors.PLACEHOLDER, cur.offset, pipeline.getSource());

        if (iterator.next().tok != TokenType.Dot)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
        iterator.inc();

        Either<Integer, String> position = parseDotAccessor();
        if (position == null) throw new ParseException(Errors.PLACEHOLDER, cur.offset + 1, pipeline.getSource());

        if (iterator.current().tok != TokenType.Assign)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());

        iterator.inc();
        NExpr expr = parseExpr();

        if (iterator.current().tok != TokenType.RCurl)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
        iterator.inc();

        return new NUnion(cur.offset, expr, position);
    }

    private NExpr parseIf() throws ParseException {
        Token cur = iterator.current();
        iterator.inc();

        NExpr condition = parseEnclosed(TokenType.LParen, TokenType.RParen);

        NExpr block = parseExpr();

        if (iterator.current().tok == TokenType.Else) {
            iterator.inc();
            NExpr elseBlock = parseExpr();

            return new NIfElse(cur.offset, condition, block, elseBlock);
        }

        return new NIf(cur.offset, condition, block);
    }

    @SuppressWarnings("unchecked")
    private NMatch parseMatch() throws ParseException {
        Token cur = iterator.current();
        iterator.inc();

        NExpr value = parseEnclosed(TokenType.LParen, TokenType.RParen);

        ArrayList<Pair<NMatch.Pattern, NExpr>> branches = new ArrayList<>();
        while (iterator.current().tok == TokenType.Case) {
            if (iterator.next().tok == TokenType.Dot) {
                int elemOffset = iterator.next().offset;
                Either<Integer, String> element = parseDotAccessor();
                if (element == null) throw new ParseException(Errors.PLACEHOLDER, elemOffset, pipeline.getSource());

                if (iterator.current().tok != TokenType.Identifier)
                    throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
                String elementVarName = iterator.current().val;

                if (iterator.next().tok != TokenType.DoubleArrow)
                    throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
                iterator.inc();

                NExpr expr = parseExpr();
                branches.add(new Pair<>(new NMatch.Pattern.Union(element, elementVarName), expr));
            } else {
                throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
            }
        }

        if (iterator.current().tok == TokenType.Else) {
            if (iterator.next().tok != TokenType.DoubleArrow)
                throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
            iterator.inc();
            NExpr expr = parseExpr();
            branches.add(new Pair<>(new NMatch.Pattern.Default(), expr));
        }

        return new NMatch(cur.offset, value, branches.toArray(new Pair[0]));
    }

    private NWhile parseWhile() throws ParseException {
        Token cur = iterator.current();
        iterator.inc();

        NExpr condition = parseEnclosed(TokenType.LParen, TokenType.RParen);

        NExpr block = parseExpr();

        return new NWhile(cur.offset, condition, block);
    }

    private NAnonFn parseAnonFn() throws ParseException {
        Token cur = iterator.current();
        iterator.inc();

        FnHeader header = parseFnHeader();
        NExpr body = parseExpr();

        return new NAnonFn(cur.offset, header, body);
    }

    private NNamedFn parseFn() throws ParseException {
        Token cur = iterator.current();

        if (iterator.next().tok != TokenType.Identifier)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
        String name = iterator.current().val;

        iterator.inc();

        FnHeader header = parseFnHeader();
        NExpr body = parseExpr();

        return new NNamedFn(cur.offset, name, header, body);
    }

    private FnHeader parseFnHeader() throws ParseException {
        return parseEnclosedAs(TokenType.LParen, TokenType.RParen, (v) -> {
            NameTypePair[] params = parseFnParams();

            if (iterator.current().tok != TokenType.SingleArrow)
                throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
            iterator.inc();

            return new FnHeader(params, parseType());
        });
    }

    private NameTypePair[] parseFnParams() throws ParseException {
        return parseDelimAs(TokenType.LParen, TokenType.RParen, TokenType.Comma, (i) -> {
            if (iterator.current().tok != TokenType.Identifier)
                throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
            String name = iterator.current().val;

            if (iterator.next().tok != TokenType.Colon)
                throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
            iterator.inc();

            Type type = parseType();

            return new NameTypePair(type, name);
        }, NameTypePair.class);
    }

    private NExpr parseBlock() throws ParseException {
        Token cur = iterator.current();

        ArrayList<AST> stmnts = new ArrayList<>();

        if (iterator.next().tok == TokenType.RCurl) {
            iterator.inc();
            return new NBlock(cur.offset);
        }

        do {
            stmnts.add(parseStatement());

            if (iterator.current().tok == TokenType.RCurl && !iterator.isEOF()) {
                iterator.inc();

                if (iterator.current().tok == TokenType.DoubleArrow) {
                    iterator.inc();
                    AST lastStatement = stmnts.getLast();

                    if (!(lastStatement instanceof NExpr lastExpr))
                        throw new ParseException(Errors.PLACEHOLDER, lastStatement.codeOffset(), pipeline.getSource());

                    stmnts.removeLast();
                    return new NBlockYielding(cur.offset, lastExpr, stmnts.toArray(new AST[0]));
                }

                return new NBlock(cur.offset, stmnts.toArray(new AST[0]));
            }
        } while (!iterator.isEOF());


        throw new ParseException(Errors.PLACEHOLDER, cur.offset, pipeline.getSource());
    }

    private AST parseVar() throws ParseException {
        Token cur = iterator.current();

        if (iterator.next().tok != TokenType.Identifier)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
        String name = iterator.current().val;

        Type type = null;
        if (iterator.next().tok == TokenType.Colon) {
            iterator.inc();
            type = parseType();
        }

        NExpr init = null;
        if (iterator.current().tok == TokenType.Assign) {
            iterator.inc();
            init = parseExpr();
        }

        return init == null ? new NVar(cur.offset, type, name) : new NVarInit(cur.offset, type, name, init);
    }

    private NTypeDef parseTypeDef() throws ParseException {
        Token cur = iterator.current();

        String[] typeParameters = new String[0];

        if (iterator.next().tok == TokenType.Greater) {
            typeParameters = parseDelimAs(TokenType.Greater, TokenType.Less, TokenType.Comma, (i) ->
                    {
                        if (iterator.current().tok != TokenType.Identifier)
                            throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
                        String str = iterator.current().val;
                        iterator.inc();
                        return str;
                    }
                    , String.class);
        }

        if (iterator.current().tok != TokenType.Identifier)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
        String name = iterator.current().val;

        if (iterator.next().tok != TokenType.Assign)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
        iterator.inc();

        return new NTypeDef(cur.offset, name, parseType(), typeParameters);
    }

    private NExpr parseNumber() throws ParseException {
        Token cur = iterator.current();

        if (cur.tok != TokenType.Number) throw new ParseException(Errors.PLACEHOLDER, cur.offset, pipeline.getSource());
        String integerPartString = cur.val;
        String decimalPartString = "";
        if (iterator.next().tok == TokenType.Dot) {
            if (iterator.next().tok != TokenType.Number)
                throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
            decimalPartString = iterator.current().val;
            iterator.inc();
        }

        BigInteger integerPart = new BigInteger(integerPartString);

        switch (iterator.current().tok) {
            case U8 -> {
                if (!decimalPartString.isEmpty())
                    throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
                if (integerPart.compareTo(new BigInteger(new byte[]{1, 0})) != -1)
                    throw new ParseException(Errors.PLACEHOLDER, cur.offset, pipeline.getSource());
                iterator.inc();
                return new NUInt8(cur.offset, integerPart.byteValue());
            }

            case U16 -> {
                if (!decimalPartString.isEmpty())
                    throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
                if (integerPart.compareTo(new BigInteger(new byte[]{1, 0, 0})) != -1)
                    throw new ParseException(Errors.PLACEHOLDER, cur.offset, pipeline.getSource());
                iterator.inc();
                return new NUInt16(cur.offset, integerPart.shortValue());
            }

            case U32 -> {
                if (!decimalPartString.isEmpty())
                    throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
                if (integerPart.compareTo(new BigInteger(new byte[]{1, 0, 0, 0, 0})) != -1)
                    throw new ParseException(Errors.PLACEHOLDER, cur.offset, pipeline.getSource());
                iterator.inc();
                return new NUInt32(cur.offset, integerPart.intValue());
            }

            case U64 -> {
                if (!decimalPartString.isEmpty())
                    throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
                if (integerPart.compareTo(new BigInteger(new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0})) != -1)
                    throw new ParseException(Errors.PLACEHOLDER, cur.offset, pipeline.getSource());
                iterator.inc();
                return new NUInt64(cur.offset, integerPart.longValue());
            }

            case I8 -> {
                if (!decimalPartString.isEmpty())
                    throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
                if (integerPart.compareTo(new BigInteger(new byte[]{0, -128})) != -1)
                    throw new ParseException(Errors.PLACEHOLDER, cur.offset, pipeline.getSource());
                iterator.inc();
                return new NInt8(cur.offset, integerPart.byteValue());
            }

            case I16 -> {
                if (!decimalPartString.isEmpty())
                    throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
                if (integerPart.compareTo(new BigInteger(new byte[]{0, -128, 0})) != -1)
                    throw new ParseException(Errors.PLACEHOLDER, cur.offset, pipeline.getSource());
                iterator.inc();
                return new NInt16(cur.offset, integerPart.shortValue());
            }

            case I32 -> {
                if (!decimalPartString.isEmpty())
                    throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
                if (integerPart.compareTo(new BigInteger(new byte[]{0, -128, 0, 0, 0})) != -1)
                    throw new ParseException(Errors.PLACEHOLDER, cur.offset, pipeline.getSource());
                iterator.inc();
                return new NInt32(cur.offset, integerPart.intValue());
            }

            case I64 -> {
                if (!decimalPartString.isEmpty())
                    throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
                if (integerPart.compareTo(new BigInteger(new byte[]{0, -128, 0, 0, 0, 0, 0, 0, 0})) != -1)
                    throw new ParseException(Errors.PLACEHOLDER, cur.offset, pipeline.getSource());
                iterator.inc();
                return new NInt64(cur.offset, integerPart.longValue());
            }

            case F32 -> {
                BigDecimal decimal = new BigDecimal(integerPartString + "." + decimalPartString);
                iterator.inc();
                return new NFloat32(cur.offset, decimal.floatValue());
            }

            case F64 -> {
                BigDecimal decimal = new BigDecimal(integerPartString + "." + decimalPartString);
                iterator.inc();
                return new NFloat64(cur.offset, decimal.doubleValue());
            }
        }

        return new NNumInfer(cur.offset, integerPartString + (decimalPartString.isEmpty() ? "" : "." + decimalPartString));
    }

    private Type parseType() throws ParseException {
        return unwrapTuple(parseFnType(parseArrayType(parseTypeAtom())));
    }

    public Type unwrapTuple(Type type) {
        if (type instanceof TupleType tup && tup.tupleTypes().length == 1)
            return tup.tupleTypes()[0];
        return type;
    }

    private Type parseTypeAtom() throws ParseException {
        Token cur = iterator.current();
        iterator.inc();

        switch (cur.tok) {
            case Bool -> {
                return PrimitiveType.Bool;
            }
            case Char -> {
                return PrimitiveType.Char;
            }

            case U8 -> {
                return PrimitiveType.U8;
            }
            case U16 -> {
                return PrimitiveType.U16;
            }
            case U32 -> {
                return PrimitiveType.U32;
            }
            case U64 -> {
                return PrimitiveType.U64;
            }

            case I8 -> {
                return PrimitiveType.I8;
            }
            case I16 -> {
                return PrimitiveType.I16;
            }
            case I32 -> {
                return PrimitiveType.I32;
            }
            case I64 -> {
                return PrimitiveType.I64;
            }

            case F32 -> {
                return PrimitiveType.F32;
            }
            case F64 -> {
                return PrimitiveType.F64;
            }

            case LParen -> {
                iterator.dec();
                return parseTupleType();
            }

            case LCurl -> {
                iterator.dec();
                return parseUnionType();
            }

            case And -> {
                return new RefType(parseType());
            }

            case Identifier -> {
                String name = cur.val;
                Type[] typeArgs = new Type[0];
                if (iterator.current().tok == TokenType.Greater) {
                    typeArgs = parseDelimAs(TokenType.Greater, TokenType.Less, TokenType.Comma, (i) -> parseType(), Type.class);
                }
                return new NamedType(name, typeArgs);
            }
        }

        return null;
    }

    private Type parseArrayType(Type left) {
        if (iterator.current().tok == TokenType.LBrack && iterator.next().tok == TokenType.RBrack) {
            iterator.inc();
            return new ArrayType(left);
        }
        return left;
    }

    private Type parseFnType(Type left) throws ParseException {
        if (iterator.current().tok != TokenType.SingleArrow)
            return left;
        iterator.inc();

        Type returnType = parseFnType(parseArrayType(parseTypeAtom()));

        return new FnType(returnType, left instanceof TupleType tup ? tup.tupleTypes() : new Type[]{left});
    }

    public TupleType parseTupleType() throws ParseException {
        NameTypePair[] nameTypePairs = parseDelimAs(TokenType.LParen, TokenType.RParen, TokenType.Comma, (i) -> {
            String name = null;
            if (iterator.current().tok == TokenType.Identifier && iterator.peek().tok == TokenType.Colon) {
                name = iterator.current().val;
                iterator.inc();
                iterator.inc();
            }
            Type type = parseFnType(parseArrayType(parseTypeAtom()));
            return new NameTypePair(type, name);
        }, NameTypePair.class);

        for (int i = 0; i < nameTypePairs.length; i++) {
            if (nameTypePairs[i].name == null) continue;
            for (int j = 0; j < nameTypePairs.length; j++) {
                if (i == j) continue;
                if (nameTypePairs[j].name == null) continue;

                if (Objects.equals(nameTypePairs[i].name, nameTypePairs[j].name))
                    throw new ParseException(Errors.PLACEHOLDER, 0, pipeline.getSource());
            }
        }

        return new TupleType(nameTypePairs);
    }

    public UnionType parseUnionType() throws ParseException {
        NameTypePair[] nameTypePairs = parseDelimAs(TokenType.LCurl, TokenType.RCurl, TokenType.Comma, (i) -> {
            String name = null;
            if (iterator.current().tok == TokenType.Identifier && iterator.peek().tok == TokenType.Colon) {
                name = iterator.current().val;
                iterator.inc();
                iterator.inc();
            }
            Type type = parseFnType(parseArrayType(parseTypeAtom()));
            return new NameTypePair(type, name);
        }, NameTypePair.class);

        for (int i = 0; i < nameTypePairs.length; i++) {
            if (nameTypePairs[i].name == null) continue;
            for (int j = 0; j < nameTypePairs.length; j++) {
                if (i == j) continue;
                if (nameTypePairs[j].name == null) continue;

                if (Objects.equals(nameTypePairs[i].name, nameTypePairs[j].name))
                    throw new ParseException(Errors.PLACEHOLDER, 0, pipeline.getSource());
            }
        }

        return new UnionType(nameTypePairs);
    }

    private NExpr parseEnclosed(TokenType start, TokenType end) throws ParseException {
        return parseEnclosedAs(start, end, (v) -> parseExpr());
    }

    private <T> T parseEnclosedAs(TokenType start, TokenType end, FunctionThrowsParseException<Void, T> sup) throws ParseException {
        if (iterator.current().tok != start)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
        iterator.inc();

        T ret = sup.apply(null);

        if (iterator.current().tok != end)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().offset, pipeline.getSource());
        iterator.inc();

        return ret;
    }

    private NExpr[] parseDelim(TokenType start, TokenType end, TokenType separator) throws ParseException {
        return parseDelimAs(start, end, separator, i -> parseExpr(), NExpr.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T[] parseDelimAs(TokenType start, TokenType end, TokenType separator, FunctionThrowsParseException<Integer, T> fn, Class<T> clazz) throws ParseException {
        if (iterator.current().tok != start)
            throw new ParseException(Errors.PARSE_DELIM_EXPECTED_START, iterator.current().offset, pipeline.getSource(), start.expandedName());

        ArrayList<T> exprs = new ArrayList<>();

        if (iterator.next().tok == end) {
            iterator.inc();
            return (T[]) Array.newInstance(clazz, 0);
        }

        int i = 0;
        while (iterator.current().tok != end) {
            exprs.add(fn.apply(i));
            i++;

            if (iterator.current().tok == end) {
                break;
            }

            if (iterator.current().tok != separator)
                throw new ParseException(Errors.PARSE_DELIM_EXPECTED_SEPARATOR, iterator.current().offset, pipeline.getSource(), separator.expandedName());

            iterator.inc();
        }
        iterator.inc();

        return exprs.toArray((T[]) Array.newInstance(clazz, 0));
    }
}