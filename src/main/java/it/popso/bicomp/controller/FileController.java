package it.popso.bicomp.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import it.popso.bicomp.aspect.BicompLogger;
import it.popso.bicomp.dto.Response;
import it.popso.bicomp.exception.FileManagerException;
import it.popso.bicomp.service.FileService;
import it.popso.bicomp.utils.DateUtils;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {
	
	private final FileService fileService;
	
	
	@PostMapping("/upload")
	@BicompLogger
	public ResponseEntity<Response<Object>> uploadFile(@RequestParam("file") MultipartFile file) throws MultipartException, FileManagerException {
		
		try {
	    	this.fileService.upload(file);
	    	
	    	return ResponseEntity.ok()
					.body(Response.builder()
						.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
						.message(new StringBuilder("Successfully uploaded file: [").append(file.getOriginalFilename()).append("]").toString())
						.isSuccess(true)
						.response(file.getOriginalFilename())
						.status(HttpStatus.OK.name())
						.statusCode(HttpStatus.OK.value())
						.build()
			);
		}
		catch(IOException e) {
			throw new FileManagerException(e);
		}
	}

}
