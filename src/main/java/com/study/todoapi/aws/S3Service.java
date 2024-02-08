package com.study.todoapi.aws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class S3Service {

    // s3버킷을 제어하는 객체
    private S3Client s3;

    @Value("${aws.credentials.accessKey}")
    private String accessKey;
    @Value("${aws.credentials.secretKey}")
    private String secretKey;
    @Value("${aws.region}")
    private String region;
    @Value("${aws.bucketName}")
    private String bucketName;


    // aws s3에 접속하여 인증을 처리하는 로직

    @PostConstruct  // S3Service객체가 생성될때 딱 1번 실행한다
    private void initAmazonS3() {
        // 액세스키와 비밀키로 사용자 인증하기
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        this.s3 = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }


    /**
     * 버킷에 파일을 업로드하고 업로드한 버킷의 url을 정보를 리턴
     * @param uploadFile - 업로드할 파일의 실제 로우데이터
     * @param fileName - 업로드할 파일명
     * @return - 버킷에 업로드된 버킷 경로(url)
     */
    public String uploadToS3Bucket(byte[] uploadFile, String fileName) {

        // 업로드할 파일을 S3 객체로 생성
        PutObjectRequest request
                = PutObjectRequest.builder()
                .bucket(bucketName) // 버킷이름
                .key(fileName) // 파일명
                .build();

        // 오브젝트를 버킷에 업로드
        s3.putObject(request, RequestBody.fromBytes(uploadFile));

        // 업로드된 파일의 URL을 반환
        return s3.utilities()
                .getUrl(b -> b.bucket(bucketName).key(fileName))
                .toString()
                ;
    }


}