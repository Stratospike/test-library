import com.paycertify.aws.action.Deploy
import com.paycertify.aws.model.AwsCredentials

def call(params, String appRepoName = null, String appVersion = null) {
    String appName

    if (params instanceof Map) {
        appName = params.applicationName

        if (!appRepoName) {
            appRepoName = params.ecrRepo
        }
        if (!appVersion) {
            appVersion = params.version
        }
    } else {
        appName = params
    }

    if (!appRepoName) {
        appRepoName = appName
    }

    if (!appName) {
        throw new IllegalArgumentException("Missing Argument: applicationName")
    }

    if (!appVersion) {
        throw new IllegalArgumentException("Missing Argument: version")
    }

    def environment = getEnvironmentName()
    def repoUrl = getDockerRepositoryUrl(environment)
    def awsRegion = getAwsRegion(environment)
    def shortCommit = appVersion[0..6]

    def awsAccessKey = "${AWS_ACCESS_KEY_ID}"
    def awsSecretAccessKey = "${AWS_SECRET_ACCESS_KEY}"

    if (environment.equalsIgnoreCase("production")) {
        awsAccessKey = "${PROD_AWS_ACCESS_KEY_ID}"
        awsSecretAccessKey = "${PROD_AWS_SECRET_ACCESS_KEY}"
    }

    def awsCredentials = new AwsCredentials(awsAccessKey, awsSecretAccessKey)
    def ecrPath = "${repoUrl}/${appRepoName}"

    boolean cron = CRON_APP.equalsIgnoreCase("true")
    if (cron) {
        echo "Updating cron ${appName} to ${environment} with image ${repoUrl}/${appRepoName}:${shortCommit}"
    } else {
        echo "Deploying ${appName} to ${environment} with image ${repoUrl}/${appRepoName}:${shortCommit}"
    }

    Deploy deploy = new Deploy(this, awsRegion, awsCredentials, environment, appName, ecrPath, cron)
    deploy.execute()
}

def getEnvironmentName() {

    def branchName = scm.branches[0].name.split("/").last()
    def environment

    switch (branchName) {
        case "master":
            environment = "production"
            break
        case "main":
            environment = "production"
            break
        case "staging":
            environment = "staging"
            break
        default:
            environment = branchName
            break
    }

    return environment
}

def getDockerRepositoryUrl(String environment) {

    def repoUrl

    switch (environment) {
        case "production":
            repoUrl = "670631891947.dkr.ecr.us-east-1.amazonaws.com"
            break
        default:
            repoUrl = "670631891947.dkr.ecr.us-east-1.amazonaws.com"
            break
    }

    return repoUrl
}

def getAwsRegion(String environment) {

    def region

    switch (environment) {
        case "production":
            region = "us-east-1"
            break
        default:
            region = "us-east-2"
            break
    }

    return region
}
