node('android') {
    stage('Checkout') {
        checkout scm
    }

    stage('Clean') {
        sh "./gradlew clean"
    }

    stage('Static Analysis') {
        checkLintAndPublishResults()
    }

    stage('Unit Test') {
        runUnitTestAndPublishResults()
    }

    stage('Assemble') {
        generateAndArchiveAPK()
    }
}

def checkLintAndPublishResults() {
    try {
        sh './gradlew :app:lintDevDebug'
    } catch(err) {
    }
    String file = 'app/build/reports/lint-results-devDebug.xml'
    androidLint pattern: file
}

def runUnitTestAndPublishResults() {
    failure = false
    try {
        sh './gradlew :app:testDevDebugUnitTest'
    } catch(err) {
        failure = true
    } finally {
        String results = 'app/build/test-results/testDevDebugUnitTest/*.xml'
        step([$class: 'JUnitResultArchiver', testResults: results])
    }
    if (failure) {
        error('Unit Test failed')
    }
}

def generateAndArchiveAPK() {
    sh './gradlew :app:assembleDevDebug'
    archiveArtifacts artifacts: 'app/build/outputs/apk/**/*.apk', excludes: 'app/build/outputs/apk/**/*.json'
}
