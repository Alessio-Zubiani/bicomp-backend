package it.popso.bicomp.service;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import it.popso.bicomp.exception.FileManagerException;
import it.popso.bicomp.model.IsoExternalCodeSet;
import it.popso.bicomp.model.IsoFile;
import it.popso.bicomp.properties.BicompConfig;
import it.popso.bicomp.properties.Cgs;
import it.popso.bicomp.properties.Rt1;
import it.popso.bicomp.properties.Tips;
import it.popso.bicomp.repository.IsoExternalCodeSetRepository;
import it.popso.bicomp.repository.IsoFileRepository;
import it.popso.bicomp.service.impl.FileServiceImpl;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
public class FileServiceImplTest {
	
	@Mock
	private IsoFileRepository isoFileRepository;
	
	@Mock
	private IsoExternalCodeSetRepository isoExternalCodeSetRepository;
	
	private BicompConfig config;
	private FileServiceImpl service;
	
	
	@BeforeAll
	public static void init() throws IOException {
		Files.createDirectory(Paths.get("src/test/resources/file/cgs"));
		Files.createDirectory(Paths.get("src/test/resources/file/tips"));
		Files.createDirectory(Paths.get("src/test/resources/file/tips/camt_052"));
		Files.createDirectory(Paths.get("src/test/resources/file/tips/camt_053"));
		Files.createDirectory(Paths.get("src/test/resources/file/rt1"));
	}
	
	@BeforeEach
    public void setup() {
		Cgs cgs = new Cgs();
		cgs.setShare("src/test/resources/file/cgs/");
		cgs.setPrefix("S204SCTPOSOIT22");
		cgs.setSuffix(".B");
		
		Tips tips = new Tips();
		tips.setCamt052Share("src/test/resources/file/tips/camt_052/");
		tips.setCamt052Prefix("camt.052");
		tips.setCamt052Suffix(".xml");
		tips.setCamt053Share("src/test/resources/file/tips/camt_053/");
		tips.setCamt053Prefix("camt.053");
		tips.setCamt053Suffix(".xml");
		
		Rt1 rt1 = new Rt1();
		rt1.setShare("src/test/resources/file/rt1/");
		rt1.setBulkPrefix("RT02SCIPOSOIT");
		rt1.setBulkSuffix(".S");
		rt1.setPsrPrefix("RT02SCIPOSOIT");
		rt1.setPsrSuffix(".P");
		this.config = new BicompConfig();
		this.config.setCgs(cgs);
		this.config.setTips(tips);
		this.config.setRt1(rt1);
		
		this.service = new FileServiceImpl(this.config, this.isoFileRepository, this.isoExternalCodeSetRepository);
	}
	
