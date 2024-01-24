package brunocamarggo.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static brunocamarggo.lox.TokenType.*;
import static java.lang.Character.isAlphabetic;
import static java.lang.Character.isDigit;

public class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line =  1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("break", BREAK);
        keywords.put("class",  CLASS);
        keywords.put("continue", CONTINUE);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    public Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case ';' -> addToken(SEMICOLON);
            case '*' -> addToken(STAR);
            case '!' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '/' -> {
                if(match('/')) {
                    scanSingleLineComment();
                } else if (match(('*'))) {
                    ScanBlockComment();
                } else {
                    addToken(SLASH);
                }
            }
            case ' ', '\r', '\t' -> { }
            case '\n' -> line++;
            case '"' -> string();
            default -> {
                if(isDigit(c)) {
                    number();
                } else if (isAlphabetic(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character: " + c);
                }

            }
        }
    }

    private void scanSingleLineComment() {
        while (peek() != '\n' && !isAtEnd()) advance();
    }

    private void ScanBlockComment() {
        while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            advance();
            if(isAtEnd()) {
                Lox.error(line, "Unterminated block comment");
                return;
            }
        }
        advance();
        advance();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        var text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected) {
        if(isAtEnd()) return false;
        if(source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') line++;
            advance();
        }
        if(isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }
        advance();
        var value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void number() {
        while (isDigit(peek())) advance();
        if(peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private char peekNext() {
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private char peekPrevious() {
        if(current - 1 < 0) return '\0';
        return source.charAt(current - 1);
    }

    private void identifier() {
        while (isAlphabetic(peek())) advance();
        var text = source.substring(start, current);
        var type = keywords.get(text);
        if(type == null) type = IDENTIFIER;
        addToken(type);
    }
}
