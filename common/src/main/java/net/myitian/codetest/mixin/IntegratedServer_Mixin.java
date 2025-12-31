package net.myitian.codetest.mixin;

import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IntegratedServer.class)
abstract class IntegratedServer_Mixin {
    @Inject(method = "initServer", at = @At("TAIL"))
    private void initServer(CallbackInfoReturnable<Boolean> cir) {
        MinecraftServer self = (MinecraftServer) (Object) this;
        self.setUsesAuthentication(false);
    }
}