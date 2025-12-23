package net.myitian.codetest.mixin.client;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.myitian.codetest.CodeTestClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin {
    @Shadow
    private String text;

    @Inject(method = "getWordSkipPosition(IIZ)I", at = @At("HEAD"), cancellable = true)
    private void getWordSkipPosition(
        int wordOffset,
        int cursorPosition,
        boolean skipOverSpaces,
        CallbackInfoReturnable<Integer> ci) {
        if (!CodeTestClient.enhancedCursorEnabled)
            return;

        int i = cursorPosition;
        if (wordOffset < 0) {
            while (wordOffset++ < 0)
                i = CodeTestClient.getPreviousBoundary(text, i, skipOverSpaces);
        } else if (wordOffset > 0) {
            while (wordOffset-- > 0)
                i = CodeTestClient.getNextBoundary(text, i, skipOverSpaces);
        }
        ci.setReturnValue(i);
        ci.cancel();
    }
}