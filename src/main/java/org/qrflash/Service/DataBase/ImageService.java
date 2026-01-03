package org.qrflash.Service.DataBase;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.qrflash.Controller.AdminController;
import org.qrflash.Exeption.ImageUploadException;
import org.qrflash.properties.MinioProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public String upload(UUID db, Long productId, MultipartFile file){
        try{
            createBucket();
        }catch(Exception e){
            log.error("Uppload::Error createBucket", e);
            throw  new ImageUploadException("Error creating bucket");
        }
        if(file.isEmpty() || file.getOriginalFilename() == null){
            throw new ImageUploadException("File with photo is empty");
        }

        String fileName = generateFileName(file, AdminController.formatedUUid(db), productId);
        try(InputStream inputStream = file.getInputStream()){
            saveImage(inputStream, fileName);
            return fileName;
        }catch (Exception e){
            throw new ImageUploadException("Error saving file");
        }
    }

    @SneakyThrows
    private void createBucket(){
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioProperties.getBucket())
                .build());
        if(!found){
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .build());
        }
    }

    private String generateFileName(MultipartFile file, String database, Long productId){
        String extension = getExtension(file);
        String fileName = productId + "."  + extension;
        String fullName = database + "/" + fileName;
        return fullName;
    }

    private String getExtension(MultipartFile file){
        return file.getOriginalFilename()
                .substring(file.getOriginalFilename().lastIndexOf(".")+1);
    }

    @SneakyThrows
    private void saveImage(InputStream inputStream, String fileName){
        minioClient.putObject(PutObjectArgs.builder()
                .stream(inputStream, inputStream.available(), -1)
                .bucket(minioProperties.getBucket())
                .object(fileName)
                .build());
    }

    public InputStream download(String filename) throws Exception{
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(minioProperties.getBucket())
                .object(filename)
                .build());
    }

    public String generatePresignedUrl(String filename) {
        try{
            if (filename.isEmpty() || filename == null || filename.equals("example.png")) {
                return "";
            }
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioProperties.getBucket())
                            .object(filename)
                            .expiry(15, TimeUnit.MINUTES)
                            .build()
            );

            //return url.replace("http://minio:9000", "http://51.21.255.211");
        }catch(Exception e){
            throw  new RuntimeException("Error creating bucket", e);
        }
    }
}
