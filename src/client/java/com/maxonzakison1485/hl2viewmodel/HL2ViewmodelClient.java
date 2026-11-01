package com.maxonzakison1485.hl2viewmodel;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Client entrypoint for the Half-Life 2 style viewmodel mod. */
public final class HL2ViewmodelClient implements ClientModInitializer {
    public static final String MOD_ID = "hl2_viewmodel";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("HL2 Viewmodel initialized. Motion system is ready for renderer integration.");
    }
}
