package net.myitian.codetest.text;

public enum IdentifierCharCategory {
    NUMBER,
    SYMBOL,
    LETTER;

    public static IdentifierCharCategory get(char c) {
        if (c >= 'a' && c <= 'z')
            return LETTER;
        if (c >= '0' && c <= '9')
            return NUMBER;
        return SYMBOL;
    }
}