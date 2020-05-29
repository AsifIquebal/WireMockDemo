pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
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