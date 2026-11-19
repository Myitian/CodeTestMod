package net.myitian.codetest.serialization;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class StringSerialization {
    public static @Nullable BlockInput deserializeBlockInput(@Nullable String str) {
        return deserializeBlockInput(str, true);
    }

    public static @Nullable BlockInput deserializeBlockInput(@Nullable String str, boolean allowNbt) {
        BlockStateParser.BlockResult result = deserializeBlockResult(str, allowNbt);
        return result == null ? null : new BlockInput(result.blockState(), result.properties().keySet(), result.nbt());
    }

    public static @Nullable BlockStateParser.BlockResult deserializeBlockResult(@Nullable String str) {
        return deserializeBlockResult(str, true);
    }

    public static @Nullable BlockStateParser.BlockResult deserializeBlockResult(@Nullable String str, boolean allowNbt) {
        if (str != null && !str.isEmpty()) {
            StringReader sr = new StringReader(str);
            try {
                return BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), sr, allowNbt);
            } catch (CommandSyntaxException ignored) {
                // handled by outer return null
            }
        }
        return null;
    }

    @Contract("null -> null")
    public static @Nullable BlockState deserializeBlockState(@Nullable String str) {
        BlockStateParser.BlockResult result = deserializeBlockResult(str, false);
        return result == null ? null : result.blockState();
    }

    @Contract("null, _ -> null")
    public static @Nullable Either<BlockStateParser.BlockResult, BlockStateParser.TagResult> deserializeBlockTagResult(@Nullable String str, boolean allowNbt) {
        if (str != null && !str.isEmpty()) {
            StringReader sr = new StringReader(str);
            try {
                return BlockStateParser.parseForTesting(BuiltInRegistries.BLOCK.asLookup(), sr, allowNbt);
            } catch (CommandSyntaxException ignored) {
                // handled by outer return null
            }
        }
        return null;
    }

    @Contract("null -> null")
    public static @Nullable ItemInput deserializeItemInput(@Nullable String str) {
        ItemParser.ItemResult result = deserializeItemResult(str);
        return result == null ? null : new ItemInput(result.item(), result.nbt());
    }

    @Contract("null -> null")
    public static @Nullable ItemParser.ItemResult deserializeItemResult(@Nullable String str) {
        if (str != null && !str.isEmpty()) {
            StringReader sr = new StringReader(str);
            try {
                return ItemParser.parseForItem(BuiltInRegistries.ITEM.asLookup(), sr);
            } catch (CommandSyntaxException ignored) {
                // handled by outer return null
            }
        }
        return null;
    }

    public static ItemStack deserializeItemStack(@Nullable String str) {
        if (str != null && !str.isEmpty()) {
            ItemInput item = deserializeItemInput(str);
            if (item != null) {
                try {
                    return item.createItemStack(1, true);
                } catch (CommandSyntaxException ignored) {
                    // allowOversizedStacks: true, so this should never happen
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Contract("null -> null")
    public static @Nullable Either<ItemParser.ItemResult, ItemParser.TagResult> deserializeItemTagResult(@Nullable String str) {
        if (str != null && !str.isEmpty()) {
            StringReader sr = new StringReader(str);
            try {
                return ItemParser.parseForTesting(BuiltInRegistries.ITEM.asLookup(), sr);
            } catch (CommandSyntaxException ignored) {
                // handled by outer return null
            }
        }
        return null;
    }

    @Contract("null -> null; !null -> !null")
    public static @Nullable String serializeBlockInput(@Nullable BlockInput block) {
        if (block == null) {
            return null;
        }
        return StringBuilderSerialization.serializeBlockInput(null, block).toString();
    }

    @Contract("null -> null; !null -> !null")
    public static @Nullable String serializeBlockResult(@Nullable BlockStateParser.BlockResult result) {
        if (result == null) {
            return null;
        }
        return StringBuilderSerialization.serializeBlockResult(null, result).toString();
    }

    @Contract("null, _ -> null; !null, _ -> !null")
    public static @Nullable String serializeBlockState(@Nullable BlockState state, @Nullable CompoundTag nbt) {
        if (state == null) {
            return null;
        }
        return StringBuilderSerialization.serializeBlockState(null, state, nbt).toString();
    }

    public static String serializeBlockState(BlockState state, Collection<Property<?>> properties, @Nullable CompoundTag nbt) {
        return StringBuilderSerialization.serializeBlockState(null, state, properties, nbt).toString();
    }

    public static <T extends Comparable<T>> String serializeBlockStatePropertyValue(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    @Contract("null -> null; !null -> !null")
    public static @Nullable String serializeBlockTagResult(@Nullable Either<BlockStateParser.BlockResult, BlockStateParser.TagResult> result) {
        if (result == null) {
            return null;
        }
        return StringBuilderSerialization.serializeBlockTagResult(null, result).toString();
    }

    @Contract("null -> null; !null -> !null")
    public static @Nullable String serializeBlockTagResult(@Nullable BlockStateParser.TagResult result) {
        if (result == null) {
            return null;
        }
        return StringBuilderSerialization.serializeBlockTagResult(null, result).toString();
    }

    @Contract("null, _ -> null")
    public static @Nullable String serializeItem(@Nullable Item item, @Nullable CompoundTag nbt) {
        if (item == null) {
            return null;
        }
        return StringBuilderSerialization.serializeItem(null, item, nbt).toString();
    }

    @Contract("null -> null; !null -> !null")
    public static @Nullable String serializeItemInput(@Nullable ItemInput item) {
        if (item == null) {
            return null;
        }
        return StringBuilderSerialization.serializeItemInput(null, item).toString();
    }

    @Contract("null -> null")
    public static @Nullable String serializeItemStack(@Nullable ItemStack item) {
        if (item == null || item == ItemStack.EMPTY) {
            return null;
        }
        return StringBuilderSerialization.serializeItemStack(null, item).toString();
    }
}