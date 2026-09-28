package com.example.edtech.infrai;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SimpleJsonParser {
    private final String text;
    private int index;

    private SimpleJsonParser(String text) {
        this.text = text;
    }

    public static Object parse(String text) {
        return new SimpleJsonParser(text).parseValue();
    }

    private Object parseValue() {
        skipWhitespace();
        char c = current();
        if (c == '{') {
            return parseObject();
        }
        if (c == '[') {
            return parseArray();
        }
        if (c == '"') {
            return parseString();
        }
        if (c == 't' || c == 'f') {
            return parseBoolean();
        }
        if (c == 'n') {
            parseNull();
            return null;
        }
        return parseNumber();
    }

    private Map<String, Object> parseObject() {
        expect('{');
        Map<String, Object> result = new LinkedHashMap<>();
        skipWhitespace();
        if (current() == '}') {
            index++;
            return result;
        }
        while (true) {
            String key = parseString();
            skipWhitespace();
            expect(':');
            Object value = parseValue();
            result.put(key, value);
            skipWhitespace();
            if (current() == '}') {
                index++;
                return result;
            }
            expect(',');
        }
    }

    private List<Object> parseArray() {
        expect('[');
        List<Object> result = new ArrayList<>();
        skipWhitespace();
        if (current() == ']') {
            index++;
            return result;
        }
        while (true) {
            result.add(parseValue());
            skipWhitespace();
            if (current() == ']') {
                index++;
                return result;
            }
            expect(',');
        }
    }

    private String parseString() {
        expect('"');
        StringBuilder builder = new StringBuilder();
        while (true) {
            char c = text.charAt(index++);
            if (c == '"') {
                return builder.toString();
            }
            if (c == '\\') {
                char escaped = text.charAt(index++);
                switch (escaped) {
                    case '"' -> builder.append('"');
                    case '\\' -> builder.append('\\');
                    case '/' -> builder.append('/');
                    case 'b' -> builder.append('\b');
                    case 'f' -> builder.append('\f');
                    case 'n' -> builder.append('\n');
                    case 'r' -> builder.append('\r');
                    case 't' -> builder.append('\t');
                    case 'u' -> {
                        String hex = text.substring(index, index + 4);
                        builder.append((char) Integer.parseInt(hex, 16));
                        index += 4;
                    }
                    default -> throw new IllegalArgumentException("Bad escape: " + escaped);
                }
            } else {
                builder.append(c);
            }
        }
    }

    private Boolean parseBoolean() {
        if (text.startsWith("true", index)) {
            index += 4;
            return true;
        }
        if (text.startsWith("false", index)) {
            index += 5;
            return false;
        }
        throw new IllegalArgumentException("Bad boolean at " + index);
    }

    private void parseNull() {
        if (!text.startsWith("null", index)) {
            throw new IllegalArgumentException("Bad null at " + index);
        }
        index += 4;
    }

    private Number parseNumber() {
        int start = index;
        while (index < text.length()) {
            char c = text.charAt(index);
            if ((c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') {
                index++;
            } else {
                break;
            }
        }
        String token = text.substring(start, index);
        if (token.contains(".") || token.contains("e") || token.contains("E")) {
            return Double.parseDouble(token);
        }
        return Long.parseLong(token);
    }

    private void skipWhitespace() {
        while (index < text.length()) {
            char c = text.charAt(index);
            if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                index++;
            } else {
                break;
            }
        }
    }

    private void expect(char expected) {
        skipWhitespace();
        if (current() != expected) {
            throw new IllegalArgumentException("Expected " + expected + " at " + index);
        }
        index++;
    }

    private char current() {
        if (index >= text.length()) {
            throw new IllegalArgumentException("Unexpected end of JSON");
        }
        return text.charAt(index);
    }
}
