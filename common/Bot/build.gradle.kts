plugins {
    kotlin("jvm") version "2.2.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8)) // 设置 JDK 8
    }
}

dependencies {
    implementation("org.java-websocket:Java-WebSocket:1.5.4")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.52")
    implementation(kotlin("stdlib"))
}

tasks.register<Copy>("generateServerConfig") {
    group = "Build"

    // 获取传递的 Gradle 属性，如果没有则使用默认值
    val wsServerUrl = project.findProperty("wsServerUrl")?.toString() ?: "ws://127.0.0.1:8888"

    from("src/main/templates/kotlin")
    into("src/main/generated/kotlin")

    include("**/*.template")
    rename { filename ->
        filename.replace(".template", "")
    }

    filter { line: String ->
        line.replace("\${WS_SERVER_URL}", wsServerUrl)
    }

    filteringCharset = "UTF-8"
}

sourceSets {
    main {
        kotlin {
            srcDir("src/main/generated/kotlin")
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateServerConfig")
}

tasks.clean {
    delete("src/main/generated")
}

