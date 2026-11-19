package net.myitian.codetest.commands.builders;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.data.BlockDataAccessor;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.myitian.codetest.CodeTest;
import net.myitian.codetest.commands.CommandBuilder;
import net.myitian.codetest.commands.CommandFeedback;

import java.util.Optional;

public record HitTestCommand<S>(CommandBuilder<S> builder) {
    private int $class(CommandContext<S> context) {
        CommandFeedback feedback = builder.feedback().apply(context.getSource());
        Minecraft minecraft = Minecraft.getInstance();
        HitResult hitResult = minecraft.hitResult;
        Class<?> $class;
        if (hitResult instanceof BlockHitResult blockHitResult) {
            feedback.sendFeedback(Component.literal("BlockHitResult").withStyle(ChatFormatting.YELLOW));
            $class = minecraft.level
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
    }

    public LiteralArgumentBuilder<S> build() {
        return builder.literal("hit-test")
            .then(builder.literal("location")
                .executes(this::location))
            .then(builder.literal("class")
                .executes(this::$class))
            .then(builder.literal("data")
                .executes(this::data));
    }

    private int data(CommandContext<S> context) throws CommandSyntaxException {
        CommandFeedback feedback = builder.feedback().apply(context.getSource());
        Minecraft minecraft = Minecraft.getInstance();
        HitResult hitResult = minecraft.hitResult;
        DataAccessor accessor = null;
        if (hitResult instanceof BlockHitResult blockHitResult) {
            feedback.sendFeedback(Component.literal("BlockHitResult").withStyle(ChatFormatting.YELLOW));
            BlockPos pos = blockHitResult.getBlockPos();
            BlockEntity blockEntity = minecraft.level.getBlockEntity(pos);
            if (blockEntity != null) {
                accessor = new BlockDataAccessor(blockEntity, pos);
            }
        } else if (hitResult instanceof EntityHitResult entityHitResult) {
            feedback.sendFeedback(Component.literal("EntityHitResult").withStyle(ChatFormatting.YELLOW));
            accessor = new EntityDataAccessor(entityHitResult.getEntity());
        }
        if (accessor == null) {
            feedback.sendFeedback(Component.literal("No NBT data found!").withStyle(ChatFormatting.RED));
            return 0;
        } else {
            feedback.sendFeedback(accessor.getPrintSuccess(accessor.getData()));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int location(CommandContext<S> context) {
        CommandFeedback feedback = builder.feedback().apply(context.getSource());
        Optional<Vec3> oVec3 = Optional.ofNullable(Minecraft.getInstance().hitResult).map(HitResult::getLocation);
        if (oVec3.isPresent()) {
            feedback.sendFeedback(Component.literal(oVec3.get().toString()));
        } else {
            feedback.sendFeedback(Component.literal("NONE"));
        }
        return Command.SINGLE_SUCCESS;
    }
}