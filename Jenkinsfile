pipeline {
    agent any

    tools {
        jdk 'JDK-17'
        maven 'Maven-3.9'
    }

    environment {
        APP_NAME           = 'producto-app'
        SONAR_PROJECT_KEY  = 'producto-app'
        SONAR_PROJECT_NAME = 'producto-app'
        ARTIFACTORY_SERVER = 'Artifactory'
        ARTIFACTORY_REPO   = 'libs-release-local'
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '5'))
        disableConcurrentBuilds()
        timestamps()
        ansiColor('xterm')
    }

    stages {
        stage('Build') {
            steps {
                sh 'mvn clean compile -Dmaven.test.skip=true --batch-mode --no-transfer-progress'
            }
            post {
                failure {
                    error 'Fallo en la compilación del proyecto.'
                }
            }
        }

        stage('Testing (JUnit + JaCoCo)') {
            steps {
                sh 'mvn verify --batch-mode --no-transfer-progress'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/**/*.xml', allowEmptyResults: false
                    jacoco(
                        execPattern: 'target/jacoco.exec',
                        classPattern: 'target/classes',
                        sourcePattern: 'src/main/java',
                        exclusionPattern: '**/model/**,**/ProductosApplication.class',
                        minimumLineCoverage: '60'
                    )
                }
                failure {
                    error 'Fallo en las pruebas unitarias o cobertura de código JaCoCo.'
                }
            }
        }

        stage('Sonar') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn sonar:sonar \
                            -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                            -Dsonar.projectName=${SONAR_PROJECT_NAME} \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                            --batch-mode \
                            --no-transfer-progress
                    '''
                }
            }
            post {
                always {
                    timeout(time: 5, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: true
                    }
                }
            }
        }

        stage('Generar artefacto y almacenar en Artifactory') {
            steps {
                sh 'mvn package -Dmaven.test.skip=true --batch-mode --no-transfer-progress'
                rtUpload(
                    serverId: ARTIFACTORY_SERVER,
                    spec: """{
                        "files": [{
                            "pattern": "target/*.jar",
                            "target": "${ARTIFACTORY_REPO}/com/example/${APP_NAME}/${BUILD_NUMBER}/"
                        }]
                    }"""
                )
                rtPublishBuildInfo(
                    serverId: ARTIFACTORY_SERVER
                )
            }
            post {
                failure {
                    error 'Error durante la publicación del artefacto en Artifactory.'
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
