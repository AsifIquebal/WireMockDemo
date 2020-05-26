pipeline {
    agent { label 'master' }
    stages {
        stage('SCM') {
            steps {
                git url: 'https://github.com/AsifIquebal/WireMockDemo.git'
            }
        }
        stage('Tests') {
            steps {
                script {
                    try {
                        sh './gradlew clean test' //run a gradle task
                    } finally {
                        echo 'finally block'
                    }
                }
            }
        }
    }
    post {
        success {
            echo 'This will run only if successful'
        }
        failure {
            echo 'This will run only if failed'
        }
    }

}