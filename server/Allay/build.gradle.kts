import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import kotlin.text.set

plugins {
    kotlin("jvm") version "2.2.0"
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "cn.huohuas001.huhobot"
description = "HuHoBot Allay Adapter"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven("https://storehouse.okaeri.eu/repository/maven-public/")
}

dependencies {
    compileOnly(group = "org.allaymc.allay", name = "api", version = "0.14.0")
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


tasks.shadowJar {
    archiveFileName.set("HuHoBot-${project.version}-AllayMC.jar")

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
