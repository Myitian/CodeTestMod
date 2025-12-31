package net.myitian.codetest;

import net.minecraft.network.chat.Component;

public interface CommandFeedback {
    void sendFeedback(Component msg);

    void sendError(Component msg);
}