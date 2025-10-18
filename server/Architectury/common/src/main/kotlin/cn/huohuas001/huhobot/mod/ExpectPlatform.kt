package cn.huohuas001.huhobot.mod

import dev.architectury.injectables.annotations.ExpectPlatform
import java.nio.file.Path

object ExpectPlatform {
    @ExpectPlatform
    fun getConfigDirectory(): Path {
        throw NotImplementedError()
    }


    @ExpectPlatform
    fun getModVersion(modId: String): String {
        throw NotImplementedError()
    }
}