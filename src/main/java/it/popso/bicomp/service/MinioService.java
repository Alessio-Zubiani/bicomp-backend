package it.popso.bicomp.service;

import java.io.InputStream;
import java.util.List;

import io.minio.ObjectWriteResponse;
import io.minio.Result;
import io.minio.messages.Item;
import it.popso.bicomp.exception.BicompException;

public interface MinioService {
	
	boolean bucketExists(String bucketName) throws BicompException;
	
	boolean isFolderExist(String bucketName, String objectName);
	
	List<Item> getObjectsByPrefixAndSuffix(String bucketName, String subFolder, String prefix, String suffix, boolean recursive) throws BicompException;
	
	InputStream getObject(String bucketName, String objectName) throws BicompException;
	
	Iterable<Result<Item>> listObjects(String bucketName, String prefix, boolean recursive) throws BicompException;
	
	void uploadFile(String bucketName, String objectName, InputStream inputStream) throws BicompException;
	
	ObjectWriteResponse createDir(String bucketName, String objectName) throws BicompException;
	
	void copyFile(String sourceBucketName, String sourceObjectName, String targetBucketName, String targetObjectName) throws BicompException;
	
	void removeFile(String bucketName, String objectName) throws BicompException;

}
