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

        opPrecedence.put(TokenType.Greater, 1);
        opPrecedence.put(TokenType.Less, 1);
        opPrecedence.put(TokenType.GreaterEq, 1);
        opPrecedence.put(TokenType.LessEq, 1);
        opPrecedence.put(TokenType.Eq, 1);
        opPrecedence.put(TokenType.NotEq, 1);
        opPrecedence.put(TokenType.BAnd, 3);
        opPrecedence.put(TokenType.BOr, 2);
        opPrecedence.put(TokenType.BXOr, 4);

        opPrecedence.put(TokenType.Plus, 5);
        opPrecedence.put(TokenType.Minus, 5);
        opPrecedence.put(TokenType.Mul, 6);
        opPrecedence.put(TokenType.Div, 6);
        opPrecedence.put(TokenType.Mod, 6);
        opPrecedence.put(TokenType.Pow, 7);
        opPrecedence.put(TokenType.ShiftR, 6);
        opPrecedence.put(TokenType.ShiftL, 6);
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
            stmnts.add(parseStatement());
        }
        return new NProg(1, 0, stmnts.toArray(AST[]::new));
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

            case Var, Boolean, Byte, Char, Short, Int, Long, Float, Double, Void, LParen -> {
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

            case NumberL -> {
                return parseNum();
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

            case Not, Inc, Dec -> {
                return new NUnaryOpPre(cur.lineNum, cur.columnNum, parseExpr(), cur.tok);
            }

        }

        errorHandler.error(Errors.PARSE_ATOM_UNEXPECTED_TOKEN, cur.lineNum, cur.tok.expandedName());
        return null;
    }

    private NExpr parseBinaryOp(NExpr left, int lastPrec) {
        Token current = iterator.current();

        if(current.tok.isBinOp()){
            int currentPrec = opPrecedence.get(current.tok);
            if(     currentPrec > lastPrec ||
                    (current.tok == TokenType.Assign || current.tok.isOpAssign()))
            {
                iterator.inc();
                NExpr right = parseBinaryOp(parseUnaryOp(call(parseAtom())), currentPrec);
                if(current.tok == TokenType.Assign){
                    if(!(left instanceof NAssignable assignable)) throw new Error("Cannot assign a value to " + left);
                    return parseBinaryOp(new NAssign(current.lineNum, current.columnNum, assignable, right), lastPrec);
                }
                return parseBinaryOp(new NBinOp(current.lineNum, current.columnNum, left, right, current.tok), lastPrec);
            }
        }
        return left;
    }

    private NExpr parseUnaryOp(NExpr left) {
        Token current  = iterator.current();
        if(current.tok.isUnaryOp()) {
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

        iterator.inc();

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

    private NExpr parseNum(){
        Token current = iterator.previous();
        iterator.inc();

        String numStr = current.val;
        String prefix = numStr.length() > 1? numStr.substring(0, 2) : "";
        String suffix = numStr.length() > 0? numStr.substring(numStr.length() - 1) : "";

        boolean isHex = prefix.equals("0x");
        boolean typeSpecified = "lLsSbBdDfF".contains(suffix);
        numStr = isHex? numStr.substring(2) : numStr;
        numStr = typeSpecified? numStr.substring(0, numStr.length() - 1) : numStr;
        
        if(!typeSpecified){
            try{ return new NInt32(current.lineNum, current.columnNum, Integer.parseInt(numStr, isHex? 16 : 10)); }
            catch (NumberFormatException a) {
                try{ return new NInt64(current.lineNum, current.columnNum, Long.parseLong(numStr, isHex? 16 : 10)); }
                catch (NumberFormatException b) {
                    if(isHex) errorHandler.error(Errors.PARSE_NUM_INVALID_HEX, current.lineNum, numStr);
                    try{ return new NFloat32(current.lineNum, current.columnNum, Float.parseFloat(numStr)); }
                    catch (NumberFormatException c) {
                        try{ return new NFloat64(current.lineNum, current.columnNum, Double.parseDouble(numStr)); }
                        catch (NumberFormatException d) {
                            errorHandler.error(Errors.PARSE_NUM_INVALID, current.lineNum, numStr);
                        }
                    }
                }
            }
        }

        try {
            switch (suffix) {
                case "l", "L" -> {
                    return new NInt64(current.lineNum, current.columnNum, Long.parseLong(numStr, isHex? 16 : 10));
                }

                case "s", "S" -> {
                    return new NInt16(current.lineNum, current.columnNum, Short.parseShort(numStr, isHex? 16 : 10));
                }

                case "b", "B" -> {
                    short val = Short.parseShort(numStr, isHex? 16 : 10);
                    if(val > 255 || val < 0) throw new NumberFormatException();
                    return new NUInt8(current.lineNum, current.columnNum, (byte)val);
                }

                case "d", "D" -> {
                    if(isHex) errorHandler.error(Errors.PARSE_NUM_INVALID_HEX, current.lineNum, numStr);
                    return new NFloat64(current.lineNum, current.columnNum, Double.parseDouble(numStr));
                }

                case "f", "F" -> {
                    if(isHex) errorHandler.error(Errors.PARSE_NUM_INVALID_HEX, current.lineNum, numStr);
                    return new NFloat32(current.lineNum, current.columnNum, Float.parseFloat(numStr));
                }

            }
        } catch (NumberFormatException ignored){}

        errorHandler.error(Errors.PARSE_NUM_INVALID, current.lineNum, numStr);
        return null;
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
            case Boolean -> {
                return PrimitiveType.Bool;
            }
            case Byte -> {
                return PrimitiveType.U8;
            }
            case Char -> {
                return PrimitiveType.Char;
            }
            case Short -> {
                return PrimitiveType.I16;
            }
            case Int -> {
                return PrimitiveType.I32;
            }
            case Long -> {
                return PrimitiveType.I64;
            }
            case Float -> {
                return PrimitiveType.F32;
            }
            case Double -> {
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