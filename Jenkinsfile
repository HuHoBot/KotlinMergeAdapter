pipeline {
    agent any

    environment {
        // 使用阿里云镜像地址
        GRADLE_DIST_URL = 'https://mirrors.aliyun.com/gradle/distributions/v9.1.0/gradle-9.1.0-bin.zip'

        WS_SERVER_URL     = credentials('ws-server-url')
        R2_ACCOUNT_ID     = credentials('r2-account-id')
        R2_ACCESS_KEY     = credentials('r2-access-key-id')
        R2_SECRET_KEY     = credentials('r2-secret-access-key')
        R2_BUCKET         = credentials('r2-bucket')
        GITHUB_TOKEN      = credentials('github-pat')
    }

    tools {
        jdk 'jdk21'
    }

    stages {

        stage('Checkout SCM') {
            steps {
                checkout scm
                sh 'git fetch --tags --force' // 保证 tag 信息
            }
        }

        stage('Detect Tag') {
            steps {
                script {
                    // 如果是 tag 构建，会把 tag 名传进 env.TAG_NAME
                    env.TAG_NAME = sh(
                        script: "git describe --tags --exact-match 2>/dev/null || echo ''",
                        returnStdout: true
                    ).trim()
                    echo "TAG_NAME = ${env.TAG_NAME}"
                }
            }
        }

        stage('Build (Multi-Module)') {
            when { expression { env.TAG_NAME != '' } }
            steps {
                sh '''
                    chmod +x gradlew
                    echo "Using Gradle mirror: $GRADLE_DIST_URL"
                    ./gradlew buildAndGather \
                        -PwsServerUrl=$WS_SERVER_URL \
                        -x test \
                        -Dorg.gradle.daemon=false
                '''
            }
        }

        stage('Prepare Artifacts') {
            when { expression { env.TAG_NAME != '' } }
            steps {
                sh '''
                    mkdir -p artifacts
                    cp build/gathered-jars/HuHoBot-*.jar artifacts/
                    echo '{"latest":"${TAG_NAME#v}"}' > artifacts/latest.json
                '''
            }
        }

        stage('Upload to R2') {
            when { expression { env.TAG_NAME != '' } }
            steps {
                sh '''
                    rclone copy artifacts r2:${R2_BUCKET}/kotlin \
                        --s3-access-key-id $R2_ACCESS_KEY \
                        --s3-secret-access-key $R2_SECRET_KEY \
                        --s3-endpoint https://${R2_ACCOUNT_ID}.r2.cloudflarestorage.com
                '''
            }
        }

        stage('Generate Changelog') {
            when { expression { env.TAG_NAME != '' } }
            steps {
                script {
                    def changelog = sh(
                        script: """
                            awk -v tag="${env.TAG_NAME}" '
                            BEGIN {RS="## "; FS="\\n"}
                            \$1 ~ tag {
                                sub(/\\[.*\\] - .*/,"")
                                gsub(/'\"'/,"\\\\'\"'") 
                                print
                                exit
                            }' CHANGELOG.md
                        """,
                        returnStdout: true
                    ).trim()
                    env.CHANGELOG_BODY = changelog
                }
            }
        }

        stage('Create GitHub Release') {
            when { expression { env.TAG_NAME != '' } }
            steps {
                sh """
                    gh release create ${env.TAG_NAME} artifacts/HuHoBot-*.jar \
                        --title "HuHoBot ${env.TAG_NAME}" \
                        --notes "${env.CHANGELOG_BODY}" \
                        --repo HuHoBot/KotlinMergeAdapter
                """
            }
        }
    }

    post {
        failure {
            echo "Build or release failed!"
        }
    }
}
