package com.kport.langueg.parse;

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

    @Override
    public AST process(Object tokens_, LanguegPipeline<?, ?> pipeline) {
        ArrayList<Token> tokens = (ArrayList<Token>) tokens_;
        iterator = new Iterator<>(tokens);

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
            long line = current.lineNum;
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
        long line = iterator.current().lineNum;
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
        long conditionLine = iterator.current().lineNum;
        AST condition = parseTuple();

        if(condition.type == Tuple){
            if(condition.children == null || condition.children.length < 1){
                throw new Error("Condition of if statement cannot be empty. Line: " + conditionLine);
            }
            throw new Error("Condition of if statement cannot be a tuple. Line: " + conditionLine);
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
        long conditionLine = iterator.current().lineNum;

        if(condition.type == Tuple){
            throw new Error("Condition of if statement cannot be a tuple. Line: " + conditionLine);
        }

        AST block = parseExpr();

        if(block.type == Block){
            exprIsBlock = true;
        }

        return new AST(While, conditionLine, condition, block);
    }

    private AST parseFor(){
        long initCondIncLine = iterator.current().lineNum;
        AST[] initCondInc = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Semicolon);

        if(initCondInc.length != 3){
            throw new Error("For cannot contain more or less than 3 expressions. Line: " + initCondIncLine);
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
        long fnLine = iterator.current().lineNum;
        AST returnAST = parseAtom();
        ASTType returnType = new ASTType(typify(returnAST)[0]);

        String name = null;
        long nameLine = -1;
        if(iterator.current().tok == TokenType.Identifier){
            name = iterator.current().val;
            nameLine = iterator.current().lineNum;
            iterator.inc();
        }

        AST[] args = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);

        //verify, that args follow: type argName
        for (AST arg : args) {
            if (arg.type != Var || arg.val == null) {
                throw new Error("Invalid function arguments at line " + iterator.current().lineNum);
            }
            arg.type = FnArg;
        }

        long blockLine = iterator.current().lineNum;
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
            throw new Error("Expected LBrack at line " + iterator.current().lineNum);


        AST[] params;
        if(iterator.next().tok == TokenType.LParen){
            params = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);
        }
        else{
            params = new AST[]{parseAtom()};
        }

        Type[] paramTypes = typify(params);

        if(iterator.current().tok != TokenType.SingleArrow)
            throw new Error("Expected SingleArrow");
        iterator.inc();

        Type returnType = typify(parseAtom())[0];

        if(iterator.current().tok != TokenType.RBrack)
            throw new Error("Expected RBrack at line " + iterator.current().lineNum);
        iterator.inc();


        return new FnType(returnType, paramTypes);


        /*do {
            AST[] fnArgASTs;
            if (iterator.peek().tok == TokenType.LParen) {
                iterator.inc();
                fnArgASTs = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);
                if (fnArgASTs.length > 0) {
                    for (AST argType : fnArgASTs) {
                        if (argType.type != Type) {
                            if (argType.type == Identifier) {
                                argType.type = Type;
                            } else {
                                throw new Error("Invalid argument type " + argType + " for function type on line " + iterator.current().lineNum);
                            }
                        }
                    }
                }
            } else {
                iterator.inc();
                fnArgASTs = new AST[]{parseAtom()};
                if (fnArgASTs[0].type != Type) {
                    if (fnArgASTs[0].type == Identifier) {
                        fnArgASTs[0].type = Type;
                    } else {
                        throw new Error("Invalid argument type " + fnArgASTs[0] + " for function type on line " + iterator.current().lineNum);
                    }
                }
            }

            if (iterator.current().tok != TokenType.SingleArrow) {
                throw new Error("Expected -> at line" + iterator.current().lineNum);
            }
            iterator.inc();

            AST returnAST = parseAtom();
            if (returnAST.type != Type) {
                if (returnAST.type == Identifier) {
                    returnAST.type = Type;
                } else {
                    throw new Error("Expected function return type after -> at line " + iterator.current().lineNum);
                }
            }

            Type returnType = typify(returnAST)[0];

            if(fnArgASTs.length > 0){
                Type[] fnArgTypes = typify(fnArgASTs);
                overloadedFns.add(new Type(returnType, fnArgTypes));
            }
            else {
                overloadedFns.add(returnType);
            }

        } while(iterator.current().tok == TokenType.Comma);

        if(iterator.current().tok != TokenType.RBrack){
            throw new Error("Expected RBrack at line " + iterator.current().lineNum);
        }
        iterator.inc();

        if(overloadedFns.size() == 1){
            return overloadedFns.get(0);
        }

        return new OverloadedFnType(overloadedFns.toArray(new Type[0]));*/
    }

    private boolean exprIsBlock = false;
    private AST parseBlock(boolean isProg){
        ArrayList<AST> exprs = new ArrayList<>();

        long blockLine = iterator.current().lineNum;
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

            if (!exprIsBlock && iterator.current().tok != TokenType.Semicolon) {
                throw new Error("Expected semicolon at line " + iterator.current().lineNum + ". Got: " + iterator.current());
            }
            iterator.inc();
            if(iterator.current().tok == TokenType.RCurl && !iterator.isEOF() && !isProg){
                exprIsBlock = true;
                return new AST(Block, blockLine, exprs.toArray(new AST[0]));
            }

        } while(!iterator.isEOF());

        if(isProg){
            return new AST(Block, blockLine, exprs.toArray(new AST[0]));
        }
        throw new Error("Reached EOF before block was closed");
    }

    private static final HashMap<TokenType, ASTValue> ASTTokTypeValues = new HashMap<>();
    static{
        ASTTokTypeValues.put(TokenType.Boolean, new ASTType(new PrimitiveType(TokenType.Boolean)));
        ASTTokTypeValues.put(TokenType.Byte, new ASTType(new PrimitiveType(TokenType.Byte)));
        ASTTokTypeValues.put(TokenType.Int, new ASTType(new PrimitiveType(TokenType.Int)));
        ASTTokTypeValues.put(TokenType.Long, new ASTType(new PrimitiveType(TokenType.Long)));
        ASTTokTypeValues.put(TokenType.Float, new ASTType(new PrimitiveType(TokenType.Float)));
        ASTTokTypeValues.put(TokenType.Double, new ASTType(new PrimitiveType(TokenType.Double)));
        ASTTokTypeValues.put(TokenType.Void, new ASTType(new PrimitiveType(TokenType.Void)));
        ASTTokTypeValues.put(TokenType.FnType, new ASTType(new PrimitiveType(TokenType.FnType)));
    }
    private AST parseVar(){
        long varLine = iterator.previous().lineNum;
        iterator.inc();
        Token identifier = iterator.current();
        if(identifier.tok != TokenType.Identifier){
            throw new Error("Expected an Identifier after var at line " + iterator.current().lineNum);
        }
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
                    if(isHex) throw new Error(numStr + " cannot be parsed as hexadecimal value");
                    try{ return new AST(Float, new ASTFloat(java.lang.Float.parseFloat(numStr)), current.lineNum); }
                    catch (NumberFormatException c) {
                        try{ return new AST(Double, new ASTDouble(java.lang.Double.parseDouble(numStr)), current.lineNum); }
                        catch (NumberFormatException d) {
                            throw new Error(numStr + " is not a valid number");
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
                    if(isHex) throw new Error(numStr + " cannot be parsed as hexadecimal value");
                    return new AST(Double, new ASTDouble(java.lang.Double.parseDouble(numStr)), current.lineNum);
                }

                case "f", "F" -> {
                    if(isHex) throw new Error(numStr + " cannot be parsed as hexadecimal value");
                    return new AST(Float, new ASTFloat(java.lang.Float.parseFloat(numStr)), current.lineNum);
                }

            }
        } catch (NumberFormatException ignored){}

        throw new Error(numStr + " is not a valid number");
    }

    private AST parseAtom(){

        if(iterator.isEOF()){
            throw new Error("Reached EOF while parsing atom " + iterator.current().tok.name());
        }

        Token cur = iterator.current();
        iterator.inc();

        switch (cur.tok){

            case LParen -> {
                iterator.dec();
                AST tup = parseTuple();

                if (iterator.current().tok == TokenType.Identifier && iterator.peek().tok != TokenType.LParen) {
                    if(tup.type != Tuple || tup.children.length == 0){
                        throw new Error("Invalid tuple type for var at line " + iterator.current().lineNum);
                    }

                    AST varAST = parseVar();

                    Type[] tupleTypes = typify(tup.children);

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
            case Boolean, Byte, Short, Int, Long, Float, Double -> {
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

        throw new Error("Unexpected Token " + cur.tok.name() + " at line " + cur.lineNum);
    }

    private AST[] parseDelim(TokenType start, TokenType end, TokenType separator){
        if(iterator.current().tok != start) {
            throw new Error("Expected " + start.name() + " at line " + iterator.current().lineNum + " got " + iterator.current());
        }
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

            if(iterator.current().tok != separator){
                throw new Error("Expected " + separator.name() + " at line " + iterator.current().lineNum);
            }

            iterator.inc();
        }

        iterator.inc();

        return exprs.toArray(new AST[0]);
    }

    private Type[] typify(AST... asts){
        return
                Arrays.stream(asts).map((type) -> {
                    if(type.type == Tuple){
                        return new TupleType(typify(type.children));
                    }

                    if (type.type == Identifier) {
                        return new CustomType(type.val.getStr());
                    }

                    if(type.val == null){
                        throw new Error("Invalid type: " + type);
                    }

                    if(type.val.isType()) {
                        return type.val.getType();
                    }

                    throw new Error("Invalid type: " + type);
                }).toArray(Type[]::new);
    }

    private boolean isBinOp(Token tok){
        return opPrecedence.containsKey(tok.tok);
    }

    private boolean isUnaryOp(Token tok){
        return unaryOps.contains(tok.tok);
    }

}