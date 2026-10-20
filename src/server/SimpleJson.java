package server;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Clean, lightweight, zero-dependency JSON serializer and parser for Java.
 * Eliminates external Maven / Gradle requirements and runs natively on standard Java.
 */
public class SimpleJson {

    // ==========================================
    // SERIALIZER
    // ==========================================

    public static String stringify(Object obj) {
        StringBuilder sb = new StringBuilder();
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        serializeValue(obj, sb, visited, 0);
        return sb.toString();
    }

    private static void serializeValue(Object obj, StringBuilder sb, Set<Object> visited, int depth) {
        if (obj == null || depth > 25) {
            sb.append("null");
            return;
        }

        if (obj instanceof String) {
            escapeString((String) obj, sb);
        } else if (obj instanceof Number || obj instanceof Boolean) {
            sb.append(obj.toString());
        } else if (obj instanceof Character) {
            escapeString(obj.toString(), sb);
        } else if (obj instanceof Map) {
            if (visited.contains(obj)) {
                sb.append("null");
                return;
            }
            visited.add(obj);
            Map<?, ?> map = (Map<?, ?>) obj;
            sb.append("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                escapeString(String.valueOf(entry.getKey()), sb);
                sb.append(":");
                serializeValue(entry.getValue(), sb, visited, depth + 1);
                first = false;
            }
            sb.append("}");
            visited.remove(obj);
        } else if (obj instanceof Iterable) {
            if (visited.contains(obj)) {
                sb.append("[]");
                return;
            }
            visited.add(obj);
            sb.append("[");
            boolean first = true;
            for (Object item : (Iterable<?>) obj) {
                if (!first) sb.append(",");
                serializeValue(item, sb, visited, depth + 1);
                first = false;
            }
            sb.append("]");
            visited.remove(obj);
        } else if (obj.getClass().isArray()) {
            sb.append("[");
            int len = java.lang.reflect.Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                if (i > 0) sb.append(",");
                serializeValue(java.lang.reflect.Array.get(obj, i), sb, visited, depth + 1);
            }
            sb.append("]");
        } else if (obj.getClass().getName().startsWith("java.")) {
            sb.append("\"").append(obj.toString()).append("\"");
        } else {
            // Serialize JavaBean / POJO via getters
            if (visited.contains(obj)) {
                sb.append("null");
                return;
            }
            visited.add(obj);
            serializeObjectGetters(obj, sb, visited, depth + 1);
            visited.remove(obj);
        }
    }

    private static void serializeObjectGetters(Object obj, StringBuilder sb, Set<Object> visited, int depth) {
        sb.append("{");
        boolean first = true;
        Method[] methods = obj.getClass().getMethods();
        for (Method m : methods) {
            if (m.getParameterCount() == 0 && !Modifier.isStatic(m.getModifiers())) {
                String name = m.getName();
                String propName = null;
                if (name.startsWith("get") && name.length() > 3 && !name.equals("getClass") && !name.equals("getDeclaringClass")) {
                    propName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                } else if (name.startsWith("is") && name.length() > 2) {
                    propName = Character.toLowerCase(name.charAt(2)) + name.substring(3);
                }

                if (propName != null) {
                    try {
                        Object val = m.invoke(obj);
                        if (!first) sb.append(",");
                        escapeString(propName, sb);
                        sb.append(":");
                        serializeValue(val, sb, visited, depth);
                        first = false;
                    } catch (Exception ignored) {}
                }
            }
        }
        sb.append("}");
    }

    private static void escapeString(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        String hex = Integer.toHexString(c);
                        sb.append("\\u");
                        for (int k = 0; k < 4 - hex.length(); k++) sb.append('0');
                        sb.append(hex);
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        sb.append('"');
    }

    // ==========================================
    // PARSER
    // ==========================================

    public static Object parse(String json) {
        if (json == null) return null;
        JsonTokenizer tokenizer = new JsonTokenizer(json.trim());
        return parseValue(tokenizer);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object result = parse(json);
        if (result instanceof Map) {
            return (Map<String, Object>) result;
        }
        return Collections.emptyMap();
    }

    private static Object parseValue(JsonTokenizer tokenizer) {
        tokenizer.skipWhitespace();
        if (tokenizer.isEof()) return null;

        char c = tokenizer.peek();
        if (c == '{') {
            return parseObject(tokenizer);
        } else if (c == '[') {
            return parseArray(tokenizer);
        } else if (c == '"') {
            return tokenizer.readString();
        } else if (c == 't' || c == 'f') {
            return tokenizer.readBoolean();
        } else if (c == 'n') {
            tokenizer.readNull();
            return null;
        } else if (c == '-' || Character.isDigit(c)) {
            return tokenizer.readNumber();
        } else {
            throw new RuntimeException("Unexpected character at " + tokenizer.getIndex() + ": " + c);
        }
    }

    private static Map<String, Object> parseObject(JsonTokenizer tokenizer) {
        Map<String, Object> map = new LinkedHashMap<>();
        tokenizer.consume('{');
        tokenizer.skipWhitespace();

        if (tokenizer.peek() == '}') {
            tokenizer.consume('}');
            return map;
        }

        while (true) {
            tokenizer.skipWhitespace();
            String key = tokenizer.readString();
            tokenizer.skipWhitespace();
            tokenizer.consume(':');
            Object value = parseValue(tokenizer);
            map.put(key, value);

            tokenizer.skipWhitespace();
            char c = tokenizer.peek();
            if (c == '}') {
                tokenizer.consume('}');
                break;
            } else if (c == ',') {
                tokenizer.consume(',');
            } else {
                throw new RuntimeException("Expected ',' or '}' at " + tokenizer.getIndex());
            }
        }

        return map;
    }

    private static List<Object> parseArray(JsonTokenizer tokenizer) {
        List<Object> list = new ArrayList<>();
        tokenizer.consume('[');
        tokenizer.skipWhitespace();

        if (tokenizer.peek() == ']') {
            tokenizer.consume(']');
            return list;
        }

        while (true) {
            Object value = parseValue(tokenizer);
            list.add(value);

            tokenizer.skipWhitespace();
            char c = tokenizer.peek();
            if (c == ']') {
                tokenizer.consume(']');
                break;
            } else if (c == ',') {
                tokenizer.consume(',');
            } else {
                throw new RuntimeException("Expected ',' or ']' at " + tokenizer.getIndex());
            }
        }

        return list;
    }

    private static class JsonTokenizer {
        private final String input;
        private int index;

        public JsonTokenizer(String input) {
            this.input = input;
            this.index = 0;
        }

        public int getIndex() { return index; }
        public boolean isEof() { return index >= input.length(); }

        public char peek() {
            if (isEof()) return '\0';
            return input.charAt(index);
        }

        public void consume(char expected) {
            if (isEof() || input.charAt(index) != expected) {
                throw new RuntimeException("Expected '" + expected + "' at index " + index);
            }
            index++;
        }

        public void skipWhitespace() {
            while (!isEof() && Character.isWhitespace(input.charAt(index))) {
                index++;
            }
        }

        public String readString() {
            consume('"');
            StringBuilder sb = new StringBuilder();
            while (!isEof()) {
                char c = input.charAt(index++);
                if (c == '"') {
                    return sb.toString();
                } else if (c == '\\') {
                    if (isEof()) throw new RuntimeException("Unterminated escape sequence");
                    char esc = input.charAt(index++);
                    switch (esc) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            String hex = input.substring(index, index + 4);
                            index += 4;
                            sb.append((char) Integer.parseInt(hex, 16));
                            break;
                        default: sb.append(esc); break;
                    }
                } else {
                    sb.append(c);
                }
            }
            throw new RuntimeException("Unterminated string literal");
        }

        public Number readNumber() {
            int start = index;
            if (peek() == '-') index++;
            while (!isEof() && Character.isDigit(peek())) index++;
            boolean isFloating = false;
            if (!isEof() && peek() == '.') {
                isFloating = true;
                index++;
                while (!isEof() && Character.isDigit(peek())) index++;
            }
            if (!isEof() && (peek() == 'e' || peek() == 'E')) {
                isFloating = true;
                index++;
                if (!isEof() && (peek() == '+' || peek() == '-')) index++;
                while (!isEof() && Character.isDigit(peek())) index++;
            }
            String numStr = input.substring(start, index);
            if (isFloating) {
                return Double.parseDouble(numStr);
            } else {
                long val = Long.parseLong(numStr);
                if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                    return (int) val;
                }
                return val;
            }
        }

        public Boolean readBoolean() {
            if (input.startsWith("true", index)) {
                index += 4;
                return true;
            } else if (input.startsWith("false", index)) {
                index += 5;
                return false;
            }
            throw new RuntimeException("Expected boolean at index " + index);
        }

        public void readNull() {
            if (input.startsWith("null", index)) {
                index += 4;
                return;
            }
            throw new RuntimeException("Expected null at index " + index);
        }
    }
}
