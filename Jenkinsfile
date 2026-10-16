// =============================================================================
// Jenkinsfile - Pipeline CI/CD para producto-app
// =============================================================================
//
// REQUISITOS PREVIOS EN JENKINS:
// -----------------------------------------------
// 1. PLUGINS DE JENKINS REQUERIDOS:
//    - Pipeline (Declarative)
//    - Git Plugin
//    - Maven Integration Plugin
//    - JaCoCo Plugin (para publicar reportes de cobertura)
//    - SonarQube Scanner for Jenkins
//    - Artifactory Plugin (JFrog Artifactory)
//    - Credentials Binding Plugin
//    - HTML Publisher Plugin (para reporte JaCoCo HTML)
//
// 2. CONFIGURACION GLOBAL DE JENKINS (Manage Jenkins > Configure System):
//    a) Maven:
//       - Nombre: Maven-3.9 (debe coincidir con la variable MAVEN_TOOL)
//    b) JDK:
//       - Nombre: JDK-17 (debe coincidir con la variable JDK_TOOL)
//    c) SonarQube Servers:
//       - Nombre del servidor: SonarQube (debe coincidir con SONAR_SERVER_NAME)
//       - URL: URL de tu instancia SonarQube (ej: http://sonarqube.empresa.com)
//       - Token: Credential de tipo 'Secret text' con id 'sonar-auth-token'
//    d) JFrog Artifactory:
//       - Configurar instancia con URL de tu Artifactory
//       - Credential de tipo 'Username with password' con id 'artifactory-credentials'
//
// 3. CREDENCIALES REQUERIDAS EN JENKINS (Manage Jenkins > Credentials):
//    - ID: sonar-auth-token        -> Tipo: Secret Text (token de SonarQube)
//    - ID: artifactory-credentials -> Tipo: Username/Password (usuario Artifactory)
//
// 4. VARIABLES DE ENTORNO REQUERIDAS:
//    - ARTIFACTORY_URL: URL base de Artifactory (configurar como env var en Jenkins
//      o pasar como parámetro del pipeline)
// =============================================================================

