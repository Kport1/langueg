package com.kport.langueg.lex;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;

public class DefaultLexer implements Lexer{

    private static final String comment = "//";
    private static final char str = '"';
    private static final char strEsc = '\\';

    private static final char longLit = 'l';
    private static final char dubLit = 'd';
    private static final char floatLit = 'f';

    private boolean wasIdentifier = false;
    private boolean wasNumber = false;
    private boolean wasToken = false;

    private String word = "";
    private int lineCounter = 1;

    @Override
    public ArrayList<Token> lex(String code) {

        ArrayList<Token> outTokens = new ArrayList<>();

        CharacterIterator iterator = new StringCharacterIterator(code);

        for (char c = iterator.first(); c != CharacterIterator.DONE; c = iterator.next()) {

            if(Character.isWhitespace(c) && word.length() == 0){
                if(c == '\n'){
                    lineCounter++;
                }
                continue;
            }

            word += c;

            if(word.equals(comment)){
                skipToChar('\n', iterator);

                resetWas();

                word = "";
                lineCounter++;
                continue;
            }
            if(word.charAt(word.length() - 1) == str){
                if(word.length() == 1){
                    word = "";
                    iterator.previous();
                }
                addTok(outTokens, iterator, true);
                outTokens.add(new Token(TokenType.StringL, collectStr(iterator)));
                continue;
            }
            /*if(c == '\n'){
                lineCounter++;
            }*/
            if(isValidToken(word)){
                resetWas();
                wasToken = true;
            }
            else if(isValidNum(word)){
                resetWas();
                wasNumber = true;
            }
            else if(isValidIdentifier(word)){
                resetWas();
                wasIdentifier = true;
            }
            else{
                addTok(outTokens, iterator, false);
            }

            if(iterator.getIndex() == code.length() - 1){
                word += "a";
                addTok(outTokens, iterator, true);
                break;
            }
        }

        resetWas();
        word = "";
        lineCounter++;

        return outTokens;
    }

    private void addTok(ArrayList<Token> outTokens, CharacterIterator iterator, boolean reqValid){
        if(wasToken){
            outTokens.add(new Token(tokens.get(word.substring(0, word.length() - 1)), lineCounter));
            iterator.previous();
            word = "";
            resetWas();
        }
        else if(wasNumber){
            outTokens.add(new Token(TokenType.NumberL, word.substring(0, word.length() - 1), lineCounter));
            iterator.previous();
            word = "";
            resetWas();
        }
        else if(wasIdentifier){
            outTokens.add(new Token(TokenType.Identifier, word.substring(0, word.length() - 1), lineCounter));
            iterator.previous();
            word = "";
            resetWas();
        }
        else if(reqValid && word.length() != 0){
            throw new Error("Invalid Token: " + word.substring(0, word.length() - 1) + " on line " + lineCounter);
        }
    }

    private static boolean isValidIdentifier(String str) {
        CharacterIterator iterator = new StringCharacterIterator(str);
        if(!Character.isJavaIdentifierStart(iterator.first())){
            return false;
        }
        for (char c = iterator.first(); c != CharacterIterator.DONE; c = iterator.next()) {
            if(!Character.isJavaIdentifierPart(c)){
                return false;
            }
        }
        return true;
    }

