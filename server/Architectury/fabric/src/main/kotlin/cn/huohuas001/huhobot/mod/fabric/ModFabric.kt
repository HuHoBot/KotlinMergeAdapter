package cn.huohuas001.huhobot.mod.fabric

import cn.huohuas001.huhobot.mod.HuHoBotMod
import net.fabricmc.api.ModInitializer

class ModFabric: ModInitializer {
    override fun onInitialize() {
        HuHoBotMod.init()
    }
}