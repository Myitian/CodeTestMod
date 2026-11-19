package net.myitian.codetest.text;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jetbrains.annotations.Nullable;

public class TextUtil {
    public static int getNextBoundary(String string, int position, boolean skipOverSpaces) {
        int len = string.length();
        if (position < 0 || len == 0) return 0;
        if (position >= len) return len;
        int cp = string.codePointAt(position);
        CaseInsensitiveCodePointCategory previous = CaseInsensitiveCodePointCategory.get(cp);
        int i = position + (cp > 0xFFFF ? 2 : 1);
        while (i < len) {
            cp = string.codePointAt(i);
            CaseInsensitiveCodePointCategory current = CaseInsensitiveCodePointCategory.get(cp);
            if (current != previous) {
                if (!skipOverSpaces) return i;
                int sp = indexOfExcept(string, ' ', i);
                return sp == -1 ? len : sp;
            }
            i += cp > 0xFFFF ? 2 : 1;
        }
        return len;
    }

    public static int getNextBoundaryInIdentifier(String string, int start) {
        int len = string.length();
        if (len <= start || start < 0) return -1;
        IdentifierCharCategory previous = IdentifierCharCategory.get(string.charAt(start));
        for (int i = start + 1; i < len; i++) {
            IdentifierCharCategory current = IdentifierCharCategory.get(string.charAt(i));
            if (current != IdentifierCharCategory.SYMBOL && current != previous) return i;
            previous = current;
        }
        return -1;
    }

    public static int getNextBoundaryInLiteral(String string, int start) {
        int len = string.length();
        if (len <= start || start < 0) return -1;
        CaseSensitiveCodePointCategory previous = CaseSensitiveCodePointCategory.get(string.codePointAt(start));
        for (int i = start + 1; i < len; i++) {
            CaseSensitiveCodePointCategory current = CaseSensitiveCodePointCategory.get(string.codePointAt(i));
            if (current == CaseSensitiveCodePointCategory.UPPERCASE_LETTER || (current != previous && !(current == CaseSensitiveCodePointCategory.LOWERCASE_LETTER && previous == CaseSensitiveCodePointCategory.UPPERCASE_LETTER)))
                return i;
            previous = current;
        }
        return -1;
    }

    public static int getPreviousBoundary(String string, int position, boolean skipOverSpaces) {
        int len = string.length();
        if (position <= 1 || len == 0) return 0;
        if (position > len) return len;
        int cp = string.codePointBefore(position);
        CaseInsensitiveCodePointCategory previous = CaseInsensitiveCodePointCategory.get(cp);
        int i = position - (cp > 0xFFFF ? 2 : 1);
        while (i > 0) {
            cp = string.codePointBefore(i);
            CaseInsensitiveCodePointCategory current = CaseInsensitiveCodePointCategory.get(cp);
            if (current != previous) {
                if (!skipOverSpaces) return i;
                int sp = lastIndexOfExcept(string, ' ', i - 1);
                return sp == -1 ? 0 : sp + 1;
            }
            i -= cp > 0xFFFF ? 2 : 1;
        }
        return 0;
    }

    public static int indexOfExcept(String str, char ch, int start) {
        int length = str.length();
        for (int i = start; i < length; i++) {
            if (str.charAt(i) != ch) return i;
        }
        return -1;
    }

    public static boolean isIdentifierMatch(@Nullable String candidate, @Nullable String remaining) {
        if (candidate == null) return remaining == null;
        if (remaining == null) return false;
        int candidateLen = candidate.length();
        int remainingLen = remaining.length();
        if (remainingLen > candidateLen) return false;
        if (remainingLen == candidateLen) return candidate.equals(remaining);
        int candidateIndex = 0;
        int remainingIndex = 0;
        IntArrayList stack = new IntArrayList(remainingLen);
        while (remainingIndex < remainingLen) {
            char remainingChar = remaining.charAt(remainingIndex);
            boolean failed = true;
            if (candidateIndex < candidateLen) {
                char candidateChar = candidate.charAt(candidateIndex);
                if (remainingChar == candidateChar) {
                    stack.push(candidateIndex);
                    remainingIndex++;
                    candidateIndex++;
                    continue;
                }
                IdentifierCharCategory current = IdentifierCharCategory.get(remainingChar);
                if (current == IdentifierCharCategory.SYMBOL) {
                    int nextCharIndex = candidate.indexOf(remainingChar, candidateIndex);
                    if (nextCharIndex >= 0) {
                        candidateIndex = nextCharIndex + 1;
                        stack.push(nextCharIndex);
                        failed = false;
                    }
                } else {
                    int nextBoundary = getNextBoundaryInIdentifier(candidate, candidateIndex);
                    while (nextBoundary > 0) {
                        if (remainingChar == candidate.charAt(nextBoundary)) {
                            candidateIndex = nextBoundary + 1;
                            stack.push(nextBoundary);
                            failed = false;
                            break;
                        }
                        nextBoundary = getNextBoundaryInIdentifier(candidate, nextBoundary);
                    }
                }
            }
            if (!failed) {
                remainingIndex++;
            } else if (remainingIndex == 0) {
                break;
            } else {
                remainingIndex--;
                candidateIndex = stack.popInt() + 1;
            }
        }
        return remainingIndex == remainingLen;
    }

    public static boolean isLiteralMatch(@Nullable String candidate, @Nullable String remaining) {
        if (candidate == null) return remaining == null;
        if (remaining == null) return false;
        int candidateLen = candidate.length();
        int remainingLen = remaining.length();
        if (remainingLen > candidateLen) return false;
        if (remainingLen == candidateLen) return candidate.equalsIgnoreCase(remaining);
        int candidateIndex = 0;
        int remainingIndex = 0;
        IntArrayList stack = new IntArrayList(remainingLen);
        while (remainingIndex < remainingLen) {
            char remainingChar = Character.toLowerCase(remaining.charAt(remainingIndex));
            boolean failed = true;
            if (candidateIndex < candidateLen) {
                char candidateChar = candidate.charAt(candidateIndex);
                if (remainingChar == Character.toLowerCase(candidateChar)) {
                    stack.push(candidateIndex);
                    remainingIndex++;
                    candidateIndex++;
                    continue;
                }
                int nextBoundary = getNextBoundaryInLiteral(candidate, candidateIndex);
                while (nextBoundary > 0) {
                    if (remainingChar == Character.toLowerCase(candidate.charAt(nextBoundary))) {
                        candidateIndex = nextBoundary + 1;
                        stack.push(nextBoundary);
                        failed = false;
                        break;
                    }
                    nextBoundary = getNextBoundaryInLiteral(candidate, nextBoundary);
                }
            }
            if (!failed) {
                remainingIndex++;
            } else if (remainingIndex == 0) {
                break;
            } else {
                remainingIndex--;
                candidateIndex = stack.popInt() + 1;
            }
        }
        return remainingIndex == remainingLen;
    }

    public static int lastIndexOfExcept(String str, char ch, int start) {
        for (int i = start; i >= 0; i--) {
            if (str.charAt(i) != ch) return i;
        }
        return -1;
    }
}