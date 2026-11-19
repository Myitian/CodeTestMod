package net.myitian.codetest.commands.builders;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.network.chat.Component;
import net.myitian.codetest.GCMonitorUtils;
import net.myitian.codetest.commands.CommandBuilder;
import net.myitian.codetest.commands.CommandFeedback;

public record GcCommand<S>(CommandBuilder<S> builder) {
    public LiteralArgumentBuilder<S> build() {
        return builder.literal("gc")
            .then(builder.literal("info")
                .executes(this::info))
            .then(builder.literal("collect")
                .executes(this::collect));
    }

    private int collect(CommandContext<S> context) {
        builder.feedback()
            .apply(context.getSource())
            .sendFeedback(Component.literal("System.gc();"));
        System.gc();
        return Command.SINGLE_SUCCESS;
    }

    private int info(CommandContext<S> context) {
        CommandFeedback feedback = builder.feedback().apply(context.getSource());
        GCMonitorUtils.printGCStats(feedback);
        return Command.SINGLE_SUCCESS;
    }
}