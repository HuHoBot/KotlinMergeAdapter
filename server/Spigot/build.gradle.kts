plugins {
    kotlin("jvm")
    id("java")
    id("com.gradleup.shadow")
}

group = "cn.huohuas001"
val shadowCommon: Configuration by configurations.creating

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

    implementation("com.alibaba.fastjson2:fastjson2:2.0.52") {
        exclude(group = "org.jetbrains")
    }

    shadowCommon("com.alibaba.fastjson2:fastjson2:2.0.52")
    shadowCommon("com.github.Anon8281:UniversalScheduler:0.1.6")

    shadowCommon("io.ktor:ktor-client-websockets:1.6.8")
    shadowCommon("io.ktor:ktor-client-cio:1.6.8")
    shadowCommon("io.ktor:ktor-client-core:1.6.8")
    shadowCommon("org.slf4j:slf4j-simple:1.7.36")
    shadowCommon("com.alibaba.fastjson2:fastjson2:2.0.52") {
        exclude(group = "org.jetbrains")
    }

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
    archiveFileName.set("HuHoBot-${project.version}-Spigot.jar")
    relocate("com.github.Anon8281.universalScheduler", "cn.huohuas001.huHoBot.spigot.universalScheduler")
    relocate("kotlinx.coroutines", "cn.huohuas001.huHoBot.libs.coroutines")
    relocate("io.ktor", "cn.huohuas001.huHoBot.libs.ktor")
    // 去除重复文件（只需在这里配置一次）
    mergeServiceFiles()

    // 其他优化选项
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
