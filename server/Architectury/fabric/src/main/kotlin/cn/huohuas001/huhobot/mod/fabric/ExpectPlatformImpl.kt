package cn.huohuas001.huhobot.mod.fabric

import cn.huohuas001.huhobot.mod.ExpectPlatform
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path

class ExpectPlatformImpl {
    /**
     * This is our actual method to [ExpectPlatform.getConfigDirectory].
     */
    fun getConfigDirectory(): Path {
        return FabricLoader.getInstance().configDir
    }

    /**
     * This is our actual method to [ExpectPlatform.getModVersion].
     */
    fun getModVersion(modId: String): String {
        val modContainer = FabricLoader.getInstance().getModContainer(modId)

        if (modContainer.isPresent) {
            val metadata = modContainer.get().metadata
            return metadata.version.friendlyString // 友好格式的版本号（如"1.0.0"）
        }

        return "unknown"
    }
}