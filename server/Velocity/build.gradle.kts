plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("java")
    id("com.gradleup.shadow")
}

group = "cn.huohuas001"
val shadowCommon: Configuration by configurations.creating

repositories {
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

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

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    shadowCommon("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Redis
    implementation("redis.clients:jedis:5.0.0")
    shadowCommon("redis.clients:jedis:5.0.0")

    implementation(project(":common-Bot"))
    shadowCommon(project(":common-Bot"))
    implementation(kotlin("stdlib"))
}

tasks.jar {
    archiveFileName.set("HuHoBot.jar")
    manifest {
        attributes["Main-Class"] = "cn.huohuas001.huHoBot"
    }
}

tasks.shadowJar {
    configurations = listOf(shadowCommon)
    archiveFileName.set("HuHoBot-${project.version}-Velocity.jar")
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

val targetJavaVersion = 17
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
    filesMatching("velocity-plugin.json") {
        expand(props)
    }
}

kotlin {
    jvmToolchain(targetJavaVersion)
}
