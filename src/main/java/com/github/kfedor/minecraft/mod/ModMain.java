package com.github.kfedor.minecraft.mod;

import com.github.kfedor.minecraft.mod.db.DbManager;
import com.github.kfedor.minecraft.mod.network.MessagePayload;
import com.github.kfedor.minecraft.mod.network.Network;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.slf4j.Logger;

/**
 * Common/server entrypoint of the mod.
 *
 * <p>Registers payload types, initializes DB connection pool,
 * and registers server-side network receivers.</p>
 */
public class ModMain implements ModInitializer {

    private static final Logger LOG = LogUtils.getLogger();
    public static final String MOD_ID = "mfproto";

    /**
     * Called by Fabric on server/common startup.
     * <ul>
     *   <li>Registers C2S payload codec</li>
     *   <li>Initializes the database pool and schema</li>
     *   <li>Registers server packet receivers</li>
     * </ul>
     */
    @Override
    public void onInitialize() {
        LOG.info("MF Proto Demo init...");
        PayloadTypeRegistry.playC2S().register(MessagePayload.TYPE, MessagePayload.STREAM_CODEC);
        DbManager.init();
        Network.registerServerReceivers();
        ServerLifecycleEvents.SERVER_STOPPING
                .register(server -> DbManager.close());
    }
}
