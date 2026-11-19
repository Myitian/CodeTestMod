package net.myitian.codetest.serialization;

import com.google.gson.JsonElement;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

public class JsonSerialization {
    public static @Nullable Component deserializeComponent(JsonReader reader) throws IOException {
        JsonElement element = TypeAdapters.JSON_ELEMENT.read(reader);
        return Component.Serializer.fromJson(element);
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
                ItemStack stack = StringSerialization.deserializeItemStack(item);
                if (stack != ItemStack.EMPTY) {
                    stack.setCount(count);
                }
                return stack;
            }
            default -> {
                return StringSerialization.deserializeItemStack(reader.nextString());
            }
        }
    }

    public static <T> void deserializeIterable(JsonReader reader, Consumer<T> consumer, FunctionWithIOException<JsonReader, T> deserializer) throws IOException {
        reader.beginArray();
        while (true) {
            switch (reader.peek()) {
                case END_DOCUMENT -> {
                    return;
                }
                case END_ARRAY -> {
                    reader.endArray();
                    return;
                }
                default -> consumer.accept(deserializer.apply(reader));
            }
        }
    }

    public static <T> void deserializeToCollection(
        JsonReader reader,
        Collection<T> collection,
        FunctionWithIOException<JsonReader, T> deserializer,
        boolean clear) throws IOException {
        if (clear) {
            collection.clear();
        }
        deserializeIterable(reader, collection::add, deserializer);
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

    public static void serializeItemStack(JsonWriter writer, @Nullable ItemStack item) throws IOException {
        if (item == null || item == ItemStack.EMPTY) {
            writer.nullValue();
            return;
        }
        String result = StringSerialization.serializeItemStack(item);
        if (item.getCount() <= 1) {
            writer.value(result);
        } else {
            writer.beginArray();
            writer.value(result);
            writer.value(item.getCount());
            writer.endArray();
        }
    }

    public static <T> void serializeIterable(
        JsonWriter writer,
        @Nullable Iterable<T> iterable,
        BiConsumerWithIOException<T, JsonWriter> serializer) throws IOException {
        if (iterable == null) {
            writer.nullValue();
            return;
        }
        writer.beginArray();
        for (T item : iterable) {
            serializer.accept(item, writer);
        }
        writer.endArray();
    }
}