    private static boolean isValidNum(String str){
        CharacterIterator iterator = new StringCharacterIterator(str);

        boolean dot = false;
        boolean litEnd = false;
        for (char c = iterator.first(); c != CharacterIterator.DONE; c = iterator.next()) {
            if(!Character.isDigit(c)){
                if(c == '.' && !dot){
                    dot = true;
                    continue;
                }
                else if(isNumberLiteralEnding(c) && iterator.next() == CharacterIterator.DONE && !litEnd && str.length() > 1){
                    litEnd = true;
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    private static boolean isNumberLiteralEnding(char c){
        return  c == longLit ||
                c == dubLit ||
                c == floatLit;
    }

    private static boolean isValidToken(String str){
        return tokens.containsKey(str);
    }

    private void resetWas(){
        wasToken = false;
        wasNumber = false;
        wasIdentifier = false;
    }

    private static final HashMap<String, TokenType> tokens = new HashMap<>();
    static {

        tokens.put("=", TokenType.Assign);
        tokens.put("var", TokenType.Var);

        tokens.put("+", TokenType.Plus);
        tokens.put("-", TokenType.Minus);
        tokens.put("*", TokenType.Mul);
        tokens.put("/", TokenType.Div);
        tokens.put("%", TokenType.Mod);
        tokens.put("**", TokenType.Pow);
        tokens.put(">>", TokenType.ShiftR);
        tokens.put("<<", TokenType.ShiftL);

        tokens.put("+=", TokenType.PlusAssign);
        tokens.put("-=", TokenType.MinusAssign);
        tokens.put("*=", TokenType.MulAssign);
        tokens.put("/=", TokenType.DivAssign);
        tokens.put("%=", TokenType.ModAssign);
        tokens.put("**=", TokenType.PowAssign);
        tokens.put(">>=", TokenType.ShiftRAssign);
        tokens.put("<<=", TokenType.ShiftLAssign);

        tokens.put("<", TokenType.Greater);
        tokens.put(">", TokenType.Less);
        tokens.put("<=", TokenType.GreaterEq);
        tokens.put(">=", TokenType.LessEq);
        tokens.put("==", TokenType.Eq);
        tokens.put("!=", TokenType.NotEq);
        tokens.put("&", TokenType.And);
        tokens.put("&&", TokenType.AndAnd);
        tokens.put("|", TokenType.Or);
        tokens.put("||", TokenType.OrOr);
        tokens.put("^", TokenType.XOr);

        tokens.put("++", TokenType.Inc);
        tokens.put("--", TokenType.Dec);

        tokens.put("!", TokenType.Not);

        tokens.put("if", TokenType.If);
        tokens.put("else", TokenType.Else);
        tokens.put("return", TokenType.Return);
        tokens.put("break", TokenType.Break);
        tokens.put("continue", TokenType.Continue);
        tokens.put("switch", TokenType.Switch);
        tokens.put("while", TokenType.While);
        tokens.put("for", TokenType.For);

        tokens.put("fn", TokenType.Fn);
        tokens.put("->", TokenType.SingleArrow);

        tokens.put("class", TokenType.Class);
        tokens.put("new", TokenType.New);
        tokens.put(".", TokenType.Dot);

        tokens.put("public", TokenType.Public);
        tokens.put("private", TokenType.Private);
        tokens.put("protected", TokenType.Protected);
        tokens.put("static", TokenType.Static);

        tokens.put("(", TokenType.LParen);
        tokens.put(")", TokenType.RParen);
        tokens.put("[", TokenType.LBrack);
        tokens.put("]", TokenType.RBrack);
        tokens.put("{", TokenType.LCurl);
        tokens.put("}", TokenType.RCurl);
        tokens.put(";", TokenType.Semicolon);
        tokens.put(",", TokenType.Comma);

        tokens.put("true", TokenType.True);
        tokens.put("false", TokenType.False);

        tokens.put("boolean", TokenType.Boolean);
        tokens.put("byte", TokenType.Byte);
        tokens.put("int", TokenType.Int);
        tokens.put("long", TokenType.Long);
        tokens.put("float", TokenType.Float);
        tokens.put("double", TokenType.Double);
        tokens.put("Fn", TokenType.FnType);
        tokens.put("void", TokenType.Void);
        tokens.put("null", TokenType.Null);
    }

    private static void skipToChar(char c, CharacterIterator iterator){
        //StringBuilder builder = new StringBuilder();
        while(iterator.next() != c && iterator.current() != CharacterIterator.DONE){
            //builder.append(iterator.current());
        }
        //return builder.toString();
    }

    private String collectStr(CharacterIterator iterator){
        StringBuilder builder = new StringBuilder();

        iterator.next();
        while(iterator.next() != str && iterator.current() != CharacterIterator.DONE){
            if(iterator.current() == strEsc){
                builder.append(switch (iterator.next()){
                    case '\\': yield '\\';
                    case 'n': yield '\n';
                    case 't': yield '\t';
                    case 'r': yield '\r';
                    case 'f': yield '\f';
                    case 'b': yield '\b';
                    default: throw new Error("Invalid escape character: \\" + iterator.current() + " on line " + lineCounter);
                });
            }
            else {
                builder.append(iterator.current());
            }
        }

        return builder.toString();
    }

    public int getLineCounter(){
        return lineCounter;
    }
}
