package it.popso.bicomp.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import io.minio.BucketExistsArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Item;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {
	
	private final MinioClient minioClient;

	
	@Override
	public boolean bucketExists(String bucketName) throws BicompException {
		
		try {
			return this.minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {
			throw new BicompException(e);
		}
	}

	@Override
	public boolean isFolderExist(String bucketName, String objectName) {
		
		boolean exist = false;
        try {
            Iterable<Result<Item>> results = this.minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).prefix(objectName).recursive(false).build());
            for(Result<Item> result : results) {
                Item item = result.get();
                if(item.isDir() && objectName.equals(item.objectName())) {
                    exist = true;
                }
            }
        } catch (Exception e) {
            log.error("Folder exists exception: ", e);
            exist = false;
        }
        
        return exist;
	}

	@Override
	public List<Item> getObjectsByPrefixAndSuffix(String bucketName, String subFolder, String prefix, String suffix, boolean recursive) throws BicompException {
		
		List<Item> list = new ArrayList<>();
		try {
	        Iterable<Result<Item>> objectsIterator = this.minioClient.listObjects(
	                ListObjectsArgs.builder().bucket(bucketName).prefix(subFolder).recursive(recursive).build());
	        if(objectsIterator != null) {
	            for(Result<Item> o : objectsIterator) {
	                Item item = o.get();
	                String fileName = item.objectName().substring(subFolder.length());
	                if(fileName.startsWith(prefix) && fileName.endsWith(suffix)) {
	                	list.add(item);
	                }
	            }
	        }
		} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException
				| InsufficientDataException | InternalException | InvalidResponseException
				| NoSuchAlgorithmException | ServerException | XmlParserException | IOException e) {
			throw new BicompException(e);
		}
		
        return list;
	}

	@Override
	public InputStream getObject(String bucketName, String objectName) throws BicompException {
		
		try {
			return this.minioClient.getObject(
			        GetObjectArgs.builder()
			                .bucket(bucketName)
			                .object(objectName)
			                .build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {
			throw new BicompException(e);
		}
	}

	@Override
	public Iterable<Result<Item>> listObjects(String bucketName, String prefix, boolean recursive) throws BicompException {
		
		return this.minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(prefix)
                        .recursive(recursive)
                        .build());
	}

	@Override
	public void uploadFile(String bucketName, String objectName, InputStream inputStream) throws BicompException {
		
		try {
			this.minioClient.putObject(
			        PutObjectArgs.builder()
			                .bucket(bucketName)
			                .object(objectName)
			                .stream(inputStream, inputStream.available(), -1)
			                .build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {
			throw new BicompException(e);
		}
	}

	@Override
	public ObjectWriteResponse createDir(String bucketName, String objectName) throws BicompException {
		
		try {
			return this.minioClient.putObject(
			        PutObjectArgs.builder()
			                .bucket(bucketName)
			                .object(objectName)
			                .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
			                .build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {
			throw new BicompException(e);
		}
	}
	
	@Override
	public void copyFile(String sourceBucketName, String sourceObjectName, String targetBucketName, String targetObjectName) throws BicompException {
        
		try {
			this.minioClient.copyObject(
			        CopyObjectArgs.builder()
			                .bucket(targetBucketName)
			                .object(targetObjectName)
			                .source(CopySource.builder().bucket(sourceBucketName).object(sourceObjectName).build())
			                .build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {
			throw new BicompException(e);
		}
    }

	@Override
    public void removeFile(String bucketName, String objectName) throws BicompException {
    	
    	try {
	        this.minioClient.removeObject(
	                RemoveObjectArgs.builder()
	                        .bucket(bucketName)
	                        .object(objectName)
	                        .build());
    	} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {
			throw new BicompException(e);
		}
    }

}
