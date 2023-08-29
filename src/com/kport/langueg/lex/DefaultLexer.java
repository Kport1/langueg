package com.kport.langueg.lex;

import com.kport.langueg.pipeline.LanguegPipeline;

import java.util.*;

public class DefaultLexer implements Lexer{

    private static final char[] comment = "//".toCharArray();
    private static final char[] blockCommentStart = "/*".toCharArray();
    private static final char[] blockCommentEnd = "*/".toCharArray();
    private static final char str = '"';
    private static final char chr = '\'';
    private static final char strEsc = '\\';

    private static final String numLiteralSuffixes = "lLsSbBdDfF";
    private static final String numLiteralBases = "bxBX";

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
        tokens.put("&=", TokenType.AndAssign);
        tokens.put("|=", TokenType.OrAssign);
        tokens.put("^=", TokenType.XOrAssign);

        tokens.put("<", TokenType.Greater);
        tokens.put(">", TokenType.Less);
        tokens.put("<=", TokenType.GreaterEq);
        tokens.put(">=", TokenType.LessEq);
        tokens.put("==", TokenType.Eq);
        tokens.put("!=", TokenType.NotEq);
        tokens.put("&&", TokenType.BAnd);
        tokens.put("&", TokenType.And);
        tokens.put("||", TokenType.BOr);
        tokens.put("|", TokenType.Or);
        tokens.put("^^", TokenType.BXOr);
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

        tokens.put("bool", TokenType.Bool);
        tokens.put("char", TokenType.Char);
        tokens.put("u8", TokenType.U8);
        tokens.put("u16", TokenType.U16);
        tokens.put("u32", TokenType.U32);
        tokens.put("u64", TokenType.U64);
        tokens.put("i8", TokenType.I8);
        tokens.put("i16", TokenType.I16);
        tokens.put("i32", TokenType.I32);
        tokens.put("i64", TokenType.I64);
        tokens.put("f32", TokenType.F32);
        tokens.put("f64", TokenType.F64);
        tokens.put("void", TokenType.Void);

