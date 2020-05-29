pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        echo 'Build Stage'
        git(url: 'https://github.com/AsifIquebal/WireMockDemo.git', branch: 'blue-ocean-pipeline')
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