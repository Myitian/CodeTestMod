package net.myitian.codetest.mixin.litematica;

import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.litematica.scheduler.tasks.TaskBase;
import fi.dy.masa.litematica.scheduler.tasks.TaskPasteSchematicPerChunkCommand;
import fi.dy.masa.litematica.world.ChunkSchematic;
import fi.dy.masa.malilib.util.IntBoundingBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.myitian.codetest.CodeTest;
import net.myitian.codetest.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TaskPasteSchematicPerChunkCommand.class)
abstract class TaskPasteSchematicPerChunkCommand_Mixin extends TaskBase {
    @Unique
    private static final ThreadLocal<ChunkAccess> codeTest$clientChunk = new ThreadLocal<>();

    @Shadow
    protected abstract boolean shouldSetBlock(BlockState stateSchematic, BlockState stateClient);

    @Inject(method = "generateStrips", at = @At(value = "HEAD"))
    private void generateStrips(int[][][] workArr, Direction stripDirection, IntBoundingBox box, ChunkSchematic chunk, boolean ignoreBeFromFill, CallbackInfo ci) {
        if (Config.enhancedSchematicPastingEnabled) {
            if (mc.level != null) {
                codeTest$clientChunk.set(mc.level.getChunk(box.minX >> 4, box.minZ >> 4));
            }
        }
    }

    @Redirect(method = "generateStrips", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private boolean isAir(BlockState stateSchematic, @Local(name = "mutablePos") BlockPos.MutableBlockPos mutablePos) {
        if (stateSchematic.isAir()) {
            return true;
        }
        if (Config.enhancedSchematicPastingEnabled) {
            ChunkAccess chunkAccess = codeTest$clientChunk.get();
            if (chunkAccess != null) {
                BlockState stateClient = chunkAccess.getBlockState(mutablePos);
                boolean byPass = shouldSetBlock(stateSchematic, stateClient);
                if (byPass) {
                    CodeTest.LOGGER.info("Bypass block at {} ({})", mutablePos, stateSchematic);
                    return true;
                }
            } else {
                CodeTest.LOGGER.warn("Client chunk is null when checking block at {} ({})", mutablePos, stateSchematic);
            }
        }
        return false;
    }

    @Inject(method = "generateStrips", at = @At(value = "RETURN"))
    private void generateStrips(CallbackInfo ci) {
        codeTest$clientChunk.remove();
    }
}