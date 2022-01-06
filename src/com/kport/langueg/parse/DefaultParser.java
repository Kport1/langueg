package com.kport.langueg.parse;

import com.kport.langueg.lex.Token;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.*;
import com.kport.langueg.util.Iterator;

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
        prog.type = ASTType.Prog;
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
                return parseBinaryOp(new AST(ASTType.BinOp, new ASTOp(cur.tok), left, right), lastPrec);
            }
        }
        return left;
    }

    private AST parseUnaryOp(AST left) {
        if(isUnaryOp(iterator.current())) {
            TokenType op = iterator.current().tok;
            if (left == null) {
                iterator.inc();
                return new AST(ASTType.UnaryOpBefore, new ASTOp(op), parseExpr());
            } else {
                iterator.inc();
                return new AST(ASTType.UnaryOpAfter, new ASTOp(op), left);
            }
        }
        return left;
    }

    private AST call(AST left){
        if(iterator.current().tok == TokenType.LParen){
            AST tup = parseTuple();
            if(tup.type == ASTType.Tuple && tup.children == null) {
                return new AST(ASTType.Call, left);
            }
            if(tup.type != ASTType.Tuple){
                return new AST(ASTType.Call, left, tup);
            }
            AST[] callArgs = new AST[tup.children.length + 1];
            System.arraycopy(tup.children, 0, callArgs, 1, tup.children.length);
            callArgs[0] = left;

            return new AST(ASTType.Call, callArgs);
        }
        return left;
    }

    private AST parseTuple(){
        AST[] exprs = parseDelim(TokenType.LParen, TokenType.RParen, TokenType.Comma);
        if(exprs.length == 0) {
            return new AST(ASTType.Tuple);
        }
        if(exprs.length == 1){
            return exprs[0];
        }
        return new AST(ASTType.Tuple, exprs);
    }

    private AST parseIf(){
        int conditionLine = iterator.current().lineNum;
        AST condition = parseTuple();

        if(condition.type == ASTType.Tuple){
            throw new Error("Condition of if statement cannot be a tuple. Line: " + conditionLine);
        }

        AST block = parseExpr();

        if(iterator.peek().tok == TokenType.Else){
            iterator.inc();
            iterator.inc();
            AST elseBlock = parseExpr();

            if(elseBlock.type == ASTType.Block){
                exprIsBlock = true;
            }

            return new AST(ASTType.If, condition, block, elseBlock);
        }

        if(block.type == ASTType.Block){
            exprIsBlock = true;
        }

        return new AST(ASTType.If, condition, block);
    }

    private boolean exprIsBlock = false;
    private AST parseBlock(boolean isProg){
        ArrayList<AST> exprs = new ArrayList<>();

        if(iterator.current().tok == TokenType.RCurl){
            return new AST(ASTType.Block);
        }

        do {
            TokenType curTok = iterator.current().tok;
            if (curTok == TokenType.LCurl) {
                exprIsBlock = true;
            }

            System.out.println(iterator.current() + "  " + iterator.current().lineNum);
            exprs.add(parseExpr());
            System.out.println(iterator.current() + "  " + iterator.current().lineNum);

            if (!exprIsBlock && iterator.current().tok != TokenType.Semicolon) {
                throw new Error("Expected semicolon at line " + iterator.current().lineNum + ". Got: " + iterator.current());
            }
            iterator.inc();
            if(iterator.current().tok == TokenType.RCurl && !iterator.isEOF() && !isProg){
                return new AST(ASTType.Block, exprs.toArray(new AST[0]));
            }

            exprIsBlock = false;
        } while(!iterator.isEOF());

        if(isProg){
            return new AST(ASTType.Block, exprs.toArray(new AST[0]));
        }
        throw new Error("Reached EOF before block was closed");
    }

    private AST parseAtom(){

        Token cur = iterator.current();
        iterator.inc();

        switch (cur.tok){

            case LParen -> {
                iterator.dec();
                return parseTuple();
            }

            case String -> {
                return new AST(ASTType.Str, new ASTStr(cur.val));
            }

            case Number -> {
                try{
                    return new AST(ASTType.Int, new ASTInt(Integer.parseInt(cur.val)));
                }
                catch (NumberFormatException e){
                    try {
                        return new AST(ASTType.Dub, new ASTDub(Double.parseDouble(cur.val)));
                    }
                    catch (NumberFormatException e1){
                        throw new Error("Invalid number " + cur.val + " on line " + cur.lineNum);
                    }
                }
            }

            case Identifier -> {
                return new AST(ASTType.Identifier, new ASTStr(cur.val));
            }

            case Var -> {
                Token identifier = iterator.current();
                if(identifier.tok != TokenType.Identifier){
                    throw new Error("Expected an Identifier after var at line " + iterator.current().lineNum);
                }
                iterator.inc();

                return new AST(ASTType.Var, new ASTStr(identifier.val));
            }

            case If -> {
                return parseIf();
            }

            case LCurl -> {
                return parseBlock(false);
            }

        }

        throw new Error("Unexpected Token " + cur.tok.name() + " at line " + cur.lineNum);
    }

    private AST[] parseDelim(TokenType start, TokenType end, TokenType separator){
        if(iterator.current().tok != start) {
            throw new Error("Expected " + start.name() + " at line " + iterator.current().lineNum);
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

}
