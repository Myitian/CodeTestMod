package net.myitian.codetest.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.myitian.codetest.CodeTest;
import net.myitian.codetest.EffectInstanceComparator;
import net.myitian.codetest.config.Config;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Collection;

@Mixin(Gui.class)
abstract class Gui_Mixin {
    @Final
    @Shadow
    private static ResourceLocation EFFECT_BACKGROUND_AMBIENT_SPRITE;
    @Final
    @Shadow
    private static ResourceLocation EFFECT_BACKGROUND_SPRITE;
    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void renderEffects(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!Config.CustomHudEffectDisplay.enabled) {
            return;
        }
        MAIN_RENDER:
        {
            Collection<MobEffectInstance> collection = minecraft.player.getActiveEffects();
            if (collection.isEmpty()) {
                break MAIN_RENDER;
            }
            if (minecraft.screen instanceof EffectRenderingInventoryScreen<?> effectRenderingInventoryScreen
                && effectRenderingInventoryScreen.canSeeEffects()) {
                break MAIN_RENDER;
            }
            RenderSystem.enableBlend();
            MobEffectInstance[] effects = collection.toArray(CodeTest.EmptyMobEffectInstanceArray);
            Arrays.sort(effects, EffectInstanceComparator.INSTANCE);
            int y = minecraft.isDemo() ? 16 : 1;
            int guiWidth = guiGraphics.guiWidth();
            double guiWidthD = guiGraphics.guiWidth();
            boolean lastIsBeneficial = effects[0].getEffect().value().isBeneficial();
            MobEffectTextureManager textureManager = minecraft.getMobEffectTextures();
            for (int i = 0, currentWidth = 0, countInLine = 0; i < effects.length; i++) {
                MobEffectInstance effect = effects[i];
                Holder<MobEffect> holder = effect.getEffect();
                countInLine++;
                boolean thisIsBeneficial = holder.value().isBeneficial();
                if (thisIsBeneficial != lastIsBeneficial) {
                    if (currentWidth == 0) {
                        y++;
                    } else {
                        currentWidth = 0;
                        countInLine = 0;
                        y += 26;
                    }
                } else {
                    currentWidth += 25;
                }
                lastIsBeneficial = thisIsBeneficial;

                int x = guiWidth - currentWidth;
                float alpha = 1;
                if (effect.isAmbient()) {
                    guiGraphics.blitSprite(EFFECT_BACKGROUND_AMBIENT_SPRITE, x, y, 24, 24);
                } else {
                    guiGraphics.blitSprite(EFFECT_BACKGROUND_SPRITE, x, y, 24, 24);
                    if (effect.endsWithin(200)) {
                        int duration = effect.getDuration();
                        int n = 10 - duration / 20;
                        alpha = Mth.clamp(duration * 0.01F, 0, 0.5F)
                            + Mth.cos(duration * Mth.PI * 0.2F) * Mth.clamp(n * 0.025F, 0, 0.25F);
                    }
                }
                guiGraphics.setColor(1, 1, 1, alpha);
                guiGraphics.blit(x + 3, y + 3, 0, 18, 18, textureManager.get(holder));
                guiGraphics.setColor(1, 1, 1, 1);

                if (countInLine >= Config.CustomHudEffectDisplay.maxCountWidth
                    || currentWidth >= Config.CustomHudEffectDisplay.maxPixelWidth
                    || currentWidth / guiWidthD >= Config.CustomHudEffectDisplay.maxWidthRatio) {
                    countInLine = 0;
                    currentWidth = 0;
                    y += 25;
                }
            }
            RenderSystem.disableBlend();
        }
        ci.cancel();
    }
}