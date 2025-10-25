plugins {
    id("com.github.johnrengelman.shadow")
}

architectury {
    platformSetupLoomIde()
    forge()
}

loom {
    accessWidenerPath.set(project(":server-Architectury:common").loom.accessWidenerPath)

    forge.apply {
        convertAccessWideners.set(true)
        extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
    }
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
val developmentForge: Configuration by configurations.getting

configurations {
    getByName("compileClasspath").extendsFrom(common)
    getByName("runtimeClasspath").extendsFrom(common)
    getByName("developmentForge").extendsFrom(common)
}

repositories {
    // KFF
    mavenCentral()
    maven {
        name = "Kotlin for Forge"
        setUrl("https://thedarkcolour.github.io/KotlinForForge/")
    }
}

dependencies {
    forge("net.minecraftforge:forge:${project.property("forge_version")}")
    // Remove the next line if you don't want to depend on the API
    modApi("dev.architectury:architectury-forge:${project.property("architectury_version")}")

    common(project(":server-Architectury:common", "namedElements")) { isTransitive = false }
    shadowCommon(project(":server-Architectury:common", "transformProductionForge")) { isTransitive = false }
    shadowCommon("org.yaml:snakeyaml:2.5")

    shadowCommon("io.ktor:ktor-client-websockets:2.3.10")
    shadowCommon("io.ktor:ktor-client-cio:2.3.10")
    shadowCommon("io.ktor:ktor-client-core:2.3.10")

    shadowCommon("com.alibaba.fastjson2:fastjson2:2.0.52")
    shadowCommon(project(":common-Bot")) { isTransitive = false }

    // Kotlin For Forge
    implementation("thedarkcolour:kotlinforforge:${project.property("kotlin_for_forge_version")}")
}

tasks.processResources {
    inputs.property("group", project.property("maven_group"))
    inputs.property("version", project.version)

    filesMatching("META-INF/mods.toml") {
        expand(mutableMapOf(
            Pair("group", project.property("maven_group")),
            Pair("version", project.version),

            Pair("mod_id", project.property("mod_id")),
            Pair("minecraft_version", project.property("minecraft_version")),
            Pair("architectury_version", project.property("architectury_version")),
            Pair("kotlin_for_forge_version", project.property("kotlin_for_forge_version"))
        ))
    }
}

tasks.shadowJar {
    exclude("fabric.mod.json")
    exclude("architectury.common.json")
    configurations = listOf(shadowCommon)
    archiveFileName.set("HuHoBot-${project.version}-Forge_devShadow.jar")

    // 去除重复文件（只需在这里配置一次）
    mergeServiceFiles()

    // 其他优化选项
    exclude("**/*.md")
    exclude("**/*.txt")
    exclude("META-INF/maven/**")
    exclude("META-INF/LICENSE**")
    exclude("org/slf4j/**")
    exclude("org/yaml/**")
    exclude("org/jetbrains/**")
    exclude("META-INF/versions/**")
    exclude("META-INF/proguard/**")
    exclude("META-INF/native-image/**")
    exclude("META-INF/scm/**")
}

tasks.remapJar {
    injectAccessWidener.set(true)
    inputFile.set(tasks.shadowJar.flatMap { it.archiveFile })
    dependsOn(tasks.shadowJar)
    archiveFileName.set("HuHoBot-${project.version}-Forge.jar")

    // 其他优化选项
    exclude("**/*.md")
    exclude("**/*.txt")
    exclude("META-INF/maven/**")
    exclude("META-INF/LICENSE**")
    exclude("org/slf4j/**")
    exclude("org/yaml/**")
    exclude("org/jetbrains/**")
    exclude("META-INF/versions/**")
    exclude("META-INF/proguard/**")
    exclude("META-INF/native-image/**")
    exclude("META-INF/scm/**")
}

tasks.jar {
    archiveClassifier.set("ForgeDev")
}

tasks.sourcesJar {
    val commonSources = project(":server-Architectury:common").tasks.getByName<Jar>("sourcesJar")
    dependsOn(commonSources)
    from(commonSources.archiveFile.map { zipTree(it) })
}

components.getByName("java") {
    this as AdhocComponentWithVariants
    this.withVariantsFromConfiguration(project.configurations["shadowRuntimeElements"]) {
        skip()
    }
}