	@Test
	@Order(1)
	void testUploadIOException() throws IOException {
		
		File file = new File("src/test/resources/file/1Q2024_ExternalCodeSets_v2_TEST.xlsx");
		MultipartFile multipartFile = new MockMultipartFile("1Q2024_ExternalCodeSets_v2_TEST.xlsx", "../1Q2024_ExternalCodeSets_v2_TEST.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new FileInputStream(file));
		
		assertThrows(IOException.class, () -> {
			this.service.upload(multipartFile);
		});
	}
	
	@Test
	@Order(2)
	void testUploadXlsx() throws FileManagerException, IOException {
		
		File file = new File("src/test/resources/file/1Q2024_ExternalCodeSets_v2_TEST.xlsx");
		MultipartFile multipartFile = new MockMultipartFile("1Q2024_ExternalCodeSets_v2_TEST.xlsx", "1Q2024_ExternalCodeSets_v2_TEST.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new FileInputStream(file));
		
		Mockito.when(this.isoFileRepository.save(Mockito.any(IsoFile.class))).thenReturn(IsoFile.builder().build());
		Mockito.when(this.isoExternalCodeSetRepository.save(Mockito.any(IsoExternalCodeSet.class))).thenReturn(IsoExternalCodeSet.builder().build());
		
		this.service.upload(multipartFile);
		
		verify(this.isoFileRepository, times(1)).save(Mockito.any(IsoFile.class));
		verify(this.isoExternalCodeSetRepository, atLeastOnce()).save(Mockito.any(IsoExternalCodeSet.class));
	}
	
	@Test
	@Order(3)
	void testUploadXls() throws FileManagerException, IOException {
		
		File file = new File("src/test/resources/file/1Q2024_ExternalCodeSets_v2_TEST.xls");
		MockMultipartFile multipartFile = new MockMultipartFile("1Q2024_ExternalCodeSets_v2_TEST.xls", "1Q2024_ExternalCodeSets_v2_TEST.xls", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new FileInputStream(file));
		
		Mockito.when(this.isoFileRepository.save(Mockito.any(IsoFile.class))).thenReturn(IsoFile.builder().build());
		Mockito.when(this.isoExternalCodeSetRepository.save(Mockito.any(IsoExternalCodeSet.class))).thenReturn(IsoExternalCodeSet.builder().build());
		
		this.service.upload(multipartFile);
		
		verify(this.isoFileRepository, times(1)).save(Mockito.any(IsoFile.class));
		verify(this.isoExternalCodeSetRepository, atLeastOnce()).save(Mockito.any(IsoExternalCodeSet.class));
	}
	
	@Test
	@Order(4)
	void testUploadCgs() throws FileManagerException, IOException {
		
		Path path = Paths.get("src/test/resources/file/S204SCTPOSOIT22_processLmrReport_Plcr.B");
		MockMultipartFile multipartFile = new MockMultipartFile("S204SCTPOSOIT22_processLmrReport_Plcr.B", "S204SCTPOSOIT22_processLmrReport_Plcr.B", "", new FileInputStream(path.toFile()));
		
		this.service.upload(multipartFile);
		
		assertThat(Files.exists(Paths.get("src/test/resources/file/cgs/S204SCTPOSOIT22_processLmrReport_Plcr.B"))).isTrue();
		
		verify(this.isoFileRepository, times(0)).save(Mockito.any(IsoFile.class));
		verify(this.isoExternalCodeSetRepository, times(0)).save(Mockito.any(IsoExternalCodeSet.class));
		
		Files.delete(new File("src/test/resources/file/cgs/S204SCTPOSOIT22_processLmrReport_Plcr.B").toPath());
	}
	
	@Test
	@Order(5)
	void testUploadCgsUnknownSuffix() throws FileManagerException, IOException {
		
		Path path = Paths.get("src/test/resources/file/S204SCTPOSOIT22_processLmrReport_Plcr.A");
		MockMultipartFile multipartFile = new MockMultipartFile("S204SCTPOSOIT22_processLmrReport_Plcr.A", "S204SCTPOSOIT22_processLmrReport_Plcr.A", "", new FileInputStream(path.toFile()));
		
		assertThrows(FileManagerException.class, () -> {
			this.service.upload(multipartFile);
		});
	}
	
	@Test
	@Order(6)
	void testUploadTipsCamt052() throws FileManagerException, IOException {
		
		Path path = Paths.get("src/test/resources/file/camt.052.xml");
		MockMultipartFile multipartFile = new MockMultipartFile("camt.052.xml", "camt.052.xml", "", new FileInputStream(path.toFile()));
		
		this.service.upload(multipartFile);
		
		assertThat(Files.exists(Paths.get("src/test/resources/file/tips/camt_052/camt.052.xml"))).isTrue();
		
		verify(this.isoFileRepository, times(0)).save(Mockito.any(IsoFile.class));
		verify(this.isoExternalCodeSetRepository, times(0)).save(Mockito.any(IsoExternalCodeSet.class));
		
		Files.delete(new File("src/test/resources/file/tips/camt_052/camt.052.xml").toPath());
	}
	
	@Test
	@Order(7)
	void testUploadTipsCamt052UnknownSuffix() throws FileManagerException, IOException {
		
		Path path = Paths.get("src/test/resources/file/camt.052.txt");
		MockMultipartFile multipartFile = new MockMultipartFile("camt.052.txt", "camt.052.txt", "", new FileInputStream(path.toFile()));
		
		assertThrows(FileManagerException.class, () -> {
			this.service.upload(multipartFile);
		});
	}
	
	@Test
	@Order(8)
	void testUploadTipsCamt053() throws FileManagerException, IOException {
		
		Path path = Paths.get("src/test/resources/file/camt.053.xml");
		MockMultipartFile multipartFile = new MockMultipartFile("camt.053.xml", "camt.053.xml", "", new FileInputStream(path.toFile()));
		
		this.service.upload(multipartFile);
		
		assertThat(Files.exists(Paths.get("src/test/resources/file/tips/camt_053/camt.053.xml"))).isTrue();
		
		verify(this.isoFileRepository, times(0)).save(Mockito.any(IsoFile.class));
		verify(this.isoExternalCodeSetRepository, times(0)).save(Mockito.any(IsoExternalCodeSet.class));
		
		Files.delete(new File("src/test/resources/file/tips/camt_053/camt.053.xml").toPath());
	}
	
	@Test
	@Order(9)
	void testUploadTipsCamt053UnknownSuffix() throws FileManagerException, IOException {
		
		Path path = Paths.get("src/test/resources/file/camt.053.txt");
		MockMultipartFile multipartFile = new MockMultipartFile("camt.053.txt", "camt.053.txt", "", new FileInputStream(path.toFile()));
		
		assertThrows(FileManagerException.class, () -> {
			this.service.upload(multipartFile);
		});
	}
	
	@Test
	@Order(10)
	void testUploadRt1Psr() throws FileManagerException, IOException {
		
		Path path = Paths.get("src/test/resources/file/RT02SCIPOSOIT22.P");
		MockMultipartFile multipartFile = new MockMultipartFile("RT02SCIPOSOIT22.P", "RT02SCIPOSOIT22.P", "", new FileInputStream(path.toFile()));
		
		this.service.upload(multipartFile);
		
		assertThat(Files.exists(Paths.get("src/test/resources/file/rt1/RT02SCIPOSOIT22.P"))).isTrue();
		
		verify(this.isoFileRepository, times(0)).save(Mockito.any(IsoFile.class));
		verify(this.isoExternalCodeSetRepository, times(0)).save(Mockito.any(IsoExternalCodeSet.class));
		
		Files.delete(new File("src/test/resources/file/rt1/RT02SCIPOSOIT22.P").toPath());
	}
	
	@Test
	@Order(11)
	void testUploadRt1PsrUnknownSuffix() throws FileManagerException, IOException {
		
		Path path = Paths.get("src/test/resources/file/RT02SCIPOSOIT22.A");
		MockMultipartFile multipartFile = new MockMultipartFile("RT02SCIPOSOIT22.A", "RT02SCIPOSOIT22.A", "", new FileInputStream(path.toFile()));
		
		assertThrows(FileManagerException.class, () -> {
			this.service.upload(multipartFile);
		});
	}
	
	@Test
	@Order(12)
	void testUploadRt1Rsf() throws FileManagerException, IOException {
		
		Path path = Paths.get("src/test/resources/file/RT02SCIPOSOIT22.S");
		MockMultipartFile multipartFile = new MockMultipartFile("RT02SCIPOSOIT22.S", "RT02SCIPOSOIT22.S", "", new FileInputStream(path.toFile()));
		
		this.service.upload(multipartFile);
		
		assertThat(Files.exists(Paths.get("src/test/resources/file/rt1/RT02SCIPOSOIT22.S"))).isTrue();
		
		verify(this.isoFileRepository, times(0)).save(Mockito.any(IsoFile.class));
		verify(this.isoExternalCodeSetRepository, times(0)).save(Mockito.any(IsoExternalCodeSet.class));
		
		Files.delete(new File("src/test/resources/file/rt1/RT02SCIPOSOIT22.S").toPath());
	}
	
	@Test
	@Order(13)
	void testUploadRt1RsfUnknownSuffix() throws FileManagerException, IOException {
		
		Path path = Paths.get("src/test/resources/file/RT02SCIPOSOIT22.A");
		MockMultipartFile multipartFile = new MockMultipartFile("RT02SCIPOSOIT22.A", "RT02SCIPOSOIT22.A", "", new FileInputStream(path.toFile()));
		
		assertThrows(FileManagerException.class, () -> {
			this.service.upload(multipartFile);
		});
	}
	
	@Test
	@Order(14)
	void testUploadFileNotManaged() throws FileManagerException, IOException {
		
		Path path = Paths.get("src/test/resources/file/NotManaged.txt");
		MockMultipartFile multipartFile = new MockMultipartFile("NotManaged.txt", "NotManaged.txt", "", new FileInputStream(path.toFile()));
		
		assertThrows(FileManagerException.class, () -> {
			this.service.upload(multipartFile);
		});
	}
	
	@AfterAll
	public static void tearDown() throws IOException {
		Files.delete(Paths.get("src/test/resources/file/cgs"));
		Files.delete(Paths.get("src/test/resources/file/tips/camt_053"));
		Files.delete(Paths.get("src/test/resources/file/tips/camt_052"));
		Files.delete(Paths.get("src/test/resources/file/tips"));
		Files.delete(Paths.get("src/test/resources/file/rt1"));
	}

}
