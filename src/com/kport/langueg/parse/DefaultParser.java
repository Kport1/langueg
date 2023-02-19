package com.kport.langueg.parse;

import com.kport.langueg.error.ErrorHandler;
import com.kport.langueg.error.Errors;
import com.kport.langueg.lex.Token;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.*;
import com.kport.langueg.parse.ast.astVals.*;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.typeCheck.types.*;
import com.kport.langueg.util.Iterator;

import java.util.*;

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
        opPrecedence.put(TokenType.And, 3);
        opPrecedence.put(TokenType.Or, 2);
        opPrecedence.put(TokenType.XOr, 4);

        opPrecedence.put(TokenType.Plus, 5);
        opPrecedence.put(TokenType.Minus, 5);
        opPrecedence.put(TokenType.Mul, 6);
        opPrecedence.put(TokenType.Div, 6);
        opPrecedence.put(TokenType.Mod, 6);
        opPrecedence.put(TokenType.Pow, 7);
        opPrecedence.put(TokenType.ShiftR, 6);
        opPrecedence.put(TokenType.ShiftL, 6);
    }
    private static final HashSet<TokenType> unaryOps = new HashSet<>();
    static {
        unaryOps.add(TokenType.Not);
        unaryOps.add(TokenType.Inc);
        unaryOps.add(TokenType.Dec);
    }

    private ErrorHandler errorHandler;

    @Override
    public AST process(Object tokens_, LanguegPipeline<?, ?> pipeline) {
        ArrayList<Token> tokens = (ArrayList<Token>) tokens_;
        iterator = new Iterator<>(tokens);

        errorHandler = pipeline.getErrorHandler();

        AST prog = parseBlock(true);
        prog.type = ASTTypeE.Prog;
        return prog;
    }

    private AST parseExpr(){
        return parseBinaryOp(parseUnaryOp(call(parseAtom())), -1);
    }

    private AST parseBinaryOp(AST left, int lastPrec) {
        Token current = iterator.current();

        if(isBinOp(current)){
            int currentPrec = opPrecedence.get(current.tok);
            if(     currentPrec > lastPrec ||
                    (current.tok == TokenType.Assign || current.tok.isOpAssign()))
            {
                iterator.inc();
                AST right = parseBinaryOp(parseUnaryOp(call(parseAtom())), currentPrec);
                return parseBinaryOp(new AST(ASTTypeE.BinOp, new ASTTok(current.tok), current.lineNum, current.columnNum, left, right), lastPrec);
            }
        }
        return left;
    }

    private AST parseUnaryOp(AST left) {
        Token current  = iterator.current();
        if(isUnaryOp(current)) {
            if (left == null) {
                iterator.inc();
                return new AST(ASTTypeE.UnaryOpBefore, new ASTTok(current.tok), current.lineNum, current.columnNum, parseExpr());
            } else {
                iterator.inc();
                return new AST(ASTTypeE.UnaryOpAfter, new ASTTok(current.tok), current.lineNum, current.columnNum, left);
            }
        }
        return left;
    }

    private AST call(AST left){
        Token current = iterator.current();
        if(current.tok == TokenType.LParen){
            int line = current.lineNum;
            int column = current.columnNum;
            AST tup = parseTuple();
            if(tup.type == ASTTypeE.Tuple && tup.children.length == 0) {
                return call(new AST(ASTTypeE.Call, line, column, left));
            }
            if(tup.type != ASTTypeE.Tuple){
                return call(new AST(ASTTypeE.Call, line, column, left, tup));
            }
            AST[] callArgs = new AST[tup.children.length + 1];
            System.arraycopy(tup.children, 0, callArgs, 1, tup.children.length);
            callArgs[0] = left;

            return call(new AST(ASTTypeE.Call, line, column, callArgs));
        }
        return left;
    }

    private AST parseTuple(){
        int line = iterator.current().lineNum;
        int column = iterator.current().columnNum;
        AST[] exprs = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);
        if(exprs.length == 0) {
            return new AST(ASTTypeE.Tuple, line, column);
        }
        if(exprs.length == 1){
            return exprs[0];
        }
        return new AST(ASTTypeE.Tuple, line, column, exprs);
    }

    private AST parseIf(){
        int ifLine = iterator.previous().lineNum;
        int ifColumn = iterator.current().columnNum;
        iterator.inc();

        AST condition = parseTuple();

        if(condition.type == ASTTypeE.Tuple){
            if(condition.children.length < 1){
                errorHandler.error(Errors.PARSE_IF_CONDITION_EMPTY, condition.line);
            }
            errorHandler.error(Errors.PARSE_IF_CONDITION_TUPLE, condition.line);
        }

        AST block = parseExpr();

        if(iterator.peek().tok == TokenType.Else){
            iterator.inc();
            iterator.inc();
            AST elseBlock = parseExpr();

            if(elseBlock.type == ASTTypeE.Block){
                exprIsBlock = true;
            }

            return new AST(ASTTypeE.If, ifLine, ifColumn, condition, block, elseBlock);
        }

        if(block.type == ASTTypeE.Block){
            exprIsBlock = true;
        }

        return new AST(ASTTypeE.If, ifLine, ifColumn, condition, block);
    }

    private AST parseWhile(){
        int whileLine = iterator.previous().lineNum;
        int whileColumn = iterator.current().columnNum;
        iterator.inc();

        AST condition = parseTuple();

        if(condition.type == ASTTypeE.Tuple){
            if(condition.children == null || condition.children.length < 1){
                errorHandler.error(Errors.PARSE_WHILE_CONDITION_EMPTY, condition.line);
            }
            errorHandler.error(Errors.PARSE_WHILE_CONDITION_TUPLE, condition.line);
        }

        AST block = parseExpr();

        if(block.type == ASTTypeE.Block){
            exprIsBlock = true;
        }

        return new AST(ASTTypeE.While, whileLine, whileColumn, condition, block);
    }

    private AST parseFor(){
        int forLine = iterator.previous().lineNum;
        int forColumn = iterator.current().columnNum;

        int initCondIncLine = iterator.next().lineNum;
        AST[] initCondInc = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Semicolon);

        if(initCondInc.length != 3){
            if(initCondInc.length == 0)
                errorHandler.error(Errors.PARSE_FOR_HEAD_EMPTY, initCondIncLine);

            errorHandler.error(Errors.PARSE_FOR_HEAD_MALFORMED, initCondIncLine);
        }

        AST block = parseExpr();

        if(block.type == ASTTypeE.Block){
            exprIsBlock = true;
        }

        AST[] asts = new AST[initCondInc.length + 1];
        System.arraycopy(initCondInc, 0, asts, 0, initCondInc.length);
        asts[asts.length - 1] = block;

        return new AST(ASTTypeE.For, forLine, forColumn, asts);
    }

    private AST parseFn(){
        //Identifier(ASTStr), Type(ASTType)
        int fnLine = iterator.previous().lineNum;
        int fnColumn = iterator.current().columnNum;
        iterator.inc();
        AST returnAST = parseAtom();

        Type returnType = null;
        try {
            returnType = Type.of(returnAST);
        } catch (TypeConversionException e) {
            errorHandler.error(Errors.PARSE_FN_RETURN_INVALID_TYPE, e.notType.line, e.notType.column);
        }

        String name = null;
        int nameLine = -1;
        int nameColumn = -1;
        if(iterator.current().tok == TokenType.Identifier){
            name = iterator.current().val;
            nameLine = iterator.current().lineNum;
            nameColumn = iterator.current().columnNum;
            iterator.inc();
        }

        AST[] args = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);

        //verify, that args follow: type argName
        for (AST arg : args) {
            if(name != null) {
                if (arg.type != ASTTypeE.Var)
                    errorHandler.error(Errors.PARSE_FN_PARAMETER_MALFORMED, arg.line, name);
                if (arg.val == null)
                    errorHandler.error(Errors.PARSE_FN_PARAMETER_VAR, arg.line, name, arg.children[0].val);
            }
            if (arg.type != ASTTypeE.Var)
                errorHandler.error(Errors.PARSE_ANON_FN_PARAMETER_MALFORMED, arg.line);
            if (arg.val == null)
                errorHandler.error(Errors.PARSE_ANON_FN_PARAMETER_VAR, arg.line, arg.children[0].val);

            arg.type = ASTTypeE.FnArg;
        }

        int blockLine = iterator.current().lineNum;
        int blockColumn = iterator.current().columnNum;
        AST block = parseExpr();

        //don't require semicolon after block
        if(block.type == ASTTypeE.Block){
            if(name != null) {
                exprIsBlock = true;
            }
            else{
                exprIsBlock = false;
                iterator.inc();
            }
        }
        //Surround expression in block and return it
        if(block.type != ASTTypeE.Block) {
            block = new AST(ASTTypeE.Block, blockLine, blockColumn, block.type == ASTTypeE.Return? block : new AST(ASTTypeE.Return, blockLine, blockColumn, block));
        }

        //params, block, (name)
        AST[] asts = new AST[args.length + (name == null? 1 : 2)];
        System.arraycopy(args, 0, asts, 0, args.length);
        asts[args.length] = block;

        if(name == null){
            return new AST(ASTTypeE.AnonFn, new ASTType(returnType), fnLine, fnColumn, asts);
        }

        asts[asts.length - 1] = new AST(ASTTypeE.Identifier, new ASTStr(name), nameLine, nameColumn);
        return new AST(ASTTypeE.Fn, new ASTType(returnType), fnLine, fnColumn, asts);
    }

    private Type parseFnType(){
        if(iterator.current().tok != TokenType.LBrack)
            errorHandler.error(Errors.PARSE_FNTYPE_EXPECTED_LBRACK, iterator.current().lineNum);


        AST[] params;
        if(iterator.next().tok == TokenType.LParen){
            params = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);
        }
        else{
            params = new AST[]{parseExpr()};
        }

        Type[] paramTypes = new Type[0];
        try {
            paramTypes = Type.of(params);
        } catch (TypeConversionException e) {
            errorHandler.error(Errors.PARSE_FNTYPE_PARAM_INVALID_TYPE, e.notType.line, e.notType.column);
        }

        if(iterator.current().tok != TokenType.SingleArrow)
            errorHandler.error(Errors.PARSE_FNTYPE_EXPECTED_ARROW, iterator.current().lineNum);
        iterator.inc();

        Type returnType = null;
        try {
            returnType = Type.of(parseExpr());
        } catch (TypeConversionException e) {
            errorHandler.error(Errors.PARSE_FNTYPE_RETURN_INVALID_TYPE, e.notType.line, e.notType.column);
        }

        if(iterator.current().tok != TokenType.RBrack)
            errorHandler.error(Errors.PARSE_FNTYPE_EXPECTED_RBRACK, iterator.current().lineNum);
        iterator.inc();


        return new FnType(returnType, paramTypes);
    }

    private boolean exprIsBlock = false;
    private AST parseBlock(boolean isProg){
        ArrayList<AST> exprs = new ArrayList<>();

        int blockLine = iterator.current().lineNum;
        int blockColumn = iterator.current().columnNum;
        if(!isProg){
            blockLine = iterator.previous().lineNum;
            blockColumn = iterator.current().columnNum;
            iterator.inc();
        }

        if(iterator.current().tok == TokenType.RCurl){
            exprIsBlock = true;
            return new AST(ASTTypeE.Block, blockLine, blockColumn, new AST[0]);
        }

        do {
            exprIsBlock = false;

            exprs.add(parseExpr());

            if (!exprIsBlock && iterator.current().tok != TokenType.Semicolon)
                errorHandler.error(Errors.PARSE_BLOCK_EXPECTED_SEMICOLON, iterator.previous().lineNum, iterator.current());

            iterator.inc();
            if(iterator.current().tok == TokenType.RCurl && !iterator.isEOF() && !isProg){
                exprIsBlock = true;
                return new AST(ASTTypeE.Block, blockLine, blockColumn, exprs.toArray(new AST[0]));
            }

        } while(!iterator.isEOF());

        if(isProg)
            return new AST(ASTTypeE.Block, blockLine, blockColumn, exprs.toArray(new AST[0]));


        errorHandler.error(Errors.PARSE_BLOCK_NOT_CLOSED, blockLine);
        return null;
    }

    private static final EnumMap<TokenType, ASTValue> tokenToPrimitiveTypeMap = new EnumMap<>(TokenType.class);
    static{
        tokenToPrimitiveTypeMap.put(TokenType.Boolean, new ASTType(PrimitiveType.Boolean));
        tokenToPrimitiveTypeMap.put(TokenType.Byte, new ASTType(PrimitiveType.Byte));
        tokenToPrimitiveTypeMap.put(TokenType.Char, new ASTType(PrimitiveType.Char));
        tokenToPrimitiveTypeMap.put(TokenType.Short, new ASTType(PrimitiveType.Short));
        tokenToPrimitiveTypeMap.put(TokenType.Int, new ASTType(PrimitiveType.Int));
        tokenToPrimitiveTypeMap.put(TokenType.Long, new ASTType(PrimitiveType.Long));
        tokenToPrimitiveTypeMap.put(TokenType.Float, new ASTType(PrimitiveType.Float));
        tokenToPrimitiveTypeMap.put(TokenType.Double, new ASTType(PrimitiveType.Double));
        tokenToPrimitiveTypeMap.put(TokenType.Void, new ASTType(PrimitiveType.Void));
    }
    private AST parseVar(ASTValue varType, int varLine, int varColumn){
        if(iterator.current().tok == TokenType.LParen){
            AST[] identifiers = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);
            for (AST ident : identifiers) {
                if(ident.type != ASTTypeE.Identifier) errorHandler.error(Errors.PARSE_VAR_DESTRUCT_EXPECTED_IDENTIFIER, ident.line);
            }

            if(iterator.current().tok != TokenType.Assign)
                errorHandler.error(Errors.PARSE_VAR_DESTRUCT_CANNOT_INFER_TYPE, varLine);

            iterator.inc();
            AST[] asts = Arrays.copyOfRange(identifiers, 0, identifiers.length + 1);
            asts[asts.length - 1] = parseExpr();
            return new AST(ASTTypeE.VarDestruct, varType, varLine, varColumn, asts);
        }
        Token identifier = iterator.current();
        if(identifier.tok != TokenType.Identifier)
            errorHandler.error(Errors.PARSE_VAR_EXPECTED_IDENTIFIER, identifier.lineNum);

        if(iterator.next().tok == TokenType.Assign){
            iterator.inc();
            return new AST(ASTTypeE.Var, varType, varLine, varColumn, new AST(ASTTypeE.Identifier, new ASTStr(identifier.val), identifier.lineNum, identifier.columnNum), parseExpr());
        }

        return new AST(ASTTypeE.Var, varType, varLine, varColumn, new AST(ASTTypeE.Identifier, new ASTStr(identifier.val), identifier.lineNum, identifier.columnNum));
    }

    private AST parseNum(){
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
            try{ return new AST(ASTTypeE.Int, new ASTInt(Integer.parseInt(numStr, isHex? 16 : 10)), current.lineNum, current.columnNum); }
            catch (NumberFormatException a) {
                try{ return new AST(ASTTypeE.Long, new ASTLong(java.lang.Long.parseLong(numStr, isHex? 16 : 10)), current.lineNum, current.columnNum); }
                catch (NumberFormatException b) {
                    if(isHex) errorHandler.error(Errors.PARSE_NUM_INVALID_HEX, current.lineNum, numStr);
                    try{ return new AST(ASTTypeE.Float, new ASTFloat(java.lang.Float.parseFloat(numStr)), current.lineNum, current.columnNum); }
                    catch (NumberFormatException c) {
                        try{ return new AST(ASTTypeE.Double, new ASTDouble(java.lang.Double.parseDouble(numStr)), current.lineNum, current.columnNum); }
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
                    return new AST(ASTTypeE.Long, new ASTLong(java.lang.Long.parseLong(numStr, isHex? 16 : 10)), current.lineNum, current.columnNum);
                }

                case "s", "S" -> {
                    return new AST(ASTTypeE.Short, new ASTShort(java.lang.Short.parseShort(numStr, isHex? 16 : 10)), current.lineNum, current.columnNum);
                }

                case "b", "B" -> {
                    short val = java.lang.Short.parseShort(numStr, isHex? 16 : 10);
                    if(val > 255 || val < 0) throw new NumberFormatException();
                    return new AST(ASTTypeE.Byte, new ASTByte((byte)val), current.lineNum, current.columnNum);
                }

                case "d", "D" -> {
                    if(isHex) errorHandler.error(Errors.PARSE_NUM_INVALID_HEX, current.lineNum, numStr);
                    return new AST(ASTTypeE.Double, new ASTDouble(java.lang.Double.parseDouble(numStr)), current.lineNum, current.columnNum);
                }

                case "f", "F" -> {
                    if(isHex) errorHandler.error(Errors.PARSE_NUM_INVALID_HEX, current.lineNum, numStr);
                    return new AST(ASTTypeE.Float, new ASTFloat(java.lang.Float.parseFloat(numStr)), current.lineNum, current.columnNum);
                }

            }
        } catch (NumberFormatException ignored){}

        errorHandler.error(Errors.PARSE_NUM_INVALID, current.lineNum, numStr);
        return null;
    }

    private AST parseAtom(){

        if(iterator.isEOF())
            errorHandler.error(Errors.PARSE_ATOM_REACHED_EOF);


        Token cur = iterator.current();
        iterator.inc();

        switch (cur.tok){

            case LParen -> {
                iterator.dec();
                AST tup = parseTuple();

                if (iterator.current().tok == TokenType.Identifier && iterator.peek().tok != TokenType.LParen) {
                    Type[] tupleTypes = new Type[0];
                    try {
                        tupleTypes = tup.type == ASTTypeE.Tuple? Type.of(tup.children) : new Type[]{Type.of(tup)};
                    } catch (TypeConversionException e) {
                        errorHandler.error(Errors.PARSE_TUPLETYPE_INVALID_TYPE, e.notType.line, e.notType.column);
                    }
                    return parseVar(new ASTType(new TupleType(tupleTypes)), tup.line, tup.column);
                }
                return tup;
            }

            case StringL -> {
                return new AST(ASTTypeE.Str, new ASTStr(cur.val), cur.lineNum, cur.columnNum);
            }

            case NumberL -> {
                return parseNum();
            }

            case LBrack -> {
                iterator.dec();
                int line = iterator.current().lineNum;
                int column = iterator.current().columnNum;
                AST[] elements = parseDelim(TokenType.LBrack, TokenType.RBrack, TokenType.Comma);
                if(elements.length == 1){
                    try {
                        return new AST(ASTTypeE.Cast, new ASTType(Type.of(elements[0])), line, column, parseExpr());
                    } catch (TypeConversionException e) {
                        errorHandler.error(Errors.PARSE_CAST_INVALID_TYPE, e.notType.line, e.notType.column);
                    }
                }
            }

            case Identifier -> {
                if(iterator.current().tok == TokenType.Identifier && iterator.peek().tok != TokenType.LParen){
                    String type = cur.val;
                    return parseVar(new ASTType(new CustomType(type)), cur.lineNum, cur.columnNum);
                }
                return new AST(ASTTypeE.Identifier, new ASTStr(cur.val), cur.lineNum, cur.columnNum);
            }

            case Var -> {
                return parseVar(null, cur.lineNum, cur.columnNum);
            }

            //Primitives
            case Boolean, Byte, Char, Short, Int, Long, Float, Double -> {
                TokenType type = cur.tok;

                if(iterator.current().tok == TokenType.Identifier && iterator.peek().tok != TokenType.LParen) {
                    return parseVar(tokenToPrimitiveTypeMap.get(type), cur.lineNum, cur.columnNum);
                }

                return new AST(ASTTypeE.Type, tokenToPrimitiveTypeMap.get(type), cur.lineNum, cur.columnNum);
            }

            case Void -> {
                return new AST(ASTTypeE.Type, tokenToPrimitiveTypeMap.get(TokenType.Void), cur.lineNum, cur.columnNum);
            }

            case FnType -> {
                Type fnType = parseFnType();

                if(iterator.current().tok == TokenType.Identifier && iterator.peek().tok != TokenType.LParen) {
                    return parseVar(new ASTType(fnType), cur.lineNum, cur.columnNum);
                }

                return new AST(ASTTypeE.Type, new ASTType(fnType), cur.lineNum, cur.columnNum);
            }

            case If -> {
                return parseIf();
            }

            case While -> {
                return parseWhile();
            }

            case For -> {
                return parseFor();
            }

            case LCurl -> {
                return parseBlock(false);
            }

            case True -> {
                return new AST(ASTTypeE.Bool, new ASTBool(true), cur.lineNum, cur.columnNum);
            }

            case False -> {
                return new AST(ASTTypeE.Bool, new ASTBool(false), cur.lineNum, cur.columnNum);
            }

            case Fn -> {
                return parseFn();
            }

            case Class -> {
                //return parseClass();
            }

            case Public, Private, Protected, Static -> {
                return new AST(ASTTypeE.Modifier, new ASTTok(cur.tok), cur.lineNum, cur.columnNum, parseExpr());
            }

            case Return -> {
                if(iterator.current().tok == TokenType.Semicolon)
                    return new AST(ASTTypeE.Return, cur.lineNum, cur.columnNum);

                return new AST(ASTTypeE.Return, cur.lineNum, cur.columnNum, parseExpr());
            }

        }

        errorHandler.error(Errors.PARSE_ATOM_UNEXPECTED_TOKEN, cur.lineNum, cur.tok.expandedName());
        return null;
    }

    private AST[] parseDelim(TokenType start, TokenType end, TokenType separator){
        if(iterator.current().tok != start)
            errorHandler.error(Errors.PARSE_DELIM_EXPECTED_START, iterator.current().lineNum, start.expandedName());

        ArrayList<AST> exprs = new ArrayList<>();

        if(iterator.next().tok == end){
            iterator.inc();
            return new AST[0];
        }

        while(iterator.current().tok != end){
            exprs.add(parseExpr());

            if(iterator.current().tok == end){
                break;
            }

            if(iterator.current().tok != separator)
                errorHandler.error(Errors.PARSE_DELIM_EXPECTED_SEPARATOR, iterator.current().lineNum, separator.expandedName());

            iterator.inc();
        }
        iterator.inc();

        return exprs.toArray(new AST[0]);
    }

    /*private Type[] typify(AST... asts){
        if(asts == null) return null;
        return
                Arrays.stream(asts).map((type) -> {
                    if(type.type == Tuple){
                        return new TupleType(typify(type.children));
                    }
                    if (type.type == Identifier) {
                        return new CustomType(type.val.getStr());
                    }
                    if(type.val.isType()) {
                        return type.val.getType();
                    }
                    if(type.val == null){
                        errorHandler.error(Errors.PARSE_TYPE_INVALID, type.line, type.type.expandedName());
                    }

                    errorHandler.error(Errors.PARSE_TYPE_INVALID, type.line, type.type.expandedName());
                    return null;
                }).toArray(Type[]::new);
    }*/

    private boolean isBinOp(Token tok){
        return opPrecedence.containsKey(tok.tok);
    }

    private boolean isUnaryOp(Token tok){
        return unaryOps.contains(tok.tok);
    }

}