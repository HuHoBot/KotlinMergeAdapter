package cn.huohuas001.huhobot.mod

import dev.architectury.injectables.annotations.ExpectPlatform
import java.nio.file.Path

object ExpectPlatform {
    @JvmStatic
    @ExpectPlatform
    fun getConfigDirectory(): Path {
        throw AssertionError()
    }

    @JvmStatic
    @ExpectPlatform
    fun getModVersion(modId: String): String{
        throw AssertionError()
    }
}