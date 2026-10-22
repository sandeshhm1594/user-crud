
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

        stage('Deploy Candidate') {
            steps {
                withCredentials([
                    string(
                        credentialsId: 'mysql-db-password',
                        variable: 'DB_PASSWORD'
                    )
                ]) {
                    bat '''
                        docker rm -f user-crud-candidate 2>NUL
                        docker run -d --name user-crud-candidate -p 8084:8080 -e "SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/newdocdb?createDatabaseIfNotExist=true" -e "SPRING_DATASOURCE_USERNAME=root" -e "DB_PASSWORD=%DB_PASSWORD%" user-crud:%BUILD_NUMBER%
                    '''
                }
            }
        }

        stage('Candidate Health Check') {
            steps {
                echo 'Checking candidate on port 8084...'

                bat '''
                    powershell -NoProfile -ExecutionPolicy Bypass -Command "$healthy=$false; for($i=1;$i -le 12;$i++){ try { $r=Invoke-WebRequest -Uri 'http://localhost:8084/home' -TimeoutSec 5 -UseBasicParsing; if($r.StatusCode -eq 200){ Write-Output 'Candidate health check passed: HTTP 200'; $healthy=$true; break } } catch { Write-Output ('Attempt '+$i+' failed: '+$_.Exception.Message) }; Start-Sleep -Seconds 5 }; if(-not $healthy){ Write-Error 'Candidate health check failed'; exit 1 }"
                '''
            }
        }

        stage('Promote Candidate') {
            steps {
                bat '''
                    docker rm -f user-crud-deployed 2>NUL
                    docker stop user-crud-candidate
                    docker rm user-crud-candidate
                '''

                withCredentials([
                    string(
                        credentialsId: 'mysql-db-password',
                        variable: 'DB_PASSWORD'
                    )
                ]) {
                    bat '''
                        docker run -d --name user-crud-deployed -p 8083:8080 -e "SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/newdocdb?createDatabaseIfNotExist=true" -e "SPRING_DATASOURCE_USERNAME=root" -e "DB_PASSWORD=%DB_PASSWORD%" user-crud:%BUILD_NUMBER%
                    '''
                }
            }
        }

        stage('Production Health Check') {
            steps {
                echo 'Checking deployed application on port 8083...'

                bat '''
                    powershell -NoProfile -ExecutionPolicy Bypass -Command "$healthy=$false; for($i=1;$i -le 12;$i++){ try { $r=Invoke-WebRequest -Uri 'http://localhost:8083/home' -TimeoutSec 5 -UseBasicParsing; if($r.StatusCode -eq 200){ Write-Output 'Production health check passed: HTTP 200'; $healthy=$true; break } } catch { Write-Output ('Attempt '+$i+' failed: '+$_.Exception.Message) }; Start-Sleep -Seconds 5 }; if(-not $healthy){ Write-Error 'Production health check failed'; exit 1 }"
                '''
            }
        }
    }

    post {
        success {
            echo 'BUILD, CANDIDATE CHECK AND DEPLOYMENT SUCCESSFUL'
        }

        failure {
            echo 'PIPELINE FAILED - CHECK CONSOLE OUTPUT'
        }
    }
}
