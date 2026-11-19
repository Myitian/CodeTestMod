package net.myitian.codetest.commands.builders;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.myitian.codetest.commands.CommandBuilder;

public record XFillCommand<S>(CommandBuilder<S> builder) {
    public LiteralArgumentBuilder<S> build() {
        return builder.literal("xfill");
    }
}