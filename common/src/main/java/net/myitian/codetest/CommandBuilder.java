package net.myitian.codetest;

import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.myitian.codetest.config.Config;
import org.apache.commons.lang3.ClassUtils;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
        if (Config.hitTestCommandEnabled) {
            consumer.accept(hitTestCommand());
        }
        if (Config.reflectionQueryCommandEnabled) {
            consumer.accept(reflectionQueryCommand());
        }
        if (Config.glfwCommandEnabled) {
            consumer.accept(glfwCommand());
        }
        try {
            LitematicaPrinterExtension.fetchInteractiveBlocks();
            consumer.accept(litematicaPrinterCommand());
        } catch (ReflectiveOperationException e) {
            CodeTest.LOGGER.info("No LitematicaPrinter found!", e);
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

    public LiteralArgumentBuilder<S> hitTestCommand() {
        return literal("hit-test")
            .then(literal("location")
                .executes(context -> {
                    CommandFeedback feedback = getFeedbackWrapper().apply(context.getSource());
                    Optional<Vec3> oVec3 = Optional.ofNullable(Minecraft.getInstance().hitResult).map(HitResult::getLocation);
                    if (oVec3.isPresent()) {
                        feedback.sendFeedback(Component.literal(oVec3.get().toString()));
                    } else {
                        feedback.sendFeedback(Component.literal("NONE"));
                    }
                    return Command.SINGLE_SUCCESS;
                }))
            .then(literal("class")
                .executes(context -> {
                    CommandFeedback feedback = getFeedbackWrapper().apply(context.getSource());
                    Minecraft minecraft = Minecraft.getInstance();
                    HitResult hitResult = minecraft.hitResult;
                    Class<?> $class;
                    if (hitResult instanceof BlockHitResult blockHitResult) {
                        feedback.sendFeedback(Component.literal("BlockHitResult").withStyle(ChatFormatting.YELLOW));
                        $class = minecraft.player
                            .level()
                            .getBlockState(blockHitResult.getBlockPos())
                            .getBlock()
                            .getClass();
                    } else if (hitResult instanceof EntityHitResult entityHitResult) {
                        feedback.sendFeedback(Component.literal("EntityHitResult").withStyle(ChatFormatting.YELLOW));
                        $class = entityHitResult.getEntity().getClass();
                    } else {
                        // This shouldn't happen. Hit tests in Minecraft should fall back to the air block by default.
                        return 0;
                    }
                    CodeTest.printAncestors(feedback, $class);
                    return Command.SINGLE_SUCCESS;
                }));
    }

    public LiteralArgumentBuilder<S> reflectionQueryCommand() {
        return literal("reflection-query")
            .then(literal("ancestors")
                .then(argument("typeName", StringArgumentType.word())
                    .executes(context -> {
                        CommandFeedback feedback = getFeedbackWrapper().apply(context.getSource());
                        String typeName = StringArgumentType.getString(context, "typeName");
                        try {
                            Class<?> $class = ClassUtils.getClass(typeName);
                            feedback.sendFeedback(CodeTest.getClassNameComponent($class));
                            CodeTest.printAncestors(feedback, $class);
                        } catch (ClassNotFoundException ex) {
                            feedback.sendError(CodeTest.throwableToComponent(ex));
                            return 0;
                        }
                        return Command.SINGLE_SUCCESS;
                    })))
            .then(literal("methods")
                .then(argument("typeName", StringArgumentType.word())
                    .executes(context -> {
                        CommandFeedback feedback = getFeedbackWrapper().apply(context.getSource());
                        String typeName = StringArgumentType.getString(context, "typeName");
                        try {
                            Class<?> $class = ClassUtils.getClass(typeName);
                            feedback.sendFeedback(CodeTest.getClassNameComponent($class));
                            CodeTest.printMembers(feedback, $class.getDeclaredMethods());
                        } catch (ClassNotFoundException ex) {
                            feedback.sendError(CodeTest.throwableToComponent(ex));
                            return 0;
                        }
                        return Command.SINGLE_SUCCESS;
                    })))
            .then(literal("fields")
                .then(argument("typeName", StringArgumentType.word())
                    .executes(context -> {
                        CommandFeedback feedback = getFeedbackWrapper().apply(context.getSource());
                        String typeName = StringArgumentType.getString(context, "typeName");
                        try {
                            Class<?> $class = ClassUtils.getClass(typeName);
                            feedback.sendFeedback(CodeTest.getClassNameComponent($class));
                            CodeTest.printMembers(feedback, $class.getDeclaredFields());
                        } catch (ClassNotFoundException ex) {
                            feedback.sendError(CodeTest.throwableToComponent(ex));
                            return 0;
                        }
                        return Command.SINGLE_SUCCESS;
                    })))
            .then(literal("constructors")
                .then(argument("typeName", StringArgumentType.word())
                    .executes(context -> {
                        CommandFeedback feedback = getFeedbackWrapper().apply(context.getSource());
                        String typeName = StringArgumentType.getString(context, "typeName");
                        try {
                            Class<?> $class = ClassUtils.getClass(typeName);
                            feedback.sendFeedback(CodeTest.getClassNameComponent($class));
                            CodeTest.printMembers(feedback, $class.getConstructors());
                        } catch (ClassNotFoundException ex) {
                            feedback.sendError(CodeTest.throwableToComponent(ex));
                            return 0;
                        }
                        return Command.SINGLE_SUCCESS;
                    })));
    }

    public LiteralArgumentBuilder<S> litematicaPrinterCommand() {
        return literal("litematica-printer")
            .then(literal("interactiveBlocks")
                .then(literal("fetch")
                    .executes(context -> {
                        try {
                            LitematicaPrinterExtension.pullInteractiveBlocks();
                            CommandFeedback feedback = getFeedbackWrapper().apply(context.getSource());
                            int size = LitematicaPrinterExtension.getEntries().size();
                            feedback.sendFeedback(Component.literal("fetch " + size + " entries"));
                            return size;
                        } catch (ReflectiveOperationException ex) {
                            throw new SimpleCommandExceptionType(CodeTest.throwableToComponent(ex)).create();
                        }
                    }))
                .then(literal("print")
                    .executes(context -> {
                        try {
                            LitematicaPrinterExtension.pullInteractiveBlocks();
                            CommandFeedback feedback = getFeedbackWrapper().apply(context.getSource());
                            Set<Map.Entry<String, Class<?>>> entries = LitematicaPrinterExtension.getEntries();
                            feedback.sendFeedback(Component.literal("InteractiveBlocks").withStyle(ChatFormatting.YELLOW));
                            for (Map.Entry<String, Class<?>> entry : entries) {
                                Class<?> $class = entry.getValue();
                                String name = LitematicaPrinterExtension.getClassName(entry.getKey(), $class);
                                feedback.sendFeedback(($class != null ? Modifier.isInterface($class.getModifiers()) ?
                                    Component.literal("interface ").withStyle(ChatFormatting.GREEN) :
                                    Component.literal("class ").withStyle(ChatFormatting.AQUA) :
                                    Component.literal("unknown ").withStyle(ChatFormatting.GRAY))
                                    .append(CodeTest.getCopyToClipboardComponent(name)));
                            }
                            return Command.SINGLE_SUCCESS;
                        } catch (ReflectiveOperationException ex) {
                            throw new SimpleCommandExceptionType(CodeTest.throwableToComponent(ex)).create();
                        }
                    }))
                .then(literal("add")
                    .then(argument("typeName", StringArgumentType.word())
                        .executes(context -> {
                            try {
                                CommandFeedback feedback = getFeedbackWrapper().apply(context.getSource());
                                String typeName = StringArgumentType.getString(context, "typeName");
                                LitematicaPrinterExtension.addInteractiveBlock(typeName);
                                feedback.sendFeedback(Component.literal("added " + typeName));
                                return Command.SINGLE_SUCCESS;
                            } catch (Exception ex) {
                                throw new SimpleCommandExceptionType(CodeTest.throwableToComponent(ex)).create();
                            }
                        })))
                .then(literal("remove")
                    .then(argument("typeName", StringArgumentType.word())
                        .executes(context -> {
                            try {
                                CommandFeedback feedback = getFeedbackWrapper().apply(context.getSource());
                                String typeName = StringArgumentType.getString(context, "typeName");
                                LitematicaPrinterExtension.removeInteractiveBlock(typeName);
                                feedback.sendFeedback(Component.literal("removed " + typeName));
                                return Command.SINGLE_SUCCESS;
                            } catch (Exception ex) {
                                throw new SimpleCommandExceptionType(CodeTest.throwableToComponent(ex)).create();
                            }
                        }))))
            .then(literal("reloadConfig")
                .executes(context -> {
                    try {
                        CommandFeedback feedback = getFeedbackWrapper().apply(context.getSource());
                        LitematicaPrinterExtension.reloadConfig();
                        feedback.sendFeedback(Component.literal("reloaded"));
                        return Command.SINGLE_SUCCESS;
                    } catch (Exception ex) {
                        throw new SimpleCommandExceptionType(CodeTest.throwableToComponent(ex)).create();
                    }
                }));
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