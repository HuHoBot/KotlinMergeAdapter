package cn.huohuas001.huhobot.mod.neoforge

import cn.huohuas001.huhobot.mod.ExpectPlatform
import net.neoforged.fml.loading.FMLPaths
import net.neoforged.fml.ModList
import java.nio.file.Path

object ExpectPlatformImpl{
    /**
     * This is our actual method to [ExpectPlatform.getConfigDirectory].
     */
    @JvmStatic // Jvm Static is required so that java can access it
    fun getConfigDirectory(): Path {
        return FMLPaths.CONFIGDIR.get()
    }

    /**
     * This is our actual method to [ExpectPlatform.getModVersion].
     */
    @JvmStatic // Jvm Static is required so that java can access it
    fun getModVersion(modId: String): String {
        val container = ModList.get().getModContainerById(modId)
        return if (container.isPresent) {
            container.get().modInfo.version.toString()
        } else {
            "unknown"
        }
    }
}