package net.myitian.codetest.screen;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.myitian.codetest.config.Config;
import net.myitian.codetest.config.ConfigCodec;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;

public class GameModeSwitcherScreen extends Screen {
    public static final ResourceLocation SLOT_SPRITE = ResourceLocation.tryParse("gamemode_switcher/slot");
    public static final ResourceLocation SELECTION_SPRITE = ResourceLocation.tryParse("gamemode_switcher/selection");
    public static final ResourceLocation BASE_TEXTURE = ResourceLocation.tryParse("textures/gui/container/gamemode_switcher.png");
    public static final int SLOT_SIZE = 26;
    public static final int SLOT_PADDING = 5;
    public static final int SLOT_PADDED = SLOT_SIZE + SLOT_PADDING;
    public static final int TEXTURE_SIZE = 128;
    public static final int BACKGROUND_WIDTH = 125;
    public static final int BACKGROUND_HEIGHT = 75;
    public static final Component SELECT_KEY = Component.translatable("debug.gamemodes.select_next", Component.translatable("debug.gamemodes.press_f4").withStyle(ChatFormatting.AQUA));
    public static final boolean MC_23w31a_OR_ABOVE = SharedConstants.getCurrentVersion().getDataVersion().getVersion() >= 3567;
    private final GameModeIcon[] gameModes = Config.gameModes.toArray(new GameModeIcon[0]);
    private final GameModeSlot[] slots = new GameModeSlot[gameModes.length];
    private int current = -1;
    private int firstMouseX;
    private int firstMouseY;
    private boolean setFirstMousePos;

    public GameModeSwitcherScreen() {
        super(GameNarrator.NO_TITLE);
        GameType defaultGameType = getDefaultSelected();
        for (int i = 0; i < gameModes.length; ++i) {
            if (gameModes[i].getType() == defaultGameType) {
                current = i;
                break;
            }
        }
    }

    public static GameType getDefaultSelected() {
        MultiPlayerGameMode multiPlayerGameMode = Minecraft.getInstance().gameMode;
        if (multiPlayerGameMode != null) {
            GameType gameType = multiPlayerGameMode.getPreviousPlayerMode();
            if (gameType != null) {
                return gameType;
            } else if (multiPlayerGameMode.getPlayerMode() == GameType.CREATIVE) {
                return GameType.SURVIVAL;
            }
        }
        return GameType.CREATIVE;
    }

    protected void init() {
        super.init();
        int totalWidth = gameModes.length * SLOT_PADDED - SLOT_PADDING;
        for (int i = 0; i < gameModes.length; i++) {
            slots[i] = new GameModeSlot(gameModes[i],
                width / 2 - totalWidth / 2 + i * SLOT_PADDED,
                height / 2 - SLOT_PADDED);
        }
    }

    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        if (minecraft != null && !InputConstants.isKeyDown(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_F3)) {
            if (current != -1 && minecraft.gameMode != null && minecraft.player != null) {
                String command = gameModes[current].getCommand();
                if (command != null) {
                    minecraft.player.connection.sendCommand(command);
                }
            }
            minecraft.setScreen(null);
            return;
        }
        context.pose().pushPose();
        RenderSystem.enableBlend();
        int halfWidth = width / 2;
        int halfHeight = height / 2;
        context.blit(BASE_TEXTURE, halfWidth - BACKGROUND_WIDTH / 2, halfHeight - 58, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        context.pose().popPose();
        super.render(context, mouseX, mouseY, partialTick);
        GameModeIcon currentIcon = getCurrentIcon();
        if (currentIcon != null) {
            context.drawCenteredString(font, currentIcon.getName(), halfWidth, halfHeight - 51, CommonColors.WHITE);
        }
        context.drawCenteredString(font, SELECT_KEY, halfWidth, halfHeight + 5, 0x00ffffff);
        if (!setFirstMousePos) {
            firstMouseX = mouseX;
            firstMouseY = mouseY;
            setFirstMousePos = true;
        }

        boolean mouseNotMoved = firstMouseX == mouseX && firstMouseY == mouseY;
        for (int i = 0; i < slots.length; i++) {
            GameModeSlot slot = slots[i];
            slot.render(context, mouseX, mouseY, partialTick);
            slot.setSelected(getCurrentIcon() == slot.icon);
            if (!mouseNotMoved && slot.isHoveredOrFocused()) {
                current = i;
            }
        }
    }

