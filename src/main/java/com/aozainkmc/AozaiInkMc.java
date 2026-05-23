package com.aozainkmc;

import com.aozainkmc.api.AozaiInkApi;
import com.aozainkmc.core.InMemoryInkMarkStore;
import com.aozainkmc.core.AozaiInkItems;
import com.aozainkmc.core.AozaiInkRecipes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(AozaiInkMc.MOD_ID)
public final class AozaiInkMc {
    public static final String MOD_ID = "aozainkmc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public AozaiInkMc(IEventBus modBus) {
        AozaiInkItems.register(modBus);
        AozaiInkRecipes.register(modBus);
        AozaiInkApi.install(new InMemoryInkMarkStore());
        LOGGER.info("Aozai Ink MC core loaded");
    }
}
