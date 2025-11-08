package com.github.kfedor.minecraft.mod;

import com.github.kfedor.minecraft.mod.client.MessageScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
/**
 * Client-only entrypoint of the mod.
 *
 * <p>Registers keybinding and opens the message screen on demand.</p>
 */
public class ModClient implements ClientModInitializer {

    private static KeyMapping openScreenKey;

    /**
     * Called by Fabric on client startup:
     * <ul>
     *   <li>Registers a keybinding to open {@link MessageScreen}</li>
     *   <li>Handles client tick to react to key presses</li>
     * </ul>
     */
    @Override
    public void onInitializeClient() {
        openScreenKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.mfproto.open_screen", GLFW.GLFW_KEY_M, "key.categories.mfproto"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openScreenKey.consumeClick()) {
                if (client.player != null) {
                    client.setScreen(new MessageScreen());
                }
            }
        });
    }
}
