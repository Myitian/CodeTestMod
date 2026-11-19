package net.myitian.codetest.commands.builders;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.myitian.codetest.CodeTest;
import net.myitian.codetest.LitematicaPrinterExtension;
import net.myitian.codetest.commands.CommandBuilder;
import net.myitian.codetest.commands.CommandFeedback;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

public record LitematicaPrinterCommand<S>(CommandBuilder<S> builder) {
    private int add(CommandContext<S> context) throws CommandSyntaxException {
        try {
            String typeName = StringArgumentType.getString(context, "typeName");
            LitematicaPrinterExtension.addInteractiveBlock(typeName);
            builder.feedback()
                .apply(context.getSource())
                .sendFeedback(Component.literal("added " + typeName));
            return Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            throw new SimpleCommandExceptionType(CodeTest.throwableToComponent(ex)).create();
        }
    }

    public LiteralArgumentBuilder<S> build() {
        return builder.literal("litematica-printer")
            .then(builder.literal("interactiveBlocks")
                .then(builder.literal("fetch")
                    .executes(this::fetch))
                .then(builder.literal("print")
                    .executes(this::print))
                .then(builder.literal("add")
                    .then(builder.argument("typeName", StringArgumentType.word())
                        .executes(this::add)))
                .then(builder.literal("remove")
                    .then(builder.argument("typeName", StringArgumentType.word())
                        .executes(this::remove))))
            .then(builder.literal("reloadConfig")
                .executes(this::reloadConfig));
    }

    private int fetch(CommandContext<S> context) throws CommandSyntaxException {
        try {
            LitematicaPrinterExtension.pullInteractiveBlocks();
            int size = LitematicaPrinterExtension.getEntries().size();
            builder.feedback()
                .apply(context.getSource())
                .sendFeedback(Component.literal("fetch " + size + " entries"));
            return size;
        } catch (ReflectiveOperationException ex) {
            throw new SimpleCommandExceptionType(CodeTest.throwableToComponent(ex)).create();
        }
    }

    private int print(CommandContext<S> context) throws CommandSyntaxException {
        try {
            LitematicaPrinterExtension.pullInteractiveBlocks();
            CommandFeedback feedback = builder.feedback().apply(context.getSource());
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
    }

    private int reloadConfig(CommandContext<S> context) throws CommandSyntaxException {
        try {
            LitematicaPrinterExtension.reloadConfig();
            builder.feedback()
                .apply(context.getSource())
                .sendFeedback(Component.literal("reloaded"));
            return Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            throw new SimpleCommandExceptionType(CodeTest.throwableToComponent(ex)).create();
        }
    }

    private int remove(CommandContext<S> context) throws CommandSyntaxException {
        try {
            String typeName = StringArgumentType.getString(context, "typeName");
            LitematicaPrinterExtension.removeInteractiveBlock(typeName);
            builder.feedback()
                .apply(context.getSource())
                .sendFeedback(Component.literal("removed " + typeName));
            return Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            throw new SimpleCommandExceptionType(CodeTest.throwableToComponent(ex)).create();
        }
    }
}