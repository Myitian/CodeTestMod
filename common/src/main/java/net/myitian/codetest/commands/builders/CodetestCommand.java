package net.myitian.codetest.commands.builders;

import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.network.chat.Component;
import net.myitian.codetest.CodeTest;
import net.myitian.codetest.commands.CommandBuilder;
import net.myitian.codetest.config.Config;

import java.io.StringWriter;

public record CodetestCommand<S>(CommandBuilder<S> builder) {
    public LiteralArgumentBuilder<S> build() {
        return builder.literal("codetest")
            .then(builder.literal("config")
                .then(builder.literal("print")
                    .executes(this::printConfig))
                .then(builder.literal("reset")
                    .then(builder.literal("*"))
                    .executes(this::resetAllConfig)
                    .then(builder.literal("gameModes")
                        .executes(this::resetGameModesConfig)))
                .then(builder.literal("reload")
                    .executes(this::reloadConfig))
                .then(builder.literal("save")
                    .executes(this::saveConfig)));
    }

    private int printConfig(CommandContext<S> context) throws CommandSyntaxException {
        try (var sw = new StringWriter(); var jw = new JsonWriter(sw)) {
            jw.setHtmlSafe(false);
            jw.setIndent("  ");
            Config.save(jw);
            jw.flush();
            sw.flush();
            builder.feedback()
                .apply(context.getSource())
                .sendFeedback(Component.literal(sw.toString()));
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CodeTest.LOGGER.warn("Failed to write config!", e);
            throw new SimpleCommandExceptionType(Component.literal(e.toString())).create();
        }
    }

    private int reloadConfig(CommandContext<S> context) {
        CodeTest.reloadConfig();
        return Command.SINGLE_SUCCESS;
    }

    private int resetAllConfig(CommandContext<S> context) {
        Config.reset();
        return Command.SINGLE_SUCCESS;
    }

    private int resetGameModesConfig(CommandContext<S> context) {
        Config.setGameModes(Config.defaultGameModes);
        return Command.SINGLE_SUCCESS;
    }

    private int saveConfig(CommandContext<S> context) {
        Config.save(CodeTest.CONFIG);
        return Command.SINGLE_SUCCESS;
    }
}