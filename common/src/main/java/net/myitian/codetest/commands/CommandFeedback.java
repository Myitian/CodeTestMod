package net.myitian.codetest.commands;

import net.minecraft.network.chat.Component;

public interface CommandFeedback {
    void sendError(Component msg);

    void sendFeedback(Component msg);
}