package com.github.kfedor.minecraft.mod.client;

import com.github.kfedor.minecraft.mod.network.MessagePayload;
import mfproto.MessageOuterClass;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Client-side helper to build protobuf messages and send them to the server.
 */
public final class ClientSender {

    private ClientSender() {
    }

    public static void sendText(String text) {
        MessageOuterClass.Message message = MessageOuterClass.Message
                .newBuilder()
                .setText(text == null ? "" : text)
                .build();
        byte[] bytes = message.toByteArray();
        ClientPlayNetworking.send(new MessagePayload(bytes));
    }

}
