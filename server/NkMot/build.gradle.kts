plugins {
    java
    kotlin("jvm")
    id("com.gradleup.shadow")
}

group = "cn.huohuas001"
val targetJavaVersion = 17
val shadowCommon: Configuration by configurations.creating

repositories {
    mavenCentral()
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        name = "repo-lanink-cn"
        url = uri("https://repo.lanink.cn/repository/maven-public/")
    }
}

dependencies {
    compileOnly("cn.nukkit:Nukkit:MOT-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    implementation("com.alibaba.fastjson2:fastjson2:2.0.52")

    implementation("io.ktor:ktor-client-websockets:1.6.8")
    implementation("io.ktor:ktor-client-cio:1.6.8")
    implementation("io.ktor:ktor-client-core:1.6.8") {
        exclude(group = "org.slf4j")
        exclude(group = "org.yaml")
    }

    implementation("com.alibaba.fastjson2:fastjson2:2.0.52") {
        exclude(group = "org.jetbrains")
    }

    shadowCommon("com.alibaba.fastjson2:fastjson2:2.0.52")
    shadowCommon("com.github.Anon8281:UniversalScheduler:0.1.6")

    shadowCommon("io.ktor:ktor-client-websockets:1.6.8")
    shadowCommon("io.ktor:ktor-client-cio:1.6.8")
    shadowCommon("io.ktor:ktor-client-core:1.6.8") {
        exclude(group = "org.slf4j")
    }
    shadowCommon("com.alibaba.fastjson2:fastjson2:2.0.52") {
        exclude(group = "org.jetbrains")
    }

    implementation(project(":common-Bot"))
    shadowCommon(project(":common-Bot"))
    implementation(kotlin("stdlib"))
}

tasks.jar {
    archiveFileName.set("HuHoBot.jar")
    dependsOn(tasks.processResources)
    manifest {
        attributes["Main-Class"] = "cn.huohuas001.huHoBot"
    }
}

tasks.shadowJar {
    dependsOn(tasks.processResources)
    configurations = listOf(shadowCommon)
    archiveFileName.set("HuHoBot-${project.version}-Nukkit-MOT.jar")
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

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(targetJavaVersion)) // 与Kotlin保持一致
    }
}

kotlin {
    jvmToolchain(targetJavaVersion) // 或者 21，取决于你想使用的版本
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
