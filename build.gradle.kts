plugins {
    kotlin("jvm") version "2.2.0" apply false
    id("com.gradleup.shadow") version "9.2.2" apply false
}

allprojects {
    group = "cn.huohuas001"
    version = "2.0.8"

    repositories {
        mavenCentral()
    }

    tasks.register("buildAndGather") {
        group = "build"
        description = "Build fabric, forge and neoforge projects with standard build, others with shadowJar"

        // 递归获取所有子项目
        fun getAllProjects(project: Project): List<Project> {
            return listOf(project) + project.subprojects.flatMap(::getAllProjects)
        }

        // 在配置阶段设置依赖关系，而不是在 doFirst 中
        getAllProjects(this.project).drop(1).forEach { project -> // drop(1) 排除根项目
            if (project.name == "fabric" || project.name == "forge" || project.name == "neoforge") {
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

            // 递归处理所有子项目
            getAllProjects(this.project).drop(1).forEach { project -> // drop(1) 排除根项目
                try {
                    val jarFile = if (project.name == "fabric" || project.name == "forge" || project.name == "neoforge") {
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
