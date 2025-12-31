package net.myitian.codetest.mixin;

import net.minecraft.Util;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.myitian.codetest.config.Config;
import net.myitian.codetest.screen.GameModeSwitcherScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
abstract class KeyboardHandler_Mixin {
    @Final
    @Shadow
    private Minecraft minecraft;
    @Shadow
    private long debugCrashKeyTime = -1L;

    @Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
    public void handleDebugKeys(int key, CallbackInfoReturnable<Boolean> cir) {
        if (Config.customGameModeSwitcherScreen
            && key == GLFW.GLFW_KEY_F4
            && (debugCrashKeyTime <= 0L || debugCrashKeyTime >= Util.getMillis() - 100L)) {
            minecraft.setScreen(new GameModeSwitcherScreen());
            cir.setReturnValue(Boolean.TRUE);
        }
    }
}
