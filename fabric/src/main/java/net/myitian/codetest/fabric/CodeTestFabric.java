package net.myitian.codetest.fabric;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.myitian.codetest.CodeTest;
import net.myitian.codetest.CommandBuilder;
import net.myitian.codetest.CommandFeedback;

public class CodeTestFabric implements ClientModInitializer {
    private static void commonSetup(Minecraft client) {
        CodeTest.reloadConfig();
    }

    private static void onClientCommandRegister(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        new CommandBuilder<>(
            ClientCommandManager::literal,
            ClientCommandManager::argument,
            source -> new CommandFeedback() {
                @Override
                public void sendFeedback(Component msg) {
                    source.sendFeedback(msg);
                }

                @Override
                public void sendError(Component msg) {
                    source.sendError(msg);
                }
            }).build(dispatcher::register);
    }

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(CodeTestFabric::commonSetup);
        ClientCommandRegistrationCallback.EVENT.register(CodeTestFabric::onClientCommandRegister);
    }
}