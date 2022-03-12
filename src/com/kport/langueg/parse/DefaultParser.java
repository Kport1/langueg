package com.kport.langueg.parse;

import com.kport.langueg.lex.Token;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.*;
import com.kport.langueg.parse.ast.astVals.*;
import com.kport.langueg.parse.typeCheck.Type;
import com.kport.langueg.util.Iterator;
import static com.kport.langueg.parse.ast.ASTTypeE.*;

import java.util.*;

public class DefaultParser implements Parser{

    private Iterator<Token> iterator;
    private static final HashMap<TokenType, Integer> opPrecedence = new HashMap<>();
    static {
        opPrecedence.put(TokenType.Assign, 0);

        opPrecedence.put(TokenType.Greater, 1);
        opPrecedence.put(TokenType.Less, 1);
        opPrecedence.put(TokenType.GreaterEq, 1);
        opPrecedence.put(TokenType.LessEq, 1);
        opPrecedence.put(TokenType.Eq, 1);
        opPrecedence.put(TokenType.NotEq, 1);
        opPrecedence.put(TokenType.And, 3);
        opPrecedence.put(TokenType.Or, 2);
        opPrecedence.put(TokenType.XOR, 4);

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
    public AST parse(ArrayList<Token> tokens) {
        iterator = new Iterator<>(tokens);

        AST prog = parseBlock(true);
        prog.type = Prog;
        return prog;
    }

    private AST parseExpr(){
        return parseBinaryOp(parseUnaryOp(call(parseAtom())), -1);
    }

    private AST parseBinaryOp(AST left, int lastPrec) {
        Token cur = iterator.current();

        if(isBinOp(cur)){
            int currentPrec = opPrecedence.get(cur.tok);
            if(currentPrec > lastPrec){
                iterator.inc();
                AST right = parseBinaryOp(parseUnaryOp(call(parseAtom())), currentPrec);
                return parseBinaryOp(new AST(BinOp, new ASTTok(cur.tok), left, right), lastPrec);
            }
        }
        return left;
    }

    private AST parseUnaryOp(AST left) {
        if(isUnaryOp(iterator.current())) {
            TokenType op = iterator.current().tok;
            if (left == null) {
                iterator.inc();
                return new AST(UnaryOpBefore, new ASTTok(op), parseExpr());
            } else {
                iterator.inc();
                return new AST(UnaryOpAfter, new ASTTok(op), left);
            }
        }
        return left;
    }

    private AST call(AST left){
        if(iterator.current().tok == TokenType.LParen){
            AST tup = parseTuple();
            if(tup.type == Tuple && tup.children == null) {
                return call(new AST(Call, left));
            }
            if(tup.type != Tuple){
                return call(new AST(Call, left, tup));
            }
            AST[] callArgs = new AST[tup.children.length + 1];
            System.arraycopy(tup.children, 0, callArgs, 1, tup.children.length);
            callArgs[0] = left;

            return call(new AST(Call, callArgs));
        }
        return left;
    }

    private AST parseTuple(){
        AST[] exprs = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);
        if(exprs.length == 0) {
            return new AST(Tuple);
        }
        if(exprs.length == 1){
            return exprs[0];
        }
        return new AST(Tuple, exprs);
    }

    private AST parseIf(){
        int conditionLine = iterator.current().lineNum;
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

            return new AST(If, condition, block, elseBlock);
        }

        if(block.type == Block){
            exprIsBlock = true;
        }

