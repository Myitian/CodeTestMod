package net.myitian.codetest;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;

import java.nio.file.Path;

public final class PlatformUtil {
    @ExpectPlatform
    public static Path getConfigDirectory() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isModLoaded(String modId) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <S> BlockPos getBlockPos(Coordinates coordinates, S source) {
        throw new AssertionError();
    }
}