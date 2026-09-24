pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "coeturi/hello-app"
        NAMESPACE = "dev"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build with Maven') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    env.IMAGE_TAG = "${env.BUILD_NUMBER}"
                    sh "docker build -t ${DOCKER_IMAGE}:${env.IMAGE_TAG} ."
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                    sh "docker push ${DOCKER_IMAGE}:${env.IMAGE_TAG}"
                }
            }
        }

        stage('Deploy to K8s') {
            steps {
                withKubeConfig([credentialsId: 'k8s-kubeconfig']) {
                    sh """
                        kubectl create deployment hello-app \
                          --image=${DOCKER_IMAGE}:${env.IMAGE_TAG} \
                          -n ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -
                        kubectl rollout status deployment/hello-app -n ${NAMESPACE} --timeout=60s
                    """
                }
            }
        }
    }
}
