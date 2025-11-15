allprojects {
    group = "cn.huohuas001"
    version = "2.0.4"

    repositories {
        mavenCentral()
    }

    tasks.register("buildAndGather") {
        group = "build"
        description = "Build fabric and forge projects with standard build, others with shadowJar"

        // 在配置阶段设置依赖关系，而不是在 doFirst 中
        subprojects.forEach { project ->
            if (project.name == "fabric" || project.name == "forge") {
                // 使用标准 build 任务
                dependsOn(project.tasks.named("build"))
            } else {
                // 其他项目使用 shadowJar 任务
                try {
                    dependsOn(project.tasks.named("shadowJar"))
                } catch (e: Exception) {
                    // 忽略没有 shadowJar 任务的项目
                }
            }
        }

        doLast {
            val gatherDir = layout.buildDirectory.dir("gathered-jars").get().asFile
            gatherDir.mkdirs()

            subprojects.forEach { project ->
                try {
                    val jarFile = if (project.name == "fabric" || project.name == "forge") {
                        // 获取标准 build 产生的 jar 文件
                        project.tasks.named("remapJar").get().outputs.files.singleFile
                    } else {
                        // 获取 shadowJar 产生的 jar 文件
                        project.tasks.named("shadowJar").get().outputs.files.singleFile
                    }

                    if (jarFile.exists()) {
                        copy {
                            from(jarFile)
                            into(gatherDir)
                        }
                    }
                } catch (e: Exception) {
                    // 忽略没有相应任务的项目
                }
            }

            println("All jars gathered to: ${gatherDir.absolutePath}")
        }
}

}
