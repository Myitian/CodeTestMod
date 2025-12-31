package net.myitian.codetest;

public enum CharCategory {
    NUMBER,
    SYMBOL,
    LETTER;

    public static CharCategory get(char c) {
        if (c >= 'a' && c <= 'z')
            return LETTER;
        if (c >= '0' && c <= '9')
            return NUMBER;
        return SYMBOL;
    }
}