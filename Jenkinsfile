
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                bat 'call "C:\\Tools\\apache-maven-3.10.0\\bin\\mvn.cmd" clean package'
            }
        }

        stage('Build Docker Image') {
            steps {
                bat 'docker build -t user-crud:%BUILD_NUMBER% .'
            }
        }

        stage('Verify Docker Image') {
            steps {
                bat 'docker images user-crud'
            }
        }
    }
}