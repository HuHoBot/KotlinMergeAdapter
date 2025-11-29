plugins {
    java
    kotlin("jvm")
    id("org.allaymc.gradle.plugin") version "0.1.2"
    id("com.gradleup.shadow")
}

group = "cn.huohua001.huhobot"
description = "HuHoBot Allay Adapter"

val shadowCommon: Configuration by configurations.creating

tasks.shadowJar {
    //configurations = listOf(shadowCommon)
    archiveFileName.set("HuHoBot-${project.version}-Allay.jar")
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


repositories {
    mavenCentral()
    maven("https://storehouse.okaeri.eu/repository/maven-public/")
}

dependencies {
    compileOnly(group = "org.projectlombok", name = "lombok", version = "1.18.34")

    implementation(project(":common-Bot"))
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

    annotationProcessor(group = "org.projectlombok", name = "lombok", version = "1.18.34")
}


