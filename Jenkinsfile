pipeline {
    agent any

    environment {
        TELEGRAM_TOKEN = '9265820276:AAGLCIEcEkKlfShnGT2syvBrqtP_iG9aiw8'
        TELEGRAM_CHAT_ID = '-1004339737142'
    }

    stages {
        stage('1. Checkout Repository') {
            steps {
                git branch: 'main', url: 'https://github.com/ardion/bacalah-quran.git'
            }
        }

        stage('2. Code Lint & Check') {
            steps {
                sh 'chmod +x gradlew && ./gradlew check --continue'
            }
        }

        stage('3. Build Android APK & Web Distribution') {
            steps {
                sh './gradlew :app:androidApp:assembleDevDebug :app:webApp:jsBrowserDevelopmentExecutableDistribution --no-daemon -Dorg.gradle.jvmargs=-Xmx2048m'
            }
        }

        stage('4. Send APK to Telegram') {
            steps {
                script {
                    def apkPath = "app/androidApp/build/outputs/apk/dev/debug/androidApp-dev-debug.apk"
                    def caption = "📦 *NEW BUILD AVAILABLE!*%0A%0A📱 *App*: Bacalah Al-Quran%0A🔢 *Build*: #${BUILD_NUMBER}%0A🌿 *Branch*: ${BRANCH_NAME}%0A🟢 *Status*: SUCCESS"

                    sh """
                        curl -s -X POST "https://api.telegram.org/bot${TELEGRAM_TOKEN}/sendDocument" \
                        -F chat_id="${TELEGRAM_CHAT_ID}" \
                        -F document=@"${apkPath}" \
                        -F caption="${caption}" \
                        -F parse_mode="Markdown"
                    """
                }
            }
        }
    }

    post {
        failure {
            sh """
                curl -s -X POST "https://api.telegram.org/bot${TELEGRAM_TOKEN}/sendMessage" \
                -d chat_id="${TELEGRAM_CHAT_ID}" \
                -d text="🔴 *BUILD FAILED!*%0A%0A📱 *App*: Bacalah Al-Quran%0A🔢 *Build*: #${BUILD_NUMBER}%0A❌ *Status*: Execution Error" \
                -d parse_mode="Markdown"
            """
        }
    }
}
