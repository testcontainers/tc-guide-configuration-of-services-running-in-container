package com.testcontainers.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;

@Testcontainers
class LocalStackTest {

  static final String bucketName = "mybucket";

  static URI s3Endpoint;
  static String accessKey;
  static String secretKey;
  static String region;

  @Container
  static LocalStackContainer localStack = new LocalStackContainer(
    DockerImageName.parse("localstack/localstack:3.4.0")
  );

  @BeforeAll
  static void beforeAll() throws IOException, InterruptedException {
    s3Endpoint = localStack.getEndpointOverride(S3);
    accessKey = localStack.getAccessKey();
    secretKey = localStack.getSecretKey();
    region = localStack.getRegion();

    localStack.execInContainer("awslocal", "s3", "mb", "s3://" + bucketName);

    org.testcontainers.containers.Container.ExecResult execResult =
      localStack.execInContainer("awslocal", "s3", "ls");
    String stdout = execResult.getStdout();
    int exitCode = execResult.getExitCode();
    assertTrue(stdout.contains(bucketName));
    assertEquals(0, exitCode);
  }

  @Test
  void shouldListBuckets() {
    StaticCredentialsProvider credentialsProvider =
      StaticCredentialsProvider.create(
        AwsBasicCredentials.create(accessKey, secretKey)
      );
    S3Client s3 = S3Client
      .builder()
      .endpointOverride(s3Endpoint)
      .credentialsProvider(credentialsProvider)
      .region(Region.of(region))
      .build();

    List<String> s3Buckets = s3
      .listBuckets()
      .buckets()
      .stream()
      .map(Bucket::name)
      .toList();

    assertTrue(s3Buckets.contains(bucketName));
  }
}
