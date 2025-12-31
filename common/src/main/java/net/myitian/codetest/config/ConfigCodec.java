package net.myitian.codetest.config;

import com.google.gson.JsonElement;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.myitian.codetest.StringBuilderTagVisitor;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.*;

public class ConfigCodec {
    private final LinkedHashMap<String, Pair<ConsumerWithIOException<JsonReader>, ConsumerWithIOException<JsonWriter>>> fieldMap = new LinkedHashMap<>();

    public static Component deserializeComponent(JsonReader reader) throws IOException {
        JsonElement element = TypeAdapters.JSON_ELEMENT.read(reader);
        return Component.Serializer.fromJson(element);
    }

    public static void serializeComponent(JsonWriter writer, Component component) throws IOException {
        if (component.getStyle().isEmpty() && component.getSiblings().isEmpty()) {
            ComponentContents contents = component.getContents();
            // simplify to a JSON String when the Component is literal and no style or siblings.
            if (contents == ComponentContents.EMPTY) {
                writer.value("");
                return;
            } else if (contents instanceof LiteralContents literal) {
                writer.value(literal.text());
                return;
            }
        }
        TypeAdapters.JSON_ELEMENT.write(writer, Component.Serializer.toJsonTree(component));
    }

    public static ItemStack deserializeItemStack(JsonReader reader) throws IOException {
        switch (reader.peek()) {
            case NULL -> {
                reader.nextNull();
                return ItemStack.EMPTY;
            }
            case BEGIN_ARRAY -> {
                String item = reader.nextString();
                int count = reader.nextInt();
                reader.endArray();
                return getItemStack(item, count);
            }
            default -> {
                return getItemStack(reader.nextString(), 1);
            }
        }
    }

    public static <T> void deserializeCollection(JsonReader reader, Collection<T> collection, FunctionWithIOException<JsonReader, T> deserializer) throws IOException {
        reader.beginArray();
        collection.clear();
        while (true) {
            switch (reader.peek()) {
                case END_DOCUMENT -> {
                    return;
                }
                case END_ARRAY -> {
                    reader.endArray();
                    return;
                }
                default -> collection.add(deserializer.apply(reader));
            }
        }
    }

    public static <T extends Serializable> void serializeCollection(JsonWriter writer, Collection<T> collection) throws IOException {
        writer.beginArray();
        for (T item : collection) {
            item.serialize(writer);
        }
        writer.endArray();
    }

    public static void serializeItemStack(JsonWriter writer, ItemStack item) throws IOException {
        if (item == ItemStack.EMPTY) {
            writer.nullValue();
            return;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item.getItem());
        StringBuilder sb = new StringBuilder()
            .append(id.getNamespace())
            .append(':')
            .append(id.getPath());
        StringBuilderTagVisitor visitor = new StringBuilderTagVisitor(sb);
        if (item.hasTag()) {
            assert item.getTag() != null;
            item.getTag().accept(visitor);
        }
        String result = visitor.toString();
        if (item.getCount() <= 1) {
            writer.value(result);
        } else {
            writer.beginArray();
            writer.value(result);
            writer.value(item.getCount());
            writer.endArray();
        }
    }

    private static ItemStack getItemStack(String string, int count) {
        StringReader sr = new StringReader(string);
        try {
            ItemParser.ItemResult result = ItemParser.parseForItem(BuiltInRegistries.ITEM.asLookup(), sr);
            ItemInput item = new ItemInput(result.item(), result.nbt());
            return item.createItemStack(count, true);
        } catch (CommandSyntaxException e) {
            return ItemStack.EMPTY;
        }
    }

    public Map<String, Pair<ConsumerWithIOException<JsonReader>, ConsumerWithIOException<JsonWriter>>> getFieldMap() {
        return fieldMap;
    }

    public boolean deserialize(JsonReader reader) throws IOException {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            return false;
        }
        reader.beginObject();
        Set<String> nameSet = new HashSet<>(fieldMap.size());
        while (reader.peek() == JsonToken.NAME) {
            String name = reader.nextName();
            var pair = fieldMap.get(name);
            if (pair != null) {
                nameSet.add(name);
                pair.getLeft().accept(reader);
            } else {
                reader.skipValue();
            }
        }
        return nameSet.size() == fieldMap.size();
    }

    public boolean serialize(JsonWriter writer) throws IOException {
        writer.beginObject();
        for (var fieldInfo : fieldMap.entrySet()) {
            writer.name(fieldInfo.getKey());
            fieldInfo.getValue().getRight().accept(writer);
        }
        writer.endObject();
        return true;
    }

    @FunctionalInterface
    public interface ConsumerWithIOException<T> {
        void accept(T t) throws IOException;
    }

    @FunctionalInterface
    public interface FunctionWithIOException<T, R> {
        R apply(T t) throws IOException;
    }

    public interface Serializable {
        void serialize(JsonWriter writer) throws IOException;
    }
}