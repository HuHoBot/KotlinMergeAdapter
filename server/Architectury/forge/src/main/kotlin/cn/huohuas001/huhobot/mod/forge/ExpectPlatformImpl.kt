package cn.huohuas001.huhobot.mod.forge

import net.minecraftforge.fml.ModList
import cn.huohuas001.huhobot.mod.ExpectPlatform
import net.minecraftforge.fml.loading.FMLPaths
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
