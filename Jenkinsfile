//  Archivo Jenkinsfile para proyecto de prueba de selenium
//
//  by: Nicolas Pulido

pipeline {
  options {
      timeout(time: 35, unit: 'MINUTES')
  }
  agent {
    kubernetes {
      label 'slave-labtest-dxc'
      defaultContainer 'jnlp'
      yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    component: ci
spec:
  serviceAccountName: cd-jenkins
  volumes:
  - name: google-cloud-key
    secret:
      secretName: registry-jenkins
  containers:
  - name: java
    image: maven:3.6-jdk-8-alpine
    command:
    - cat
    tty: true
  - name: gcloud
    image: gcr.io/cloud-builders/gcloud
    volumeMounts:
    - name: google-cloud-key
      readOnly: true
      mountPath: "/var/secrets/google"
    command:
    - cat
    env:
    - name: GOOGLE_APPLICATION_CREDENTIALS
      value: /var/secrets/google/key.json
    tty: true
"""
    }
  }
  environment {
    COMMITTER_EMAIL = sh (
      returnStdout: true,
      script: 'git --no-pager show -s --format=\'%ae\''
    ).trim()
    TAG_NAME = sh (
      returnStdout: true,
      script: 'git tag --points-at HEAD | awk NF'
    ).trim()
  }
  stages {
    stage('Initialize') {
      steps {
        container('gcloud') {
          sh 'gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS'
        }
        container('java') {
          sh "mvn -v"
          sh "javac -version"
        }
      }
    }
    stage('Tests Execution') {
      steps {
        echo 'Iniciando Ejecucion de Pruebas'
        container ('java'){
          //sh "mvn build"
          sh "mvn clean install"
          sh "mvn -v"
          sh "mvn test"
        }
      }
    }
    stage('Results Publishing') {
      steps {
        echo 'Publicando Reportes'
      }
    }
  }
  post {
    always {
      echo "Pipeline currentResult: ${currentBuild.currentResult}"
      echo "Pipeline Finalizado"
    }
    aborted {
      echo "Pipeline Abortado"
    }
    failure {
      echo "Pipeline Fallido"
    }
    success {
      echo "Pipeline Exitoso!!"
    }
  }//end post
}//end pipeline
