import com.paycertify.aws.action.Deploy
import com.paycertify.aws.model.AwsAccount
import com.paycertify.aws.model.AwsCredentials
import com.paycertify.aws.model.DeploymentLayout

def call(params, String appRepoName = null, String appVersion = null) {
    (applicationName, repoName, version, cron) = parseParameters(params, appRepoName, appVersion)

    final DeploymentLayout layout = new DeploymentLayout(scm.branches[0].name)
    final String environment = layout.getEnvironmentName()
    final String repoUrl = layout.getDockerRepositoryUrl()
    final String awsRegion = layout.getAwsRegion()
    final AwsCredentials awsCredentials = getAwsCredentials(layout)

    final String shortCommit = version[0..6]
    final String ecrPath = "${repoUrl}/${repoName}:${shortCommit}"

    Deploy deploy = new Deploy(this, awsRegion, awsCredentials, environment, applicationName, ecrPath, cron)
    deploy.execute()
}

private List parseParameters(params, String repoName, String version) {
    String applicationName
    boolean cron = false

    if (params instanceof Map) {
        applicationName = params.applicationName

        if (!repoName) {
            repoName = params.repoName
        }
        if (!version) {
            version = params.version
        }

        if (params.cron) {
            cron = true
        } else if (params.cron == false) {
            cron = false
        }
    } else {
        applicationName = params
    }

    if (cron == null) {
        cron = CRON_APP.equalsIgnoreCase("true")
    }

    if (!repoName) {
        repoName = applicationName
    }

    if (!applicationName) {
        throw new IllegalArgumentException("Missing Argument: applicationName")
    }

    if (!version) {
        throw new IllegalArgumentException("Missing Argument: version")
    }

    [applicationName, repoName, version, cron]
}

private AwsCredentials getAwsCredentials(DeploymentLayout layout) {
    switch (layout.getAwsAccount()) {
        case AwsAccount.NONPROD:
            return new AwsCredentials("${AWS_ACCESS_KEY_ID}", "${AWS_SECRET_ACCESS_KEY}")
        case AwsAccount.PREPROD:
            return new AwsCredentials("${PREPROD_AWS_ACCESS_KEY_ID}", "${PREPROD_AWS_SECRET_ACCESS_KEY}")
        case AwsAccount.PROD:
            return new AwsCredentials("${PROD_AWS_ACCESS_KEY_ID}", "${PROD_AWS_SECRET_ACCESS_KEY}")
        default:
            throw new IllegalStateException("Unknown AWS Account")
    }
}