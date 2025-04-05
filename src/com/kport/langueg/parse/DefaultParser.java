package com.kport.langueg.parse;

import com.kport.langueg.error.Errors;
import com.kport.langueg.error.stage.parse.ParseException;
import com.kport.langueg.lex.Token;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.BinOp;
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
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.NNumInfer;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.floating.NFloat32;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.floating.NFloat64;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.integer.*;
import com.kport.langueg.parse.ast.nodes.expr.operators.*;
import com.kport.langueg.parse.ast.nodes.statement.NTypeDef;
import com.kport.langueg.parse.ast.nodes.statement.NVarInit;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.typeCheck.types.*;
import com.kport.langueg.util.Either;
import com.kport.langueg.util.Iterator;
import com.kport.langueg.util.Pair;
import com.kport.langueg.util.Span;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class DefaultParser implements Parser {

    private LanguegPipeline<?, ?> pipeline;

    private Iterator<Token> iterator;
    private static final HashMap<BinOp, Integer> opPrecedence = new HashMap<>();

    static {
        opPrecedence.put(BinOp.And, 1);
        opPrecedence.put(BinOp.Or, 1);
        opPrecedence.put(BinOp.XOr, 1);

        opPrecedence.put(BinOp.Greater, 2);
        opPrecedence.put(BinOp.Less, 2);
        opPrecedence.put(BinOp.GreaterEq, 2);
        opPrecedence.put(BinOp.LessEq, 2);
        opPrecedence.put(BinOp.Eq, 2);
        opPrecedence.put(BinOp.NotEq, 2);

        opPrecedence.put(BinOp.Plus, 3);
        opPrecedence.put(BinOp.Minus, 3);
        opPrecedence.put(BinOp.Mul, 4);
        opPrecedence.put(BinOp.Div, 4);
        opPrecedence.put(BinOp.Mod, 4);
        opPrecedence.put(BinOp.Pow, 5);

        opPrecedence.put(BinOp.BitAnd, 6);
        opPrecedence.put(BinOp.BitOr, 6);
        opPrecedence.put(BinOp.BitXOr, 6);

        opPrecedence.put(BinOp.ShiftR, 7);
        opPrecedence.put(BinOp.ShiftL, 7);
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
        return new NProg(new Span(0, iterator.last().location().end()), stmnts.toArray(AST[]::new));
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
            return new NBlock(iterator.current().location());
        }

        return parseBinOpRec(
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
            throw new ParseException(Errors.PARSE_ATOM_REACHED_EOF, iterator.previous().location(), pipeline.getSource());

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

            case LBrack -> {
                return parseArray();
            }

            case String -> {
                iterator.inc();
                return new NStr(cur.location(), cur.val);
            }

            case Number -> {
                return parseNumber();
            }

            case Identifier -> {
                iterator.inc();
                return new NIdent(cur.location(), cur.val);
            }

            case True -> {
                iterator.inc();
                return new NBool(cur.location(), true);
            }

            case False -> {
                iterator.inc();
                return new NBool(cur.location(), false);
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
                NExpr expr = parseExpr();
                return new NReturn(Span.union(cur.location(), expr.location()), expr);
            }

            case Minus, Not, Inc, Dec -> {
                iterator.inc();
                NExpr expr = parseUnaryOp(parseDotAccess(parseCall(parseAtom())));
                return new NUnaryOpPre(Span.union(cur.location(), expr.location()), expr, cur.tok);
            }

            case And -> {
                iterator.inc();
                NExpr expr = parseUnaryOp(parseDotAccess(parseCall(parseAtom())));
                return new NRef(Span.union(cur.location(), expr.location()), expr);
            }

            case Mul -> {
                iterator.inc();
                NExpr expr = parseUnaryOp(parseDotAccess(parseCall(parseAtom())));
                return new NDeRef(Span.union(cur.location(), expr.location()), expr);
            }
        }

        throw new ParseException(Errors.PARSE_ATOM_UNEXPECTED_TOKEN, cur.location(), pipeline.getSource(), cur.tok.expandedName());
    }

    private BinOp parseBinOp() {
        Token cur = iterator.current();
        iterator.inc();
        if (cur.tok == TokenType.Less && iterator.current().tok == TokenType.Less) {
            iterator.inc();
            return BinOp.ShiftR;
        }
        if (cur.tok == TokenType.Greater && iterator.current().tok == TokenType.Greater) {
            iterator.inc();
            return BinOp.ShiftL;
        }
        if (cur.tok == TokenType.Less && iterator.current().tok == TokenType.Assign) {
            iterator.inc();
            return BinOp.LessEq;
        }
        if (cur.tok == TokenType.Greater && iterator.current().tok == TokenType.Assign) {
            iterator.inc();
            return BinOp.GreaterEq;
        }
        return BinOp.fromTokenType(cur.tok);
    }

    private NExpr parseBinOpRec(NExpr left, int lastPrec) throws ParseException {
        Token cur = iterator.current();
        if (cur.tok == TokenType.Assign)
            return parseAssign(left);
        if (!BinOp.isBinOp(cur.tok))
            return left;

        int iterIndex = iterator.getIndex();
        BinOp op = parseBinOp();
        if (iterator.current().tok == TokenType.Assign)
            return parseCompoundAssign(left, op);
        int currentPrec = opPrecedence.get(op);
        if (currentPrec <= lastPrec) {
            iterator.setIndex(iterIndex);
            return left;
        }

        NExpr right = parseBinOpRec(parseCast(parseUnaryOp(parseDotAccess(parseCall(parseAtom())))), currentPrec);
        return parseBinOpRec(new NBinOp(Span.union(left.location(), right.location()), left, right, op), lastPrec);
    }

    private NExpr parseAssign(NExpr left) throws ParseException {
        if (!(left instanceof NAssignable leftAssignable)) {
            throw new ParseException(Errors.PLACEHOLDER, left.location(), pipeline.getSource());
        }
        iterator.inc();
        NExpr expr = parseExpr();
        return new NAssign(Span.union(left.location(), expr.location()), leftAssignable, expr);
    }

    private NExpr parseCompoundAssign(NExpr left, BinOp op) throws ParseException {
        if (!(left instanceof NAssignable leftAssignable)) {
            throw new ParseException(Errors.PLACEHOLDER, left.location(), pipeline.getSource());
        }
        iterator.inc();
        NExpr right = parseExpr();
        return new NAssignCompound(Span.union(left.location(), right.location()), leftAssignable, right, op);
    }

    private NExpr parseUnaryOp(NExpr left) {
        Token cur = iterator.current();
        if (!cur.tok.isUnaryOpPost()) return left;

        iterator.inc();
        return new NUnaryOpPost(Span.union(left.location(), cur.location()), left, cur.tok);
    }

    private NExpr parseDotAccess(NExpr left) throws ParseException {
        Token cur = iterator.current();
        if (cur.tok != TokenType.Dot) return left;
        iterator.inc();

        NDotAccessSpecifier specifier = parseDotAccessor();
        return parseDotAccess(new NDotAccess(Span.union(left.location(), specifier.location()), left, specifier));
    }

    private NDotAccessSpecifier parseDotAccessor() throws ParseException {
        Token cur = iterator.current();
        iterator.inc();
        return switch (cur.tok) {
            case TokenType.Number -> new NDotAccessSpecifier(cur.location(), Either.left(Integer.parseInt(cur.val)));

            case TokenType.Identifier -> new NDotAccessSpecifier(cur.location(), Either.right(cur.val));

            default -> throw new ParseException(Errors.PLACEHOLDER, cur.location(), pipeline.getSource());
        };
    }

    private NExpr parseCall(NExpr left) throws ParseException {
        Token cur = iterator.current();
        if (cur.tok != TokenType.LParen) return left;

        NExpr arg = parseTuple();
        return parseCall(new NCall(Span.union(left.location(), arg.location()), left, arg));
    }

    private NExpr parseCast(NExpr left) throws ParseException {
        Token cur = iterator.current();
        if (cur.tok != TokenType.As) return left;

        iterator.inc();
        Pair<Type, Span> type = parseType();
        return new NCast(Span.union(left.location(), type.right), type.left, left);
    }

    @SuppressWarnings("unchecked")
    private NExpr parseTuple() throws ParseException {
        Span beginSpan = iterator.current().location();

        final boolean[] trailingComma = {false};
        Pair<NDotAccessSpecifier, NExpr>[] exprs = parseDelimAs(TokenType.LParen, TokenType.RParen, TokenType.Comma, i -> {
            trailingComma[0] = false;

            NDotAccessSpecifier specifier = null;
            if (iterator.current().tok == TokenType.Dot) {
                iterator.inc();
                specifier = parseDotAccessor();
                if (iterator.current().tok != TokenType.Assign)
                    throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
                iterator.inc();
            }

            NExpr expr = parseExpr();
            if (iterator.current().tok == TokenType.Comma)
                trailingComma[0] = true;
            return new Pair<>(specifier, expr);
        }, Pair.class);

        Span endSpan = iterator.peek(-1).location();

        if (!trailingComma[0] && exprs.length == 1 && exprs[0].left == null) return exprs[0].right;

        boolean[] elemIsSet = new boolean[exprs.length];
        HashSet<String> usedNames = new HashSet<>();
        for (int i = 0; i < exprs.length; i++) {
            if (exprs[i].left == null) {
                if (elemIsSet[i])
                    throw new ParseException(Errors.PLACEHOLDER, exprs[i].right.location(), pipeline.getSource());
                elemIsSet[i] = true;
                continue;
            }

            switch (exprs[i].left.specifier) {
                case Either.Left<Integer, String> index -> {
                    if (index.value() >= exprs.length)
                        throw new ParseException(Errors.PLACEHOLDER, exprs[i].left.location(), pipeline.getSource());
                    if (elemIsSet[index.value()])
                        throw new ParseException(Errors.PLACEHOLDER, exprs[i].left.location(), pipeline.getSource());
                    elemIsSet[index.value()] = true;
                }
                case Either.Right<Integer, String> name -> {
                    if (usedNames.contains(name.value()))
                        throw new ParseException(Errors.PLACEHOLDER, exprs[i].left.location(), pipeline.getSource());
                    usedNames.add(name.value());
                }
            }
        }

        return new NTuple(Span.union(beginSpan, endSpan), exprs);
    }

    private NUnion parseUnion() throws ParseException {
        Span beginSpan = iterator.current().location();

        if (iterator.current().tok != TokenType.LCurl)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());

        if (iterator.next().tok != TokenType.Dot)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
        iterator.inc();

        NDotAccessSpecifier specifier = parseDotAccessor();

        if (iterator.current().tok != TokenType.Assign)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());

        iterator.inc();
        NExpr expr = parseExpr();

        if (iterator.current().tok != TokenType.RCurl)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());

        Span endSpan = iterator.current().location();
        iterator.inc();

        return new NUnion(Span.union(beginSpan, endSpan), expr, specifier);
    }

    private NArray parseArray() throws ParseException {
        Span beginSpan = iterator.current().location();
        NExpr[] elems = parseDelim(TokenType.LBrack, TokenType.RBrack, TokenType.Comma);
        Span endSpan = iterator.peek(-1).location();
        return new NArray(Span.union(beginSpan, endSpan), elems);
    }

    private NExpr parseIf() throws ParseException {
        Span beginSpan = iterator.current().location();
        iterator.inc();

        NExpr condition = parseEnclosed(TokenType.LParen, TokenType.RParen);

        NExpr block = parseExpr();

        if (iterator.current().tok == TokenType.Else) {
            iterator.inc();
            NExpr elseBlock = parseExpr();

            return new NIfElse(Span.union(beginSpan, elseBlock.location()), condition, block, elseBlock);
        }

        return new NIf(Span.union(beginSpan, block.location()), condition, block);
    }

    @SuppressWarnings("unchecked")
    private NMatch parseMatch() throws ParseException {
        Span beginSpan = iterator.current().location();
        iterator.inc();

        NExpr value = parseEnclosed(TokenType.LParen, TokenType.RParen);

        ArrayList<Pair<NMatch.Pattern, NExpr>> branches = new ArrayList<>();
        while (iterator.current().tok == TokenType.Case) {
            if (iterator.next().tok != TokenType.Dot)
                throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());

            NDotAccessSpecifier specifier = parseDotAccessor();

            if (iterator.current().tok != TokenType.Identifier)
                throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
            String elementVarName = iterator.current().val;

            if (iterator.next().tok != TokenType.DoubleArrow)
                throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
            iterator.inc();

            NExpr expr = parseExpr();
            branches.add(new Pair<>(new NMatch.Pattern.Union(specifier, elementVarName), expr));
        }

        if (iterator.current().tok == TokenType.Else) {
            if (iterator.next().tok != TokenType.DoubleArrow)
                throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
            iterator.inc();
            NExpr expr = parseExpr();
            branches.add(new Pair<>(new NMatch.Pattern.Default(), expr));
        }

        Span endSpan = iterator.peek(-1).location();

        return new NMatch(Span.union(beginSpan, endSpan), value, branches.toArray(new Pair[0]));
    }

    private NWhile parseWhile() throws ParseException {
        Span beginSpan = iterator.current().location();
        iterator.inc();

        NExpr condition = parseEnclosed(TokenType.LParen, TokenType.RParen);

        NExpr block = parseExpr();

        return new NWhile(Span.union(beginSpan, block.location()), condition, block);
    }

    private NAnonFn parseAnonFn() throws ParseException {
        Span beginSpan = iterator.current().location();
        iterator.inc();

        Pair<Type, Span> type = parseType();
        if (!(type.left instanceof FnType fnType))
            throw new ParseException(Errors.PLACEHOLDER, type.right, pipeline.getSource());

        if (iterator.current().tok != TokenType.Assign)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());

        iterator.inc();

        NExpr body = parseExpr();

        return new NAnonFn(Span.union(beginSpan, body.location()), fnType, body);
    }

    private NNamedFn parseFn() throws ParseException {
        Span beginSpan = iterator.current().location();

        if (iterator.next().tok != TokenType.Identifier)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
        String name = iterator.current().val;

        if (iterator.next().tok != TokenType.Colon)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
        iterator.inc();

        Pair<Type, Span> type = parseType();
        if (!(type.left instanceof FnType fnType))
            throw new ParseException(Errors.PLACEHOLDER, type.right, pipeline.getSource());

        if (iterator.current().tok != TokenType.Assign)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
        iterator.inc();

        NExpr body = parseExpr();

        return new NNamedFn(Span.union(beginSpan, body.location()), name, fnType, body);
    }

    private NExpr parseBlock() throws ParseException {
        Span beginSpan = iterator.current().location();

        ArrayList<AST> stmnts = new ArrayList<>();

        if (iterator.next().tok == TokenType.RCurl) {
            iterator.inc();
            return new NBlock(Span.union(beginSpan, iterator.peek(-1).location()));
        }

        do {
            stmnts.add(parseStatement());

            if (iterator.current().tok == TokenType.RCurl && !iterator.isEOF()) {
                iterator.inc();

                if (iterator.current().tok == TokenType.DoubleArrow) {
                    iterator.inc();
                    AST lastStatement = stmnts.getLast();

                    if (!(lastStatement instanceof NExpr lastExpr))
                        throw new ParseException(Errors.PLACEHOLDER, lastStatement.location(), pipeline.getSource());

                    stmnts.removeLast();
                    return new NBlockYielding(Span.union(beginSpan, iterator.peek(-1).location()), lastExpr, stmnts.toArray(new AST[0]));
                }

                return new NBlock(Span.union(beginSpan, iterator.peek(-1).location()), stmnts.toArray(new AST[0]));
            }
        } while (!iterator.isEOF());


        throw new ParseException(Errors.PLACEHOLDER, beginSpan, pipeline.getSource());
    }

    private AST parseVar() throws ParseException {
        Span beginSpan = iterator.current().location();

        if (iterator.next().tok != TokenType.Identifier)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
        String name = iterator.current().val;

        Type type = null;
        if (iterator.next().tok == TokenType.Colon) {
            iterator.inc();
            type = parseType().left;
        }

        if (iterator.current().tok != TokenType.Assign)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());

        iterator.inc();
        NExpr init = parseExpr();

        return new NVarInit(Span.union(beginSpan, init.location()), type, name, init);
    }

    private NTypeDef parseTypeDef() throws ParseException {
        Span beginSpan = iterator.current().location();

        String[] typeParameters = new String[0];

        if (iterator.next().tok == TokenType.Greater) {
            typeParameters = parseDelimAs(TokenType.Greater, TokenType.Less, TokenType.Comma, (i) ->
                    {
                        if (iterator.current().tok != TokenType.Identifier)
                            throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
                        String str = iterator.current().val;
                        iterator.inc();
                        return str;
                    }
                    , String.class);
        }

        if (iterator.current().tok != TokenType.Identifier)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
        String name = iterator.current().val;

        if (iterator.next().tok != TokenType.Assign)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
        iterator.inc();

        Pair<Type, Span> definition = parseType();

        return new NTypeDef(Span.union(beginSpan, definition.right), name, definition.left, typeParameters);
    }

    private NExpr parseNumber() throws ParseException {
        Span numSpan = iterator.current().location();

        if (iterator.current().tok != TokenType.Number)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());

        String integerPartString = iterator.current().val;
        String decimalPartString = "";

        if (iterator.next().tok == TokenType.Dot) {
            if (iterator.next().tok != TokenType.Number)
                throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
            decimalPartString = iterator.current().val;
            numSpan = Span.union(numSpan, iterator.current().location);
            iterator.inc();
        }

        BigInteger integerPart = new BigInteger(integerPartString);

        Token typePostfix = iterator.current();
        iterator.inc();
        switch (typePostfix.tok) {
            case U8 -> {
                if (!decimalPartString.isEmpty())
                    throw new ParseException(Errors.PLACEHOLDER, numSpan, pipeline.getSource());
                if (integerPart.compareTo(new BigInteger(new byte[]{1, 0})) > -1)
                    throw new ParseException(Errors.PLACEHOLDER, numSpan, pipeline.getSource());
                return new NUInt8(Span.union(numSpan, typePostfix.location()), integerPart.byteValue());
            }

            case U16 -> {
                if (!decimalPartString.isEmpty())
                    throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
                if (integerPart.compareTo(new BigInteger(new byte[]{1, 0, 0})) > -1)
                    throw new ParseException(Errors.PLACEHOLDER, numSpan, pipeline.getSource());
                return new NUInt16(Span.union(numSpan, typePostfix.location()), integerPart.shortValue());
            }

            case U32 -> {
                if (!decimalPartString.isEmpty())
                    throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
                if (integerPart.compareTo(new BigInteger(new byte[]{1, 0, 0, 0, 0})) > -1)
                    throw new ParseException(Errors.PLACEHOLDER, numSpan, pipeline.getSource());
                return new NUInt32(Span.union(numSpan, typePostfix.location()), integerPart.intValue());
            }

            case U64 -> {
                if (!decimalPartString.isEmpty())
                    throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
                if (integerPart.compareTo(new BigInteger(new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0})) > -1)
                    throw new ParseException(Errors.PLACEHOLDER, numSpan, pipeline.getSource());
                return new NUInt64(Span.union(numSpan, typePostfix.location()), integerPart.longValue());
            }

            case I8 -> {
                if (!decimalPartString.isEmpty())
                    throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
                if (integerPart.compareTo(new BigInteger(new byte[]{0, -128})) > -1)
                    throw new ParseException(Errors.PLACEHOLDER, numSpan, pipeline.getSource());
                return new NInt8(Span.union(numSpan, typePostfix.location()), integerPart.byteValue());
            }

            case I16 -> {
                if (!decimalPartString.isEmpty())
                    throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
                if (integerPart.compareTo(new BigInteger(new byte[]{0, -128, 0})) > -1)
                    throw new ParseException(Errors.PLACEHOLDER, numSpan, pipeline.getSource());
                return new NInt16(Span.union(numSpan, typePostfix.location()), integerPart.shortValue());
            }

            case I32 -> {
                if (!decimalPartString.isEmpty())
                    throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
                if (integerPart.compareTo(new BigInteger(new byte[]{0, -128, 0, 0, 0})) > -1)
                    throw new ParseException(Errors.PLACEHOLDER, numSpan, pipeline.getSource());
                return new NInt32(Span.union(numSpan, typePostfix.location()), integerPart.intValue());
            }

            case I64 -> {
                if (!decimalPartString.isEmpty())
                    throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
                if (integerPart.compareTo(new BigInteger(new byte[]{0, -128, 0, 0, 0, 0, 0, 0, 0})) > -1)
                    throw new ParseException(Errors.PLACEHOLDER, numSpan, pipeline.getSource());
                return new NInt64(Span.union(numSpan, typePostfix.location()), integerPart.longValue());
            }

            case F32 -> {
                BigDecimal decimal = new BigDecimal(integerPartString + "." + decimalPartString);
                return new NFloat32(Span.union(numSpan, typePostfix.location()), decimal.floatValue());
            }

            case F64 -> {
                BigDecimal decimal = new BigDecimal(integerPartString + "." + decimalPartString);
                return new NFloat64(Span.union(numSpan, typePostfix.location()), decimal.doubleValue());
            }

            default -> iterator.dec();
        }

        return new NNumInfer(numSpan, integerPartString + (decimalPartString.isEmpty() ? "" : "." + decimalPartString));
    }

    private Pair<Type, Span> parseType() throws ParseException {
        return parseFnType(parseArrayType(parseTypeAtom()));
    }

    private Pair<Type, Span> parseTypeAtom() throws ParseException {
        Token cur = iterator.current();
        iterator.inc();

        return switch (cur.tok) {
            case Bool -> new Pair<>(PrimitiveType.Bool, cur.location());
            case Char -> new Pair<>(PrimitiveType.Char, cur.location());

            case U8 -> new Pair<>(PrimitiveType.U8, cur.location());
            case U16 -> new Pair<>(PrimitiveType.U16, cur.location());
            case U32 -> new Pair<>(PrimitiveType.U32, cur.location());
            case U64 -> new Pair<>(PrimitiveType.U64, cur.location());

            case I8 -> new Pair<>(PrimitiveType.I8, cur.location());
            case I16 -> new Pair<>(PrimitiveType.I16, cur.location());
            case I32 -> new Pair<>(PrimitiveType.I32, cur.location());
            case I64 -> new Pair<>(PrimitiveType.I64, cur.location());

            case F32 -> new Pair<>(PrimitiveType.F32, cur.location());
            case F64 -> new Pair<>(PrimitiveType.F64, cur.location());


            case LParen -> {
                iterator.dec();
                yield parseTupleType();
            }

            case LCurl -> {
                iterator.dec();
                yield parseUnionType();
            }

            case And -> {
                Pair<Type, Span> type = parseType();
                yield new Pair<>(new RefType(type.left), Span.union(cur.location(), type.right));
            }

            case Identifier -> {
                Span namedTypeSpan = cur.location();

                String name = cur.val;
                Type[] typeArgs = new Type[0];
                if (iterator.current().tok == TokenType.Greater) {
                    typeArgs = parseDelimAs(TokenType.Greater, TokenType.Less, TokenType.Comma, (i) -> parseType().left, Type.class);
                    namedTypeSpan = Span.union(namedTypeSpan, iterator.peek(-1).location());
                }
                yield new Pair<>(new NamedType(name, typeArgs), namedTypeSpan);
            }

            default -> throw new ParseException(Errors.PLACEHOLDER, cur.location(), pipeline.getSource());
        };
    }

    private Pair<Type, Span> parseArrayType(Pair<Type, Span> left) {
        if (iterator.current().tok == TokenType.LBrack && iterator.next().tok == TokenType.RBrack) {
            iterator.inc();
            return new Pair<>(new ArrayType(left.left), Span.union(left.right, iterator.peek(-1).location()));
        }
        return left;
    }

    private Pair<Type, Span> parseFnType(Pair<Type, Span> left) throws ParseException {
        if (iterator.current().tok != TokenType.SingleArrow)
            return left;
        iterator.inc();

        Pair<Type, Span> returnType = parseType();

        return new Pair<>(new FnType(returnType.left, left.left), Span.union(left.right, returnType.right));
    }

    @SuppressWarnings("unchecked")
    public Pair<Type, Span> parseTupleType() throws ParseException {
        Span beginSpan = iterator.current().location();

        final boolean[] trailingComma = {false};
        Pair<NameTypePair, Span>[] nameTypePairs = parseDelimAs(TokenType.LParen, TokenType.RParen, TokenType.Comma, i -> {
            trailingComma[0] = false;
            Span beginSpanPair = iterator.current().location();

            String name = null;
            if (iterator.current().tok == TokenType.Identifier && iterator.peek().tok == TokenType.Colon) {
                name = iterator.current().val;
                iterator.inc();
                iterator.inc();
            }
            Pair<Type, Span> type = parseType();

            Span endSpanPair = iterator.peek(-1).location();

            if (iterator.current().tok == TokenType.Comma)
                trailingComma[0] = true;

            return new Pair<>(new NameTypePair(type.left, name), Span.union(beginSpanPair, endSpanPair));
        }, Pair.class);

        Span endSpan = iterator.peek(-1).location();

        if (!trailingComma[0] && nameTypePairs.length == 1 && nameTypePairs[0].left.name == null)
            return new Pair<>(nameTypePairs[0].left.type, Span.union(beginSpan, endSpan));

        HashSet<String> names = new HashSet<>();
        for (Pair<NameTypePair, Span> nameTypePair : nameTypePairs) {
            if (nameTypePair.left.name == null) continue;
            if (names.contains(nameTypePair.left.name))
                throw new ParseException(Errors.PLACEHOLDER, nameTypePair.right, pipeline.getSource());
            names.add(nameTypePair.left.name);
        }

        return new Pair<>(new TupleType(Arrays.stream(nameTypePairs).map(pair -> pair.left).toArray(NameTypePair[]::new)), Span.union(beginSpan, endSpan));
    }

    @SuppressWarnings("unchecked")
    public Pair<Type, Span> parseUnionType() throws ParseException {
        Span beginSpan = iterator.current().location();
        Pair<NameTypePair, Span>[] nameTypePairs = parseDelimAs(TokenType.LCurl, TokenType.RCurl, TokenType.Comma, (i) -> {
            Span beginSpanPair = iterator.current().location();

            String name = null;
            if (iterator.current().tok == TokenType.Identifier && iterator.peek().tok == TokenType.Colon) {
                name = iterator.current().val;
                iterator.inc();
                iterator.inc();
            }
            Pair<Type, Span> type = parseType();

            Span endSpanPair = iterator.peek(-1).location();

            return new Pair<>(new NameTypePair(type.left, name), Span.union(beginSpanPair, endSpanPair));
        }, Pair.class);
        Span endSpan = iterator.peek(-1).location();

        HashSet<String> names = new HashSet<>();
        for (Pair<NameTypePair, Span> nameTypePair : nameTypePairs) {
            if (nameTypePair.left.name == null) continue;
            if (names.contains(nameTypePair.left.name))
                throw new ParseException(Errors.PLACEHOLDER, nameTypePair.right, pipeline.getSource());
            names.add(nameTypePair.left.name);
        }

        return new Pair<>(new UnionType(Arrays.stream(nameTypePairs).map(pair -> pair.left).toArray(NameTypePair[]::new)), Span.union(beginSpan, endSpan));
    }

    private NExpr parseEnclosed(TokenType start, TokenType end) throws ParseException {
        return parseEnclosedAs(start, end, (v) -> parseExpr());
    }

    private <T> T parseEnclosedAs(TokenType start, TokenType end, FunctionThrowsParseException<Void, T> supplier) throws ParseException {
        if (iterator.current().tok != start)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
        iterator.inc();

        T ret = supplier.apply(null);

        if (iterator.current().tok != end)
            throw new ParseException(Errors.PLACEHOLDER, iterator.current().location(), pipeline.getSource());
        iterator.inc();

        return ret;
    }

    private NExpr[] parseDelim(TokenType start, TokenType end, TokenType separator) throws ParseException {
        return parseDelimAs(start, end, separator, i -> parseExpr(), NExpr.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T[] parseDelimAs(TokenType start, TokenType end, TokenType separator, FunctionThrowsParseException<Integer, T> fn, Class<T> clazz) throws ParseException {
        if (iterator.current().tok != start)
            throw new ParseException(Errors.PARSE_DELIM_EXPECTED_START, iterator.current().location(), pipeline.getSource(), start.expandedName());

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
                throw new ParseException(Errors.PARSE_DELIM_EXPECTED_SEPARATOR, iterator.current().location(), pipeline.getSource(), separator.expandedName());

            iterator.inc();
        }
        iterator.inc();

        return exprs.toArray((T[]) Array.newInstance(clazz, 0));
    }
}