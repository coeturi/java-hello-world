pipeline {
    agent any

    parameters {
        choice(name: 'TARGET_NS', choices: ['dev', 'stage', 'release', 'prod'], description: 'Target namespace')
    }

    environment {
        JAVA_HOME = "/usr/lib/jvm/java-21-openjdk-amd64"
        PATH = "/usr/lib/jvm/java-21-openjdk-amd64/bin:${env.PATH}"
        DOCKER_IMAGE = "coeturi/hello-app"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build with Maven') {
            steps {
                sh '''
                    export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
                    export PATH=$JAVA_HOME/bin:$PATH
                    mvn clean package -DskipTests
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    env.IMAGE_TAG = "${env.BUILD_NUMBER}"
                    sh 'docker build -t coeturi/hello-app:${IMAGE_TAG} .'
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
                    sh 'docker push coeturi/hello-app:${IMAGE_TAG}'
                }
            }
        }

        stage('Deploy to K8s') {
            steps {
                sh '''
                    minikube image load coeturi/hello-app:${IMAGE_TAG}
                    kubectl create deployment hello-app \
                      --image=coeturi/hello-app:${IMAGE_TAG} \
                      -n ${TARGET_NS} --dry-run=client -o yaml | kubectl apply -f -
                    kubectl rollout status deployment/hello-app -n ${TARGET_NS} --timeout=120s
                '''
            }
        }
    }
}
