pipeline {
    agent any

    environment {
        GRADLE_MIRROR = "https://mirrors.aliyun.com/gradle/distributions/"
        WS_SERVER_URL = credentials('ws-server-url')  // 你的 WebSocket URL
        R2_ACCOUNT_ID = credentials('r2-account-id')
        R2_ACCESS_KEY = credentials('r2-access-key-id')
        R2_SECRET_KEY = credentials('r2-secret-access-key')
        R2_BUCKET = credentials('r2-bucket')
        GITHUB_TOKEN = credentials('github-pat')
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                sh 'git fetch --tags --force'
            }
        }

        stage('Detect Tag') {
            steps {
                script {
                    def tag = sh(
                        script: "git describe --tags --exact-match || true",
                        returnStdout: true
                    ).trim()

                    if (!tag) {
                        echo "Not a tag build, skipping release stages"
                        env.IS_TAG = "false"
                    } else {
                        echo "Tag detected: ${tag}"
                        env.IS_TAG = "true"
                        env.TAG_NAME = tag
                    }
                }
            }
        }

        stage('Patch Gradle Wrapper (Aliyun Mirror)') {
            when {
                expression { env.IS_TAG == "true" }
            }
            steps {
                sh """
                    set -e
                    WRAPPER_FILE=gradle/wrapper/gradle-wrapper.properties
                    echo "Patching Gradle Wrapper distributionUrl"
                    # 注意：这是 shell 注释，不影响 Groovy
                    sed -i "s|https\\\\://services.gradle.org/distributions/|${GRADLE_MIRROR}|g" \$WRAPPER_FILE
                    grep distributionUrl \$WRAPPER_FILE
                """
            }
        }



        stage('Build (Multi-Module)') {
            when {
                expression { env.IS_TAG == "true" }
            }
            steps {
                sh '''
                    chmod +x gradlew
                    ./gradlew buildAndGather \
                        -PwsServerUrl=${WS_SERVER_URL} \
                        -x test \
                        -Dorg.gradle.daemon=false
                '''
            }
        }

        stage('Prepare Artifacts') {
            when {
                expression { env.IS_TAG == "true" }
            }
            steps {
                sh '''
                    mkdir -p artifacts
                    cp build/gathered-jars/HuHoBot-*.jar artifacts/ || true
                    ls -lah artifacts
                '''
            }
        }

        stage('Upload to R2') {
            when {
                expression { env.IS_TAG == "true" }
            }
            steps {
                sh '''
                    echo "Uploading artifacts to R2"
                    # 假设你使用 r2-upload-action 或自写脚本
                    r2-upload \
                        --account ${R2_ACCOUNT_ID} \
                        --access-key ${R2_ACCESS_KEY} \
                        --secret-key ${R2_SECRET_KEY} \
                        --bucket ${R2_BUCKET} \
                        --source artifacts \
                        --destination kotlin
                '''
            }
        }

        stage('Create GitHub Release') {
            when {
                expression { env.IS_TAG == "true" }
            }
            steps {
                sh '''
                    echo "Creating GitHub Release ${TAG_NAME}"
                    gh release create ${TAG_NAME} artifacts/* \
                        --repo HuHoBot/KotlinMergeAdapter \
                        --title "${TAG_NAME}" \
                        --notes "Automated release from Jenkins"
                '''
            }
        }
    }

    post {
        always {
            echo "Build finished"
        }
        success {
            echo "Build succeeded"
        }
        failure {
            echo "Build failed"
        }
    }
}
