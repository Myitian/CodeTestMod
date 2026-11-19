package net.myitian.codetest.mixin;

import net.minecraft.client.gui.components.EditBox;
import net.myitian.codetest.config.Config;
import net.myitian.codetest.text.TextUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EditBox.class)
abstract class EditBox_Mixin {
    @Shadow
    private String value;

    @Inject(method = "getWordPosition(IIZ)I", at = @At("HEAD"), cancellable = true)
    private void getWordPosition(
        int n,
        int pos,
        boolean skipWs,
        CallbackInfoReturnable<Integer> ci) {
        if (!Config.enhancedCursorEnabled)
            return;

        int i = pos;
        if (n < 0) {
            while (n++ < 0)
                i = TextUtil.getPreviousBoundary(value, i, skipWs);
        } else if (n > 0) {
            while (n-- > 0)
                i = TextUtil.getNextBoundary(value, i, skipWs);
        }
        ci.setReturnValue(i);
    }
}