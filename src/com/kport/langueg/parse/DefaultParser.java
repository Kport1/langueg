package com.kport.langueg.parse;

import com.kport.langueg.error.ErrorHandler;
import com.kport.langueg.error.Errors;
import com.kport.langueg.lex.Token;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.*;
import com.kport.langueg.parse.ast.nodes.*;
import com.kport.langueg.parse.ast.nodes.expr.*;
import com.kport.langueg.parse.ast.nodes.statement.*;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.typeCheck.types.*;
import com.kport.langueg.util.Iterator;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;

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
        NNamedFn modInterface = null;
        while(!iterator.isEOF()){
            if(iterator.current().tok == TokenType.Module){
                if(modInterface != null) errorHandler.error(Errors.PLACEHOLDER);
                iterator.inc();
                modInterface = parseFn();
                iterator.inc();
            }
            stmnts.add(parseStatement());
        }
        return new NProg(1, 0,
                modInterface == null? new NNamedFn(1, 0, PrimitiveType.Void, "", new FnParamDef[0], new NBlock(1, 0)) : modInterface,
                stmnts.toArray(AST[]::new));
    }

    private AST parseStatement(){
        Token cur = iterator.current();
        iterator.inc();

        AST res = switch (cur.tok){

            case Fn -> parseFn();

            case If -> parseIf();

            case Return -> {
                if(iterator.current().tok == TokenType.Semicolon)
                    yield new NReturnVoid(cur.lineNum, cur.columnNum);

                NExpr expr = parseExpr();
                yield new NReturn(cur.lineNum, cur.columnNum, expr);
            }

            case Break -> null;
            case Continue -> null;
            case Switch -> null;

            case While -> parseWhile();

            case For -> parseFor();

            case LCurl -> parseBlock();

            case Var, Bool, Char, U8, U16, U32, U64, I8, I16, I32, I64, F32, F64, Void, LParen -> {
                iterator.dec();
                yield parseVar();
            }

            case Identifier -> {
                if(iterator.current().tok == TokenType.Identifier) {
                    iterator.dec();
                    yield parseVar();
                }
                iterator.dec();
                yield parseExpr();
            }

            case Semicolon -> null;

            default -> {
                iterator.dec();
                yield parseExpr();
            }
        };

        boolean reqSemicolon = switch(cur.tok){
            case Fn, If, While, For, LCurl -> false;
            default -> true;
        };

        if(reqSemicolon && iterator.current().tok != TokenType.Semicolon){
            throw new Error();
        }
        iterator.inc();

        return res;
    }

    private NExpr parseExpr(){
        return parseBinaryOp(parseUnaryOp(call(parseAtom())), -1);
    }

    private NExpr parseAtom(){

        if(iterator.isEOF())
            errorHandler.error(Errors.PARSE_ATOM_REACHED_EOF);


        Token cur = iterator.current();
        iterator.inc();

        switch (cur.tok){

            case LParen -> {
                iterator.dec();
                return parseTuple();
            }

            case StringL -> {
                return new NStr(cur.lineNum, cur.columnNum, cur.val);
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
                return new NIdent(cur.lineNum, cur.columnNum, cur.val);
            }

            case True -> {
                return new NBool(cur.lineNum, cur.columnNum, true);
            }

            case False -> {
                return new NBool(cur.lineNum, cur.columnNum, false);
            }

            case Fn -> {
                return parseAnonFn();
            }

            case Minus, Not, Inc, Dec -> {
                return new NUnaryOpPre(cur.lineNum, cur.columnNum, parseUnaryOp(call(parseAtom())), cur.tok);
            }

        }

        errorHandler.error(Errors.PARSE_ATOM_UNEXPECTED_TOKEN, cur.lineNum, cur.tok.expandedName());
        return null;
    }

    private NExpr parseBinaryOp(NExpr left, int lastPrec) {
        Token current = iterator.current();

        if(current.tok.isBinOp() || current.tok.isOpAssign() || current.tok == TokenType.Assign){
            int currentPrec = opPrecedence.get(current.tok);
            if(currentPrec > lastPrec || current.tok.isOpAssign() || current.tok == TokenType.Assign) {
                iterator.inc();
                NExpr right = parseBinaryOp(parseUnaryOp(call(parseAtom())), currentPrec);
                if(current.tok == TokenType.Assign){
                    if(!(left instanceof NAssignable assignable)) throw new Error("Cannot assign a value to:\n" + left);
                    return parseBinaryOp(new NAssign(current.lineNum, current.columnNum, assignable, right), lastPrec);
                }
                return parseBinaryOp(new NBinOp(current.lineNum, current.columnNum, left, right, current.tok), lastPrec);
            }
        }
        return left;
    }

    private NExpr parseUnaryOp(NExpr left) {
        Token current  = iterator.current();
        if(current.tok.isUnaryOpPost()) {
            iterator.inc();
            return new NUnaryOpPost(current.lineNum, current.columnNum, left, current.tok);
        }
        return left;
    }

    private NExpr call(NExpr left){
        Token current = iterator.current();
        if(current.tok == TokenType.LParen){
            int line = current.lineNum;
            int column = current.columnNum;
            NExpr[] args = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);

            return call(new NCall(line, column, left, args));
        }
        return left;
    }

    private NExpr parseTuple(){
        int line = iterator.current().lineNum;
        int column = iterator.current().columnNum;
        NExpr[] exprs = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);
        if(exprs.length == 1) return exprs[0];

        return new NTuple(line, column, exprs);
    }

    private AST parseIf(){
        iterator.dec();
        int ifLine = iterator.current().lineNum;
        int ifColumn = iterator.current().columnNum;
        iterator.inc();

        NExpr condition = parseTuple();

        if(condition instanceof NTuple tup){
            if(tup.elements.length < 1){
                errorHandler.error(Errors.PARSE_IF_CONDITION_EMPTY, condition.line);
            }
            errorHandler.error(Errors.PARSE_IF_CONDITION_TUPLE, condition.line);
        }

        AST block = parseStatement();
        iterator.dec();

        if(iterator.peek().tok == TokenType.Else){
            iterator.inc();
            iterator.inc();
            AST elseBlock = parseStatement();

            return new NIfElse(ifLine, ifColumn, condition, block, elseBlock);
        }

        return new NIf(ifLine, ifColumn, condition, block);
    }

    private NWhile parseWhile(){
        iterator.dec();
        int whileLine = iterator.current().lineNum;
        int whileColumn = iterator.current().columnNum;
        iterator.inc();

        NExpr condition = parseTuple();

        if(condition instanceof NTuple tup){
            if(tup.elements.length == 0){
                errorHandler.error(Errors.PARSE_WHILE_CONDITION_EMPTY, condition.line);
            }
            errorHandler.error(Errors.PARSE_WHILE_CONDITION_TUPLE, condition.line);
        }

        AST block = parseStatement();

        return new NWhile(whileLine, whileColumn, condition, block);
    }

    private NFor parseFor(){
        int forLine = iterator.previous().lineNum;
        int forColumn = iterator.current().columnNum;

        int initCondIncLine = iterator.next().lineNum;
        AST[] initCondInc = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Semicolon);

        if(initCondInc.length != 3){
            if(initCondInc.length == 0)
                errorHandler.error(Errors.PARSE_FOR_HEAD_EMPTY, initCondIncLine);

            errorHandler.error(Errors.PARSE_FOR_HEAD_MALFORMED, initCondIncLine);
        }

        AST block = parseStatement();

        return new NFor(forLine, forColumn, initCondInc[0], initCondInc[1], initCondInc[2], block);
    }

    private NAnonFn parseAnonFn(){
        iterator.dec();
        int fnLine = iterator.current().lineNum;
        int fnColumn = iterator.current().columnNum;
        iterator.inc();

        Type returnType = parseType();

        FnParamDef[] params = parseFnParams();

        AST stmnt = parseStatement();

        iterator.inc();

        return new NAnonFn(fnLine, fnColumn, returnType, params, stmnt);
    }

    private NNamedFn parseFn(){
        iterator.dec();
        int fnLine = iterator.current().lineNum;
        int fnColumn = iterator.current().columnNum;
        iterator.inc();

        Type returnType = parseType();

        if(iterator.current().tok != TokenType.Identifier) errorHandler.error(Errors.PLACEHOLDER);

        String name = iterator.current().val;
        iterator.inc();

        FnParamDef[] params = parseFnParams();

        AST stmnt = parseStatement();
        iterator.dec();

        return new NNamedFn(fnLine, fnColumn, returnType, name, params, stmnt);
    }

    private FnParamDef[] parseFnParams(){
        return parseDelimAs(TokenType.LParen, TokenType.RParen, TokenType.Comma, (i) -> {
            Type type = parseType();

            if(iterator.current().tok != TokenType.Identifier) errorHandler.error(Errors.PLACEHOLDER);
            String name = iterator.current().val;

            iterator.inc();

            return new FnParamDef(type, name);
        }, FnParamDef.class);
    }

    private NBlock parseBlock(){
        ArrayList<AST> stmnts = new ArrayList<>();

        iterator.dec();
        int blockLine = iterator.current().lineNum;
        int blockColumn = iterator.current().columnNum;
        iterator.inc();

        if(iterator.current().tok == TokenType.RCurl){
            return new NBlock(blockLine, blockColumn);
        }

        do {
            stmnts.add(parseStatement());
            if(iterator.current().tok == TokenType.RCurl && !iterator.isEOF()){
                return new NBlock(blockLine, blockColumn, stmnts.toArray(new AST[0]));
            }

        } while(!iterator.isEOF());


        errorHandler.error(Errors.PARSE_BLOCK_NOT_CLOSED, blockLine);
        return null;
    }

    private AST parseVar(){
        int varLine = iterator.current().lineNum;
        int varColumn = iterator.current().columnNum;

        Type varType = null;
        if(iterator.current().tok == TokenType.Var) iterator.inc();
        else varType = parseType();

        if(iterator.current().tok == TokenType.LParen){
            AST[] identifiers = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);
            String[] names = new String[identifiers.length];
            for (int i = 0; i < identifiers.length; i++) {
                if(identifiers[i] instanceof NIdent ident){
                    names[i] = ident.name;
                }
                else {
                    errorHandler.error(Errors.PARSE_VAR_DESTRUCT_EXPECTED_IDENTIFIER, identifiers[i].line);
                }
            }

            if(iterator.current().tok != TokenType.Assign)
                errorHandler.error(Errors.PARSE_VAR_DESTRUCT_CANNOT_INFER_TYPE, varLine);

            iterator.inc();
            return new NVarDestruct(varLine, varColumn, null, names, parseExpr());
        }
        Token identifier = iterator.current();
        if(identifier.tok != TokenType.Identifier)
            errorHandler.error(Errors.PARSE_VAR_EXPECTED_IDENTIFIER, identifier.lineNum);

        if(iterator.next().tok == TokenType.Assign){
            iterator.inc();
            return new NVarInit(varLine, varColumn, varType, identifier.val, parseExpr());
        }

        return new NVar(varLine, varColumn, varType, identifier.val);
    }

    private NExpr parseInt(){
        Token current = iterator.previous();
        iterator.inc();
        String numStr = current.val;

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
            errorHandler.error(Errors.PARSE_INT_INVALID, current.lineNum, numStr);
            throw new Error();
        }

        //TODO check for overflow
        return switch (postfix){
            case "u8" -> {
                yield new NUInt8(current.lineNum, current.columnNum, tmpParseRes.byteValue());
            }
            case "u16" -> {
                yield new NUInt16(current.lineNum, current.columnNum, tmpParseRes.shortValue());
            }
            case "u32", "u" -> {
                yield new NUInt32(current.lineNum, current.columnNum, tmpParseRes.intValue());
            }
            case "u64" -> {
                yield new NUInt64(current.lineNum, current.columnNum, tmpParseRes.longValue());
            }
            case "i8" -> {
                yield new NInt8(current.lineNum, current.columnNum, tmpParseRes.byteValue());
            }
            case "i16" -> {
                yield new NInt16(current.lineNum, current.columnNum, tmpParseRes.shortValue());
            }
            case "i32", "i", "" -> {
                yield new NInt32(current.lineNum, current.columnNum, tmpParseRes.intValue());
            }
            case "i64" -> {
                yield new NInt64(current.lineNum, current.columnNum, tmpParseRes.longValue());
            }
            default -> {
                errorHandler.error(Errors.PARSE_INT_INVALID, current.lineNum, numStr);
                throw new Error();
            }
        };
    }

    private NExpr parseFloat(){
        Token current = iterator.previous();
        iterator.inc();

        String numStr = current.val;
        int postfixBeginIndex = numStr.length();
        postfixBeginIndex = numStr.lastIndexOf("f") == -1? postfixBeginIndex : numStr.lastIndexOf("f");
        postfixBeginIndex = numStr.lastIndexOf("d") == -1? postfixBeginIndex : numStr.lastIndexOf("d");
        String postfix = numStr.substring(postfixBeginIndex);

        BigDecimal decimal = new BigDecimal(numStr.substring(0, postfixBeginIndex));

        return switch (postfix){
            case "f", "" -> {
                yield new NFloat32(current.lineNum, current.columnNum, decimal.floatValue());
            }
            case "d" -> {
                yield new NFloat64(current.lineNum, current.columnNum, decimal.doubleValue());
            }
            default -> {
                errorHandler.error(Errors.PARSE_INT_INVALID, current.lineNum, numStr);
                throw new Error();
            }
        };
    }

    private Type parseType(){
        return unwrapTuple(parseFnType(parseTypeAtom()));
    }

    public Type unwrapTuple(Type type){
        if(type instanceof TupleType tup && tup.getTupleTypes().length == 1)
            return tup.getTupleTypes()[0];
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

    private Type parseFnType(Type left){
        if(iterator.current().tok != TokenType.SingleArrow)
            return left;
        iterator.inc();

        Type returnType = parseFnType(parseTypeAtom());

        return new FnType(returnType, left instanceof TupleType tup? tup.getTupleTypes() : new Type[]{ left });
    }

    public TupleType parseTupleType(){
        Type[] types = parseDelimAs(TokenType.LParen, TokenType.RParen, TokenType.Comma, (i) -> parseFnType(parseTypeAtom()), Type.class);

        return new TupleType(types);
    }

    private NExpr[] parseDelim(TokenType start, TokenType end, TokenType separator){
        return parseDelimAs(start, end, separator, i -> parseExpr(), NExpr.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T[] parseDelimAs(TokenType start, TokenType end, TokenType separator, Function<Integer, T> fn, Class<T> clazz) {
        if(iterator.current().tok != start)
            errorHandler.error(Errors.PARSE_DELIM_EXPECTED_START, iterator.current().lineNum, start.expandedName());

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
                errorHandler.error(Errors.PARSE_DELIM_EXPECTED_SEPARATOR, iterator.current().lineNum, separator.expandedName());

            iterator.inc();
        }
        iterator.inc();

        return exprs.toArray((T[])Array.newInstance(clazz, 0));
    }
}