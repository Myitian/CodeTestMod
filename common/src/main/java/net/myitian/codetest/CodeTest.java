package net.myitian.codetest;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.myitian.codetest.config.Config;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.*;
import java.nio.file.Path;
import java.util.Map;

// Misc utils for this mod
public final class CodeTest {
    public static final String MOD_ID = "codetest";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Path CONFIG_PATH = PlatformUtil.getConfigDirectory().resolve(MOD_ID + ".json");
    public static final Style COPY_TO_CLIPBOARD = Style.EMPTY
        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click")));
    public static final Component SEMICOLON = Component.literal(";").withStyle(ChatFormatting.GRAY);
    public static final Component COMMA = Component.literal(",").withStyle(ChatFormatting.GRAY);
    public static final Component PAIRED_BRACKET = Component.literal("()").withStyle(ChatFormatting.GRAY);
    public static final Component CLOSE_BRACKET = Component.literal(")").withStyle(ChatFormatting.GRAY);
    public static final Component COLON = Component.literal(":");
    public static final Component SPACE = Component.literal(" ");
    public static final MobEffectInstance[] EmptyMobEffectInstanceArray = new MobEffectInstance[0];

    public static void reloadConfig() {
        File configFile = CONFIG_PATH.toFile();
        if (!Config.load(configFile)) {
            Config.save(configFile);
        }
    }

