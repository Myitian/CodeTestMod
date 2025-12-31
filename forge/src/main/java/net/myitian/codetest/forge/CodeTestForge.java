package net.myitian.codetest.forge;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.myitian.codetest.CodeTest;
import net.myitian.codetest.CommandBuilder;
import net.myitian.codetest.CommandFeedback;

@Mod(CodeTest.MOD_ID)
public final class CodeTestForge {
    @Mod.EventBusSubscriber(modid = CodeTest.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ForgeBus {
        @SubscribeEvent
        public static void onClientCommandRegister(RegisterClientCommandsEvent event) {
            new CommandBuilder<>(
                Commands::literal,
                Commands::argument,
                source -> new CommandFeedback() {
                    @Override
                    public void sendFeedback(Component msg) {
                        source.sendSystemMessage(msg);
                    }

                    @Override
                    public void sendError(Component msg) {
                        source.sendFailure(msg);
                    }
                }).build(event.getDispatcher()::register);
        }
    }

    @Mod.EventBusSubscriber(modid = CodeTest.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            CodeTest.reloadConfig();
        }
    }
}