pipeline {

    // -------------------------------------------------------
    // Agente de ejecución
    // -------------------------------------------------------
    agent any

    // -------------------------------------------------------
    // Herramientas (deben estar configuradas en Jenkins)
    // -------------------------------------------------------
    tools {
        maven 'Maven-3.9'
        jdk   'JDK-17'
    }

    // -------------------------------------------------------
    // Variables de entorno del pipeline
    // NOTA: No incluir valores sensibles aquí.
    // Los secretos se inyectan desde las Credentials de Jenkins.
    // -------------------------------------------------------
    environment {
        // ---- Identificación del artefacto ----
        APP_NAME          = 'producto-app'
        APP_VERSION       = '1.0.0-SNAPSHOT'

        // ---- SonarQube ----
        // SONAR_SERVER_NAME debe coincidir con el nombre configurado en
        // Manage Jenkins > Configure System > SonarQube Servers
        SONAR_SERVER_NAME = 'SonarQube'
        SONAR_PROJECT_KEY = 'producto-app'

        // ---- Artifactory ----
        // ARTIFACTORY_URL se espera como variable de entorno del agente Jenkins
        // o configurada en Manage Jenkins > Configure System
        // Ejemplo: https://mi-artifactory.empresa.com/artifactory
        ARTIFACTORY_REPO_KEY = 'libs-snapshot-local'

        // ---- Maven ----
        MAVEN_OPTS = '-Xmx512m -XX:+TieredCompilation -XX:TieredStopAtLevel=1'

        // ---- Paths de reportes ----
        JACOCO_EXEC_PATH  = 'target/jacoco.exec'
        JACOCO_HTML_PATH  = 'target/site/jacoco'
        JACOCO_XML_PATH   = 'target/site/jacoco/jacoco.xml'
        SUREFIRE_PATH     = 'target/surefire-reports'
    }

    // -------------------------------------------------------
    // Opciones del pipeline
    // -------------------------------------------------------
    options {
        // Tiempo máximo total del pipeline
        timeout(time: 30, unit: 'MINUTES')
        // Conservar los últimos 10 builds y artefactos
        buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '5'))
        // No ejecutar builds concurrentes del mismo branch
        disableConcurrentBuilds()
        // Mostrar timestamps en consola
        timestamps()
        // Colores ANSI en consola
        ansiColor('xterm')
    }

    // -------------------------------------------------------
    // Parámetros del pipeline (opcionales, permiten triggers manuales con config)
    // -------------------------------------------------------
    parameters {
        booleanParam(
            name: 'SKIP_SONAR',
            defaultValue: false,
            description: 'Omitir el análisis de SonarQube (útil en PRs locales)'
        )
        booleanParam(
            name: 'SKIP_PUBLISH',
            defaultValue: false,
            description: 'Omitir la publicación en Artifactory'
        )
    }

    // =======================================================
    // STAGES
    // =======================================================
    stages {

        // ---------------------------------------------------
        // STAGE 1: Checkout del código fuente
        // ---------------------------------------------------
        stage('Checkout') {
            steps {
                echo '====== [STAGE] Checkout del código fuente ======'
                checkout scm
                script {
                    // Obtener información del commit para trazabilidad
                    env.GIT_COMMIT_SHORT = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()
                    env.GIT_BRANCH_NAME = env.BRANCH_NAME ?: sh(
                        script: 'git rev-parse --abbrev-ref HEAD',
                        returnStdout: true
                    ).trim()
                    echo "Branch: ${env.GIT_BRANCH_NAME} | Commit: ${env.GIT_COMMIT_SHORT}"
                }
            }
        }

        // ---------------------------------------------------
        // STAGE 2: Build (compilación)
        // ---------------------------------------------------
        stage('Build') {
            steps {
                echo '====== [STAGE] Build - Compilación del proyecto ======'
                sh '''
                    mvn clean compile \
                        -Dmaven.test.skip=true \
                        --batch-mode \
                        --no-transfer-progress
                '''
            }
            post {
                success { echo '[Build] Compilación exitosa.' }
                failure { echo '[Build] ERROR: La compilación falló.' }
            }
        }

        // ---------------------------------------------------
        // STAGE 3: Testing (JUnit 5 + JaCoCo)
        // ---------------------------------------------------
        stage('Testing') {
            steps {
                echo '====== [STAGE] Testing - JUnit 5 + JaCoCo ======'
                sh '''
                    mvn verify \
                        --batch-mode \
                        --no-transfer-progress
                '''
            }
            post {
                always {
                    // Publicar resultados de JUnit
                    junit testResults: "${SUREFIRE_PATH}/**/*.xml",
                          allowEmptyResults: false

                    // Publicar reporte de cobertura JaCoCo
                    jacoco(
                        execPattern:      "${JACOCO_EXEC_PATH}",
                        classPattern:     'target/classes',
                        sourcePattern:    'src/main/java',
                        exclusionPattern: '**/model/**,**/ProductosApplication.class',
                        minimumLineCoverage:   '70',
                        minimumBranchCoverage: '60',
                        changeBuildStatus: true
                    )

                    // Publicar reporte HTML de JaCoCo
                    publishHTML(target: [
                        allowMissing:          false,
                        alwaysLinkToLastBuild: true,
                        keepAll:               true,
                        reportDir:             "${JACOCO_HTML_PATH}",
                        reportFiles:           'index.html',
                        reportName:            'JaCoCo Coverage Report'
                    ])
                }
                success { echo '[Testing] Todas las pruebas pasaron con cobertura suficiente.' }
                failure { echo '[Testing] ERROR: Pruebas fallidas o cobertura insuficiente.' }
            }
        }

        // ---------------------------------------------------
        // STAGE 4: Análisis SonarQube
        // ---------------------------------------------------
        stage('Sonar Analysis') {
            when {
                // Omitir si el parámetro SKIP_SONAR está activado
                not { expression { return params.SKIP_SONAR } }
            }
            steps {
                echo '====== [STAGE] Sonar - Análisis de calidad de código ======'
                // El bloque withSonarQubeEnv inyecta automáticamente:
                //   SONAR_HOST_URL y SONAR_AUTH_TOKEN
                // desde la configuración de Jenkins (sin exponer credenciales)
                withSonarQubeEnv("${SONAR_SERVER_NAME}") {
                    sh """
                        mvn sonar:sonar \
                            -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                            -Dsonar.projectName=${APP_NAME} \
                            -Dsonar.projectVersion=${APP_VERSION}-${GIT_COMMIT_SHORT} \
                            -Dsonar.coverage.jacoco.xmlReportPaths=${JACOCO_XML_PATH} \
                            --batch-mode \
                            --no-transfer-progress
                    """
                }
            }
            post {
                success { echo '[Sonar] Análisis completado. Revisar el dashboard de SonarQube.' }
                failure { echo '[Sonar] ERROR: El análisis de SonarQube falló.' }
            }
        }

        // ---------------------------------------------------
        // STAGE 5: Quality Gate (SonarQube)
        // ---------------------------------------------------
        stage('Quality Gate') {
            when {
                not { expression { return params.SKIP_SONAR } }
            }
            steps {
                echo '====== [STAGE] Quality Gate - Verificando umbral de calidad ======'
                // Espera el resultado del Quality Gate de SonarQube
                // abortPipeline: true -> el pipeline falla si el QG falla
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
            post {
                success { echo '[Quality Gate] PASSED: El código cumple los estándares de calidad.' }
                failure { echo '[Quality Gate] FAILED: El código no supera el Quality Gate de SonarQube.' }
            }
        }

        // ---------------------------------------------------
        // STAGE 6: Package (generar el JAR final)
        // ---------------------------------------------------
        stage('Package') {
            steps {
                echo '====== [STAGE] Package - Generando JAR ejecutable ======'
                sh '''
                    mvn package \
                        -Dmaven.test.skip=true \
                        --batch-mode \
                        --no-transfer-progress
                '''
            }
            post {
                success {
                    // Archivar el JAR como artefacto de Jenkins
                    archiveArtifacts artifacts: 'target/*.jar',
                                     fingerprint: true,
                                     allowEmptyArchive: false
                    echo '[Package] JAR generado y archivado correctamente.'
                }
                failure { echo '[Package] ERROR: No se pudo generar el JAR.' }
            }
        }

        // ---------------------------------------------------
        // STAGE 7: Publicar en JFrog Artifactory
        // Solo se ejecuta si Build, Testing y Sonar son exitosos.
        // ---------------------------------------------------
        stage('Publish to Artifactory') {
            when {
                allOf {
                    // No omitir publicación
                    not { expression { return params.SKIP_PUBLISH } }
                    // Solo publicar desde la rama principal (main o master)
                    anyOf {
                        branch 'main'
                        branch 'master'
                        // Descomentar para publicar también desde develop:
                        // branch 'develop'
                    }
                }
            }
            steps {
                echo '====== [STAGE] Publish - Publicando artefacto en JFrog Artifactory ======'
                // Las credenciales se leen desde Jenkins Credentials Store
                // usando el ID 'artifactory-credentials'.
                // La variable ARTIFACTORY_URL debe estar configurada en el agente.
                withCredentials([
                    usernamePassword(
                        credentialsId: 'artifactory-credentials',
                        usernameVariable: 'ARTIFACTORY_USER',
                        passwordVariable: 'ARTIFACTORY_PASSWORD'
                    )
                ]) {
                    sh """
                        mvn deploy \
                            -Dmaven.test.skip=true \
                            -DaltDeploymentRepository=artifactory::default::${ARTIFACTORY_URL}/${ARTIFACTORY_REPO_KEY} \
                            -Dusername=${ARTIFACTORY_USER} \
                            -Dpassword=${ARTIFACTORY_PASSWORD} \
                            --batch-mode \
                            --no-transfer-progress
                    """
                }
            }
            post {
                success {
                    echo "[Publish] Artefacto publicado en Artifactory: ${ARTIFACTORY_REPO_KEY}"
                }
                failure {
                    echo '[Publish] ERROR: No se pudo publicar el artefacto en Artifactory.'
                }
            }
        }

    } // end stages

    // =======================================================
    // POST (acciones después del pipeline completo)
    // =======================================================
    post {
        always {
            echo '====== Pipeline finalizado ======'
            // Limpiar workspace para liberar espacio en el agente
            cleanWs(
                cleanWhenSuccess:  true,
                cleanWhenFailure:  false,
                cleanWhenAborted:  true,
                notFailBuild:      true
            )
        }
        success {
            echo "Pipeline EXITOSO - Branch: ${env.GIT_BRANCH_NAME} | Commit: ${env.GIT_COMMIT_SHORT}"
            // Aquí se puede agregar notificación (Slack, email, etc.)
            // emailext subject: "BUILD SUCCESS: ${APP_NAME}",
            //          body: "El pipeline completó exitosamente.",
            //          to: 'equipo@empresa.com'
        }
        failure {
            echo "Pipeline FALLIDO - Revisar los logs del stage que falló."
            // emailext subject: "BUILD FAILURE: ${APP_NAME}",
            //          body: "El pipeline falló. Revisar Jenkins para más detalles.",
            //          to: 'equipo@empresa.com'
        }
        unstable {
            echo "Pipeline INESTABLE - Pruebas fallidas o cobertura insuficiente."
        }
    }

} // end pipeline
