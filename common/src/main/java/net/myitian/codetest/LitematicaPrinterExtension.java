package net.myitian.codetest;

import org.apache.commons.lang3.ClassUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LitematicaPrinterExtension {
    public static final Path CONFIG_PATH = PlatformUtil.getConfigDirectory().resolve("codetest--litematica-printer-extension.txt");
    public static final LinkedHashMap<String, Class<?>> interactiveBlocksMap = new LinkedHashMap<>();
    public static Field interactiveBlocks = null;

    public static void reloadConfig() throws IOException {
        try {
            fetchInteractiveBlocks();
        } catch (ReflectiveOperationException e) {
            return;
        }
        try {
            File configFile = CONFIG_PATH.toFile();
            boolean canRead = false;
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                canRead = true;
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    line = line.trim();
                    Class<?> $class = getClassByName(line);
                    interactiveBlocksMap.put(getClassName(line, $class), $class);
                }
                pushInteractiveBlocks();
            } catch (FileNotFoundException e) {
                if (!canRead) {
                    pullInteractiveBlocks();
                }
                saveConfig();
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static void saveConfig() throws IOException {
        File configFile = CONFIG_PATH.toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            for (String $class : interactiveBlocksMap.keySet()) {
                writer.write($class);
                writer.newLine();
            }
        }
    }

    public static Class<?> getClassByName(String name) {
        try {
            return ClassUtils.getClass(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static String getClassName(String source, Class<?> $class) {
        return $class == null ? source : $class.getName();
    }

    public static List<Class<?>> fetchInteractiveBlocks() throws ReflectiveOperationException {
        try {
            if (interactiveBlocks == null) {
                interactiveBlocks = getField();
            }
            return (List<Class<?>>) interactiveBlocks.get(null);
        } catch (ReflectiveOperationException e) {
            interactiveBlocks = null;
            throw e;
        }
    }

    private static Field getField() throws ClassNotFoundException, NoSuchFieldException {
        Class<?> $class;
        try {
            $class = Class.forName("me.aleksilassila.litematica.printer.BlockHelper");
        } catch (ClassNotFoundException e) {
            CodeTest.LOGGER.info("Failed to get class!", e);
            $class = Class.forName("me.aleksilassila.litematica.printer.v1_20.BlockHelper");
        }
        return $class.getField("interactiveBlocks");
    }

    public static Set<Map.Entry<String, Class<?>>> getEntries() {
        return interactiveBlocksMap.entrySet();
    }

    public static void pullInteractiveBlocks() throws ReflectiveOperationException {
        List<Class<?>> result = fetchInteractiveBlocks();
        for (Class<?> $class : result) {
            interactiveBlocksMap.put($class.getName(), $class);
        }
    }

    public static void addInteractiveBlock(String typeName) throws ReflectiveOperationException, IOException {
        pullInteractiveBlocks();
        typeName = typeName.trim();
        Class<?> $class = getClassByName(typeName);
        interactiveBlocksMap.put(getClassName(typeName, $class), $class);
        pushInteractiveBlocks();
    }

    public static void removeInteractiveBlock(String typeName) throws ReflectiveOperationException, IOException {
        pullInteractiveBlocks();
        interactiveBlocksMap.remove(typeName.trim());
        pushInteractiveBlocks();
    }

    public static void pushInteractiveBlocks() throws ReflectiveOperationException, IOException {
        List<Class<?>> result = fetchInteractiveBlocks();
        result.clear();
        for (Class<?> $class : interactiveBlocksMap.values()) {
            if ($class != null) {
                result.add($class);
            }
        }
        saveConfig();
    }
}
