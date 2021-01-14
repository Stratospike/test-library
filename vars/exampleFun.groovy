import com.paycertify.aws.action.Deploy
import com.paycertify.aws.model.AwsCredentials
import com.paycertify.aws.model.DeploymentLayout

def call(params, String appRepoName = null, String appVersion = null) {
    (applicationName, repoName, version) = parseParameters(params, appRepoName, appVersion)

    final DeploymentLayout layout = new DeploymentLayout(scm.branches[0].name)
    final String environment = layout.getEnvironmentName()
    final String repoUrl = layout.getDockerRepositoryUrl()
    final String awsRegion = layout.getAwsRegion()

    final String shortCommit = version[0..6]
    final String ecrPath = "${repoUrl}/${repoName}:${shortCommit}"
    final AwsCredentials awsCredentials = getAwsCredentials(environment)

    boolean cron = CRON_APP.equalsIgnoreCase("true")

    Deploy deploy = new Deploy(this, awsRegion, awsCredentials, environment, applicationName, ecrPath, cron)
    deploy.execute()
}

private List parseParameters(params, String repoName, String version) {
    String applicationName

    if (params instanceof Map) {
        applicationName = params.applicationName

        if (!repoName) {
            repoName = params.repoName
        }
        if (!version) {
            version = params.version
        }
    } else {
        applicationName = params
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

    [applicationName, repoName, version]
}

private AwsCredentials getAwsCredentials(String environment) {
    if (environment.equalsIgnoreCase("production")) {
        return new AwsCredentials("${PROD_AWS_ACCESS_KEY_ID}", "${PROD_AWS_SECRET_ACCESS_KEY}")
    } else {
        return new AwsCredentials("${AWS_ACCESS_KEY_ID}", "${AWS_SECRET_ACCESS_KEY}")
    }
}
