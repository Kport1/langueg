package com.kport.langueg.parse;

import com.kport.langueg.error.ErrorHandler;
import com.kport.langueg.error.ErrorIntercept;
import com.kport.langueg.error.Errors;
import com.kport.langueg.lex.Token;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.*;
import com.kport.langueg.parse.ast.astVals.*;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.typeCheck.types.*;
import com.kport.langueg.util.Iterator;

import static com.kport.langueg.parse.ast.ASTTypeE.*;

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
        prog.type = Prog;
        return prog;
    }

    private AST parseExpr(){
        return parseBinaryOp(parseUnaryOp(call(parseAtom())), -1);
    }

    private AST parseBinaryOp(AST left, int lastPrec) {
        Token current = iterator.current();

        if(isBinOp(current)){
            int currentPrec = opPrecedence.get(current.tok);
            if(currentPrec > lastPrec){
                iterator.inc();
                AST right = parseBinaryOp(parseUnaryOp(call(parseAtom())), currentPrec);
                return parseBinaryOp(new AST(BinOp, new ASTTok(current.tok), current.lineNum, left, right), lastPrec);
            }
        }
        return left;
    }

    private AST parseUnaryOp(AST left) {
        Token current  = iterator.current();
        if(isUnaryOp(current)) {
            if (left == null) {
                iterator.inc();
                return new AST(UnaryOpBefore, new ASTTok(current.tok), current.lineNum, parseExpr());
            } else {
                iterator.inc();
                return new AST(UnaryOpAfter, new ASTTok(current.tok), current.lineNum, left);
            }
        }
        return left;
    }

    private AST call(AST left){
        Token current = iterator.current();
        if(current.tok == TokenType.LParen){
            int line = current.lineNum;
            AST tup = parseTuple();
            if(tup.type == Tuple && tup.children == null) {
                return call(new AST(Call, line, left));
            }
            if(tup.type != Tuple){
                return call(new AST(Call, line, left, tup));
            }
            AST[] callArgs = new AST[tup.children.length + 1];
            System.arraycopy(tup.children, 0, callArgs, 1, tup.children.length);
            callArgs[0] = left;

            return call(new AST(Call, line, callArgs));
        }
        return left;
    }

    private AST parseTuple(){
        int line = iterator.current().lineNum;
        AST[] exprs = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);
        if(exprs.length == 0) {
            return new AST(Tuple, line);
        }
        if(exprs.length == 1){
            return exprs[0];
        }
        return new AST(Tuple, line, exprs);
    }

    private AST parseIf(){
        int conditionLine = iterator.current().lineNum;

        AST condition = parseTuple();

        if(condition.type == Tuple){
            if(condition.children == null || condition.children.length < 1){
                errorHandler.error(Errors.PARSE_IF_CONDITION_EMPTY, conditionLine);
            }
            errorHandler.error(Errors.PARSE_IF_CONDITION_TUPLE, conditionLine);
        }

        AST block = parseExpr();

        if(iterator.peek().tok == TokenType.Else){
            iterator.inc();
            iterator.inc();
            AST elseBlock = parseExpr();

            if(elseBlock.type == Block){
                exprIsBlock = true;
            }

            return new AST(If, conditionLine, condition, block, elseBlock);
        }

        if(block.type == Block){
            exprIsBlock = true;
        }

        return new AST(If, conditionLine, condition, block);
    }

    private AST parseWhile(){
        AST condition = parseTuple();
        int conditionLine = iterator.current().lineNum;

        if(condition.type == Tuple){
            if(condition.children == null || condition.children.length < 1){
                errorHandler.error(Errors.PARSE_WHILE_CONDITION_EMPTY, conditionLine);
            }
            errorHandler.error(Errors.PARSE_WHILE_CONDITION_TUPLE, conditionLine);
        }

        AST block = parseExpr();

        if(block.type == Block){
            exprIsBlock = true;
        }

        return new AST(While, conditionLine, condition, block);
    }

    private AST parseFor(){
        int initCondIncLine = iterator.current().lineNum;
        AST[] initCondInc = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Semicolon);

        if(initCondInc.length != 3){
            if(initCondInc.length == 0)
                errorHandler.error(Errors.PARSE_FOR_HEAD_EMPTY, initCondIncLine);

            errorHandler.error(Errors.PARSE_FOR_HEAD_MALFORMED, initCondIncLine);
        }

        AST block = parseExpr();

        if(block.type == Block){
            exprIsBlock = true;
        }

        AST[] asts = new AST[initCondInc.length + 1];
        System.arraycopy(initCondInc, 0, asts, 0, initCondInc.length);
        asts[asts.length - 1] = block;

        return new AST(For, initCondIncLine, asts);
    }

    private AST parseFn(){
        //Identifier(ASTStr), Type(ASTType)
        int fnLine = iterator.current().lineNum;
        AST returnAST = parseAtom();
        ASTType returnType = new ASTType(typify(returnAST)[0]);

        String name = null;
        int nameLine = -1;
        if(iterator.current().tok == TokenType.Identifier){
            name = iterator.current().val;
            nameLine = iterator.current().lineNum;
            iterator.inc();
        }

        AST[] args = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);

        //verify, that args follow: type argName
        for (AST arg : args) {
            if(name != null) {
                if (arg.type != Var)
                    errorHandler.error(Errors.PARSE_FN_PARAMETER_MALFORMED, arg.line, name);
                if (arg.val == null)
                    errorHandler.error(Errors.PARSE_FN_PARAMETER_VAR, arg.line, name, arg.children[0].val);
            }
            if (arg.type != Var)
                errorHandler.error(Errors.PARSE_ANON_FN_PARAMETER_MALFORMED, arg.line);
            if (arg.val == null)
                errorHandler.error(Errors.PARSE_ANON_FN_PARAMETER_VAR, arg.line, arg.children[0].val);

            arg.type = FnArg;
        }

        int blockLine = iterator.current().lineNum;
        AST block = parseExpr();

        //don't require semicolon after block
        if(block.type == Block){
            if(name != null) {
                exprIsBlock = true;
            }
            else{
                exprIsBlock = false;
                iterator.inc();
            }
        }
        //Surround expression in block and return it
        if(block.type != Block) {
            block = new AST(Block, blockLine, block.type == Return? block : new AST(Return, blockLine, block));
        }

        //args, block, (name)
        AST[] asts = new AST[args.length + (name == null? 1 : 2)];
        System.arraycopy(args, 0, asts, 0, args.length);
        asts[args.length] = block;

        if(name != null){
            asts[asts.length - 1] = new AST(Identifier, new ASTStr(name), nameLine);
        }

        return new AST(Fn, returnType, fnLine, asts);
    }

    private Type parseFnType(){
        if(iterator.current().tok != TokenType.LBrack)
            errorHandler.error(Errors.PARSE_FNTYPE_EXPECTED_LBRACK, iterator.current().lineNum);


        AST[] params;
        if(iterator.next().tok == TokenType.LParen){
            params = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);
        }
        else{
            params = new AST[]{parseAtom()};
        }

        Type[] paramTypes = typify(params);

        if(iterator.current().tok != TokenType.SingleArrow)
            errorHandler.error(Errors.PARSE_FNTYPE_EXPECTED_ARROW, iterator.current().lineNum);
        iterator.inc();

        Type returnType = typify(parseAtom())[0];

        if(iterator.current().tok != TokenType.RBrack)
            errorHandler.error(Errors.PARSE_FNTYPE_EXPECTED_RBRACK, iterator.current().lineNum);
        iterator.inc();


        return new FnType(returnType, paramTypes);
    }

    private boolean exprIsBlock = false;
    private AST parseBlock(boolean isProg){
        ArrayList<AST> exprs = new ArrayList<>();

        int blockLine = iterator.current().lineNum;
        if(!isProg){
            blockLine = iterator.previous().lineNum;
            iterator.inc();
        }

        if(iterator.current().tok == TokenType.RCurl){
            exprIsBlock = true;
            return new AST(Block, blockLine);
        }

        do {
            exprIsBlock = false;

            exprs.add(parseExpr());

            if (!exprIsBlock && iterator.current().tok != TokenType.Semicolon)
                errorHandler.error(Errors.PARSE_BLOCK_EXPECTED_SEMICOLON, iterator.current().lineNum, iterator.current());

            iterator.inc();
            if(iterator.current().tok == TokenType.RCurl && !iterator.isEOF() && !isProg){
                exprIsBlock = true;
                return new AST(Block, blockLine, exprs.toArray(new AST[0]));
            }

        } while(!iterator.isEOF());

        if(isProg)
            return new AST(Block, blockLine, exprs.toArray(new AST[0]));


        errorHandler.error(Errors.PARSE_BLOCK_NOT_CLOSED, blockLine);
        return null;
    }

    private static final HashMap<TokenType, ASTValue> ASTTokTypeValues = new HashMap<>();
    static{
        ASTTokTypeValues.put(TokenType.Boolean, new ASTType(new PrimitiveType(TokenType.Boolean)));
        ASTTokTypeValues.put(TokenType.Byte, new ASTType(new PrimitiveType(TokenType.Byte)));
        ASTTokTypeValues.put(TokenType.Char, new ASTType(new PrimitiveType(TokenType.Char)));
        ASTTokTypeValues.put(TokenType.Short, new ASTType(new PrimitiveType(TokenType.Short)));
        ASTTokTypeValues.put(TokenType.Int, new ASTType(new PrimitiveType(TokenType.Int)));
        ASTTokTypeValues.put(TokenType.Long, new ASTType(new PrimitiveType(TokenType.Long)));
        ASTTokTypeValues.put(TokenType.Float, new ASTType(new PrimitiveType(TokenType.Float)));
        ASTTokTypeValues.put(TokenType.Double, new ASTType(new PrimitiveType(TokenType.Double)));
        ASTTokTypeValues.put(TokenType.Void, new ASTType(new PrimitiveType(TokenType.Void)));
        ASTTokTypeValues.put(TokenType.FnType, new ASTType(new PrimitiveType(TokenType.FnType)));
    }
    private AST parseVar(){
        int varLine = iterator.previous().lineNum;
        iterator.inc();
        Token identifier = iterator.current();
        if(identifier.tok != TokenType.Identifier)
            errorHandler.error(Errors.PARSE_VAR_EXPECTED_IDENTIFIER, identifier.lineNum);

        if(iterator.next().tok == TokenType.Assign){
            iterator.inc();
            return new AST(Var, varLine, new AST(Identifier, new ASTStr(identifier.val), identifier.lineNum), parseExpr());
        }

        return new AST(Var, varLine, new AST(Identifier, new ASTStr(identifier.val), identifier.lineNum));
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
            try{ return new AST(Int, new ASTInt(Integer.parseInt(numStr, isHex? 16 : 10)), current.lineNum); }
            catch (NumberFormatException a) {
                try{ return new AST(Long, new ASTLong(java.lang.Long.parseLong(numStr, isHex? 16 : 10)), current.lineNum); }
                catch (NumberFormatException b) {
                    if(isHex) errorHandler.error(Errors.PARSE_NUM_INVALID_HEX, current.lineNum, numStr);
                    try{ return new AST(Float, new ASTFloat(java.lang.Float.parseFloat(numStr)), current.lineNum); }
                    catch (NumberFormatException c) {
                        try{ return new AST(Double, new ASTDouble(java.lang.Double.parseDouble(numStr)), current.lineNum); }
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
                    return new AST(Long, new ASTLong(java.lang.Long.parseLong(numStr, isHex? 16 : 10)), current.lineNum);
                }

                case "s", "S" -> {
                    return new AST(Short, new ASTShort(java.lang.Short.parseShort(numStr, isHex? 16 : 10)), current.lineNum);
                }

                case "b", "B" -> {
                    short val = java.lang.Short.parseShort(numStr, isHex? 16 : 10);
                    if(val > 255) throw new NumberFormatException();
                    return new AST(Byte, new ASTByte((byte)val), current.lineNum);
                }

                case "d", "D" -> {
                    if(isHex) errorHandler.error(Errors.PARSE_NUM_INVALID_HEX, current.lineNum, numStr);
                    return new AST(Double, new ASTDouble(java.lang.Double.parseDouble(numStr)), current.lineNum);
                }

                case "f", "F" -> {
                    if(isHex) errorHandler.error(Errors.PARSE_NUM_INVALID_HEX, current.lineNum, numStr);
                    return new AST(Float, new ASTFloat(java.lang.Float.parseFloat(numStr)), current.lineNum);
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
                    AST varAST = parseVar();

                    Type[] tupleTypes = tup.type == Tuple? typify(tup.children) : typify(tup);

                    varAST.val = new ASTType(new TupleType(tupleTypes));
                    return varAST;
                }
                return tup;
            }

            case StringL -> {
                return new AST(Str, new ASTStr(cur.val), cur.lineNum);
            }

            case NumberL -> {
                return parseNum();
            }

            case LBrack -> {
                iterator.dec();
                int line = iterator.current().lineNum;
                AST[] elements = parseDelim(TokenType.LBrack, TokenType.RBrack, TokenType.Comma);
                if(elements.length == 1 && isType(elements[0])){
                    return new AST(Cast, new ASTType(typify(elements[0])[0]), line, parseExpr());
                }
            }

            case Identifier -> {
                if(iterator.current().tok == TokenType.Identifier && iterator.peek().tok != TokenType.LParen){
                    String type = cur.val;
                    AST varAST = parseVar();
                    varAST.val = new ASTType(new CustomType(type));
                    return varAST;
                }
                return new AST(Identifier, new ASTStr(cur.val), cur.lineNum);
            }

            case Var -> {
                return parseVar();
            }

            //Primitives
            case Boolean, Byte, Char, Short, Int, Long, Float, Double -> {
                TokenType type = cur.tok;

                if(iterator.current().tok == TokenType.Identifier && iterator.peek().tok != TokenType.LParen) {
                    AST varAST = parseVar();
                    varAST.val = ASTTokTypeValues.get(type);
                    return varAST;
                }

                return new AST(Type, ASTTokTypeValues.get(type), cur.lineNum);
            }

            case Void -> {
                return new AST(Type, ASTTokTypeValues.get(TokenType.Void), cur.lineNum);
            }

            case FnType -> {
                Type fnType = parseFnType();

                if(iterator.current().tok == TokenType.Identifier && iterator.peek().tok != TokenType.LParen) {
                    AST varAST = parseVar();
                    varAST.val = new ASTType(fnType);
                    return varAST;
                }

                return new AST(Type, new ASTType(fnType), cur.lineNum);
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
                return new AST(Bool, new ASTBool(true), cur.lineNum);
            }

            case False -> {
                return new AST(Bool, new ASTBool(false), cur.lineNum);
            }

            case Fn -> {
                return parseFn();
            }

            case Class -> {
                //return parseClass();
            }

            case Public, Private, Protected, Static -> {
                return new AST(Modifier, new ASTTok(cur.tok), cur.lineNum, parseExpr());
            }

            case Return -> {
                return new AST(Return, cur.lineNum, parseExpr());
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

    private Type[] typify(AST... asts){
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
    }

    private static boolean isType(AST ast){
        if(ast == null) return false;
        if(ast.type == Tuple) return Arrays.stream(ast.children).allMatch(DefaultParser::isType);
        if(ast.type == Identifier) return true;
        return ast.val.isType();
    }

    private boolean isBinOp(Token tok){
        return opPrecedence.containsKey(tok.tok);
    }

    private boolean isUnaryOp(Token tok){
        return unaryOps.contains(tok.tok);
    }

}