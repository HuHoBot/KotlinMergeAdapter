// fabric/src/main/kotlin/cn/huohuas001/huhobot/mod/fabric/ExpectPlatformImpl.kt
package cn.huohuas001.huhobot.mod.fabric

import cn.huohuas001.huhobot.mod.ExpectPlatform
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path

object ExpectPlatformImpl{
    /**
     * This is our actual method to [ExpectPlatform.getConfigDirectory].
     */
    @JvmStatic // Jvm Static is required so that java can access it
    fun getConfigDirectory(): Path {
        return FabricLoader.getInstance().configDir
    }

    /**
     * This is our actual method to [ExpectPlatform.getModVersion].
     */
    @JvmStatic // Jvm Static is required so that java can access it
    fun getModVersion(modId: String): String {
        val modContainer = FabricLoader.getInstance().getModContainer(modId)
        if (modContainer.isPresent) {
            val metadata = modContainer.get().metadata
            return metadata.version.friendlyString // 获取友好格式的版本号
        }
        return "unknown"
    }
}
