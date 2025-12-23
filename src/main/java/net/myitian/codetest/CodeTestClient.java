package net.myitian.codetest;

import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CodeTestClient implements ClientModInitializer {
    public static boolean enhancedMatchingEnabled = true;
    public static boolean enhancedCursorEnabled = true;
    public static boolean handCommandEnabled = true;
    public static boolean glfwCommandEnabled = true;

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

    public static List<Text> getLore(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).styledLines();
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        if (handCommandEnabled) {
            dispatcher.register(literal("hand")
                .executes(context -> {
                    FabricClientCommandSource src = context.getSource();
                    ItemStack stack = src.getPlayer().getMainHandStack();
                    src.sendFeedback(stack.getFormattedName());
                    for (Text text : getLore(stack)) {
                        src.sendFeedback(text);
                    }
                    return Command.SINGLE_SUCCESS;
                }));
        }
        if (glfwCommandEnabled) {
            dispatcher.register(literal("glfw")
                .then(literal("SetWindowSize")
                    .then(argument("width", IntegerArgumentType.integer())
                        .then(argument("height", IntegerArgumentType.integer())
                            .executes(context -> {
                                long handle = MinecraftClient.getInstance().getWindow().getHandle();
                                int width = IntegerArgumentType.getInteger(context, "width");
                                int height = IntegerArgumentType.getInteger(context, "height");
                                GLFW.glfwSetWindowSize(handle, width, height);
                                return Command.SINGLE_SUCCESS;
                            }))))
                .then(literal("SetWindowPos")
                    .then(argument("xpos", IntegerArgumentType.integer())
                        .then(argument("ypos", IntegerArgumentType.integer())
                            .executes(context -> {
                                long handle = MinecraftClient.getInstance().getWindow().getHandle();
                                int xpos = IntegerArgumentType.getInteger(context, "xpos");
                                int ypos = IntegerArgumentType.getInteger(context, "ypos");
                                GLFW.glfwSetWindowPos(handle, xpos, ypos);
                                return Command.SINGLE_SUCCESS;
                            })))));
        }
    }

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(CodeTestClient::registerCommands);
    }
}