
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

        stage('Health Check') {
            steps {
                echo 'Checking Spring Boot application health...'

                bat '''
                    powershell -NoProfile -ExecutionPolicy Bypass -Command "$healthy=$false; for($i=1;$i -le 12;$i++){ try { $r=Invoke-WebRequest -Uri 'http://localhost:8083/home' -TimeoutSec 5 -UseBasicParsing; if($r.StatusCode -eq 200){ Write-Output 'Health check passed: HTTP 200'; $healthy=$true; break } } catch { Write-Output ('Attempt '+$i+' failed: '+$_.Exception.Message) }; Start-Sleep -Seconds 5 }; if(-not $healthy){ Write-Error 'Health check failed after 12 attempts'; exit 1 }"
                '''
            }
        }
    }

    post {
        success {
            echo 'BUILD, DOCKER DEPLOYMENT AND HEALTH CHECK SUCCESSFUL'
        }

        failure {
            echo 'PIPELINE FAILED - CHECK CONSOLE OUTPUT'
        }
    }
}
