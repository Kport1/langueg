package com.kport.langueg.lex;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

public class DefaultLexer implements Lexer{

    private static final String comment = "//";
    private static final char str = '"';
    private static final char strEsc = '\\';

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

                wasIdentifier = false;
                wasNumber = false;
                wasToken = false;

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
                outTokens.add(new Token(TokenType.String, collectStr(iterator)));
                continue;
            }
            /*if(c == '\n'){
                lineCounter++;
            }*/
            if(isValidToken(word)){
                wasToken = true;
            }
            else if(isValidNum(word)){
                wasNumber = true;
            }
            else if(isValidIdentifier(word)){
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

        wasIdentifier = false;
        wasNumber = false;
        wasToken = false;
        word = "";
        lineCounter++;

        return outTokens;
    }

    private void addTok(ArrayList<Token> outTokens, CharacterIterator iterator, boolean reqValid){
        if(wasToken){
            outTokens.add(new Token(tokens.get(word.substring(0, word.length() - 1)), lineCounter));
            iterator.previous();
            word = "";
            wasToken = false;
        }
        else if(wasNumber){
            outTokens.add(new Token(TokenType.Number, word.substring(0, word.length() - 1), lineCounter));
            iterator.previous();
            word = "";
            wasNumber = false;
        }
        else if(wasIdentifier){
            outTokens.add(new Token(TokenType.Identifier, word.substring(0, word.length() - 1), lineCounter));
            iterator.previous();
            word = "";
            wasIdentifier = false;
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
        for (char c = iterator.first(); c != CharacterIterator.DONE; c = iterator.next()) {
            if(!Character.isDigit(c)){
                if(c == '.' && !dot){
                    dot = true;
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    private static boolean isValidToken(String str){
        return tokens.containsKey(str);
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

        tokens.put("<", TokenType.Greater);
        tokens.put(">", TokenType.Less);
        tokens.put("<=", TokenType.GreaterEq);
        tokens.put(">=", TokenType.LessEq);
        tokens.put("==", TokenType.Eq);
        tokens.put("!=", TokenType.NotEq);
        tokens.put("&", TokenType.And);
        tokens.put("|", TokenType.Or);
        tokens.put("^", TokenType.XOR);

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
