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
                    def caption = """📦 *NEW BUILD AVAILABLE!*

📱 *App*: Bacalah Al-Quran
🔢 *Build*: #${BUILD_NUMBER}
🌿 *Branch*: ${BRANCH_NAME}
🟢 *Status*: SUCCESS"""

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
            script {
                def failMsg = """🔴 *BUILD FAILED!*

📱 *App*: Bacalah Al-Quran
🔢 *Build*: #${BUILD_NUMBER}
❌ *Status*: Execution Error"""

                sh """
                    curl -s -X POST "https://api.telegram.org/bot${TELEGRAM_TOKEN}/sendMessage" \
                    -d chat_id="${TELEGRAM_CHAT_ID}" \
                    -d text="${failMsg}" \
                    -d parse_mode="Markdown"
                """
            }
        }
    }
}
