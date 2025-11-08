package com.github.kfedor.minecraft.mod;

import org.junit.jupiter.api.Test;

import static mfproto.MessageOuterClass.Message;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProtobufTest {

    @Test
    void messageRoundTripOk() throws Exception {
        Message original = Message.newBuilder()
                .setText("Hello Minecraft!")
                .build();

        byte[] bytes = original.toByteArray();
        Message decoded = Message.parseFrom(bytes);

        assertEquals("Hello Minecraft!", decoded.getText());
    }

}