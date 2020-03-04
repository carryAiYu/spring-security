def projectProperties = [
	[$class: 'BuildDiscarderProperty',
		strategy: [$class: 'LogRotator', numToKeepStr: '5']],
	pipelineTriggers([cron('@daily')])
]
properties(projectProperties)

def SUCCESS = hudson.model.Result.SUCCESS.toString()
currentBuild.result = SUCCESS

try {
	parallel check: {
		stage('Check') {
			node {
				checkout scm
				sh "git clean -dfx"
				try {
					withCredentials([usernamePassword(credentialsId: 'gradle_enterprise_cache_user',
							passwordVariable: 'GRADLE_ENTERPRISE_CACHE_PASSWORD',
							usernameVariable: 'GRADLE_ENTERPRISE_CACHE_USERNAME'),
                        string(credentialsId: 'gradle_enterprise_secret_access_key',
							variable: 'GRADLE_ENTERPRISE_ACCESS_KEY')]) {
							withEnv(["JAVA_HOME=${ tool 'jdk8' }",
							"GRADLE_ENTERPRISE_CACHE_USERNAME=${GRADLE_ENTERPRISE_CACHE_USERNAME}",
							"GRADLE_ENTERPRISE_CACHE_PASSWORD=${GRADLE_ENTERPRISE_CACHE_PASSWORD}",
							"GRADLE_ENTERPRISE_ACCESS_KEY=${GRADLE_ENTERPRISE_ACCESS_KEY}"]) {
							sh "./gradlew check --stacktrace"
						}
					}
				} catch(Exception e) {
					currentBuild.result = 'FAILED: check'
					throw e
				} finally {
					junit '**/build/test-results/*/*.xml'
				}
			}
		}
	}
} catch(Exception e) {
	currentBuild.result = 'FAILED: deploys'
	throw e
} finally {
	def buildStatus = currentBuild.result
	def buildNotSuccess =  !SUCCESS.equals(buildStatus)
	def lastBuildNotSuccess = !SUCCESS.equals(currentBuild.previousBuild?.result)

	if(buildNotSuccess || lastBuildNotSuccess) {

		stage('Notifiy') {
			node {
				final def RECIPIENTS = [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']]

				def subject = "${buildStatus}: Build ${env.JOB_NAME} ${env.BUILD_NUMBER} status is now ${buildStatus}"
				def details = """The build status changed to ${buildStatus}. For details see ${env.BUILD_URL}"""

// 				emailext (
// 					subject: subject,
// 					body: details,
// 					recipientProviders: RECIPIENTS,
// 					to: "$SPRING_SECURITY_TEAM_EMAILS"
// 				)
			}
		}
	}
}
