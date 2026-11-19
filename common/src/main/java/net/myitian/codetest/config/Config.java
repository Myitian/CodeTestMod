package net.myitian.codetest.config;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.myitian.codetest.CodeTest;
import net.myitian.codetest.screen.GameModeSwitcherScreen;
import net.myitian.codetest.serialization.ConsumerWithIOException;
import net.myitian.codetest.serialization.JsonSerialization;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class Config {
    public static final List<GameModeSwitcherScreen.GameModeIcon> defaultGameModes = List.of(
        new GameModeSwitcherScreen.GameModeIcon(
            GameType.CREATIVE,
            Component.translatable("gameMode.creative"),
            "gamemode creative",
            new ItemStack(Blocks.GRASS_BLOCK)),
        new GameModeSwitcherScreen.GameModeIcon(
            GameType.SURVIVAL,
            Component.translatable("gameMode.survival"),
            "gamemode survival",
            new ItemStack(Items.IRON_SWORD)),
        new GameModeSwitcherScreen.GameModeIcon(
            GameType.ADVENTURE,
            Component.translatable("gameMode.adventure"),
            "gamemode adventure",
            new ItemStack(Items.MAP)),
        new GameModeSwitcherScreen.GameModeIcon(
            GameType.SPECTATOR,
            Component.translatable("gameMode.spectator"),
            "gamemode spectator",
            new ItemStack(Items.ENDER_EYE))
    );
    public static final ArrayList<GameModeSwitcherScreen.GameModeIcon> gameModes = new ArrayList<>(defaultGameModes);
    private static final ConfigCodec CODEC = new ConfigCodec();
    public static boolean enhancedCursorEnabled = true;
    public static boolean enhancedMatchingEnabled = true;
    public static boolean enhancedSchematicPastingEnabled = true;
    public static boolean noAutoCreateWorldScreen = true;
    public static boolean blockInfoCommandEnabled = true;
    public static boolean cameraInfoCommandEnabled = true;
    public static boolean gcCommandEnabled = true;
    public static boolean glfwCommandEnabled = true;
    public static boolean hitTestCommandEnabled = true;
    public static boolean litematicaPrinterCommandEnabled = true;
    public static boolean reflectionQueryCommandEnabled = true;
    public static boolean offlineIntegratedServer = true;
    public static boolean customGameModeSwitcherScreen = true;

    static {
        registerCodec(CODEC.getFieldMap());
    }

    public static boolean load(File configFile) {
        try (var reader = new JsonReader(new FileReader(configFile))) {
            reader.setLenient(true);
            return CODEC.deserialize(reader);
        } catch (Exception e) {
            CodeTest.LOGGER.info("Failed to read config: {}", e.getLocalizedMessage());
        }
        return false;
    }

    public static void registerCodec(Map<String, Pair<ConsumerWithIOException<JsonReader>, ConsumerWithIOException<JsonWriter>>> map) {
        map.put("enhancedCursorEnabled", Pair.of(
            reader -> enhancedCursorEnabled = reader.nextBoolean(),
            writer -> writer.value(enhancedCursorEnabled)));
        map.put("enhancedMatchingEnabled", Pair.of(
            reader -> enhancedMatchingEnabled = reader.nextBoolean(),
            writer -> writer.value(enhancedMatchingEnabled)));
        map.put("enhancedSchematicPastingEnabled", Pair.of(
            reader -> enhancedSchematicPastingEnabled = reader.nextBoolean(),
            writer -> writer.value(enhancedSchematicPastingEnabled)));
        map.put("noAutoCreateWorldScreen", Pair.of(
            reader -> noAutoCreateWorldScreen = reader.nextBoolean(),
            writer -> writer.value(noAutoCreateWorldScreen)));
        map.put("blockInfoCommandEnabled", Pair.of(
            reader -> blockInfoCommandEnabled = reader.nextBoolean(),
            writer -> writer.value(blockInfoCommandEnabled)));
        map.put("cameraInfoCommandEnabled", Pair.of(
            reader -> cameraInfoCommandEnabled = reader.nextBoolean(),
            writer -> writer.value(cameraInfoCommandEnabled)));
        map.put("gcCommandEnabled", Pair.of(
            reader -> gcCommandEnabled = reader.nextBoolean(),
            writer -> writer.value(gcCommandEnabled)));
        map.put("glfwCommandEnabled", Pair.of(
            reader -> glfwCommandEnabled = reader.nextBoolean(),
            writer -> writer.value(glfwCommandEnabled)));
        map.put("hitTestCommandEnabled", Pair.of(
            reader -> hitTestCommandEnabled = reader.nextBoolean(),
            writer -> writer.value(hitTestCommandEnabled)));
        map.put("litematicaPrinterCommandEnabled", Pair.of(
            reader -> litematicaPrinterCommandEnabled = reader.nextBoolean(),
            writer -> writer.value(litematicaPrinterCommandEnabled)));
        map.put("reflectionQueryCommandEnabled", Pair.of(
            reader -> reflectionQueryCommandEnabled = reader.nextBoolean(),
            writer -> writer.value(reflectionQueryCommandEnabled)));
        map.put("offlineIntegratedServer", Pair.of(
            reader -> offlineIntegratedServer = reader.nextBoolean(),
            writer -> writer.value(offlineIntegratedServer)));
        map.put("customGameModeSwitcherScreen", Pair.of(
            reader -> customGameModeSwitcherScreen = reader.nextBoolean(),
            writer -> writer.value(customGameModeSwitcherScreen)));
        map.put("gameModes", Pair.of(
            reader -> JsonSerialization.deserializeToCollection(reader, gameModes, GameModeSwitcherScreen.GameModeIcon::deserialize, true),
            writer -> JsonSerialization.serializeIterable(writer, gameModes, GameModeSwitcherScreen.GameModeIcon::serialize)));
    }

    public static void reset() {
        setGameModes(defaultGameModes);
        enhancedCursorEnabled = true;
        enhancedMatchingEnabled = true;
        enhancedSchematicPastingEnabled = true;
        noAutoCreateWorldScreen = true;
        blockInfoCommandEnabled = true;
        cameraInfoCommandEnabled = true;
        gcCommandEnabled = true;
        glfwCommandEnabled = true;
        hitTestCommandEnabled = true;
        litematicaPrinterCommandEnabled = true;
        reflectionQueryCommandEnabled = true;
        offlineIntegratedServer = true;
        customGameModeSwitcherScreen = true;
    }

    public static boolean save(File configFile) {
        try (var writer = new JsonWriter(new FileWriter(configFile))) {
            writer.setHtmlSafe(false);
            writer.setIndent("  ");
            return save(writer);
        } catch (Exception e) {
            CodeTest.LOGGER.warn("Failed to write config: {}", e.getLocalizedMessage());
        }
        return false;
    }

    public static boolean save(JsonWriter writer) throws IOException {
        return CODEC.serialize(writer);
    }

    public static void setGameModes(Collection<GameModeSwitcherScreen.GameModeIcon> gameModes) {
        Config.gameModes.clear();
        Config.gameModes.addAll(gameModes);
    }
}