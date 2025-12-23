package net.myitian.codetest.mixin.client;

import net.minecraft.command.CommandSource;
import net.minecraft.util.Identifier;
import net.myitian.codetest.CodeTestClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Mixin(CommandSource.class)
public interface CommandSourceMixin {
    @Inject(method = "forEachMatching(Ljava/lang/Iterable;Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private static <T> void forEachMatching(
        Iterable<T> candidates,
        String remaining, Function<T, Identifier> identifier,
        Consumer<T> action,
        CallbackInfo ci) {
        if (!CodeTestClient.enhancedMatchingEnabled)
            return;

        if (remaining.isEmpty()) {
            for (T candidate : candidates)
                action.accept(candidate);
            ci.cancel();
            return;
        }
        boolean invert = false;
        if (remaining.startsWith("!")) {
            invert = true;
            remaining = remaining.substring(1);
        }
        if (remaining.startsWith("?")) {
            Pattern pattern = Pattern.compile(remaining.substring(1));
            for (T candidate : candidates) {
                Identifier id = identifier.apply(candidate);
                if (pattern.matcher(id.toString()).find() != invert)
                    action.accept(candidate);
            }
        } else {
            for (T candidate : candidates) {
                Identifier id = identifier.apply(candidate);
                if (CodeTestClient.isIdentifierMatch(id.toString(), remaining) != invert)
                    action.accept(candidate);
            }
        }
        ci.cancel();
    }
}