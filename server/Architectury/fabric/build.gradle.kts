plugins {
    id("com.github.johnrengelman.shadow")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.quiltmc.org/repository/release/")
    }
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    accessWidenerPath.set(project(":server-Architectury:common").loom.accessWidenerPath)
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
val developmentFabric: Configuration by configurations.getting

configurations {
    getByName("compileClasspath").extendsFrom(common)
    getByName("runtimeClasspath").extendsFrom(common)
    getByName("developmentFabric").extendsFrom(common)
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${project.property("fabric_loader_version")}")
    modApi("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_api_version")}")
    // Remove the next line if you don't want to depend on the API
    modApi("dev.architectury:architectury-fabric:${project.property("architectury_version")}")

    common(project(":server-Architectury:common", "namedElements")) {
        isTransitive = false
    }
    shadowCommon(project(":server-Architectury:common", "transformProductionFabric")){ isTransitive = false }
    shadowCommon("org.yaml:snakeyaml:2.5")
    shadowCommon("org.java-websocket:Java-WebSocket:1.5.4")
    shadowCommon("com.alibaba.fastjson2:fastjson2:2.0.52")
    shadowCommon(project(":common-Bot")){ isTransitive = false }

    // Fabric Kotlin
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("fabric_kotlin_version")}")
}

tasks.processResources {
    inputs.property("group", project.property("maven_group"))
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(mutableMapOf(
            Pair("group", project.property("maven_group")),
            Pair("version", project.version),

            Pair("mod_id", project.property("mod_id")),
            Pair("minecraft_version", project.property("minecraft_version")),
            Pair("architectury_version", project.property("architectury_version")),
            Pair("fabric_kotlin_version", project.property("fabric_kotlin_version"))
        ))
    }
}

tasks.shadowJar {
    exclude("architectury.common.json")
    configurations = listOf(shadowCommon)
    archiveFileName.set("HuHoBot-${project.version}-Fabric_devShadow.jar")
}

tasks.remapJar {
    injectAccessWidener.set(true)
    inputFile.set(tasks.shadowJar.flatMap { it.archiveFile })
    dependsOn(tasks.shadowJar)
    archiveFileName.set("HuHoBot-${project.version}-Fabric.jar")
}

tasks.jar {
    archiveClassifier.set("FabricDev")
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