        tokens.put("null", TokenType.Null);
    }
    private static final List<LexemeMatcher> lexemes = new ArrayList<>();
    static {
        lexemes.add(new LexemeMatcher() {
            @Override
            public boolean isLexeme(char[] s) {
                return tokens.containsKey(new String(s));
            }

            @Override
            public Token getToken(char[] s, int line, int column) {
                return new Token(tokens.get(new String(s)), line, column);
            }
        });

        lexemes.add(new LexemeMatcher() {
            @Override
            public boolean isLexeme(char[] s) {
                return isIdentifier(s);
            }

            @Override
            public Token getToken(char[] s, int line, int column) {
                return new Token(TokenType.Identifier, new String(s), line, column);
            }
        });

        lexemes.add(new LexemeMatcher() {
            @Override
            public boolean isLexeme(char[] s) {
                return isNumber(s);
            }

            @Override
            public Token getToken(char[] s, int line, int column) {
                return new Token(TokenType.NumberL, new String(s), line, column);
            }
        });
    }

    public ArrayList<Token> process(Object code_, LanguegPipeline<?, ?> pipeline) {
        char[] code = ((String) code_).toCharArray();
        if(code.length == 0){
            throw new Error("""
                    
                    Bruh write some code you lazy piece of shit. I'm not gonna do your work for you. Like seriously?
                    Do you think I have time to write your code? I'm busy type checking and analyzing the complete and utter bullshit you throw at me constantly.
                    I wouldn't have to spend so much time on that if you pea brained amoeba could fucking remember what types your variables are supposed to have.
                    Go try and write some assembly, maybe you'll realize, that my only job is to catch your dumbass mistakes - and god do your two braincells produce a fuckton of those.
                    """);
        }

        ArrayList<Token> tokens = new ArrayList<>();

        int line = 1, prevLine;
        int column = 0, prevColumn;

        char[] word = new char[0];
        char[] prevWord;

        OUTER:
        for(int i = 0; i < code.length; i++, column++){
            prevLine = line;
            prevColumn = column;
            if(code[i] == '\r') i++;
            if(code[i] == '\n'){
                line++;
                column = -1;
            }
            if(Character.isWhitespace(code[i]) && word.length == 0)
                continue;

            if(Arrays.equals(Arrays.copyOfRange(code, i, i + comment.length), comment) && word.length == 0){
                while(i + 1 < code.length && code[i + 1] != '\n') i++;
                continue;
            }

            if(Arrays.equals(Arrays.copyOfRange(code, i, i + blockCommentStart.length), blockCommentStart) && word.length == 0){
                for(; i <= code.length - blockCommentEnd.length; i++, column++){
                    if(Arrays.equals(Arrays.copyOfRange(code, i, i + blockCommentEnd.length), blockCommentEnd)){
                        i++;
                        column++;
                        continue OUTER;
                    }
                    if(code[i] == '\r') i++;
                    if(code[i] == '\n'){
                        line++;
                        column = -1;
                    }
                }
                throw new Error("Block comment not closed");
            }

            if(code[i] == str){
                String str = collectString(code, i);
                i += str.length() + 1;
                tokens.add(new Token(TokenType.StringL, str, prevLine, prevColumn));
                continue;
            }

            //append current char to word and remember previous
            prevWord = word;
            word = Arrays.copyOf(word, word.length + 1);
            word[word.length - 1] = code[i];

            //Check if current word is valid
            boolean wordCurrentlyValid = false;
            for (LexemeMatcher lexemeMatcher : lexemes) {
                wordCurrentlyValid |= lexemeMatcher.isLexeme(word);
            }

            //Try adding previous word, if current one isn't valid
            if(!wordCurrentlyValid){
                for (LexemeMatcher lexemeMatcher : lexemes) {
                    if (lexemeMatcher.isLexeme(prevWord)) {
                        Token tok = lexemeMatcher.getToken(prevWord, prevLine, prevColumn - prevWord.length);
                        tokens.add(tok);
                        word = new char[0];
                        i--;
                        line = prevLine;
                        column--;
                        continue OUTER;
                    }
                }

                throw new Error("Invalid token: " + new String(word));
            }

            //Try adding word, if EOF has been reached
            if(i == code.length - 1){
                for (LexemeMatcher lexemeMatcher : lexemes) {
                    if (lexemeMatcher.isLexeme(word)) {
                        Token tok = lexemeMatcher.getToken(word, prevLine, prevColumn - word.length + 1);
                        tokens.add(tok);
                        continue OUTER;
                    }
                }

                throw new Error("Invalid token: " + new String(word));
            }
        }
        return tokens;
    }

    private static String collectString(char[] chars, int off){
        StringBuilder s = new StringBuilder();
        for(int i = off + 1; i < chars.length; i++){
            if(chars[i] == str) break;
            if(i == chars.length - 1) throw new Error("String doesn't close");
            s.append(chars[i]);
        }
        return s.toString();
    }

    private static boolean isNumber(char[] chars){
        if(chars.length == 0) return false;
        int startI = 0;
        if(chars.length >= 2 && chars[0] == '0'){
            if(numLiteralBases.indexOf(chars[1]) != -1) startI = 2;
        }

        boolean dot = false;
        for(int i = startI; i < chars.length; i++){
            char c = chars[i];
            if(c == '.'){
                if(dot) return false;
                dot = true;
                continue;
            }

            if( '0' <= c && c <= '9' ||
                'A' <= c && c <= 'F' ||
                'a' <= c && c <= 'f' )
                continue;

            if(i == chars.length - 1 && chars.length > 1){
                return numLiteralSuffixes.indexOf(c) != -1;
            }
            return false;
        }
        return true;
    }

    private static boolean isIdentifier(char[] chars){
        if(chars.length == 0) return false;
        int startCharT = Character.getType(chars[0]);
        if( startCharT != Character.UPPERCASE_LETTER &&
            startCharT != Character.LOWERCASE_LETTER &&
            startCharT != Character.CONNECTOR_PUNCTUATION)
            return false;

        for(int i = 1; i < chars.length; i++){
            int charT = Character.getType(chars[i]);
            if( charT != Character.UPPERCASE_LETTER &&
                charT != Character.LOWERCASE_LETTER &&
                charT != Character.CONNECTOR_PUNCTUATION &&
                charT != Character.DECIMAL_DIGIT_NUMBER)
                return false;
        }
        return true;
    }

    private interface LexemeMatcher{
        boolean isLexeme(char[] s);
        Token getToken(char[] s, int line, int column);
    }
}
