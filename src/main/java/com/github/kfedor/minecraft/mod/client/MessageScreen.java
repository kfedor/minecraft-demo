package com.github.kfedor.minecraft.mod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Minimal GUI screen: single-line input + Send/Cancel buttons.
 *
 * <p>On "Send", the screen forwards text to {@link ClientSender}
 * and closes itself. The text length is limited to {@code 256} to match DB schema.</p>
 */
public class MessageScreen extends Screen {

    private static final int FIELD_WIDTH = 220;
    private static final int FIELD_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int GAP_Y = 30;
    private static final int GAP_X = 20;
    private static final int NUDGE_Y = 10;

    /** Maximum length accepted by the screen (must match DB VARCHAR(256)). */
    public static final int MAX_TEXT_LEN = 256;

    private EditBox input;

    public MessageScreen() {
        super(Component.translatable("screen.mfproto.title"));
    }

    /**
     * Lays out all widgets (input field and buttons) relative to screen center.
     * Called by Minecraft when the screen is opened or resized.
     */
    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int fieldX = centerX - FIELD_WIDTH / 2;
        int fieldY = centerY - NUDGE_Y;

        input = new EditBox(this.font, fieldX, fieldY, FIELD_WIDTH, FIELD_HEIGHT,
                Component.translatable("screen.mfproto.input.placeholder"));
        input.setMaxLength(MAX_TEXT_LEN);
        input.setFocused(true);
        addRenderableWidget(input);

        int buttonsY = fieldY + GAP_Y;
        int sendX = centerX - BUTTON_WIDTH - GAP_X / 2;
        int cancelX = centerX + GAP_X / 2;

        Button sendButton = Button.builder(Component.translatable("screen.mfproto.send"), b -> sendAndClose())
                .pos(sendX, buttonsY).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addRenderableWidget(sendButton);

        Button cancelButton = Button.builder(Component.translatable("screen.mfproto.cancel"), b -> onClose())
                .pos(cancelX, buttonsY).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addRenderableWidget(cancelButton);
    }

    private void sendAndClose() {
        String text = input.getValue();
        ClientSender.sendText(text);
        onClose();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
