package com.paycertify.aws.action

import com.paycertify.aws.model.AwsCredentials

class Deploy {
    private final String awsRegion
    private final AwsCredentials awsCredentials
    private final String environment
    private final String applicationName
    private final String ecrPath
    private final boolean cron

    Deploy(
            String awsRegion, AwsCredentials awsCredentials,
            String environment, String applicationName,
            String ecrPath, boolean cron) {
        this.awsRegion = awsRegion
        this.awsCredentials = awsCredentials
        this.environment = environment
        this.applicationName = applicationName
        this.ecrPath = ecrPath
        this.cron = cron
    }

    void execute() {
        if (cron) {
            deployCron()
        } else {
            deployApplication()
        }
    }

    private void deployApplication() {
        echo("""
             docker run fabfuel/ecs-deploy:1.11.0 ecs deploy \
               ${environment} \
               ${environment}-${applicationName} \
               --region ${awsRegion} \
               --access-key-id ${awsCredentials.accessKey} \
               --secret-access-key ${awsCredentials.secretAccessKey} \
               --no-deregister \
               --image ${applicationName} ${ecrPath} \
               --image datadog-agent datadog/agent:latest 
          """)

    }

    private void deployCron() {
        echo("""
             docker run fabfuel/ecs-deploy:1.11.0 ecs cron \
               ${environment} \
               ${environment}-${applicationName} \
               ${environment}-${applicationName}-job \
               --region ${awsRegion} \
               --access-key-id ${awsCredentials.accessKey} \
               --secret-access-key ${awsCredentials.secretAccessKey} \
               --no-deregister \
               --image ${applicationName} ${ecrPath}
          """)
    }
}