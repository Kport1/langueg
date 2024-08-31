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
import com.kport.langueg.util.Either;
import com.kport.langueg.util.Iterator;
import com.kport.langueg.util.Pair;

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
        return new NProg(0, stmnts.toArray(AST[]::new));
    }

    private AST parseStatement(){
        Token cur = iterator.current();

        return switch (cur.tok){

            case Var -> parseVar();

            case Fn -> parseFn();

            case TypeDef -> parseTypeDef();

            default -> parseExpr();
        };
    }

    private NExpr parseExpr(){
        if(iterator.current().tok == TokenType.Semicolon){
            iterator.inc();
            return new NBlock(iterator.current().offset);
        }

        return  parseBinaryOp(
                    parseCast(
                        parseUnaryOp(
                            parseDotAccess(
                                parseCall(parseAtom())
                            )
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

            case LCurl -> {
                if(iterator.peek().tok == TokenType.Dot)
                    return parseUnion();

                return parseBlock();
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
        }

        errorHandler.error(Errors.PARSE_ATOM_UNEXPECTED_TOKEN, cur.offset, cur.tok.expandedName());
        return null;
    }

    private NExpr parseBinaryOp(NExpr left, int lastPrec) {
        Token cur = iterator.current();
        if(!(BinOp.isBinOp(cur.tok) || CompoundAssign.isCompoundAssign(cur.tok) || cur.tok == TokenType.Assign)) return left;

        int currentPrec = opPrecedence.get(cur.tok);
        if(currentPrec <= lastPrec) return left;

        iterator.inc();
        NExpr right = parseBinaryOp(parseCast(parseUnaryOp(parseDotAccess(parseCall(parseAtom())))), currentPrec);

        if(cur.tok == TokenType.Assign){
            if(!(left instanceof NAssignable assignable)) throw new Error("Cannot assign a value to:\n" + left);
            return parseBinaryOp(new NAssign(cur.offset, assignable, right), lastPrec);
        }
        if(CompoundAssign.isCompoundAssign(cur.tok)){
            if(!(left instanceof NAssignable assignable)) throw new Error("Cannot assign a value to:\n" + left);
            return parseBinaryOp(new NAssignCompound(cur.offset, assignable, right, cur.tok), lastPrec);
        }
        if(BinOp.isBinOp(cur.tok)) {
            return parseBinaryOp(new NBinOp(cur.offset, left, right, cur.tok), lastPrec);
        }
        throw new Error();
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

        Either<Integer, String> accessor = parseDotAccessor();
        if(accessor == null) errorHandler.error(Errors.PLACEHOLDER, cur.offset + 1);

        return parseDotAccess(new NDotAccess(cur.offset, left, accessor));
    }

    private Either<Integer, String> parseDotAccessor(){
        return switch(Objects.requireNonNull(parseAtom())){
            case NInt32 int32 -> {
                if(int32.val < 0){
                    errorHandler.error(Errors.PLACEHOLDER, int32.codeOffset());
                    yield null;
                }
                yield Either.left(int32.val);
            }

            case NUInt32 uint32 -> Either.left(uint32.val);

            case NIdent ident -> Either.right(ident.identifier);

            case NExpr ignored -> null;
        };
    }

    private NExpr parseCall(NExpr left){
        Token cur = iterator.current();
        if(cur.tok != TokenType.LParen) return left;

        NExpr[] args = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);
        return parseCall(new NCall(cur.offset, left, args));
    }

    private NExpr parseCast(NExpr left){
        Token cur = iterator.current();
        if(cur.tok != TokenType.As) return left;

        iterator.inc();
        return new NCast(cur.offset, parseType(), left);
    }

    private NExpr parseTuple(){
        Token cur = iterator.current();

        final boolean[] trailingComma = {false};
        Pair<Either<Integer, String>, NExpr>[] exprs = parseDelimAs(TokenType.LParen, TokenType.RParen, TokenType.Comma, i -> {
            trailingComma[0] = false;

            Either<Integer, String> position = null;
            if(iterator.current().tok == TokenType.Dot){
                iterator.inc();
                position = parseDotAccessor();
                if(iterator.current().tok != TokenType.Assign) errorHandler.error(Errors.PLACEHOLDER, iterator.current().offset);
                iterator.inc();
            }

            NExpr expr = parseExpr();
            if(iterator.current().tok == TokenType.Comma) trailingComma[0] = true;
            return new Pair<>(position, expr);
        }, Pair.class);

        if(!trailingComma[0] && exprs.length == 1 && exprs[0].left == null) return exprs[0].right;

        boolean[] elemIsSet = new boolean[exprs.length];
        HashSet<String> usedNames = new HashSet<>();
        for (int i = 0; i < exprs.length; i++) {
            if(exprs[i].left == null){
                if(elemIsSet[i]) errorHandler.error(Errors.PLACEHOLDER, exprs[i].right.codeOffset());
                elemIsSet[i] = true;
                continue;
            }

            switch (exprs[i].left){
                case Either.Left<Integer, String> index -> {
                    if(index.value() >= exprs.length) errorHandler.error(Errors.PLACEHOLDER, 0);
                    if(elemIsSet[index.value()]) errorHandler.error(Errors.PLACEHOLDER, 0);
                    elemIsSet[index.value()] = true;
                }
                case Either.Right<Integer, String> name -> {
                    if(usedNames.contains(name.value())) errorHandler.error(Errors.PLACEHOLDER, 0);
                    usedNames.add(name.value());
                }
            }
        }

        return new NTuple(cur.offset, exprs);
    }

    private NUnion parseUnion(){
        Token cur = iterator.current();
        if(iterator.current().tok != TokenType.LCurl) errorHandler.error(Errors.PLACEHOLDER, cur.offset);

        if (iterator.next().tok != TokenType.Dot) errorHandler.error(Errors.PLACEHOLDER, iterator.current().offset);
        iterator.inc();

        Either<Integer, String> position = parseDotAccessor();
        if(position == null) errorHandler.error(Errors.PLACEHOLDER, cur.offset + 1);

        if(iterator.current().tok != TokenType.Assign) errorHandler.error(Errors.PLACEHOLDER, iterator.current().offset);

        iterator.inc();
        NExpr expr = parseExpr();

        if(iterator.current().tok != TokenType.RCurl) errorHandler.error(Errors.PLACEHOLDER, iterator.current().offset);
        iterator.inc();

        return new NUnion(cur.offset, expr, position);
    }

    private NExpr parseIf(){
        Token cur = iterator.current();
        iterator.inc();

        NExpr condition = parseEnclosed(TokenType.LParen, TokenType.RParen);

        NExpr block = parseExpr();

        if(iterator.current().tok == TokenType.Else){
            iterator.inc();
            NExpr elseBlock = parseExpr();

            return new NIfElse(cur.offset, condition, block, elseBlock);
        }

        return new NIf(cur.offset, condition, block);
    }

    @SuppressWarnings("unchecked")
    private NMatch parseMatch(){
        Token cur = iterator.current();
        iterator.inc();

        NExpr value = parseEnclosed(TokenType.LParen, TokenType.RParen);

        ArrayList<Pair<NMatch.Pattern, NExpr>> branches = new ArrayList<>();
        while (iterator.current().tok == TokenType.Case){
            if(iterator.next().tok == TokenType.Dot){
                int elemOffset = iterator.next().offset;
                Either<Integer, String> element = parseDotAccessor();
                if(element == null) errorHandler.error(Errors.PLACEHOLDER, elemOffset);

                if(iterator.current().tok != TokenType.Identifier) errorHandler.error(Errors.PLACEHOLDER, iterator.current().offset);
                String elementVarName = iterator.current().val;

                if(iterator.next().tok != TokenType.DoubleArrow) errorHandler.error(Errors.PLACEHOLDER, iterator.current().offset);
                iterator.inc();

                NExpr expr = parseExpr();
                branches.add(new Pair<>(new NMatch.Pattern.Union(element, elementVarName), expr));
            }
            else {
                errorHandler.error(Errors.PLACEHOLDER, iterator.current().offset);
            }
        }

        if(iterator.current().tok == TokenType.Else){
            if(iterator.next().tok != TokenType.DoubleArrow) errorHandler.error(Errors.PLACEHOLDER, iterator.current().offset);
            iterator.inc();
            NExpr expr = parseExpr();
            branches.add(new Pair<>(new NMatch.Pattern.Default(), expr));
        }

        return new NMatch(cur.offset, value, branches.toArray(new Pair[0]));
    }

    private NWhile parseWhile(){
        Token cur = iterator.current();
        iterator.inc();

        NExpr condition = parseEnclosed(TokenType.LParen, TokenType.RParen);

        NExpr block = parseExpr();

        return new NWhile(cur.offset, condition, block);
    }

    private NAnonFn parseAnonFn(){
        Token cur = iterator.current();
        iterator.inc();

        FnHeader header = parseFnHeader();
        NExpr body = parseExpr();

        return new NAnonFn(cur.offset, header, body);
    }

    private NNamedFn parseFn(){
        Token cur = iterator.current();

        if (iterator.next().tok != TokenType.Identifier) errorHandler.error(Errors.PLACEHOLDER, 0);
        String name = iterator.current().val;

        iterator.inc();

        FnHeader header = parseFnHeader();
        NExpr body = parseExpr();

        return new NNamedFn(cur.offset, name, header, body);
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

    private NExpr parseBlock(){
        Token cur = iterator.current();

        ArrayList<AST> stmnts = new ArrayList<>();

        if(iterator.next().tok == TokenType.RCurl){
            iterator.inc();
            return new NBlock(cur.offset);
        }

        do {
            stmnts.add(parseStatement());

            if(iterator.current().tok == TokenType.RCurl && !iterator.isEOF()){
                iterator.inc();

                if(iterator.current().tok == TokenType.DoubleArrow){
                    iterator.inc();
                    AST lastStatement = stmnts.getLast();

                    if(!(lastStatement instanceof NExpr lastExpr)){
                        errorHandler.error(Errors.PLACEHOLDER, lastStatement.codeOffset());
                        return null;
                    }

                    stmnts.removeLast();
                    return new NBlockYielding(cur.offset, lastExpr, stmnts.toArray(new AST[0]));
                }

                return new NBlock(cur.offset, stmnts.toArray(new AST[0]));
            }
        } while(!iterator.isEOF());


        errorHandler.error(Errors.PARSE_BLOCK_NOT_CLOSED, cur.offset);
        return null;
    }

    private AST parseVar(){
        Token cur = iterator.current();

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

        return init == null? new NVar(cur.offset, type, name) : new NVarInit(cur.offset, type, name, init);
    }

    private NTypeDef parseTypeDef(){
        Token cur = iterator.current();

        String[] typeParameters = new String[0];

        if(iterator.next().tok == TokenType.Greater) {
            typeParameters = parseDelimAs(TokenType.Greater, TokenType.Less, TokenType.Comma, (i) ->
                    {
                        if (iterator.current().tok != TokenType.Identifier)
                            errorHandler.error(Errors.PLACEHOLDER, iterator.current().offset);
                        String str = iterator.current().val;
                        iterator.inc();
                        return str;
                    }
                    , String.class);
        }

        if(iterator.current().tok != TokenType.Identifier)
            errorHandler.error(Errors.PLACEHOLDER, iterator.current().offset);
        String name = iterator.current().val;

        if(iterator.next().tok != TokenType.Assign)
            errorHandler.error(Errors.PLACEHOLDER, iterator.current().offset);
        iterator.inc();

        return new NTypeDef(cur.offset, name, parseType(), typeParameters);
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
                if (tmpParseRes.compareTo(BigInteger.valueOf(1 << 8)) >= 0) throw new Error();
                yield new NUInt8(cur.offset, tmpParseRes.byteValue());
            }
            case "u16" -> {
                if (tmpParseRes.compareTo(BigInteger.valueOf(1 << 16)) >= 0) throw new Error();
                yield new NUInt16(cur.offset, tmpParseRes.shortValue());
            }
            case "u32", "u" -> {
                if (tmpParseRes.compareTo(BigInteger.valueOf(1L << 32)) >= 0) throw new Error();
                yield new NUInt32(cur.offset, tmpParseRes.intValue());
            }
            case "u64" -> {
                if (tmpParseRes.compareTo(BigInteger.valueOf(1).shiftLeft(64)) >= 0) throw new Error();
                yield new NUInt64(cur.offset, tmpParseRes.longValue());
            }
            case "i8" -> new NInt8(cur.offset, tmpParseRes.byteValue());

            case "i16" -> new NInt16(cur.offset, tmpParseRes.shortValue());

            case "i32", "i", "" -> new NInt32(cur.offset, tmpParseRes.intValue());

            case "i64" -> new NInt64(cur.offset, tmpParseRes.longValue());

            default -> {
                errorHandler.error(Errors.PARSE_INT_INVALID, cur.offset, numStr);
                throw new Error();
            }
        };
    }

    private NExpr parseFloat(){
        Token cur = iterator.current();
        iterator.inc();

        String numStr = cur.val;
        int postfixBeginIndex = numStr.lastIndexOf("f") == -1? numStr.length() : numStr.lastIndexOf("f");
        String postfix = numStr.substring(postfixBeginIndex);

        BigDecimal decimal = new BigDecimal(numStr.substring(0, postfixBeginIndex));

        return switch (postfix){
            case "f32", "" -> new NFloat32(cur.offset, decimal.floatValue());

            case "f64" -> new NFloat64(cur.offset, decimal.doubleValue());

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
                if(iterator.current().tok == TokenType.Greater){
                    typeArgs = parseDelimAs(TokenType.Greater, TokenType.Less, TokenType.Comma, (i) -> parseType(), Type.class);
                }
                return new NamedType(name, typeArgs);
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

        for (int i = 0; i < nameTypePairs.length; i++) {
            if(nameTypePairs[i].name == null) continue;
            for (int j = 0; j < nameTypePairs.length; j++) {
                if(i == j) continue;
                if(nameTypePairs[j].name == null) continue;

                if(Objects.equals(nameTypePairs[i].name, nameTypePairs[j].name)) errorHandler.error(Errors.PLACEHOLDER,0);
            }
        }

        return new TupleType(nameTypePairs);
    }

    public UnionType parseUnionType(){
        NameTypePair[] nameTypePairs = parseDelimAs(TokenType.LCurl, TokenType.RCurl, TokenType.Comma, (i) -> {
            String name = null;
            if (iterator.current().tok == TokenType.Identifier && iterator.peek().tok == TokenType.Colon){
                name = iterator.current().val;
                iterator.inc();
                iterator.inc();
            }
            Type type = parseFnType(parseArrayType(parseTypeAtom()));
            return new NameTypePair(type, name);
        }, NameTypePair.class);

        for (int i = 0; i < nameTypePairs.length; i++) {
            if(nameTypePairs[i].name == null) continue;
            for (int j = 0; j < nameTypePairs.length; j++) {
                if(i == j) continue;
                if(nameTypePairs[j].name == null) continue;

                if(Objects.equals(nameTypePairs[i].name, nameTypePairs[j].name)) errorHandler.error(Errors.PLACEHOLDER,0);
            }
        }

        return new UnionType(nameTypePairs);
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