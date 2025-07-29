package it.popso.bicomp.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import it.popso.bicomp.exception.FileManagerException;

public interface FileService {
	
	void upload(MultipartFile file) throws FileManagerException, IOException;
	
}
