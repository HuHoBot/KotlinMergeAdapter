plugins {
    java
    kotlin("jvm")
    id("org.allaymc.gradle.plugin") version "0.1.2"
    id("com.gradleup.shadow")
}

group = "cn.huohua001.huhobot"
description = "HuHoBot Merge Adapter"

val shadowCommon: Configuration by configurations.creating

repositories {
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.lucko.me/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.opencollab.dev/main/")
    maven("https://storehouse.okaeri.eu/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

allay {
    // API 版本（如果 `apiOnly = true` 则必需）。
    api = "0.17.0"

    // 将此字段设置为 `false` 以访问服务器模块并使用内部 API。但是不推荐这样做，
    // 因为内部 API 可能随时更改。
    // 默认值为 `true`。
    apiOnly = true

    // 指定在 `runServer` 任务中使用的服务器版本。如果 `apiOnly` 设置为 `false`，
    // 这也将作为依赖版本。如果此字段设置为 `null`，将使用最新的服务器版本。
    // 默认值为 `null`。
    server = null

    // 配置插件描述符（plugin.json）。
    plugin {
        entrance = ".allay.HuHoBotAllay"

        apiVersion = ">=0.17.0"

        name = "HuHoBot"

        authors += "HuoHuas001"
        website = "https://github.com/HuHoBot/KotlinMergeAdapter"
    }


}

tasks.withType<AbstractCopyTask>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

dependencies {
    compileOnly(group = "org.projectlombok", name = "lombok", version = "1.18.34")

    implementation(project(":common-Bot"))
    shadowCommon(project(":common-Bot"))

    implementation(kotlin("stdlib"))
    implementation(group = "eu.okaeri", name = "okaeri-configs-yaml-snakeyaml", version = "5.0.13")
    implementation(group = "com.alibaba.fastjson2", name = "fastjson2", version = "2.0.52")

    implementation("io.ktor:ktor-client-websockets:1.6.8")
    implementation("io.ktor:ktor-client-cio:1.6.8")
    implementation("io.ktor:ktor-client-core:1.6.8") {
        exclude(group = "org.slf4j")
        exclude(group = "org.yaml")
    }

    implementation("com.alibaba.fastjson2:fastjson2:2.0.52") {
        exclude(group = "org.jetbrains")
    }

    shadowCommon("com.alibaba.fastjson2:fastjson2:2.0.52") {
        exclude(group = "org.jetbrains")
    }

    shadowCommon("io.ktor:ktor-client-websockets:1.6.8")
    shadowCommon("io.ktor:ktor-client-cio:1.6.8")
    shadowCommon("io.ktor:ktor-client-core:1.6.8")
    shadowCommon("org.slf4j:slf4j-simple:1.7.36")

    //BungeeCord
    compileOnly("net.md-5:bungeecord-api:1.16-R0.4") {
        exclude(group = "net.md-5", module = "bungeecord-protocol")
    }

    //Velocity
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

    //Spigot
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("org.jetbrains:annotations:13.0")
    compileOnly("org.apache.logging.log4j:log4j-core:2.17.1")
    compileOnly("org.apache.logging.log4j:log4j-api:2.17.1")

    implementation("com.github.Anon8281:UniversalScheduler:0.1.6")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.52")

    shadowCommon("com.alibaba.fastjson2:fastjson2:2.0.52")
    shadowCommon("com.github.Anon8281:UniversalScheduler:0.1.6")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    shadowCommon("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Redis
    implementation("redis.clients:jedis:5.0.0")
    shadowCommon("redis.clients:jedis:5.0.0")

    annotationProcessor(group = "org.projectlombok", name = "lombok", version = "1.18.34")
}

tasks.shadowJar {
    configurations = listOf(shadowCommon)
    archiveFileName.set("HuHoBot-${project.version}-Merge.jar")
    relocate("kotlinx.coroutines", "cn.huohuas001.huhobot.libs.coroutines")
    relocate("io.ktor", "cn.huohuas001.huhobot.libs.ktor")
    mergeServiceFiles()

    exclude("**/*.md")
    exclude("**/*.txt")
    exclude("META-INF/maven/**")
    exclude("META-INF/LICENSE**")
    exclude("org/jetbrains/**")
    exclude("META-INF/versions/**")
    exclude("META-INF/proguard/**")
    exclude("META-INF/native-image/**")
    exclude("META-INF/scm/**")
    exclude("**/module-info.class")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("bungee.yml") {
        expand(props)
    }
    filesMatching("velocity-plugin.json") {
        expand(props)
    }
    /*filesMatching("plugin.json") {
        expand(props)
    }*/
    filesMatching("plugin.yml") {
        expand(props)
    }
    filesMatching("config.yml") {
        expand(props)
    }
}

