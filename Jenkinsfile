
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
                withCredentials([
                    string(
                        credentialsId: 'mysql-db-password',
                        variable: 'DB_PASSWORD'
                    )
                ]) {
                    bat 'call "C:\\Tools\\apache-maven-3.10.0\\bin\\mvn.cmd" clean package'
                }
            }
        }

        stage('Archive JAR') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar',
                                 fingerprint: true
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
        
stage('Deploy to Docker') {
    steps {
        withCredentials([
            string(
                credentialsId: 'mysql-db-password',
                variable: 'DB_PASSWORD'
            )
        ]) {
            bat '''
                docker rm -f user-crud-deployed 2>NUL
                docker run -d --name user-crud-deployed -p 8083:8080 -e "SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/newdocdb?createDatabaseIfNotExist=true" -e "SPRING_DATASOURCE_USERNAME=root" -e "DB_PASSWORD=%DB_PASSWORD%" user-crud:%BUILD_NUMBER%
            '''
        }
    }
}
    }

    post {
        success {
            echo 'BUILD AND DOCKER IMAGE CREATION SUCCESSFUL'
        }

        failure {
            echo 'PIPELINE FAILED - CHECK CONSOLE OUTPUT'
        }
    }
}