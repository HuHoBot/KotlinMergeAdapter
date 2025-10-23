plugins {
    kotlin("jvm") version "2.2.0"
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "cn.huohuas001"
version = "1.3.3"

repositories {
    maven("https://jitpack.io")
    maven("https://repo.lucko.me/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.opencollab.dev/main/")
    mavenCentral()
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("org.jetbrains:annotations:13.0")
    implementation("com.github.Anon8281:UniversalScheduler:0.1.6")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.52")

    implementation(project(":common-Bot"))
    implementation(kotlin("stdlib"))
}

tasks.jar {
    archiveFileName.set("HuHoBot.jar")
    manifest {
        attributes["Main-Class"] = "cn.huohuas001.huHoBot"
    }
}

tasks.shadowJar {
    archiveFileName.set("HuHoBot-${project.version}-Spigot.jar")
    relocate("com.github.Anon8281.universalScheduler", "cn.huohuas001.huHoBot.spigot.universalScheduler")
    minimize()
}

val targetJavaVersion = 8
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
}



tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
    filesMatching("config.yml") {
        expand(props)
    }
}
