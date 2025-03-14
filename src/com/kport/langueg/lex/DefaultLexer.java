package com.kport.langueg.lex;

import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultLexer implements Lexer {
    private static final HashMap<CharSequence, TokenType> tokens = new HashMap<>();
    private static int maxTokenLen = 1;

    static {
        tokens.put("=", TokenType.Assign);
        tokens.put("var", TokenType.Var);

        tokens.put("+", TokenType.Plus);
        tokens.put("-", TokenType.Minus);
        tokens.put("*", TokenType.Mul);
        tokens.put("/", TokenType.Div);
        tokens.put("%", TokenType.Mod);
        tokens.put("**", TokenType.Pow);

        tokens.put("<", TokenType.Greater);
        tokens.put(">", TokenType.Less);
        tokens.put("==", TokenType.Eq);
        tokens.put("!=", TokenType.NotEq);
        tokens.put("&&", TokenType.BitAnd);
        tokens.put("&", TokenType.And);
        tokens.put("||", TokenType.BitOr);
        tokens.put("|", TokenType.Or);
        tokens.put("^^", TokenType.BitXOr);
        tokens.put("^", TokenType.XOr);

        tokens.put("++", TokenType.Inc);
        tokens.put("--", TokenType.Dec);

        tokens.put("!", TokenType.Not);

        tokens.put("if", TokenType.If);
        tokens.put("else", TokenType.Else);
        tokens.put("return", TokenType.Return);
        tokens.put("break", TokenType.Break);
        tokens.put("continue", TokenType.Continue);
        tokens.put("match", TokenType.Match);
        tokens.put("case", TokenType.Case);
        tokens.put("while", TokenType.While);
        tokens.put("for", TokenType.For);

        tokens.put("fn", TokenType.Fn);
        tokens.put("->", TokenType.SingleArrow);

        tokens.put("mod", TokenType.Module);

        tokens.put("(", TokenType.LParen);
        tokens.put(")", TokenType.RParen);
        tokens.put("[", TokenType.LBrack);
        tokens.put("]", TokenType.RBrack);
        tokens.put("{", TokenType.LCurl);
        tokens.put("}", TokenType.RCurl);
        tokens.put(":", TokenType.Colon);
        tokens.put(";", TokenType.Semicolon);
        tokens.put(",", TokenType.Comma);
        tokens.put(".", TokenType.Dot);
        tokens.put("=>", TokenType.DoubleArrow);

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

        tokens.put("type", TokenType.TypeDef);
        tokens.put("as", TokenType.As);

        for (CharSequence cs : tokens.keySet()) maxTokenLen = Math.max(maxTokenLen, cs.length());
    }

    private static final Pattern integerPattern = Pattern.compile("[0-9]+");
    private static final Pattern lineComment = Pattern.compile("//[^\\n]*");
    private static final Pattern blockComment = Pattern.compile("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/");
    private static final Pattern identifier = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*");
    private static final Pattern string = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"");

    private static final List<LexemeMatcher> lexemes = new ArrayList<>();

    static {
        lexemes.add((begin, code) -> {
            Matcher m = lineComment.matcher(code).region(begin, code.length());
            if (m.lookingAt())
                return new Pair<>(new Token(TokenType.LineComment, code.subSequence(m.start(), m.end()).toString()), m.end() - begin);
            return null;
        });

        lexemes.add((begin, code) -> {
            Matcher m = blockComment.matcher(code).region(begin, code.length());
            if (m.lookingAt())
                return new Pair<>(new Token(TokenType.BlockComment, code.subSequence(m.start(), m.end()).toString()), m.end() - begin);
            return null;
        });

        lexemes.add((begin, code) -> {
            Matcher m = string.matcher(code).region(begin, code.length());
            if (m.lookingAt())
                return new Pair<>(new Token(TokenType.String, code.subSequence(m.start(), m.end()).toString()), m.end() - begin);
            return null;
        });

        lexemes.add((begin, code) -> {
            for (int i = Math.min(maxTokenLen - 1, code.length() - begin); i > 0; i--) {
                CharSequence cs = code.subSequence(begin, begin + i);
                if (tokens.containsKey(cs)) return new Pair<>(new Token(tokens.get(cs)), i);
            }
            return null;
        });

        lexemes.add((begin, code) -> {
            Matcher m = integerPattern.matcher(code).region(begin, code.length());
            if (m.lookingAt())
                return new Pair<>(new Token(TokenType.Number, code.subSequence(m.start(), m.end()).toString()), m.end() - begin);
            return null;
        });

        lexemes.add((begin, code) -> {
            Matcher m = identifier.matcher(code).region(begin, code.length());
            if (m.lookingAt())
                return new Pair<>(new Token(TokenType.Identifier, code.subSequence(m.start(), m.end()).toString()), m.end() - begin);
            return null;
        });
    }

    public ArrayList<Token> process(Object code_, LanguegPipeline<?, ?> pipeline) {
        CharSequence code = ((CharSequence) code_);

        ArrayList<Token> tokens = new ArrayList<>();

        int offset = 0;
        OUTER:
        while (offset < code.length()) {
            if (Character.isWhitespace(code.charAt(offset))) {
                offset++;
                continue;
            }
            for (LexemeMatcher lexeme : lexemes) {
                Pair<Token, Integer> token_len = lexeme.getNext(offset, code);
                if (token_len == null) continue;
                if (token_len.left.tok != TokenType.LineComment && token_len.left.tok != TokenType.BlockComment) {
                    token_len.left.offset = offset;
                    tokens.add(token_len.left);
                }
                offset += token_len.right;
                continue OUTER;
            }
            throw new Error("Lexical analysis failed at " + offset);
        }

        return tokens;
    }

    private interface LexemeMatcher {
        Pair<Token, Integer> getNext(int begin, CharSequence code);
    }
}
