package net.myitian.codetest.xfill;

import net.minecraft.world.level.Level;

public interface XFillFunction {
    void apply(XFillContext context, int x, int y, int z, Level level);
}