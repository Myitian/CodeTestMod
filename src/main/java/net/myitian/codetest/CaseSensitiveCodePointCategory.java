package net.myitian.codetest;

public enum CaseSensitiveCodePointCategory {
    UPPERCASE_LETTER,
    LOWERCASE_LETTER,
    OTHER_LETTER,
    MARK,
    NUMBER,
    SEPARATOR,
    OTHER,
    PUNCTUATION,
    SYMBOL;

    public static CaseSensitiveCodePointCategory get(int codePoint) {
        return switch (Character.getType(codePoint)) {
            case Character.UPPERCASE_LETTER -> UPPERCASE_LETTER;
            case Character.LOWERCASE_LETTER -> LOWERCASE_LETTER;
            case Character.TITLECASE_LETTER,
                 Character.MODIFIER_LETTER,
                 Character.OTHER_LETTER -> OTHER_LETTER;
            case Character.NON_SPACING_MARK,
                 Character.ENCLOSING_MARK,
                 Character.COMBINING_SPACING_MARK -> MARK;
            case Character.DECIMAL_DIGIT_NUMBER,
                 Character.LETTER_NUMBER,
                 Character.OTHER_NUMBER -> NUMBER;
            case Character.SPACE_SEPARATOR,
                 Character.LINE_SEPARATOR,
                 Character.PARAGRAPH_SEPARATOR -> SEPARATOR;
            case Character.DASH_PUNCTUATION,
                 Character.START_PUNCTUATION,
                 Character.END_PUNCTUATION,
                 Character.CONNECTOR_PUNCTUATION,
                 Character.OTHER_PUNCTUATION,
                 Character.INITIAL_QUOTE_PUNCTUATION,
                 Character.FINAL_QUOTE_PUNCTUATION -> PUNCTUATION;
            case Character.MATH_SYMBOL,
                 Character.CURRENCY_SYMBOL,
                 Character.MODIFIER_SYMBOL,
                 Character.OTHER_SYMBOL -> SYMBOL;
            default -> OTHER;
        };
    }
}