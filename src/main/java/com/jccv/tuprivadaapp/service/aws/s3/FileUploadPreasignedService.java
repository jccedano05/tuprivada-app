package com.jccv.tuprivadaapp.service.aws.s3;


import com.jccv.tuprivadaapp.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FileUploadPreasignedService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadPreasignedService.class);

    @Value("${aws.accessKeyId}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey}")
    private String secretAccessKey;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    public URL generatePresignedUrl(String fileName, String contentType) {
        try {

            if (fileName == null || fileName.isEmpty()) {
                throw new BadRequestException("File name must not be empty");
            }
            if (contentType == null || contentType.isEmpty()) {
                throw new BadRequestException("Content type must not be empty");
            }


            // Configurar las credenciales
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
            Region awsRegion = Region.of(region);
            S3Presigner presigner = S3Presigner.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                    .region(awsRegion)
                    .build();



            // Crear la solicitud para obtener el Presigned URL
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))  // El tiempo que el presigned URL será válido
                    .putObjectRequest(objectRequest)
                    .build();

            // Generar la URL prefirmada
            URL presignedUrl = presigner.presignPutObject(presignRequest).url();

            // Logging para depurar
            logger.info("Generated presigned URL for file: {}", fileName);

            return presignedUrl;
        } catch (S3Exception e) {
            logger.error("Error while generating presigned URL for file: {}. AWS Error: {}", fileName, e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Error while generating presigned URL", e);
        }catch (Exception e) {
            logger.error("Unexpected error while generating presigned URL for file: {}", fileName, e);
            throw new RuntimeException("Unexpected error while generating presigned URL", e);
        }
    }



    public URL generatePresignedDownloadUrl(String fileName) {
        try {
            if (fileName == null || fileName.isEmpty()) {
                throw new BadRequestException("File name must not be empty");
            }

            // Configurar las credenciales
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
            Region awsRegion = Region.of(region);
            S3Presigner presigner = S3Presigner.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                    .region(awsRegion)
                    .build();

            // Crear la solicitud para obtener el Presigned URL de descarga
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))  // El tiempo que la URL prefirmada será válida
                    .getObjectRequest(getObjectRequest)
                    .build();

            // Generar la URL prefirmada
            URL presignedUrl = presigner.presignGetObject(presignRequest).url();

            // Logging para depurar
            logger.info("Generated presigned URL for downloading file: {}", fileName);

            return presignedUrl;
        } catch (S3Exception e) {
            logger.error("Error while generating presigned URL for downloading file: {}. AWS Error: {}", fileName, e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Error while generating presigned URL", e);
        } catch (Exception e) {
            logger.error("Unexpected error while generating presigned URL for downloading file: {}", fileName, e);
            throw new RuntimeException("Unexpected error while generating presigned URL", e);
        }
    }

}