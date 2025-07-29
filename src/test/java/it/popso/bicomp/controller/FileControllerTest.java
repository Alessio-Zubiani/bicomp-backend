package it.popso.bicomp.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import it.popso.bicomp.exception.FileManagerException;
import it.popso.bicomp.service.FileService;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class FileControllerTest {
	
	@Mock
	private FileService fileService;
	
	private FileController controller;
	

	@BeforeEach
	public void setup() {
		this.controller = new FileController(this.fileService);
	}
	
	@Test
	@Order(1)
	void testUpload() throws FileNotFoundException, IOException, FileManagerException {
		
		File file = new File("src/test/resources/file/1Q2024_ExternalCodeSets_v2_TEST.xlsx");
		MultipartFile multipartFile = new MockMultipartFile("1Q2024_ExternalCodeSets_v2_TEST.xlsx", "../1Q2024_ExternalCodeSets_v2_TEST.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new FileInputStream(file));
		
		Mockito.doNothing().when(this.fileService).upload(Mockito.any(MultipartFile.class));
		
		this.controller.uploadFile(multipartFile);
		
		verify(this.fileService, times(1)).upload(Mockito.any(MultipartFile.class));
	}
	
	@Test
	@Order(2)
	void testUploadMultipartException() throws FileNotFoundException, IOException, FileManagerException {
		
		File file = new File("src/test/resources/file/1Q2024_ExternalCodeSets_v2_TEST.xlsx");
		MultipartFile multipartFile = new MockMultipartFile("1Q2024_ExternalCodeSets_v2_TEST.xlsx", "../1Q2024_ExternalCodeSets_v2_TEST.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new FileInputStream(file));
		
		Mockito.doThrow(new MultipartException("MultipartException")).when(this.fileService).upload(Mockito.any(MultipartFile.class));
		
		assertThrows(MultipartException.class, () -> {
			this.controller.uploadFile(multipartFile);
		});
		
		verify(this.fileService, times(1)).upload(Mockito.any(MultipartFile.class));
	}
	
	@Test
	@Order(3)
	void testUploadFileManagerException() throws FileNotFoundException, IOException, FileManagerException {
		
		File file = new File("src/test/resources/file/1Q2024_ExternalCodeSets_v2_TEST.xlsx");
		MultipartFile multipartFile = new MockMultipartFile("1Q2024_ExternalCodeSets_v2_TEST.xlsx", "../1Q2024_ExternalCodeSets_v2_TEST.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new FileInputStream(file));
		
		Mockito.doThrow(new FileManagerException("FileManagerException")).when(this.fileService).upload(Mockito.any(MultipartFile.class));
		
		assertThrows(FileManagerException.class, () -> {
			this.controller.uploadFile(multipartFile);
		});
		
		verify(this.fileService, times(1)).upload(Mockito.any(MultipartFile.class));
	}
	
	@Test
	@Order(4)
	void testUploadIOException() throws FileNotFoundException, IOException, FileManagerException {
		
		File file = new File("src/test/resources/file/1Q2024_ExternalCodeSets_v2_TEST.xlsx");
		MultipartFile multipartFile = new MockMultipartFile("1Q2024_ExternalCodeSets_v2_TEST.xlsx", "../1Q2024_ExternalCodeSets_v2_TEST.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new FileInputStream(file));
		
		Mockito.doThrow(new IOException("IOException")).when(this.fileService).upload(Mockito.any(MultipartFile.class));
		
		assertThrows(FileManagerException.class, () -> {
			this.controller.uploadFile(multipartFile);
		});
		
		verify(this.fileService, times(1)).upload(Mockito.any(MultipartFile.class));
	}
	
}
