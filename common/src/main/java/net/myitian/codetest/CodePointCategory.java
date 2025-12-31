package net.myitian.codetest;

public enum CodePointCategory {
    LETTER,
    MARK,
    NUMBER,
    SEPARATOR,
    OTHER,
    PUNCTUATION,
    SYMBOL;

    public static CodePointCategory get(int codePoint) {
        return switch (Character.getType(codePoint)) {
            case Character.UPPERCASE_LETTER,
                 Character.LOWERCASE_LETTER,
                 Character.TITLECASE_LETTER,
                 Character.MODIFIER_LETTER,
                 Character.OTHER_LETTER -> LETTER;
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