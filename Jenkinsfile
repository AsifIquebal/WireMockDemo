pipeline {
  agent any
  stages {
    stage('Build') {
      parallel {
        stage('Build') {
          steps {
            echo 'hello'
          }
        }

        stage('') {
          steps {
            sleep 5
          }
        }

      }
    }

    stage('Print') {
      steps {
        echo 'Hello World!'
      }
    }

  }
}