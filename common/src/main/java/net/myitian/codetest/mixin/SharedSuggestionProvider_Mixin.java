package net.myitian.codetest.mixin;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;
import net.myitian.codetest.CodeTest;
import net.myitian.codetest.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

@Mixin(SharedSuggestionProvider.class)
interface SharedSuggestionProvider_Mixin {
    @Inject(method = "filterResources(Ljava/lang/Iterable;Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private static <T> void filterResources(
        Iterable<T> candidates,
        String remaining,
        Function<T, ResourceLocation> identifier,
        Consumer<T> action,
        CallbackInfo ci) {
        if (!Config.enhancedMatchingEnabled)
            return;
        RETURN:
        {
            if (remaining.isEmpty()) {
                for (T candidate : candidates)
                    action.accept(candidate);
                break RETURN;
            }
            boolean invert = false;
            if (remaining.startsWith("!")) {
                invert = true;
                remaining = remaining.substring(1);
            }
            if (remaining.startsWith("?")) {
                Pattern pattern;
                try {
                    pattern = Pattern.compile(remaining.substring(1));
                } catch (Exception ignored) {
                    break RETURN;
                }
                for (T candidate : candidates) {
                    ResourceLocation id = identifier.apply(candidate);
                    if (pattern.matcher(id.toString()).find() != invert)
                        action.accept(candidate);
                }
            } else {
                for (T candidate : candidates) {
                    ResourceLocation id = identifier.apply(candidate);
                    if (CodeTest.isIdentifierMatch(id.toString(), remaining) != invert)
                        action.accept(candidate);
                }
            }
        }
        ci.cancel();
    }
}