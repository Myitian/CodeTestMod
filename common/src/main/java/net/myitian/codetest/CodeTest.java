package net.myitian.codetest;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.myitian.codetest.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

public final class CodeTest {
    public static final String MOD_ID = "codetest";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Path CONFIG_PATH = PlatformUtil.getConfigDirectory().resolve(MOD_ID + ".json");

    public static void reloadConfig() {
        File configFile = CONFIG_PATH.toFile();
        if (!Config.load(configFile)) {
            Config.save(configFile);
        }
    }

    public static boolean isLiteralMatch(String candidate, String remaining) {
        if (candidate == null || remaining == null)
            return false;
        int cLen = candidate.length();
        int rLen = remaining.length();
        if (rLen > cLen)
            return false;
        if (rLen == cLen)
            return candidate.equalsIgnoreCase(remaining);
        int ci = 0;
        int ri = 0;
        IntArrayList stack = new IntArrayList(rLen);
        while (ri < rLen) {
            char rc = Character.toLowerCase(remaining.charAt(ri));
            boolean failed = true;
            if (ci < cLen) {
                char cc = candidate.charAt(ci);
                if (rc == Character.toLowerCase(cc)) {
                    stack.push(ci);
                    ri++;
                    ci++;
                    continue;
                }
                int nextB = getNextBoundaryInLiteral(candidate, ci);
                while (nextB > 0) {
                    if (rc == Character.toLowerCase(candidate.charAt(nextB))) {
                        ci = nextB + 1;
                        stack.push(nextB);
                        failed = false;
                        break;
                    }
                    nextB = getNextBoundaryInLiteral(candidate, nextB);
                }
            }
            if (!failed) {
                ri++;
            } else if (ri == 0) {
                break;
            } else {
                ri--;
                ci = stack.popInt() + 1;
            }
        }
        return ri == rLen;
    }

    public static int getNextBoundaryInLiteral(String string, int start) {
        int len = string.length();
        if (len <= start || start < 0)
            return -1;
        CaseSensitiveCodePointCategory previous = CaseSensitiveCodePointCategory.get(string.codePointAt(start));
        for (int i = start + 1; i < len; i++) {
            CaseSensitiveCodePointCategory current = CaseSensitiveCodePointCategory.get(string.codePointAt(i));
            if (current == CaseSensitiveCodePointCategory.UPPERCASE_LETTER
                || (current != previous
                && !(current == CaseSensitiveCodePointCategory.LOWERCASE_LETTER
                && previous == CaseSensitiveCodePointCategory.UPPERCASE_LETTER)))
                return i;
            previous = current;
        }
        return -1;
    }

    public static boolean isIdentifierMatch(String candidate, String remaining) {
        if (candidate == null || remaining == null)
            return false;
        int cLen = candidate.length();
        int rLen = remaining.length();
        if (rLen > cLen)
            return false;
        if (rLen == cLen)
            return candidate.equals(remaining);
        int ci = 0;
        int ri = 0;
        IntArrayList stack = new IntArrayList(rLen);
        while (ri < rLen) {
            char rc = remaining.charAt(ri);
            boolean failed = true;
            if (ci < cLen) {
                char cc = candidate.charAt(ci);
                if (rc == cc) {
                    stack.push(ci);
                    ri++;
                    ci++;
                    continue;
                }
                CharCategory current = CharCategory.get(rc);
                if (current == CharCategory.SYMBOL) {
                    int nextCI = candidate.indexOf(rc, ci);
                    if (nextCI >= 0) {
                        ci = nextCI + 1;
                        stack.push(nextCI);
                        failed = false;
                    }
                } else {
                    int nextB = getNextBoundaryInIdentifier(candidate, ci);
                    while (nextB > 0) {
                        if (rc == candidate.charAt(nextB)) {
                            ci = nextB + 1;
                            stack.push(nextB);
                            failed = false;
                            break;
                        }
                        nextB = getNextBoundaryInIdentifier(candidate, nextB);
                    }
                }
            }
            if (!failed) {
                ri++;
            } else if (ri == 0) {
                break;
            } else {
                ri--;
                ci = stack.popInt() + 1;
            }
        }
        return ri == rLen;
    }

    public static int getNextBoundaryInIdentifier(String string, int start) {
        int len = string.length();
        if (len <= start || start < 0)
            return -1;
        CharCategory previous = CharCategory.get(string.charAt(start));
        for (int i = start + 1; i < len; i++) {
            CharCategory current = CharCategory.get(string.charAt(i));
            if (current != CharCategory.SYMBOL && current != previous)
                return i;
            previous = current;
        }
        return -1;
    }

    public static int getNextBoundary(String string, int position, boolean skipOverSpaces) {
        int len = string.length();
        if (position < 0 || len == 0)
            return 0;
        if (position >= len)
            return len;
        int cp = string.codePointAt(position);
        CodePointCategory previous = CodePointCategory.get(cp);
        int i = position + (cp > 0xFFFF ? 2 : 1);
        while (i < len) {
            cp = string.codePointAt(i);
            CodePointCategory current = CodePointCategory.get(cp);
            if (current != previous) {
                if (!skipOverSpaces)
                    return i;
                int sp = indexOfExcept(string, ' ', i);
                return sp == -1 ? len : sp;
            }
            i += cp > 0xFFFF ? 2 : 1;
        }
        return len;
    }

    public static int getPreviousBoundary(String string, int position, boolean skipOverSpaces) {
        int len = string.length();
        if (position <= 1 || len == 0)
            return 0;
        if (position > len)
            return len;
        int cp = string.codePointBefore(position);
        CodePointCategory previous = CodePointCategory.get(cp);
        int i = position - (cp > 0xFFFF ? 2 : 1);
        while (i > 0) {
            cp = string.codePointBefore(i);
            CodePointCategory current = CodePointCategory.get(cp);
            if (current != previous) {
                if (!skipOverSpaces)
                    return i;
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
            if (str.charAt(i) != ch)
                return i;
        }
        return -1;
    }

    public static int lastIndexOfExcept(String str, char ch, int start) {
        for (int i = start; i >= 0; i--) {
            if (str.charAt(i) != ch)
                return i;
        }
        return -1;
    }
}