pipeline {
    agent any

    environment {
        GITHUB_TOKEN = credentials('github-pat')

        WS_SERVER_URL = credentials('ws-server-url')

        R2_ACCOUNT_ID = credentials('r2-account-id')
        R2_ACCESS_KEY = credentials('r2-access-key-id')
        R2_SECRET_KEY = credentials('r2-secret-access-key')
        R2_BUCKET     = credentials('r2-bucket')
    }

    stages {

        stage('Check Tag') {
            when { buildingTag() }
            steps {
                echo "Release tag: ${env.TAG_NAME}"
            }
        }

        stage('Checkout') {
            when { buildingTag() }
            steps {
                checkout scm
                sh 'git fetch --tags --force'
            }
        }

        stage('Build (Multi-Module)') {
            when { buildingTag() }
            tools {
                // 按你项目里最“高”的那个 JVM 来
                jdk 'jdk21'
            }
            steps {
                sh '''
                chmod +x gradlew
                ./gradlew buildAndGather \
                  -PwsServerUrl="$WS_SERVER_URL" \
                  -x test \
                  -Dorg.gradle.daemon=false
                '''
            }
        }

        stage('Prepare Artifacts') {
            when { buildingTag() }
            steps {
                sh '''
                mkdir -p artifacts
                cp build/gathered-jars/HuHoBot-*.jar artifacts/

                VERSION="${TAG_NAME#v}"
                echo "{\\"latest\\": \\"$VERSION\\"}" > artifacts/latest.json
                '''
            }
        }

        stage('Upload to R2') {
            when { buildingTag() }
            steps {
                sh '''
                rclone config create r2 s3 \
                  provider=Cloudflare \
                  access_key_id=$R2_ACCESS_KEY \
                  secret_access_key=$R2_SECRET_KEY \
                  endpoint=https://$R2_ACCOUNT_ID.r2.cloudflarestorage.com || true

                rclone copy artifacts r2:$R2_BUCKET/kotlin --progress
                '''
            }
        }

        stage('Extract Changelog') {
            when { buildingTag() }
            steps {
                sh '''
                VERSION="${TAG_NAME#v}"

                awk -v version="[v$VERSION]" '
                  BEGIN {RS="## "; FS="\\n"}
                  $1 ~ version {
                    sub(/\\[.*\\] - .*\\n/, "")
                    print
                    exit
                  }
                ' CHANGELOG.md > release_notes.txt
                '''
            }
        }

        stage('Create GitHub Release') {
            when { buildingTag() }
            steps {
                sh '''
                gh release create "$TAG_NAME" \
                  artifacts/HuHoBot-*.jar \
                  --title "HuHoBot $TAG_NAME" \
                  --notes-file release_notes.txt
                '''
            }
        }
    }
}
