package net.myitian.codetest;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.world.phys.Vec3;
import net.myitian.codetest.commands.CommandFeedback;
import net.myitian.codetest.config.Config;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.*;
import java.nio.file.Path;

// Misc utils for this mod
public final class CodeTest {
    public static final String MOD_ID = "codetest";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Path CONFIG_DIR = PlatformUtil.getConfigDirectory().resolve(MOD_ID);
    public static final File CONFIG = CONFIG_DIR.resolve("MainConfig.json").toFile();
    private static final Style COPY_TO_CLIPBOARD = Style.EMPTY
        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click")));
    private static final Component SEMICOLON = Component.literal(";").withStyle(ChatFormatting.GRAY);
    private static final Component COMMA = Component.literal(",").withStyle(ChatFormatting.GRAY);
    private static final Component PAIRED_BRACKET = Component.literal("()").withStyle(ChatFormatting.GRAY);
    private static final Component CLOSE_BRACKET = Component.literal(")").withStyle(ChatFormatting.GRAY);
    private static final Component SPACE = Component.literal(" ");

    public static MutableComponent applyCopyToClipboard(MutableComponent component) {
        String value = component.getString();
        return component.withStyle(COPY_TO_CLIPBOARD
            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value)).withInsertion(value));
    }

    public static MutableComponent getClassNameComponent(Class<?> $class) {
        return applyCopyToClipboard(Component.literal($class.getName()).withStyle(ChatFormatting.YELLOW));
    }

    public static ChatFormatting getColorByType(Class<?> $class) {
        return $class.isPrimitive() ? ChatFormatting.LIGHT_PURPLE :
            Modifier.isInterface($class.getModifiers()) ? ChatFormatting.AQUA : ChatFormatting.GREEN;
    }

    public static MutableComponent getCopyToClipboardComponent(String value) {
        return Component.literal(value).withStyle(COPY_TO_CLIPBOARD
            .withColor(ChatFormatting.WHITE)
            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value)).withInsertion(value));
    }

    public static Component getExceptionTypesComponent(Executable executable) {
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

    public static MutableComponent getFieldNameComponent(Field field) {
        return Component.empty()
            .append(getTypeNameComponent(field.getType()))
            .append(SPACE)
            .append(getMemberNameComponent(field, ChatFormatting.WHITE));
    }

    public static MutableComponent getMemberNameComponent(Member member, ChatFormatting style) {
        return applyCopyToClipboard(Component.literal(member.getName()).withStyle(style));
    }

    public static MutableComponent getMethodNameComponent(Method method) {
        return Component.empty()
            .append(getTypeNameComponent(method.getReturnType()))
            .append(SPACE)
            .append(getMemberNameComponent(method, ChatFormatting.YELLOW));
    }

    public static Component getParameterTypesComponent(Executable executable) {
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

    public static MutableComponent getTypeNameComponent(Class<?> $class) {
        return applyCopyToClipboard(Component.literal($class.getTypeName()).withStyle(getColorByType($class)));
    }

    public static Component getVec3Component(Vec3 vec3) {
        return applyCopyToClipboard(Component.literal("(").withStyle(ChatFormatting.WHITE)
            .append(Component.literal(String.valueOf(vec3.x)).withStyle(ChatFormatting.GREEN))
            .append(Component.literal(", "))
            .append(Component.literal(String.valueOf(vec3.y)).withStyle(ChatFormatting.GREEN))
            .append(Component.literal(", "))
            .append(Component.literal(String.valueOf(vec3.z)).withStyle(ChatFormatting.GREEN))
            .append(Component.literal(")")));
    }

    public static Component getVec3Component(Vector3f vec3) {
        return applyCopyToClipboard(Component.literal("(").withStyle(ChatFormatting.WHITE)
            .append(Component.literal(String.valueOf(vec3.x)).withStyle(ChatFormatting.GREEN))
            .append(Component.literal(", "))
            .append(Component.literal(String.valueOf(vec3.y)).withStyle(ChatFormatting.GREEN))
            .append(Component.literal(", "))
            .append(Component.literal(String.valueOf(vec3.z)).withStyle(ChatFormatting.GREEN))
            .append(Component.literal(")")));
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

    public static void printAncestors(CommandFeedback feedback, Class<?> $class) {
        while ($class != null) {
            feedback.sendFeedback(getTypeNameComponent($class));
            for (Class<?> $interface : $class.getInterfaces()) {
                feedback.sendFeedback(getTypeNameComponent($interface));
            }
            $class = $class.getSuperclass();
        }
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

    public static void reloadConfig() {
        if (!Config.load(CONFIG)) {
            Config.save(CONFIG);
        }
    }

    public static Component throwableToComponent(Throwable throwable) {
        return applyCopyToClipboard(Component.literal(throwable.getClass().getTypeName()).withStyle(ChatFormatting.RED)
            .append(": ")
            .append(throwable.getLocalizedMessage()));
    }
}