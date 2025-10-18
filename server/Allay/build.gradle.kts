import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.2.0"
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "cn.huohuas001.huhobot"
description = "HuHoBot Allay Adapter"
version = "0.1.1"

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

    annotationProcessor(group = "org.projectlombok", name = "lombok", version = "1.18.34")
}


tasks.shadowJar {
    archiveClassifier = "AllayMC"
}
