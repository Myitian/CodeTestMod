package net.myitian.codetest.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.myitian.codetest.CodeTest;
import net.myitian.codetest.LitematicaPrinterExtension;
import net.myitian.codetest.commands.builders.*;
import net.myitian.codetest.config.Config;

import java.util.function.Consumer;
import java.util.function.Function;

public record CommandBuilder<S>(
    Literal<S> literal,
    Argument<S> argument,
    Function<S, CommandFeedback> feedback) {

    public RequiredArgumentBuilder<S, ?> argument(String name, ArgumentType<?> type) {
        return argument.argument(name, type);
    }

    public void build(Consumer<LiteralArgumentBuilder<S>> consumer) {
        consumer.accept(new CodetestCommand<>(this).build());
        if (Config.blockInfoCommandEnabled) {
            consumer.accept(new BlockInfoCommand<>(this).build());
        }
        if (Config.cameraInfoCommandEnabled) {
            consumer.accept(new CameraInfoCommand<>(this).build());
        }
        if (Config.gcCommandEnabled) {
            consumer.accept(new GcCommand<>(this).build());
        }
        if (Config.glfwCommandEnabled) {
            consumer.accept(new GlfwCommand<>(this).build());
        }
        if (Config.hitTestCommandEnabled) {
            consumer.accept(new HitTestCommand<>(this).build());
        }
        if (Config.reflectionQueryCommandEnabled) {
            consumer.accept(new ReflectionQueryCommand<>(this).build());
        }
        if (Config.litematicaPrinterCommandEnabled) {
            try {
                LitematicaPrinterExtension.fetchInteractiveBlocks();
                consumer.accept(new LitematicaPrinterCommand<>(this).build());
            } catch (ReflectiveOperationException e) {
                CodeTest.LOGGER.info("No LitematicaPrinter found!", e);
            }
        }
    }

    public LiteralArgumentBuilder<S> literal(String name) {
        return literal.literal(name);
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