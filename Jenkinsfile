pipeline {
    agent any

    tools {
        jdk 'jdk21'
    }

    environment {
        WS_SERVER_URL = credentials('ws-server-url')

        R2_ACCOUNT_ID = credentials('r2-account-id')
        R2_ACCESS_KEY = credentials('r2-access-key-id')
        R2_SECRET_KEY = credentials('r2-secret-access-key')
        R2_BUCKET     = credentials('r2-bucket')

        GITHUB_TOKEN  = credentials('github-pat')

        // 阿里云 Gradle 镜像（关键）
        GRADLE_MIRROR = 'https://mirrors.aliyun.com/gradle/distributions/v9.1.0/gradle-9.1.0-bin.zip'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                sh 'git fetch --tags --force'
            }
        }

        stage('Build (Multi-Module)') {
            when {
                buildingTag()
            }
            steps {
                sh '''
                    chmod +x gradlew
                    ./gradlew buildAndGather \
                      -PwsServerUrl=$WS_SERVER_URL \
                      -x test \
                      -Dorg.gradle.daemon=false \
                      -Dorg.gradle.wrapper.distributionUrl=$GRADLE_MIRROR
                '''
            }
        }

        stage('Prepare Artifacts') {
            when {
                buildingTag()
            }
            steps {
                sh '''
                    mkdir -p artifacts
                    cp build/gathered-jars/HuHoBot-*.jar artifacts/

                    VERSION=${GIT_TAG_NAME#v}
                    echo "{\"latest\":\"$VERSION\"}" > artifacts/latest.json
                '''
            }
        }

        stage('Upload to R2') {
            when {
                buildingTag()
            }
            steps {
                sh '''
                    rclone copy artifacts r2:$R2_BUCKET/kotlin \
                      --s3-provider Cloudflare \
                      --s3-endpoint https://$R2_ACCOUNT_ID.r2.cloudflarestorage.com \
                      --s3-access-key-id $R2_ACCESS_KEY \
                      --s3-secret-access-key $R2_SECRET_KEY
                '''
            }
        }

        stage('Create GitHub Release') {
            when {
                buildingTag()
            }
            steps {
                sh '''
                    TAG=${GIT_TAG_NAME}

                    gh release delete "$TAG" -y || true

                    gh release create "$TAG" artifacts/HuHoBot-*.jar \
                      --title "HuHoBot $TAG" \
                      --notes-file CHANGELOG.md
                '''
            }
        }
    }
}
