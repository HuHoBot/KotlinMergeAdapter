package cn.huohuas001.huhobot.mod.forge

import cn.huohuas001.huhobot.mod.HuHoBotMod
import dev.architectury.platform.forge.EventBuses
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext

@Mod(HuHoBotMod.MOD_ID)
class ModForge {
    init {

        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(HuHoBotMod.MOD_ID, FMLJavaModLoadingContext.get().modEventBus)
        HuHoBotMod.init()
    }
}