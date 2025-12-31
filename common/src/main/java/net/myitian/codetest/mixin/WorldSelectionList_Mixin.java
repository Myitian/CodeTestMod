package net.myitian.codetest.mixin;

import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.myitian.codetest.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldSelectionList.class)
abstract class WorldSelectionList_Mixin {
    @Redirect(method = "loadLevels", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelCandidates;isEmpty()Z"))
    private boolean loadLevels_isEmpty(LevelStorageSource.LevelCandidates instance) {
        return !Config.noAutoCreateWorldScreen && instance.isEmpty();
    }
}
