package net.myitian.codetest.mixin;

import net.minecraft.client.gui.components.EditBox;
import net.myitian.codetest.CodeTest;
import net.myitian.codetest.config.Config;
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
        int wordOffset,
        int cursorPosition,
        boolean skipOverSpaces,
        CallbackInfoReturnable<Integer> ci) {
        if (!Config.enhancedCursorEnabled)
            return;

        int i = cursorPosition;
        if (wordOffset < 0) {
            while (wordOffset++ < 0)
                i = CodeTest.getPreviousBoundary(value, i, skipOverSpaces);
        } else if (wordOffset > 0) {
            while (wordOffset-- > 0)
                i = CodeTest.getNextBoundary(value, i, skipOverSpaces);
        }
        ci.setReturnValue(i);
    }
}