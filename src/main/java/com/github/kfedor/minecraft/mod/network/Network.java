package com.github.kfedor.minecraft.mod.network;

import com.github.kfedor.minecraft.mod.ModMain;
import com.github.kfedor.minecraft.mod.db.DbManager;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.UUID;

import static mfproto.MessageOuterClass.Message;

/**
 * Server-side networking glue:
 * registers C2S receivers and routes incoming payloads to the DB layer.
 */
public final class Network {

    private static final Logger LOG = LogUtils.getLogger();

    private Network() {
    }

    /**
     * Registers all server-side receivers (called once from {@link ModMain#onInitialize()}).
     *
     * <p>Handler decodes protobuf bytes to {@code mfproto.MessageOuterClass.Message},
     * extracts player UUID and message text, then delegates persistence to {@link DbManager}.</p>
     */
    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(
                MessagePayload.TYPE,
                (payload, context) -> {
                    ServerPlayer player = context.player();
                    MinecraftServer server = player.getServer();
                    if (server == null) {
                        LOG.warn("Server is null for player {}", player.getGameProfile().getName());
                        return;
                    }

                    final byte[] data = payload.data();
                    UUID playerId = player.getGameProfile().getId();

                    server.execute(() -> {
                        try {
                            Message message = Message.parseFrom(data);
                            String text = message.getText();
                            LOG.debug("Received message ({} bytes) from {}: \"{}\"",
                                    data.length, playerId, text);
                            if (text.length() > 256) {
                                text = text.substring(0, 256);
                            }
                            DbManager.insertMessage(playerId, text);
                        } catch (InvalidProtocolBufferException exception) {
                            LOG.warn("Bad protobuf from {} ({} bytes)", playerId, data.length, exception);
                        } catch (Exception exception) {
                            LOG.error("Failed to handle message from {}", playerId, exception);
                        }
                    });
                }
        );
    }
}
