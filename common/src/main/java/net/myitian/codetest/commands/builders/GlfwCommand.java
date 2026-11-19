package net.myitian.codetest.commands.builders;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.myitian.codetest.commands.CommandBuilder;
import org.lwjgl.glfw.GLFW;

public record GlfwCommand<S>(CommandBuilder<S> builder) {
    public LiteralArgumentBuilder<S> build() {
        return builder.literal("glfw")
            .then(builder.literal("SetWindowSize")
                .then(builder.argument("width", IntegerArgumentType.integer())
                    .then(builder.argument("height", IntegerArgumentType.integer())
                        .executes(this::setWindowSize))))
            .then(builder.literal("SetWindowPos")
                .then(builder.argument("xpos", IntegerArgumentType.integer())
                    .then(builder.argument("ypos", IntegerArgumentType.integer())
                        .executes(this::setWindowPos))));
    }

    private int setWindowPos(CommandContext<S> context) {
        long handle = Minecraft.getInstance().getWindow().getWindow();
        int xpos = IntegerArgumentType.getInteger(context, "xpos");
        int ypos = IntegerArgumentType.getInteger(context, "ypos");
        GLFW.glfwSetWindowPos(handle, xpos, ypos);
        return Command.SINGLE_SUCCESS;
    }

    private int setWindowSize(CommandContext<S> context) {
        long handle = Minecraft.getInstance().getWindow().getWindow();
        int width = IntegerArgumentType.getInteger(context, "width");
        int height = IntegerArgumentType.getInteger(context, "height");
        GLFW.glfwSetWindowSize(handle, width, height);
        return Command.SINGLE_SUCCESS;
    }
}