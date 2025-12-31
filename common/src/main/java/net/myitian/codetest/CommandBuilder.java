package net.myitian.codetest;

import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.myitian.codetest.config.Config;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.StringWriter;
import java.util.function.Consumer;
import java.util.function.Function;

public record CommandBuilder<S>(
    Literal<S> literal,
    Argument<S> argument,
    Function<S, CommandFeedback> getFeedbackWrapper) {

    public LiteralArgumentBuilder<S> literal(String name) {
        return literal.literal(name);
    }

    public RequiredArgumentBuilder<S, ?> argument(String name, ArgumentType<?> type) {
        return argument.argument(name, type);
    }

    public void build(Consumer<LiteralArgumentBuilder<S>> consumer) {
        consumer.accept(codetestCommand());
        if (Config.glfwCommandEnabled) {
            consumer.accept(glfwCommand());
        }
    }

    public LiteralArgumentBuilder<S> codetestCommand() {
        return literal("codetest")
            .then(literal("config")
                .then(literal("print")
                    .executes(context -> {
                        try (var sw = new StringWriter(); var jw = new JsonWriter(sw)) {
                            jw.setHtmlSafe(false);
                            jw.setIndent("  ");
                            Config.save(jw);
                            jw.flush();
                            sw.flush();
                            getFeedbackWrapper
                                .apply(context.getSource())
                                .sendFeedback(Component.literal(sw.toString()));
                            return Command.SINGLE_SUCCESS;
                        } catch (Exception e) {
                            CodeTest.LOGGER.warn("Failed to write config!", e);
                            throw new SimpleCommandExceptionType(Component.literal(e.toString())).create();
                        }
                    }))
                .then(literal("reset")
                    .then(literal("gameModes")
                        .executes(context -> {
                            Config.setGamemodes(Config.defaultGameModes);
                            return Command.SINGLE_SUCCESS;
                        })))
                .then(literal("reload")
                    .executes(context -> {
                        CodeTest.reloadConfig();
                        return Command.SINGLE_SUCCESS;
                    }))
                .then(literal("save")
                    .executes(context -> {
                        File configFile = CodeTest.CONFIG_PATH.toFile();
                        Config.save(configFile);
                        return Command.SINGLE_SUCCESS;
                    })));
    }

    public LiteralArgumentBuilder<S> glfwCommand() {
        return literal("glfw")
            .then(literal("SetWindowSize")
                .then(argument("width", IntegerArgumentType.integer())
                    .then(argument("height", IntegerArgumentType.integer())
                        .executes(context -> {
                            long handle = Minecraft.getInstance().getWindow().getWindow();
                            int width = IntegerArgumentType.getInteger(context, "width");
                            int height = IntegerArgumentType.getInteger(context, "height");
                            GLFW.glfwSetWindowSize(handle, width, height);
                            return Command.SINGLE_SUCCESS;
                        }))))
            .then(literal("SetWindowPos")
                .then(argument("xpos", IntegerArgumentType.integer())
                    .then(argument("ypos", IntegerArgumentType.integer())
                        .executes(context -> {
                            long handle = Minecraft.getInstance().getWindow().getWindow();
                            int xpos = IntegerArgumentType.getInteger(context, "xpos");
                            int ypos = IntegerArgumentType.getInteger(context, "ypos");
                            GLFW.glfwSetWindowPos(handle, xpos, ypos);
                            return Command.SINGLE_SUCCESS;
                        }))));
    }

    @FunctionalInterface
    public interface Argument<S> {
        RequiredArgumentBuilder<S, ?> argument(String name, ArgumentType<?> type);
    }

    @FunctionalInterface
    public interface Literal<S> {
        LiteralArgumentBuilder<S> literal(String name);
    }
}