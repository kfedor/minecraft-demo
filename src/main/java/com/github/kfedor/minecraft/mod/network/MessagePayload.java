package com.github.kfedor.minecraft.mod.network;

import com.github.kfedor.minecraft.mod.ModMain;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Custom C2S payload carrying a protobuf-encoded message.
 *
 * <p>Uses a {@code StreamCodec<RegistryFriendlyByteBuf, MessagePayload>}
 * to (de)serialize raw {@code byte[]} with the mod's channel id.</p>
 */
public record MessagePayload(byte[] data) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MessagePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModMain.MOD_ID, "message"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MessagePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BYTE_ARRAY, MessagePayload::data,
                    MessagePayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
