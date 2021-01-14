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
    }
    else {
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
    def repoUrl = getDockerReposirotyUrl(environment)
    def awsRegion = getAwsRegion(environment)
    def shortCommit = appVersion[0..6]

    if(CRON_APP.equalsIgnoreCase("true")){
        echo "Updating cron ${appName} to ${environment} with image ${repoUrl}/${appRepoName}:${shortCommit}"
        updateCron("${repoUrl}/${appRepoName}", "${shortCommit}", "${environment}", "${appName}", "${awsRegion}")
    } else {
        echo "Deploying ${appName} to ${environment} with image ${repoUrl}/${appRepoName}:${shortCommit}"
        deploy("${repoUrl}/${appRepoName}", "${shortCommit}", "${environment}", "${appName}", "${awsRegion}")
    }
}

def getEnvironmentName() {

    def branchName = scm.branches[0].name.split("/").last()
    def environment

    switch(branchName) {
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

def getDockerReposirotyUrl(String environment) {

    def repoUrl

    switch(environment) {
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

    switch(environment) {
        case "production":
            region = "us-east-1"
            break
        default:
            region = "us-east-2"
            break
    }

    return region

}

def deploy(String imgRepo, String imgVersion, String env, String appName, String awsRegion){

    // AWS_ACCESS_KEY_ID & AWS_SECRET_ACCESS_KEY &
    // PROD_AWS_ACCESS_KEY_ID & PROD_AWS_SECRET_ACCESS_KEY
    // variables are provided by the Jenkins Environment block.

    def awsAccessKey = "${AWS_ACCESS_KEY_ID}"
    def awsSecretAccessKey = "${AWS_SECRET_ACCESS_KEY}"

    if(env.equalsIgnoreCase("production")) {
        awsAccessKey = "${PROD_AWS_ACCESS_KEY_ID}"
        awsSecretAccessKey = "${PROD_AWS_SECRET_ACCESS_KEY}"
    }

    println("""
     docker run fabfuel/ecs-deploy:1.11.0 ecs deploy \
       ${env} \
       ${env}-${appName} \
       --region ${awsRegion} \
       --access-key-id ${awsAccessKey} \
       --secret-access-key ${awsSecretAccessKey} \
       --no-deregister \
       --image ${appName} ${imgRepo}:${imgVersion} \
       --image datadog-agent datadog/agent:latest 
  """)

}

def updateCron(String imgRepo, String imgVersion, String env, String appName, String awsRegion){

    // AWS_ACCESS_KEY_ID & AWS_SECRET_ACCESS_KEY &
    // PROD_AWS_ACCESS_KEY_ID & PROD_AWS_SECRET_ACCESS_KEY
    // variables are provided by the Jenkins Environment block.

    def awsAccessKey = "${AWS_ACCESS_KEY_ID}"
    def awsSecretAccessKey = "${AWS_SECRET_ACCESS_KEY}"

    if(env.equalsIgnoreCase("production")) {
        awsAccessKey = "${PROD_AWS_ACCESS_KEY_ID}"
        awsSecretAccessKey = "${PROD_AWS_SECRET_ACCESS_KEY}"
    }

    println("""
     docker run fabfuel/ecs-deploy:1.11.0 ecs cron \
       ${env} \
       ${env}-${appName} \
       ${env}-${appName}-job \
       --region ${awsRegion} \
       --access-key-id ${awsAccessKey} \
       --secret-access-key ${awsSecretAccessKey} \
       --no-deregister \
       --image ${appName} ${imgRepo}:${imgVersion}
  """)
}
