package net.myitian.codetest.neoforge;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.myitian.codetest.CodeTest;
import net.myitian.codetest.CommandBuilder;
import net.myitian.codetest.CommandFeedback;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@Mod(value = CodeTest.MOD_ID, dist = Dist.CLIENT)
public class CodeTestNeoForge {
    @EventBusSubscriber(modid = CodeTest.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
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

    @EventBusSubscriber(modid = CodeTest.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            CodeTest.reloadConfig();
        }
    }
}