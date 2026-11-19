package net.myitian.codetest.serialization;

import com.mojang.datafixers.util.Either;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.myitian.codetest.StringBuilderTagVisitor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class StringBuilderSerialization {
    @Contract("null, _ -> new; !null, _ -> param1")
    public static StringBuilder serializeBlockInput(
        @Nullable StringBuilder sb,
        @Nullable BlockInput input) {
        BlockState state = null;
        Collection<Property<?>> prop = null;
        CompoundTag nbt = null;
        if (input != null) {
            state = input.getState();
            prop = input.getDefinedProperties();
            nbt = input.tag;
        }
        return serializeBlockState(sb, state, prop, nbt);
    }

    @Contract("null, _ -> new; !null, _ -> param1")
    public static StringBuilder serializeBlockResult(
        @Nullable StringBuilder sb,
        @Nullable BlockStateParser.BlockResult result) {
        BlockState state = null;
        Collection<Property<?>> prop = null;
        CompoundTag nbt = null;
        if (result != null) {
            state = result.blockState();
            prop = result.properties().keySet();
            nbt = result.nbt();
        }
        return serializeBlockState(sb, state, prop, nbt);
    }

    @Contract("null, _, _ -> new; !null, _, _ -> param1")
    public static StringBuilder serializeBlockState(
        @Nullable StringBuilder sb,
        @Nullable BlockState state,
        @Nullable CompoundTag nbt) {
        return serializeBlockState(sb, state, state == null ? null : state.getProperties(), nbt);
    }

    @Contract("null, _, _, _ -> new; !null, _, _, _ -> param1")
    public static StringBuilder serializeBlockState(
        @Nullable StringBuilder sb,
        @Nullable BlockState state,
        @Nullable Collection<Property<?>> properties,
        @Nullable CompoundTag nbt) {
        ResourceLocation id = null;
        Collection<Map.Entry<String, String>> prop = null;
        if (state != null) {
            id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (properties != null && !properties.isEmpty()) {
                prop = new BlockStatePropertyValueAccessor(state, properties);
            }
        }
        return serializeResourceLocationWithAdditionalData(sb, null, id, prop, nbt);
    }

    @Contract("null, _ -> new; !null, _ -> param1")
    public static StringBuilder serializeBlockTagResult(
        @Nullable StringBuilder sb,
        @Nullable Either<BlockStateParser.BlockResult, BlockStateParser.TagResult> result) {
        if (result != null) {
            Optional<BlockStateParser.BlockResult> blockResult = result.left();
            if (blockResult.isPresent()) {
                return serializeBlockResult(sb, blockResult.get());
            } else {
                return serializeBlockTagResult(sb, result.right().orElse(null));
            }
        }
        return new StringBuilder();
    }

    @Contract("null, _ -> new; !null, _ -> param1")
    public static StringBuilder serializeBlockTagResult(
        @Nullable StringBuilder sb,
        @Nullable BlockStateParser.TagResult result) {
        ResourceLocation id = null;
        Collection<Map.Entry<String, String>> prop = null;
        CompoundTag nbt = null;
        if (result != null) {
            id = result.tag().unwrapKey().map(TagKey::location).orElse(null);
            prop = result.vagueProperties().entrySet();
            nbt = result.nbt();
        }
        return serializeResourceLocationWithAdditionalData(sb, "#", id, prop, nbt);
    }

    @Contract("null, _, _ -> new; !null, _, _ -> param1")
    public static StringBuilder serializeItem(
        @Nullable StringBuilder sb,
        @Nullable Item item,
        @Nullable CompoundTag nbt) {
        ResourceLocation id = item == null ? null : BuiltInRegistries.ITEM.getKey(item);
        return serializeResourceLocationWithAdditionalData(sb, null, id, null, nbt);
    }

    @Contract("null, _ -> new; !null, _ -> param1")
    public static StringBuilder serializeItemInput(
        @Nullable StringBuilder sb,
        @Nullable ItemInput input) {
        Item item = null;
        CompoundTag nbt = null;
        if (input != null) {
            item = input.getItem();
            nbt = input.tag;
        }
        return serializeItem(sb, item, nbt);
    }

    @Contract("null, _ -> new; !null, _ -> param1")
    public static StringBuilder serializeItemResult(
        @Nullable StringBuilder sb,
        @Nullable ItemParser.ItemResult result) {
        ResourceLocation id = null;
        CompoundTag nbt = null;
        if (result != null) {
            id = result.item().unwrapKey().map(ResourceKey::location).orElse(null);
            nbt = result.nbt();
        }
        return serializeResourceLocationWithAdditionalData(sb, null, id, null, nbt);
    }

    @Contract("null, _ -> new; !null, _ -> param1")
    public static StringBuilder serializeItemStack(
        @Nullable StringBuilder sb,
        @Nullable ItemStack stack) {
        Item item = null;
        CompoundTag nbt = null;
        if (stack != null && stack != ItemStack.EMPTY) {
            item = stack.getItem();
            nbt = stack.hasTag() ? stack.getTag() : null;
        }
        return serializeItem(sb, item, nbt);
    }

    @Contract("null, _ -> new; !null, _ -> param1")
    public static StringBuilder serializeItemTagResult(
        @Nullable StringBuilder sb,
        @Nullable ItemParser.TagResult result) {
        ResourceLocation id = null;
        CompoundTag nbt = null;
        if (result != null) {
            id = result.tag().unwrapKey().map(TagKey::location).orElse(null);
            nbt = result.nbt();
        }
        return serializeResourceLocationWithAdditionalData(sb, "#", id, null, nbt);
    }

    @Contract("null, _, _, _, _ -> new; !null, _, _, _, _ -> param1")
    public static StringBuilder serializeResourceLocationWithAdditionalData(
        @Nullable StringBuilder sb,
        @Nullable String prefix,
        @Nullable ResourceLocation id,
        @Nullable Collection<Map.Entry<String, String>> properties,
        @Nullable CompoundTag nbt) {
        if (sb == null) {
            sb = new StringBuilder();
        }
        if (prefix != null) {
            sb.append(prefix);
        }
        if (id != null) {
            sb.append(id.getNamespace())
                .append(':')
                .append(id.getPath());
            if (properties != null && !properties.isEmpty()) {
                sb.append('[');
                boolean first = true;
                for (Map.Entry<String, String> entry : properties) {
                    if (!first) {
                        sb.append(',');
                    }
                    first = false;
                    sb.append(entry.getKey())
                        .append('=')
                        .append(entry.getValue());
                }
                sb.append(']');
            }
            if (nbt != null) {
                StringBuilderTagVisitor visitor = new StringBuilderTagVisitor(sb);
                nbt.accept(visitor);
            }
        }
        return sb;
    }

    static final class BlockStatePropertyValueAccessor extends AbstractCollection<Map.Entry<String, String>> {
        private final BlockState state;
        private final Collection<Property<?>> properties;

        public BlockStatePropertyValueAccessor(BlockState state, Collection<Property<?>> properties) {
            this.state = state;
            this.properties = properties;
        }

        @Override
        public boolean isEmpty() {
            return properties.isEmpty();
        }

        @Override
        public Iterator<Map.Entry<String, String>> iterator() {
            return new IteratorImpl(state, properties);
        }

        @Override
        public int size() {
            return properties.size();
        }

        static final class IteratorImpl implements Iterator<Map.Entry<String, String>> {
            private final BlockState state;
            private final Iterator<Property<?>> propertyIterator;

            public IteratorImpl(BlockState state, Collection<Property<?>> properties) {
                this.state = state;
                propertyIterator = properties.iterator();
            }

            @Override
            public boolean hasNext() {
                return propertyIterator.hasNext();
            }

            @Override
            public Map.Entry<String, String> next() {
                Property<?> property = propertyIterator.next();
                String name = property.getName();
                String value = StringSerialization.serializeBlockStatePropertyValue(state, property);
                return new AbstractMap.SimpleEntry<>(name, value);
            }
        }
    }
}