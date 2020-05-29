pipeline {
  agent any
  stages {
    stage('Build') {
      agent any
      environment {
        options = 'skipDefaultCheckout'
      }
      steps {
        isUnix()
        sleep 2
      }
    }

    stage('Test') {
      steps {
        withGradle() {
          timestamps() {
            sh './gradlew clean test --no-daemon'
          }

        }

      }
    }

  }
}