    public static boolean isLiteralMatch(String candidate, String remaining) {
        if (candidate == null || remaining == null) return false;
        int cLen = candidate.length();
        int rLen = remaining.length();
        if (rLen > cLen) return false;
        if (rLen == cLen) return candidate.equalsIgnoreCase(remaining);
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

    public static boolean isIdentifierMatch(String candidate, String remaining) {
        if (candidate == null || remaining == null) return false;
        int cLen = candidate.length();
        int rLen = remaining.length();
        if (rLen > cLen) return false;
        if (rLen == cLen) return candidate.equals(remaining);
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
        if (len <= start || start < 0) return -1;
        CharCategory previous = CharCategory.get(string.charAt(start));
        for (int i = start + 1; i < len; i++) {
            CharCategory current = CharCategory.get(string.charAt(i));
            if (current != CharCategory.SYMBOL && current != previous) return i;
            previous = current;
        }
        return -1;
    }

    public static int getNextBoundary(String string, int position, boolean skipOverSpaces) {
        int len = string.length();
        if (position < 0 || len == 0) return 0;
        if (position >= len) return len;
        int cp = string.codePointAt(position);
        CodePointCategory previous = CodePointCategory.get(cp);
        int i = position + (cp > 0xFFFF ? 2 : 1);
        while (i < len) {
            cp = string.codePointAt(i);
            CodePointCategory current = CodePointCategory.get(cp);
            if (current != previous) {
                if (!skipOverSpaces) return i;
                int sp = indexOfExcept(string, ' ', i);
                return sp == -1 ? len : sp;
            }
            i += cp > 0xFFFF ? 2 : 1;
        }
        return len;
    }

    public static int getPreviousBoundary(String string, int position, boolean skipOverSpaces) {
        int len = string.length();
        if (position <= 1 || len == 0) return 0;
        if (position > len) return len;
        int cp = string.codePointBefore(position);
        CodePointCategory previous = CodePointCategory.get(cp);
        int i = position - (cp > 0xFFFF ? 2 : 1);
        while (i > 0) {
            cp = string.codePointBefore(i);
            CodePointCategory current = CodePointCategory.get(cp);
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

    public static int lastIndexOfExcept(String str, char ch, int start) {
        for (int i = start; i >= 0; i--) {
            if (str.charAt(i) != ch) return i;
        }
        return -1;
    }

    public static @NotNull MutableComponent getCopyToClipboardComponent(String value) {
        return Component.literal(value).withStyle(COPY_TO_CLIPBOARD
            .withColor(ChatFormatting.WHITE)
            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value)).withInsertion(value));
    }

    public static @NotNull MutableComponent applyCopyToClipboard(MutableComponent component) {
        String value = component.getString();
        return component.withStyle(COPY_TO_CLIPBOARD
            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value)).withInsertion(value));
    }

    public static void printMembers(CommandFeedback feedback, Member[] members) {
        Class<?> last = null;
        for (Member member : members) {
            Class<?> dc = member.getDeclaringClass();
            if (!dc.equals(last)) {
                last = dc;
                feedback.sendFeedback((Modifier.isInterface(dc.getModifiers()) ?
                    Component.literal("interface ").withStyle(ChatFormatting.GREEN) :
                    Component.literal("class ").withStyle(ChatFormatting.AQUA))
                    .append(getCopyToClipboardComponent(dc.getName())));
            }
            feedback.sendFeedback(memberToComponent(member));
        }
    }

    public static Component throwableToComponent(Throwable throwable) {
        return applyCopyToClipboard(Component.literal(throwable.getClass().getTypeName()).withStyle(ChatFormatting.RED)
            .append(": ")
            .append(throwable.getLocalizedMessage()));
    }

    public static Component memberToComponent(Member member) {
        int modifiers = member.getModifiers();
        LABEL:
        if (member instanceof Field field) {
            return Component.literal((modifiers == 0 ? "" : Modifier.toString(modifiers) + " ")).withStyle(ChatFormatting.BLUE)
                .append(getFieldNameComponent(field))
                .append(SEMICOLON);
        } else if (member instanceof Executable executable) {
            MutableComponent middle;
            if (executable instanceof Constructor<?>) {
                Class<?> c = executable.getDeclaringClass();
                middle = applyCopyToClipboard(Component.literal(c.getSimpleName()).withStyle(getColorByType(c)));
            } else if (executable instanceof Method method) {
                middle = getMethodNameComponent(method);
            } else {
                break LABEL;
            }
            return Component.literal((modifiers == 0 ? "" : Modifier.toString(modifiers) + " ")).withStyle(ChatFormatting.BLUE)
                .append(middle)
                .append(getParameterTypesComponent(executable))
                .append(getExceptionTypesComponent(executable))
                .append(SEMICOLON);
        }
        return getCopyToClipboardComponent(member.toString());
    }

    public static @NotNull MutableComponent getFieldNameComponent(Field field) {
        return Component.empty()
            .append(getTypeNameComponent(field.getType()))
            .append(SPACE)
            .append(getMemberNameComponent(field, ChatFormatting.WHITE));
    }

    public static @NotNull MutableComponent getMethodNameComponent(Method method) {
        return Component.empty()
            .append(getTypeNameComponent(method.getReturnType()))
            .append(SPACE)
            .append(getMemberNameComponent(method, ChatFormatting.YELLOW));
    }

    public static @NotNull MutableComponent getTypeNameComponent(Class<?> $class) {
        return applyCopyToClipboard(Component.literal($class.getTypeName()).withStyle(getColorByType($class)));
    }

    public static @NotNull MutableComponent getClassNameComponent(Class<?> $class) {
        return applyCopyToClipboard(Component.literal($class.getName()).withStyle(ChatFormatting.YELLOW));
    }

    public static @NotNull MutableComponent getMemberNameComponent(Member member, ChatFormatting style) {
        return applyCopyToClipboard(Component.literal(member.getName()).withStyle(style));
    }

    public static @NotNull ChatFormatting getColorByType(Class<?> $class) {
        return $class.isPrimitive() ? ChatFormatting.LIGHT_PURPLE :
            Modifier.isInterface($class.getModifiers()) ? ChatFormatting.AQUA : ChatFormatting.GREEN;
    }

    public static @NotNull Component getParameterTypesComponent(Executable executable) {
        Class<?>[] classes = executable.getParameterTypes();
        if (classes.length == 0) {
            return PAIRED_BRACKET;
        }
        MutableComponent result = Component.literal("(").withStyle(ChatFormatting.GRAY);
        boolean first = true;
        for (Class<?> $class : classes) {
            if (first) {
                first = false;
            } else {
                result.append(COMMA);
            }
            result.append(getTypeNameComponent($class));
        }
        return result.append(CLOSE_BRACKET);
    }

    public static @NotNull Component getExceptionTypesComponent(Executable executable) {
        Class<?>[] classes = executable.getExceptionTypes();
        if (classes.length == 0) {
            return CommonComponents.EMPTY;
        }
        MutableComponent result = Component.literal(" throws ").withStyle(ChatFormatting.BLUE);
        boolean first = true;
        for (Class<?> $class : classes) {
            if (first) {
                first = false;
            } else {
                result.append(COMMA);
            }
            result.append(getTypeNameComponent($class));
        }
        return result;
    }

    public static @NotNull Component getVec3Component(Vec3 vec3) {
        return applyCopyToClipboard(Component.literal("(").withStyle(ChatFormatting.WHITE)
            .append(Component.literal(String.valueOf(vec3.x)).withStyle(ChatFormatting.GREEN))
            .append(Component.literal(", "))
            .append(Component.literal(String.valueOf(vec3.y)).withStyle(ChatFormatting.GREEN))
            .append(Component.literal(", "))
            .append(Component.literal(String.valueOf(vec3.z)).withStyle(ChatFormatting.GREEN))
            .append(Component.literal(")")));
    }

    public static @NotNull Component getVec3Component(Vector3f vec3) {
        return applyCopyToClipboard(Component.literal("(").withStyle(ChatFormatting.WHITE)
            .append(Component.literal(String.valueOf(vec3.x)).withStyle(ChatFormatting.GREEN))
            .append(Component.literal(", "))
            .append(Component.literal(String.valueOf(vec3.y)).withStyle(ChatFormatting.GREEN))
            .append(Component.literal(", "))
            .append(Component.literal(String.valueOf(vec3.z)).withStyle(ChatFormatting.GREEN))
            .append(Component.literal(")")));
    }

    public static @NotNull Component getTagComponent(ResourceLocation tagId) {
        return applyCopyToClipboard(Component.literal("#")
            .append(Component.literal(tagId.getNamespace())))
            .append(COLON)
            .append(Component.literal(tagId.getPath()));
    }

    public static void printAncestors(CommandFeedback feedback, Class<?> $class) {
        while ($class != null) {
            feedback.sendFeedback(getTypeNameComponent($class));
            for (Class<?> $interface : $class.getInterfaces()) {
                feedback.sendFeedback(getTypeNameComponent($interface));
            }
            $class = $class.getSuperclass();
        }
    }

    public static void printBlockStates(CommandFeedback feedback, BlockState blockState) {
        Map<Property<?>, Comparable<?>> map = blockState.getValues();
        if (map.isEmpty()) {
            feedback.sendFeedback(Component.literal("(None)"));
        } else {
            for (Map.Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
                Property<?> property = entry.getKey();
                String line = property.getName() + "=" + getPropertyName(property, entry.getValue());
                feedback.sendFeedback(applyCopyToClipboard(Component.literal(line)));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> String getPropertyName(Property<T> property, Comparable<?> value) {
        return property.getName((T) value);
    }
}