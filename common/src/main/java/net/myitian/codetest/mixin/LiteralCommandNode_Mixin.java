package net.myitian.codetest.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.myitian.codetest.CodeTest;
import net.myitian.codetest.config.Config;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(LiteralCommandNode.class)
abstract class LiteralCommandNode_Mixin {
    @Shadow(remap = false)
    private @Final String literal;

    @Inject(method = "listSuggestions", at = @At("HEAD"), remap = false, cancellable = true)
    private void listSuggestions(
        CommandContext<?> context,
        SuggestionsBuilder builder,
        CallbackInfoReturnable<CompletableFuture<Suggestions>> ci) {
        if (!Config.enhancedMatchingEnabled)
            return;

        if (CodeTest.isLiteralMatch(literal, builder.getRemaining()))
            ci.setReturnValue(builder.suggest(literal).buildFuture());
        else
            ci.setReturnValue(Suggestions.empty());
    }
}