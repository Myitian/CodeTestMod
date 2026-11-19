package net.myitian.codetest.mixin.litematica;

import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.litematica.scheduler.tasks.TaskBase;
import fi.dy.masa.litematica.scheduler.tasks.TaskPasteSchematicPerChunkCommand;
import fi.dy.masa.litematica.world.ChunkSchematic;
import fi.dy.masa.malilib.util.IntBoundingBox;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.myitian.codetest.CodeTest;
import net.myitian.codetest.config.Config;
import org.jetbrains.annotations.Nullable;
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
    private static final ThreadLocal<State> codeTest$state = new ThreadLocal<>();

    @Inject(method = "generateStrips", at = @At(value = "HEAD"))
    private void generateStrips(int[][][] workArr, Direction stripDirection, IntBoundingBox box, ChunkSchematic chunk, boolean ignoreBeFromFill, CallbackInfo ci) {
        if (Config.enhancedSchematicPastingEnabled) {
            if (mc.level != null) {
                codeTest$state.set(new State(mc.level, box));
            }
        }
    }

    @Inject(method = "generateStrips", at = @At(value = "RETURN"))
    private void generateStrips(CallbackInfo ci) {
        State state = codeTest$state.get();
        if (state != null && state.skippedCount > 0) {
            CodeTest.LOGGER.info("skipped {} blocks", state.skippedCount);
        }
        codeTest$state.remove();
    }

    @Redirect(method = "generateStrips", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private boolean isAir(BlockState stateSchematic, @Local(name = "mutablePos") BlockPos.MutableBlockPos mutablePos) {
        if (!Config.enhancedSchematicPastingEnabled) {
            return stateSchematic.isAir();
        }
        if (stateSchematic.isAir()) {
            return true;
        }
        State state = codeTest$state.get();
        if (state != null) {
            ChunkAccess chunkAccess = state.chunkAccess;
            if (chunkAccess != null) {
                BlockState stateClient = chunkAccess.getBlockState(mutablePos);
                boolean skip = !shouldSetBlock(stateSchematic, stateClient);
                if (skip) {
                    state.skippedCount++;
                    return true;
                }
            } else if (state.skippedCount >= 0) {
                CodeTest.LOGGER.warn("Client chunk {} is null", state.chunkPos);
                state.skippedCount = -1;
            }
        }
        return false;
    }

    @Shadow
    protected abstract boolean shouldSetBlock(BlockState stateSchematic, BlockState stateClient);

    static final class State {
        @Nullable
        public final ChunkAccess chunkAccess;
        public final ChunkPos chunkPos;
        public int skippedCount = 0;

        public State(ClientLevel level, IntBoundingBox box) {
            int chunkX = box.minX >> 4;
            int chunkZ = box.minZ >> 4;
            chunkAccess = level.getChunk(chunkX, chunkZ);
            chunkPos = new ChunkPos(chunkX, chunkZ);
        }
    }
}