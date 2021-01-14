package com.paycertify.aws.model;

class AwsCredentials {
    final String accessKey
    final String secretAccessKey

    AwsCredentials(String accessKey, String secretAccessKey) {
        this.accessKey = accessKey
        this.secretAccessKey = secretAccessKey
    }
}
