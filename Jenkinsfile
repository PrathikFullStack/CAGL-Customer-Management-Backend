pipeline {
    agent any

    parameters {
        string(name: 'SERVICE_NAME', defaultValue: 'AppzillonBanking-CM', description: 'Microservice directory name under apz_java_microservices/')
        string(name: 'DOCKER_IMAGE_NAME', defaultValue: 'cagl-customer-management', description: 'Docker image repository name')
    }

    environment {
        // Docker Hub settings (Free tier)
        DOCKER_HUB_USER       = 'your-dockerhub-username'
        DOCKER_CREDENTIALS_ID = 'docker-hub-credentials'
        
        // Free Cloud Deploy Hook (e.g., Render.com Deploy Hook URL stored in Jenkins Secret Text)
        DEPLOY_HOOK_CRED_ID   = 'render-deploy-hook-url'
        
        IMAGE_TAG             = "${env.BUILD_NUMBER}"
    }

    tools {
        maven 'Maven 3'
        jdk 'JDK 17'
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Build Shared Libraries') {
            steps {
                echo 'Installing dependencies-lib modules...'
                bat 'mvn clean install -DskipTests -f dependencies-lib/pom.xml'
            }
        }

        stage('Build & Test Microservice') {
            steps {
                echo "Packaging microservice: ${params.SERVICE_NAME}..."
                bat "mvn clean package -DskipTests -f apz_java_microservices/${params.SERVICE_NAME}/pom.xml"
            }
        }

        stage('Build Docker Image') {
            steps {
                echo "Building Docker Image: ${DOCKER_HUB_USER}/${params.DOCKER_IMAGE_NAME}:${IMAGE_TAG}..."
                bat "docker build --build-arg SERVICE_NAME=${params.SERVICE_NAME} -t ${DOCKER_HUB_USER}/${params.DOCKER_IMAGE_NAME}:${IMAGE_TAG} -t ${DOCKER_HUB_USER}/${params.DOCKER_IMAGE_NAME}:latest ."
            }
        }

        stage('Push to Docker Hub') {
            steps {
                echo 'Pushing Docker image to Docker Hub...'
                withCredentials([usernamePassword(credentialsId: "${DOCKER_CREDENTIALS_ID}", usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
                    bat "docker push ${DOCKER_HUB_USER}/${params.DOCKER_IMAGE_NAME}:${IMAGE_TAG}"
                    bat "docker push ${DOCKER_HUB_USER}/${params.DOCKER_IMAGE_NAME}:latest"
                    bat "docker logout"
                }
            }
        }

        stage('Deploy to Free Cloud (Render/Koyeb)') {
            steps {
                echo 'Triggering automated redeployment on Free Cloud...'
                withCredentials([string(credentialsId: "${DEPLOY_HOOK_CRED_ID}", variable: 'HOOK_URL')]) {
                    bat 'curl -X POST %HOOK_URL%'
                }
            }
        }
    }

    post {
        always {
            echo 'Cleaning up build images...'
            bat "docker rmi ${DOCKER_HUB_USER}/${params.DOCKER_IMAGE_NAME}:${IMAGE_TAG} ${DOCKER_HUB_USER}/${params.DOCKER_IMAGE_NAME}:latest || exit 0"
        }
        success {
            echo "Successfully built and deployed ${params.SERVICE_NAME}!"
        }
        failure {
            echo 'Deployment pipeline encountered errors. Please check the logs.'
        }
    }
}