    private GameModeIcon getCurrentIcon() {
        return current == -1 ? null : gameModes[current];
    }

    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_F4) {
            setFirstMousePos = false;
            current = gameModes.length == 0 ? -1 : (current + 1) % gameModes.length;
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public boolean isPauseScreen() {
        return false;
    }

    public static class GameModeIcon implements ConfigCodec.Serializable {
        GameType type;
        Component name;
        String command;
        ItemStack renderItem;

        public GameModeIcon() {
            this(null, null, null, null);
        }

        public GameModeIcon(GameType type, Component name, String command, ItemStack renderItem) {
            setType(type);
            setName(name);
            setCommand(command);
            setRenderItem(renderItem);
        }

        public static GameModeIcon deserialize(JsonReader reader) throws IOException {
            reader.beginObject();
            GameModeIcon selection = new GameModeIcon();
            PARSE:
            while (true) {
                switch (reader.peek()) {
                    case END_OBJECT -> {
                        reader.endObject();
                        break PARSE;
                    }
                    case END_DOCUMENT -> {
                        break PARSE;
                    }
                    default -> {
                        switch (reader.nextName()) {
                            case "type" -> {
                                if (reader.peek() == JsonToken.NULL) {
                                    reader.nextNull();
                                    selection.setType(null);
                                } else {
                                    selection.setType(GameType.byName(reader.nextString(), null));
                                }
                            }
                            case "name" -> selection.setName(ConfigCodec.deserializeComponent(reader));
                            case "command" -> {
                                if (reader.peek() == JsonToken.NULL) {
                                    reader.nextNull();
                                    selection.setCommand(null);
                                } else {
                                    selection.setCommand(reader.nextString());
                                }
                            }
                            case "item" -> selection.setRenderItem(ConfigCodec.deserializeItemStack(reader));
                            default -> reader.skipValue();
                        }
                    }
                }
            }
            return selection;
        }

        public GameType getType() {
            return type;
        }

        public void setType(GameType type) {
            this.type = type;
        }

        public Component getName() {
            return name;
        }

        public void setName(Component name) {
            this.name = name == null ? CommonComponents.EMPTY : name;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public ItemStack getRenderItem() {
            return renderItem;
        }

        public void setRenderItem(ItemStack renderItem) {
            this.renderItem = renderItem == null ? ItemStack.EMPTY : renderItem;
        }

        public void serialize(JsonWriter writer) throws IOException {
            writer.beginObject();
            writer.name("type");
            if (type == null) {
                writer.nullValue();
            } else {
                writer.value(type.getName());
            }
            writer.name("name");
            ConfigCodec.serializeComponent(writer, name);
            writer.name("command");
            writer.value(command);
            writer.name("item");
            ConfigCodec.serializeItemStack(writer, renderItem);
            writer.endObject();
        }
    }

    public static class GameModeSlot extends AbstractWidget {
        final GameModeIcon icon;
        private boolean isSelected;

        public GameModeSlot(GameModeIcon icon, int x, int y) {
            super(x, y, SLOT_SIZE, SLOT_SIZE, icon.getName());
            this.icon = icon;
        }

        public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
            drawSlot(context);
            context.renderItem(icon.getRenderItem(), getX() + SLOT_PADDING, getY() + SLOT_PADDING);
            if (isSelected) {
                drawSelection(context);
            }

        }

        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            defaultButtonNarrationText(narrationElementOutput);
        }

        public boolean isHoveredOrFocused() {
            return super.isHoveredOrFocused() || isSelected;
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }

        private void drawSlot(GuiGraphics context) {
            if (MC_23w31a_OR_ABOVE) {
                context.blitSprite(SLOT_SPRITE, getX(), getY(), SLOT_SIZE, SLOT_SIZE);
            } else {
                context.blit(BASE_TEXTURE, getX(), getY(), 0, BACKGROUND_HEIGHT, SLOT_SIZE, SLOT_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
            }
        }


        private void drawSelection(GuiGraphics context) {
            if (MC_23w31a_OR_ABOVE) {
                context.blitSprite(SELECTION_SPRITE, getX(), getY(), SLOT_SIZE, SLOT_SIZE);
            } else {
                context.blit(BASE_TEXTURE, getX(), getY(), SLOT_SIZE, BACKGROUND_HEIGHT, SLOT_SIZE, SLOT_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
            }
        }
    }
}
