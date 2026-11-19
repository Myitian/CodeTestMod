package net.myitian.codetest.xfill;

import net.minecraft.commands.arguments.blocks.BlockInput;

public record XFillPreset(BlockInput[] usedBlocks, XFillFunction function) {
}