        return new AST(If, condition, block);
    }

    private AST parseWhile(){
        AST condition = parseTuple();
        int conditionLine = iterator.current().lineNum;

        if(condition.type == Tuple){
            throw new Error("Condition of if statement cannot be a tuple. Line: " + conditionLine);
        }

        AST block = parseExpr();

        if(block.type == Block){
            exprIsBlock = true;
        }

        return new AST(While, condition, block);
    }

    private AST parseFor(){
        int initCondIncLine = iterator.current().lineNum;
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

        return new AST(For, asts);
    }

    private AST parseFn(){
        AST returnType = parseAtom();

        /*if(!(ASTTokTypeValues.containsKey(typeToken.tok) || typeToken.tok == TokenType.Void || typeToken.val != null)){
            throw new Error("Expected type token at line " + typeToken.lineNum + ". Got: " + typeToken);
        }*/

        //iterator.inc();
        String name = null;
        if(iterator.current().tok == TokenType.Identifier){
            name = iterator.current().val;
            iterator.inc();
        }

        AST[] args = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);

        for (AST arg : args) {
            if (arg.type != Var || arg.val == null) {
                throw new Error("Invalid function arguments at line " + iterator.current().lineNum);
            }
            arg.type = FnArg;
        }

        AST block = parseExpr();

        if(block.type == Block){
            exprIsBlock = true;
        }

        AST[] asts = new AST[args.length + (name == null? 1 : 2)];
        System.arraycopy(args, 0, asts, 0, args.length);
        asts[args.length] = block;
        if(name != null){
            asts[asts.length - 1] = new AST(Identifier, new ASTStr(name));
        }

        if(returnType.val.getTok() != TokenType.FnType) {
            return new AST(Fn, returnType.val, asts);
        }
        asts = Arrays.copyOf(asts, asts.length + returnType.children.length);
        System.arraycopy(returnType.children, 0, asts, asts.length - returnType.children.length, returnType.children.length);
        return new AST(Fn, ASTTokTypeValues.get(TokenType.FnType), asts);
    }

    private Type parseFnType(){
        if(iterator.current().tok != TokenType.LBrack){
            throw new Error("Expected LBrack at line " + iterator.current().lineNum);
        }

        AST[] fnArgASTs;
        if(iterator.peek().tok == TokenType.LParen){
            iterator.inc();
            fnArgASTs = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);
            if(fnArgASTs.length > 0) {
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
        }
        else{
            iterator.inc();
            fnArgASTs = new AST[]{parseAtom()};
            if(fnArgASTs[0].type != Type){
                if(fnArgASTs[0].type == Identifier){
                    fnArgASTs[0].type = Type;
                }
                else {
                    throw new Error("Invalid argument type " + fnArgASTs[0] + " for function type on line " + iterator.current().lineNum);
                }
            }
        }

        if(iterator.current().tok != TokenType.SingleArrow){
            throw new Error("Expected -> at line" + iterator.current().lineNum);
        }
        iterator.inc();

        AST returnAST = parseAtom();
        if(returnAST.type != Type){
            if(returnAST.type == Identifier){
                returnAST.type = Type;
            }
            else {
                throw new Error("Expected function return type after -> at line " + iterator.current().lineNum);
            }
        }

        if(iterator.current().tok != TokenType.RBrack){
            throw new Error("Expected RBrack at line " + iterator.current().lineNum);
        }
        iterator.inc();

        Type returnType = returnAST.val.isStr()? new Type(returnAST.val.getStr()) : returnAST.val.getType();

        if(fnArgASTs.length > 0){
            Type[] fnArgTypes = Arrays.stream(fnArgASTs).map((arg) -> {
                if (arg.val.isStr()) {
                    return new Type(arg.val.getStr());
                }
                return arg.val.getType();
            }).toArray(Type[]::new);
            return new Type(returnType, fnArgTypes);
        }

        return new Type(returnType);
    }

    private boolean exprIsBlock = false;
    private AST parseBlock(boolean isProg){
        ArrayList<AST> exprs = new ArrayList<>();

        if(iterator.current().tok == TokenType.RCurl){
            return new AST(Block);
        }

        do {
            TokenType curTok = iterator.current().tok;
            if (curTok == TokenType.LCurl) {
                exprIsBlock = true;
            }

            exprs.add(parseExpr());

            if (!exprIsBlock && iterator.current().tok != TokenType.Semicolon) {
                throw new Error("Expected semicolon at line " + iterator.current().lineNum + ". Got: " + iterator.current());
            }
            iterator.inc();
            if(iterator.current().tok == TokenType.RCurl && !iterator.isEOF() && !isProg){
                return new AST(Block, exprs.toArray(new AST[0]));
            }

            exprIsBlock = false;
        } while(!iterator.isEOF());

        if(isProg){
            return new AST(Block, exprs.toArray(new AST[0]));
        }
        throw new Error("Reached EOF before block was closed");
    }

    //TODO
    private AST parseClass(){
        Token name = iterator.current();
        iterator.inc();
        iterator.inc();
        AST block = parseBlock(false);

        ArrayList<AST> fields = new ArrayList<>();
        ArrayList<AST> methods = new ArrayList<>();
        ArrayList<AST> blocks = new ArrayList<>();

        for (AST expr : block.children) {
            if(isVarDecl(expr)){
                fields.add(expr);
            }
            if(expr.type == Fn){
                methods.add(expr);
            }
            if(expr.type == Block){
                blocks.add(expr);
            }
        }

        int size = fields.size() + methods.size() + blocks.size();
        AST[] asts = new AST[size];
        AST[] fieldsArr = fields.toArray(new AST[0]);
        AST[] methodsArr = methods.toArray(new AST[0]);
        AST[] blocksArr = blocks.toArray(new AST[0]);

        System.arraycopy(fieldsArr, 0, asts, 0, fields.size());
        System.arraycopy(methodsArr, 0, asts, fields.size(), methods.size());
        System.arraycopy(blocksArr, 0, asts, fields.size() + methods.size(), blocks.size());

        return new AST(Class, new ASTStr(name.val), asts);
    }

    private static final HashMap<TokenType, ASTValue> ASTTokTypeValues = new HashMap<>();
    static{
        ASTTokTypeValues.put(TokenType.Boolean, new ASTType(new Type(TokenType.Boolean)));
        ASTTokTypeValues.put(TokenType.Byte, new ASTType(new Type(TokenType.Byte)));
        ASTTokTypeValues.put(TokenType.Int, new ASTType(new Type(TokenType.Int)));
        ASTTokTypeValues.put(TokenType.Long, new ASTType(new Type(TokenType.Long)));
        ASTTokTypeValues.put(TokenType.Float, new ASTType(new Type(TokenType.Float)));
        ASTTokTypeValues.put(TokenType.Double, new ASTType(new Type(TokenType.Double)));
        ASTTokTypeValues.put(TokenType.Void, new ASTType(new Type(TokenType.Void)));
        ASTTokTypeValues.put(TokenType.FnType, new ASTType(new Type(TokenType.FnType)));
    }
    private AST parseVar(){
        Token identifier = iterator.current();
        if(identifier.tok != TokenType.Identifier){
            throw new Error("Expected an Identifier after var at line " + iterator.current().lineNum);
        }
        if(iterator.next().tok == TokenType.Assign){
            iterator.inc();
            return new AST(Var, new AST(Identifier, new ASTStr(identifier.val)), parseExpr());
        }

        return new AST(Var, new AST(Identifier, new ASTStr(identifier.val)));
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
                return parseTuple();
            }

            case StringL -> {
                return new AST(Str, new ASTStr(cur.val));
            }

            case NumberL -> {
                try{
                    return new AST(Int, new ASTInt(Integer.parseInt(cur.val)));
                }
                catch (NumberFormatException e){
                    try {
                        return new AST(Double, new ASTDub(java.lang.Double.parseDouble(cur.val)));
                    }
                    catch (NumberFormatException e1){
                        throw new Error("Invalid number " + cur.val + " on line " + cur.lineNum);
                    }
                }
            }

            case Identifier -> {
                if(iterator.current().tok == TokenType.Identifier && iterator.peek().tok != TokenType.LParen){
                    String type = cur.val;
                    AST varAST = parseVar();
                    varAST.val = new ASTType(new Type(type));
                    return varAST;
                }
                return new AST(Identifier, new ASTStr(cur.val));
            }

            case Var -> {
                return parseVar();
            }

            //Primitives
            case Boolean, Byte, Int, Long, Float, Double -> {
                TokenType type = cur.tok;

                if(iterator.current().tok == TokenType.Identifier && iterator.peek().tok != TokenType.LParen) {
                    AST varAST = parseVar();
                    varAST.val = ASTTokTypeValues.get(type);
                    return varAST;
                }

                return new AST(Type, ASTTokTypeValues.get(type));
            }

            case Void -> {
                return new AST(Type, ASTTokTypeValues.get(TokenType.Void));
            }

            case FnType -> {
                Type fnType = parseFnType();

                if(iterator.current().tok == TokenType.Identifier && iterator.peek().tok != TokenType.LParen) {
                    AST varAST = parseVar();
                    varAST.val = new ASTType(fnType);
                    return varAST;
                }

                return new AST(Type, new ASTType(fnType));
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
                return new AST(Bool, new ASTBool(true));
            }

            case False -> {
                return new AST(Bool, new ASTBool(false));
            }

            case Fn -> {
                return parseFn();
            }

            case Class -> {
                return parseClass();
            }

            case Public, Private, Protected, Static -> {
                return new AST(Modifier, new ASTTok(cur.tok), parseExpr());
            }

            case Return -> {
                return new AST(Return, parseExpr());
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

    private boolean isBinOp(Token tok){
        return opPrecedence.containsKey(tok.tok);
    }

    private boolean isUnaryOp(Token tok){
        return unaryOps.contains(tok.tok);
    }

    private boolean isVarDecl(AST ast){
        return ast.type == Var || (ast.children != null && ast.children.length == 2 && ast.children[0].type == Var);
    }

}