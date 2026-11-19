package net.myitian.codetest.commands.builders;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.myitian.codetest.CodeTest;
import net.myitian.codetest.commands.CommandBuilder;
import net.myitian.codetest.commands.CommandFeedback;

public record CameraInfoCommand<S>(CommandBuilder<S> builder) {
    public LiteralArgumentBuilder<S> build() {
        return builder.literal("camera-info")
            .executes(this::run);
    }

    private int run(CommandContext<S> context) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        CommandFeedback feedback = builder.feedback().apply(context.getSource());
        feedback.sendFeedback(Component.literal("Position:").withStyle(ChatFormatting.AQUA));
        feedback.sendFeedback(CodeTest.getVec3Component(camera.getPosition()));
        feedback.sendFeedback(Component.literal("LookVector:").withStyle(ChatFormatting.AQUA));
        feedback.sendFeedback(CodeTest.getVec3Component(camera.getLookVector()));
        feedback.sendFeedback(Component.literal("FluidInCamera:").withStyle(ChatFormatting.AQUA));
        feedback.sendFeedback(Component.literal(camera.getFluidInCamera().toString()));
        return Command.SINGLE_SUCCESS;
    }
}