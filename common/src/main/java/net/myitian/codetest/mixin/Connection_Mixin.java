package net.myitian.codetest.mixin;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.SkipPacketException;
import net.myitian.codetest.CodeTest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
abstract class Connection_Mixin {
    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    private void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable, CallbackInfo ci) {
        if (!(throwable instanceof SkipPacketException)) {
            CodeTest.LOGGER.error("Connection.exceptionCaught", throwable);
        }
    }
}