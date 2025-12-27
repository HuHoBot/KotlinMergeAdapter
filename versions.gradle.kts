// 版本矩阵配置文件
// 定义要支持的Minecraft版本和对应的依赖版本

object VersionMatrix {
    // 定义支持的Minecraft版本
    val supportedVersions = listOf(
        "1.20.4",
        "1.21.1"
    )
    
    // Minecraft版本到依赖版本的映射
    val dependencies = mapOf(
        "1.20.4" to mapOf(
            "architectury_version" to "11.1.17",
            "forge_version" to "1.20.4-49.2.0",
            "neoforge_version" to "20.4.251",
            "fabric_loader_version" to "0.16.5",
            "fabric_api_version" to "0.92.6+1.20.1",
            "kotlin_for_forge_version" to "4.3.0"
        ),
        "1.21.1" to mapOf(
            "architectury_version" to "13.0.8",
            "forge_version" to "1.21.1-52.0.0",
            "neoforge_version" to "21.1.209",
            "fabric_loader_version" to "0.16.5",
            "fabric_api_version" to "0.105.2+1.21",
            "kotlin_for_forge_version" to "5.1.0"
        )
    )
}
