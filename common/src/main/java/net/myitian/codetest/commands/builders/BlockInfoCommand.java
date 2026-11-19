package net.myitian.codetest.commands.builders;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.data.BlockDataAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.myitian.codetest.CodeTest;
import net.myitian.codetest.commands.BlockPosArgumentEx;
import net.myitian.codetest.commands.CommandBuilder;
import net.myitian.codetest.commands.CommandFeedback;

public record BlockInfoCommand<S>(CommandBuilder<S> builder) {
    private int $class(CommandContext<S> context) throws CommandSyntaxException {
        Minecraft minecraft = Minecraft.getInstance();
        BlockPos pos = BlockPosArgumentEx.getInWorldBlockPos(context, minecraft.level, "pos");
        Class<?> $class = minecraft.level.getBlockState(pos).getBlock().getClass();
        CommandFeedback feedback = builder.feedback().apply(context.getSource());
        CodeTest.printAncestors(feedback, $class);
        return Command.SINGLE_SUCCESS;
    }

    public LiteralArgumentBuilder<S> build() {
        return builder.literal("block-info")
            .then(builder.argument("pos", BlockPosArgument.blockPos())
                .then(builder.literal("class")
                    .executes(this::$class))
                .then(builder.literal("data")
                    .executes(this::data)));
    }

    private int data(CommandContext<S> context) throws CommandSyntaxException {
        Minecraft minecraft = Minecraft.getInstance();
        BlockPos pos = BlockPosArgumentEx.getInWorldBlockPos(context, minecraft.level, "pos");
        BlockEntity blockEntity = minecraft.level.getBlockEntity(pos);
        Component message;
        if (blockEntity == null) {
            message = Component.literal("No NBT data found!").withStyle(ChatFormatting.RED);
        } else {
            BlockDataAccessor accessor = new BlockDataAccessor(blockEntity, pos);
            message = accessor.getPrintSuccess(accessor.getData());
        }
        builder.feedback()
            .apply(context.getSource())
            .sendFeedback(message);
        return blockEntity == null ? 0 : Command.SINGLE_SUCCESS;
    }
}