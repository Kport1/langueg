package com.kport.langueg.parse;

import com.kport.langueg.error.ErrorHandler;
import com.kport.langueg.error.Errors;
import com.kport.langueg.lex.Token;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.*;
import com.kport.langueg.parse.ast.nodes.*;
import com.kport.langueg.parse.ast.nodes.expr.*;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NAssignable;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NIdent;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NDotAccess;
import com.kport.langueg.parse.ast.nodes.expr.integer.*;
import com.kport.langueg.parse.ast.nodes.statement.*;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.typeCheck.types.*;
import com.kport.langueg.util.Iterator;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class DefaultParser implements Parser{

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

        opPrecedence.put(TokenType.BAnd, 6);
        opPrecedence.put(TokenType.BOr, 6);
        opPrecedence.put(TokenType.BXOr, 6);

        opPrecedence.put(TokenType.ShiftR, 7);
        opPrecedence.put(TokenType.ShiftL, 7);
    }

    private ErrorHandler errorHandler;

    @Override
    @SuppressWarnings("unchecked")
    public AST process(Object tokens_, LanguegPipeline<?, ?> pipeline) {
        ArrayList<Token> tokens = (ArrayList<Token>) tokens_;
        iterator = new Iterator<>(tokens);

        errorHandler = pipeline.getErrorHandler();

        return parseProg();
    }

    private NProg parseProg(){
        ArrayList<AST> stmnts = new ArrayList<>();
        while(!iterator.isEOF()){
            switch (iterator.current().tok){
                default -> stmnts.add(parseStatement());
            }
        }
        return new NProg(0, stmnts.toArray(AST[]::new));
    }

    private AST parseStatement(){
        Token cur = iterator.current();

        AST res = switch (cur.tok){

            case Fn -> parseFn();

            case If -> parseIf();

            case Return -> {
                if(iterator.next().tok == TokenType.Semicolon)
                    yield new NReturnVoid(cur.offset);

                NExpr expr = parseExpr();
                yield new NReturn(cur.offset, expr);
            }

            case Break -> null;
            case Continue -> null;
            case Switch -> null;

            case While -> parseWhile();

            case LCurl -> parseBlock();

            case Var -> parseVar();

            case Semicolon -> new NBlock(cur.offset);

            default -> parseExpr();

        };

        boolean reqSemicolon = switch(cur.tok){
            case Fn, If, While, For, LCurl -> false;
            default -> true;
        };

        if(reqSemicolon){
            if(iterator.current().tok != TokenType.Semicolon)
                throw new Error();
            iterator.inc();
        }

        return res;
    }

    private NExpr parseExpr(){
        return  parseBinaryOp(
                    parseDotAccess(
                        parseUnaryOp(
                            call(parseAtom())
                        )
                    ),
                -1);
    }

    private NExpr parseAtom(){

        if(iterator.isEOF())
            errorHandler.error(Errors.PARSE_ATOM_REACHED_EOF, 0);


        Token cur = iterator.current();

        switch (cur.tok){

            case LParen -> {
                return parseTuple();
            }

            case StringL -> {
                iterator.inc();
                return new NStr(cur.offset, cur.val);
            }

            case IntL -> {
                return parseInt();
            }

            case FloatL -> {
                return parseFloat();
            }

            case LBrack -> {
                //TODO
            }

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

            case Fn -> {
                return parseAnonFn();
            }

            case Minus, Not, Inc, Dec -> {
                iterator.inc();
                return new NUnaryOpPre(cur.offset, parseUnaryOp(call(parseAtom())), cur.tok);
            }

        }

        errorHandler.error(Errors.PARSE_ATOM_UNEXPECTED_TOKEN, cur.offset, cur.tok.expandedName());
        return null;
    }

    private NExpr parseBinaryOp(NExpr left, int lastPrec) {
        Token cur = iterator.current();

        if(cur.tok.isBinOp() || cur.tok.isCompoundAssign() || cur.tok == TokenType.Assign){
            int currentPrec = opPrecedence.get(cur.tok);
            if(currentPrec > lastPrec || cur.tok.isCompoundAssign() || cur.tok == TokenType.Assign) {
                iterator.inc();
                NExpr right = parseBinaryOp(parseUnaryOp(call(parseAtom())), currentPrec);
                if(cur.tok == TokenType.Assign){
                    if(!(left instanceof NAssignable assignable)) throw new Error("Cannot assign a value to:\n" + left);
                    return parseBinaryOp(new NAssign(cur.offset, assignable, right), lastPrec);
                }
                return parseBinaryOp(new NBinOp(cur.offset, left, right, cur.tok), lastPrec);
            }
        }
        return left;
    }

    private NExpr parseUnaryOp(NExpr left) {
        Token cur = iterator.current();
        if(!cur.tok.isUnaryOpPost()) return left;

        iterator.inc();
        return new NUnaryOpPost(cur.offset, left, cur.tok);
    }

    private NExpr parseDotAccess(NExpr left){
        Token cur = iterator.current();
        if(cur.tok != TokenType.Dot) return left;

        iterator.inc();
        return new NDotAccess(cur.offset, left, parseAtom());
    }

    private NExpr call(NExpr left){
        Token cur = iterator.current();
        if(cur.tok == TokenType.LParen){
            NExpr[] args = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);

            return call(new NCall(cur.offset, left, args));
        }
        return left;
    }

    private NExpr parseTuple(){
        int offset = iterator.current().offset;
        NExpr[] exprs = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);
        if(exprs.length == 1) return exprs[0];

        return new NTuple(offset, exprs);
    }

    private AST parseIf(){
        int offset = iterator.current().offset;
        iterator.inc();

        NExpr condition = parseEnclosed(TokenType.LParen, TokenType.RParen);

        AST block = parseStatement();

        if(iterator.current().tok == TokenType.Else){
            iterator.inc();
            AST elseBlock = parseStatement();

            return new NIfElse(offset, condition, block, elseBlock);
        }

        return new NIf(offset, condition, block);
    }

    private NWhile parseWhile(){
        int offset = iterator.current().offset;
        iterator.inc();

        NExpr condition = parseEnclosed(TokenType.LParen, TokenType.RParen);

        AST block = parseStatement();

        return new NWhile(offset, condition, block);
    }

    private NAnonFn parseAnonFn(){
        int offset = iterator.current().offset;
        iterator.inc();

        FnHeader header = parseFnHeader();
        AST stmnt = parseStatement();

        iterator.inc();

        return new NAnonFn(offset, header, stmnt);
    }

    private NNamedFn parseFn(){
        int offset = iterator.current().offset;

        if (iterator.next().tok != TokenType.Identifier) errorHandler.error(Errors.PLACEHOLDER, 0);
        String name = iterator.current().val;

        iterator.inc();

        FnHeader header = parseFnHeader();
        AST stmnt = parseStatement();

        iterator.inc();

        return new NNamedFn(offset, name, header, stmnt);
    }

    private FnHeader parseFnHeader(){
        return parseEnclosedAs(TokenType.LParen, TokenType.RParen, () -> {
            NameTypePair[] params = parseFnParams();

            if (iterator.current().tok != TokenType.SingleArrow) errorHandler.error(Errors.PLACEHOLDER, 0);
            iterator.inc();

            return new FnHeader(params, parseType());
        });
    }

    private NameTypePair[] parseFnParams(){
        return parseDelimAs(TokenType.LParen, TokenType.RParen, TokenType.Comma, (i) -> {
            if(iterator.current().tok != TokenType.Identifier) errorHandler.error(Errors.PLACEHOLDER, 0);
            String name = iterator.current().val;

            if(iterator.next().tok != TokenType.Colon) errorHandler.error(Errors.PLACEHOLDER, 0);
            iterator.inc();

            Type type = parseType();

            return new NameTypePair(type, name);
        }, NameTypePair.class);
    }

    private NBlock parseBlock(){
        ArrayList<AST> stmnts = new ArrayList<>();

        int offset = iterator.current().offset;

        if(iterator.next().tok == TokenType.RCurl){
            return new NBlock(offset);
        }

        do {
            stmnts.add(parseStatement());
            if(iterator.current().tok == TokenType.RCurl && !iterator.isEOF()){
                return new NBlock(offset, stmnts.toArray(new AST[0]));
            }

        } while(!iterator.isEOF());


        errorHandler.error(Errors.PARSE_BLOCK_NOT_CLOSED, offset);
        return null;
    }

    private AST parseVar(){
        int varOffset = iterator.current().offset;

        if(iterator.next().tok != TokenType.Identifier) errorHandler.error(Errors.PLACEHOLDER, 0);
        String name = iterator.current().val;

        Type type = null;
        if(iterator.next().tok == TokenType.Colon){
            iterator.inc();
            type = parseType();
        }

        NExpr init = null;
        if (iterator.current().tok == TokenType.Assign){
            iterator.inc();
            init = parseExpr();
        }

        return init == null? new NVar(varOffset, type, name) : new NVarInit(varOffset, type, name, init);
    }

    private NExpr parseInt(){
        Token cur = iterator.current();
        iterator.inc();
        String numStr = cur.val;

        int postfixBeginIndex = numStr.length();
        postfixBeginIndex = numStr.lastIndexOf('i') == -1? postfixBeginIndex : numStr.lastIndexOf('i');
        postfixBeginIndex = numStr.lastIndexOf('u') == -1? postfixBeginIndex : numStr.lastIndexOf('u');
        String postfix = numStr.substring(postfixBeginIndex);

        BigInteger tmpParseRes;
        try {
            if (numStr.startsWith("0x")) {
                tmpParseRes = new BigInteger(numStr.substring(2, postfixBeginIndex), 16);
            } else if (numStr.startsWith("o")) {
                tmpParseRes = new BigInteger(numStr.substring(1, postfixBeginIndex), 8);
            } else {
                tmpParseRes = new BigInteger(numStr.substring(0, postfixBeginIndex));
            }
        } catch (NumberFormatException e){
            errorHandler.error(Errors.PARSE_INT_INVALID, cur.offset, numStr);
            throw new Error();
        }

        //TODO check for overflow
        return switch (postfix){
            case "u8" -> {
                yield new NUInt8(cur.offset, tmpParseRes.byteValue());
            }
            case "u16" -> {
                yield new NUInt16(cur.offset, tmpParseRes.shortValue());
            }
            case "u32", "u" -> {
                yield new NUInt32(cur.offset, tmpParseRes.intValue());
            }
            case "u64" -> {
                yield new NUInt64(cur.offset, tmpParseRes.longValue());
            }
            case "i8" -> {
                yield new NInt8(cur.offset, tmpParseRes.byteValue());
            }
            case "i16" -> {
                yield new NInt16(cur.offset, tmpParseRes.shortValue());
            }
            case "i32", "i", "" -> {
                yield new NInt32(cur.offset, tmpParseRes.intValue());
            }
            case "i64" -> {
                yield new NInt64(cur.offset, tmpParseRes.longValue());
            }
            default -> {
                errorHandler.error(Errors.PARSE_INT_INVALID, cur.offset, numStr);
                throw new Error();
            }
        };
    }

    private NExpr parseFloat(){
        Token cur = iterator.previous();
        iterator.inc();

        String numStr = cur.val;
        int postfixBeginIndex = numStr.length();
        postfixBeginIndex = numStr.lastIndexOf("f") == -1? postfixBeginIndex : numStr.lastIndexOf("f");
        postfixBeginIndex = numStr.lastIndexOf("d") == -1? postfixBeginIndex : numStr.lastIndexOf("d");
        String postfix = numStr.substring(postfixBeginIndex);

        BigDecimal decimal = new BigDecimal(numStr.substring(0, postfixBeginIndex));

        return switch (postfix){
            case "f", "" -> {
                yield new NFloat32(cur.offset, decimal.floatValue());
            }
            case "d" -> {
                yield new NFloat64(cur.offset, decimal.doubleValue());
            }
            default -> {
                errorHandler.error(Errors.PARSE_INT_INVALID, cur.offset, numStr);
                throw new Error();
            }
        };
    }

    private Type parseType(){
        return unwrapTuple(parseFnType(parseArrayType(parseTypeAtom())));
    }

    public Type unwrapTuple(Type type){
        if(type instanceof TupleType tup && tup.tupleTypes().length == 1)
            return tup.tupleTypes()[0];
        return type;
    }

    private Type parseTypeAtom(){
        Token cur = iterator.current();
        iterator.inc();

        switch (cur.tok){
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

            case Void -> {
                return PrimitiveType.Void;
            }
            case LParen -> {
                iterator.dec();
                return parseTupleType();
            }
            case Identifier -> {
                return new CustomType(cur.val);
            }
        }

        return null;
    }

    private Type parseArrayType(Type left){
        if (iterator.current().tok == TokenType.LBrack && iterator.next().tok == TokenType.RBrack){
            iterator.inc();
            return new ArrayType(left);
        }
        return left;
    }

    private Type parseFnType(Type left){
        if(iterator.current().tok != TokenType.SingleArrow)
            return left;
        iterator.inc();

        Type returnType = parseFnType(parseArrayType(parseTypeAtom()));

        return new FnType(returnType, left instanceof TupleType tup? tup.tupleTypes() : new Type[]{ left });
    }

    public TupleType parseTupleType(){
        NameTypePair[] nameTypePairs = parseDelimAs(TokenType.LParen, TokenType.RParen, TokenType.Comma, (i) -> {
            String name = null;
            if (iterator.current().tok == TokenType.Identifier && iterator.peek().tok == TokenType.Colon){
                name = iterator.current().val;
                iterator.inc();
                iterator.inc();
            }
            Type type = parseFnType(parseArrayType(parseTypeAtom()));
            return new NameTypePair(type, name);
        }, NameTypePair.class);

        return new TupleType(nameTypePairs);
    }

    private NExpr parseEnclosed(TokenType start, TokenType end){
        return parseEnclosedAs(start, end, this::parseExpr);
    }

    private <T> T parseEnclosedAs(TokenType start, TokenType end, Supplier<T> sup){
        if (iterator.current().tok != start)
            errorHandler.error(Errors.PLACEHOLDER, 0);
        iterator.inc();

        T ret = sup.get();

        if (iterator.current().tok != end)
            errorHandler.error(Errors.PLACEHOLDER, 0);
        iterator.inc();

        return ret;
    }

    private NExpr[] parseDelim(TokenType start, TokenType end, TokenType separator){
        return parseDelimAs(start, end, separator, i -> parseExpr(), NExpr.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T[] parseDelimAs(TokenType start, TokenType end, TokenType separator, Function<Integer, T> fn, Class<T> clazz) {
        if(iterator.current().tok != start)
            errorHandler.error(Errors.PARSE_DELIM_EXPECTED_START, iterator.current().offset, start.expandedName());

        ArrayList<T> exprs = new ArrayList<>();

        if(iterator.next().tok == end){
            iterator.inc();
            return (T[])Array.newInstance(clazz, 0);
        }

        int i = 0;
        while(iterator.current().tok != end){
            exprs.add(fn.apply(i));
            i++;

            if(iterator.current().tok == end){
                break;
            }

            if(iterator.current().tok != separator)
                errorHandler.error(Errors.PARSE_DELIM_EXPECTED_SEPARATOR, iterator.current().offset, separator.expandedName());

            iterator.inc();
        }
        iterator.inc();

        return exprs.toArray((T[])Array.newInstance(clazz, 0));
    }
}