plugins {
    id("com.gradleup.shadow")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

loom {
    accessWidenerPath.set(project(":server-Architectury:common").loom.accessWidenerPath)
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
val developmentNeoForge: Configuration by configurations.getting

configurations {
    getByName("compileClasspath").extendsFrom(common)
    getByName("runtimeClasspath").extendsFrom(common)
    getByName("developmentNeoForge").extendsFrom(common)
}

repositories {
    // KFF
    mavenCentral()
    maven {
        name = "NeoForge"
        setUrl("https://maven.neoforged.net/releases/")
    }
    maven {
        name = "Kotlin for Forge"
        setUrl("https://thedarkcolour.github.io/KotlinForForge/")
    }
}

dependencies {
    neoForge("net.neoforged:neoforge:${project.property("neoforge_version")}")
    // Remove the next line if you don't want to depend on the API
    modApi("dev.architectury:architectury-neoforge:${project.property("architectury_version")}")

    common(project(":server-Architectury:common", "namedElements")) { isTransitive = false }
    shadowCommon(project(":server-Architectury:common", "transformProductionNeoForge")) { isTransitive = false }
    shadowCommon("org.yaml:snakeyaml:2.5")

    shadowCommon("io.ktor:ktor-client-websockets:1.6.8")
    shadowCommon("io.ktor:ktor-client-cio:1.6.8")
    shadowCommon("io.ktor:ktor-client-core:1.6.8") {
        exclude(group = "org.slf4j")
    }

    shadowCommon("com.alibaba.fastjson2:fastjson2:2.0.52")
    shadowCommon(project(":common-Bot")) { isTransitive = false }

    // Kotlin For Forge
    implementation("thedarkcolour:kotlinforforge:${project.property("kotlin_for_forge_version")}")
}

tasks.processResources {
    inputs.property("group", project.property("maven_group"))
    inputs.property("version", project.version)
    inputs.property("neoforge_version", project.property("neoforge_version"))

    filesMatching("META-INF/mods.toml") {
        expand(mutableMapOf(
            Pair("group", project.property("maven_group")),
            Pair("version", project.version),

            Pair("mod_id", project.property("mod_id")),
            Pair("minecraft_version", project.property("minecraft_version")),
            Pair("neoforge_version", project.property("neoforge_version")),
            Pair("architectury_version", project.property("architectury_version")),
            Pair("kotlin_for_forge_version", project.property("kotlin_for_forge_version"))
        ))
    }
}

tasks.shadowJar {
    exclude("fabric.mod.json")
    exclude("architectury.common.json")
    configurations = listOf(shadowCommon)
    archiveFileName.set("HuHoBot-${project.version}-NeoForge_devShadow.jar")
}

tasks.remapJar {
    injectAccessWidener.set(true)
    inputFile.set(tasks.shadowJar.flatMap { it.archiveFile })
    dependsOn(tasks.shadowJar)
    archiveFileName.set("HuHoBot-${project.version}-NeoForge.jar")
}

tasks.jar {
    archiveClassifier.set("NeoForgeDev")
}

tasks.sourcesJar {
    val commonSources = project(":server-Architectury:common").tasks.getByName<Jar>("sourcesJar")
    dependsOn(commonSources)
    from(commonSources.archiveFile.map { zipTree(it) })
}
