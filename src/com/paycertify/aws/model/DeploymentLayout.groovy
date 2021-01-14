package com.paycertify.aws.model

class DeploymentLayout {
    private final AwsAccount awsAccount
    private final String environmentName

    DeploymentLayout(String fullBranchName) {
        final String lastBranchName = fullBranchName.split("/").last()

        switch (lastBranchName) {
            case "master":
            case "main":
            case "production":
                this.awsAccount = AwsAccount.PRODUCTION
                this.environmentName = "production"
                break;
            case "sandbox":
                this.awsAccount = AwsAccount.PRODUCTION
                this.environmentName = "sandbox"
                break;
            default:
                this.awsAccount = fullBranchName.startsWith("env/") ? AwsAccount.PRE_PROD : AwsAccount.NON_PROD
                this.environmentName = lastBranchName
                break;
        }
    }

    AwsAccount getAwsAccount() {
        return awsAccount
    }

    String getAwsRegion() {
        switch (environmentName) {
            case "production":
                return "us-east-1"
            default:
                return "us-east-2"
        }
    }

    String getEnvironmentName() {
        return environmentName
    }

    String getDockerRepositoryUrl() {
        switch (environmentName) {
            case "production":
                return "670631891947.dkr.ecr.us-east-1.amazonaws.com"
            default:
                return "670631891947.dkr.ecr.us-east-1.amazonaws.com"
        }
    }
}
