package com.paycertify.aws.model

class DeploymentLayout {
    private String fullBranchName

    DeploymentLayout(String fullBranchName) {
        this.fullBranchName = fullBranchName
    }

    String getEnvironmentName() {
        def branchName = fullBranchName.split("/").last()

        switch (branchName) {
            case "master":
                return "production"
            case "main":
                return "production"
            case "staging":
                return "staging"
            default:
                return branchName
        }
    }

    String getDockerRepositoryUrl() {
        String environment = getEnvironmentName()

        switch (environment) {
            case "production":
                return "670631891947.dkr.ecr.us-east-1.amazonaws.com"
            default:
                return "670631891947.dkr.ecr.us-east-1.amazonaws.com"
        }
    }

    String getAwsRegion() {
        String environment = getEnvironmentName()

        def region

        switch (environment) {
            case "production":
                return "us-east-1"
            default:
                return "us-east-2"
        }
    }
}
