package it.popso.bicomp.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import jakarta.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.minio.messages.Item;
import it.popso.bicomp.dto.CgsLacDetailDto;
import it.popso.bicomp.dto.CgsLacDto;
import it.popso.bicomp.dto.PageableCgsEntryDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.CgsLmr;
import it.popso.bicomp.model.CgsLmrLacBulk;
import it.popso.bicomp.model.CgsLmrLacBulkStatement;
import it.popso.bicomp.model.CgsLmrLtinBulk;
import it.popso.bicomp.model.CgsLmrLtinBulkStatement;
import it.popso.bicomp.model.CgsLmrLtinBulkStatementEntry;
import it.popso.bicomp.model.CgsLmrPlcrBulk;
import it.popso.bicomp.model.CgsLmrPlcrBulkStatement;
import it.popso.bicomp.model.CgsLmrPlcrBulkStatementEntry;
import it.popso.bicomp.properties.Access;
import it.popso.bicomp.properties.BicompConfig;
import it.popso.bicomp.properties.Bucket;
import it.popso.bicomp.properties.Cgs;
import it.popso.bicomp.properties.Minio;
import it.popso.bicomp.repository.CgsLmrLacBulkRepository;
import it.popso.bicomp.repository.CgsLmrLacBulkStatementRepository;
import it.popso.bicomp.repository.CgsLmrLtinBulkRepository;
import it.popso.bicomp.repository.CgsLmrLtinBulkStatementEntryRepository;
import it.popso.bicomp.repository.CgsLmrLtinBulkStatementRepository;
import it.popso.bicomp.repository.CgsLmrPlcrBulkRepository;
import it.popso.bicomp.repository.CgsLmrPlcrBulkStatementEntryRepository;
import it.popso.bicomp.repository.CgsLmrPlcrBulkStatementRepository;
import it.popso.bicomp.repository.CgsLmrRepository;
import it.popso.bicomp.service.impl.CgsServiceImpl;
import it.popso.bicomp.utils.DateUtils;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class CgsServiceImplTest {
	
	@Mock
	private MinioService minioService;
	
	@Mock
	private CgsLmrRepository cgsLmrRepository;
	
	@Mock
	private CgsLmrLacBulkRepository cgsLmrLacBulkRepository;
	
	@Mock
	private CgsLmrLacBulkStatementRepository cgsLmrLacBulkStatementRepository;
	
	@Mock
	private CgsLmrLtinBulkRepository cgsLmrLtinBulkRepository;
	
	@Mock
	private CgsLmrLtinBulkStatementRepository cgsLmrLtinBulkStatementRepository;
	
	@Mock
	private CgsLmrLtinBulkStatementEntryRepository cgsLmrLtinBulkStatementEntryRepository;
	
	@Mock
	private CgsLmrPlcrBulkRepository cgsLmrPlcrBulkRepository;
	
	@Mock
	private CgsLmrPlcrBulkStatementRepository cgsLmrPlcrBulkStatementRepository;
	
	@Mock
	private CgsLmrPlcrBulkStatementEntryRepository cgsLmrPlcrBulkStatementEntryRepository;
	
	private BicompConfig config;
	private CgsServiceImpl service;
	private DateUtils dateUtils;
	private MockedStatic<DateUtils> mockDateUtils;
	
	
	@BeforeEach
    public void setup() {
		
		Cgs cgs = new Cgs();
		cgs.setShare("cgs");
		cgs.setPrefix("S204SCTPOSOIT22");
		cgs.setSuffix(".B");
		cgs.setPaymentReportFolder("cgs");
		cgs.setPaymentReportPrefix("CGS_PAYMENT_");
		cgs.setPaymentReportSuffix(".txt");
		
		Access access = new Access();
		access.setName("name");
		access.setSecret("secret");
		Bucket bucket = new Bucket();
		bucket.setName("bucket");
		Minio minio = new Minio();
		minio.setUrl("url");
		minio.setAccess(access);
		minio.setBucket(bucket);
		
		this.config = new BicompConfig();
		this.config.setCgs(cgs);
		this.config.setMinio(minio);
		
		this.dateUtils = Mockito.mock(DateUtils.class);
		this.mockDateUtils = Mockito.mockStatic(DateUtils.class);
		
		this.service = new CgsServiceImpl(this.config, this.minioService, this.cgsLmrRepository, this.cgsLmrLacBulkRepository, this.cgsLmrLacBulkStatementRepository, 
				this.cgsLmrLtinBulkRepository, this.cgsLmrLtinBulkStatementRepository, this.cgsLmrLtinBulkStatementEntryRepository, 
				this.cgsLmrPlcrBulkRepository, this.cgsLmrPlcrBulkStatementRepository, this.cgsLmrPlcrBulkStatementEntryRepository);
	}
	
	@Test
	@Order(1)
	void testProcessLmrReportNoReport() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of();
		
		this.service.processLmrReport(list);
		
		verify(this.minioService, times(0)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(0)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, times(0)).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, times(0)).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(2)
	void testProcessLmrReportLtinPlcr() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr.B"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		this.service.processLmrReport(list);
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(1)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(3)
	void testProcessLmrReportNoAccountStatement() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_NoAccountStatement.B"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_NoAccountStatement.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		assertThrows(BicompException.class, () -> {
			this.service.processLmrReport(list);
		});
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(1)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, times(0)).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(4)
	void testProcessLmrReportMultipleAccountStatement() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_MultipleAccountStatement.B"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_MultipleAccountStatement.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		assertThrows(BicompException.class, () -> {
			this.service.processLmrReport(list);
		});
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(1)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, times(0)).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(5)
	void testProcessLmrReportEmptyOpeningBalance() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_EmptyBalance.B"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_EmptyBalance.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		assertThrows(BicompException.class, () -> {
			this.service.processLmrReport(list);
		});
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(1)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, times(0)).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(6)
	void testProcessLmrReportDebitOpeningBalance() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr_DebitOpeningBalance.B"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr_DebitOpeningBalance.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		assertThrows(BicompException.class, () -> {
			this.service.processLmrReport(list);
		});
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(1)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, times(0)).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(7)
	void testProcessLmrReportDebitClosingBalance() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr_DebitClosingBalance.B"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr_DebitClosingBalance.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		assertThrows(BicompException.class, () -> {
			this.service.processLmrReport(list);
		});
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(1)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, times(0)).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(8)
	void testProcessLmrReportDebitLtin() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr_DebitLtin.B"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr_DebitLtin.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		this.service.processLmrReport(list);
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(1)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(9)
	void testProcessLmrReportBalanceNotAllowed() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr_BalanceNotAllowed.B"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr_BalanceNotAllowed.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		assertThrows(BicompException.class, () -> {
			this.service.processLmrReport(list);
		});
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(1)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, times(0)).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(10)
	void testProcessLmrReportMultipleLtinStatement() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_MultipleLtinStatement.B"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_MultipleLtinStatement.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		assertThrows(BicompException.class, () -> {
			this.service.processLmrReport(list);
		});
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(1)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(11)
	void testProcessLmrReportDebitLtinStatement() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_DebitLtinStatement.B"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_DebitLtinStatement.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		this.service.processLmrReport(list);
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(1)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(12)
	void testProcessLmrReportLtinStatementNoEntry() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinStatement_NoEntry.B"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinStatement_NoEntry.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		assertThrows(BicompException.class, () -> {
			this.service.processLmrReport(list);
		});
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(1)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(13)
	void testProcessLmrReportNoPlcr() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_NoPlcr.B"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_NoPlcr.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		this.service.processLmrReport(list);
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(1)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(14)
	void testProcessLmrReportPlcrSettledPendingBalance() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_PlcrSettledPendingBalance.B"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_PlcrSettledPendingBalance.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		this.service.processLmrReport(list);
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(1)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, times(0)).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(15)
	void testProcessLmrReportPlcrPendingBalance() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_PlcrPendingBalance.B"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_PlcrPendingBalance.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		this.service.processLmrReport(list);
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(1)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, times(0)).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(16)
	void testProcessLmrReportPlcrCreditPendingBalance() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_PlcrCreditPendingBalance.B"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_PlcrCreditPendingBalance.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		assertThrows(BicompException.class, () -> {
			this.service.processLmrReport(list);
		});
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(1)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(17)
	void testProcessLmrReportLtinPlcrNullAgent() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr_NullAgent.B"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr_NullAgent.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		this.service.processLmrReport(list);
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(1)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, atLeastOnce()).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(18)
	void testProcessLmrReportDataIntegrityViolationException() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr.B"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		Mockito.when(this.cgsLmrRepository.save(Mockito.any(CgsLmr.class))).thenThrow(new DataIntegrityViolationException("LmrReport already exists"));
		
		assertThrows(DataIntegrityViolationException.class, () -> {
			this.service.processLmrReport(list);
		});
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(1)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, times(0)).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, times(0)).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(19)
	void testProcessLmrReportB2b() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcrB2b.B"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcrB2b.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		this.service.processLmrReport(list);
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(1)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, times(0)).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(20)
	void testProcessLmrReportCore() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcrCore.B"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcrCore.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		this.service.processLmrReport(list);
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(1)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, times(0)).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(21)
	void testGetLmrReportEmpty() throws BicompException {
		
		List<Item> list = List.of();		
		Mockito.when(this.minioService.getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
			.thenReturn(list);
		
		List<Item> result = this.service.getLmrReport();
		
		assertThat(result).isEmpty();
		verify(this.minioService, times(1)).getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());
	}
	
	@Test
	@Order(22)
	void testGetLmrReport() throws BicompException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr.B"));
		Mockito.when(this.minioService.getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
			.thenReturn(list);
		
		List<Item> result = this.service.getLmrReport();
		
		assertThat(result).hasSize(1);
		verify(this.minioService, times(1)).getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());
	}
	
	@Test
	@Order(23)
	void testGetLmrReportBicompException() throws BicompException {
		
		Mockito.when(this.minioService.getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
			.thenThrow(new BicompException("Minio exception"));
		
		assertThrows(BicompException.class, () -> {
			this.service.getLmrReport();
		});
		
		verify(this.minioService, times(1)).getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());
	}
	
	@Test
	@Order(24)
	void testProcessLmrReportJAXBException() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr_JaxbException.B"));
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr_JaxbException.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		assertThrows(JAXBException.class, () -> {
			this.service.processLmrReport(list);
		});
		
		verify(this.dateUtils, times(0)).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(0)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, times(0)).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, times(0)).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(25)
	void testGetCgsLastBalanceByDate() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		Optional<CgsLmr> o = Optional.of(CgsLmr.builder()
				.id(BigDecimal.ONE)
				.settlementBic("POSOIT22XXX")
				.reportName("S204SCTPOSOIT22230606143016000.B")
				.sendingInstitute("EBAPFRPA")
				.receivingInstitute("POSOIT22")
				.serviceId("SCT")
				.fileType("P")
				.fileSettlementDate(new Date())
				.fileLac("01")
				.build()
			);
		
		CgsLmrLacBulk cgsLmrLacBulk = CgsLmrLacBulk.builder()
				.id(BigDecimal.ONE)
				.bulkReference("BSCT230606001244ACGS000000000000001")
				.cgsLmr(o.get())
				.build();
		
		Optional<CgsLmrLacBulkStatement> oc = Optional.of(CgsLmrLacBulkStatement.builder()
				.id(BigDecimal.ONE)
				.statementReference("BSCT230606001244ACGS000000000000001")
				.accountOwner("POSOIT22XXX")
				.openingBalance(new BigDecimal(10))
				.openingBalanceSide('C')
				.closingBalance(new BigDecimal(20))
				.closingBalanceSide('C')
				.settlementDate(new Date())
				.cgsLmrLacBulk(cgsLmrLacBulk)
				.build()
			);
		
		Mockito.when(this.cgsLmrRepository.findLastStatementByDate(Mockito.any(Date.class), Mockito.any(PageRequest.class)))
			.thenReturn(o);
		Mockito.when(this.cgsLmrLacBulkStatementRepository.findByLmrId(Mockito.any(BigDecimal.class))).thenReturn(oc);
		
		List<CgsLacDto> result = this.service.getCgsLastBalanceByDate("01-09-2023");
		assertThat(result).isNotEmpty();
		assertThat(result.get(0).getLacNumber()).isEqualTo(o.get().getFileLac());
		assertThat(result.get(0).getOpeningBalance()).isEqualTo(oc.get().getOpeningBalance());
		
		verify(this.cgsLmrRepository, times(1)).findLastStatementByDate(Mockito.any(Date.class), Mockito.any(PageRequest.class));
		verify(this.cgsLmrLacBulkStatementRepository, times(1)).findByLmrId(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(26)
	void testGetCgsLastBalanceByDateNoCgsLmr() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		Optional<CgsLmr> o = Optional.empty();
		Mockito.when(this.cgsLmrRepository.findLastStatementByDate(Mockito.any(Date.class), Mockito.any(PageRequest.class)))
			.thenReturn(o);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getCgsLastBalanceByDate("01-09-2023");
		});
		
		verify(this.cgsLmrRepository, times(1)).findLastStatementByDate(Mockito.any(Date.class), Mockito.any(PageRequest.class));
		verify(this.cgsLmrLacBulkStatementRepository, times(0)).findByLmrId(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(27)
	void testGetCgsLastBalanceByDateNoCgsLmrBulkStatement() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		Optional<CgsLmr> o = Optional.of(CgsLmr.builder()
				.id(BigDecimal.ONE)
				.settlementBic("POSOIT22XXX")
				.reportName("S204SCTPOSOIT22230606143016000.B")
				.sendingInstitute("EBAPFRPA")
				.receivingInstitute("POSOIT22")
				.serviceId("SCT")
				.fileType("P")
				.fileSettlementDate(new Date())
				.fileLac("01")
				.build()
			);
		
		Optional<CgsLmrLacBulkStatement> oc = Optional.empty();		
		Mockito.when(this.cgsLmrRepository.findLastStatementByDate(Mockito.any(Date.class), Mockito.any(PageRequest.class)))
			.thenReturn(o);
		Mockito.when(this.cgsLmrLacBulkStatementRepository.findByLmrId(Mockito.any(BigDecimal.class))).thenReturn(oc);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getCgsLastBalanceByDate("01-09-2023");
		});
		
		verify(this.cgsLmrRepository, times(1)).findLastStatementByDate(Mockito.any(Date.class), Mockito.any(PageRequest.class));
		verify(this.cgsLmrLacBulkStatementRepository, times(1)).findByLmrId(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(28)
	void testGetCurrentDateLac() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		List<CgsLmr> list = Arrays.asList(CgsLmr.builder()
				.id(BigDecimal.ONE)
				.settlementBic("POSOIT22XXX")
				.reportName("S204SCTPOSOIT22230606143016000.B")
				.sendingInstitute("EBAPFRPA")
				.receivingInstitute("POSOIT22")
				.serviceId("SCT")
				.fileType("P")
				.fileSettlementDate(new Date())
				.fileLac("01")
				.build(), CgsLmr.builder()
				.id(new BigDecimal(2))
				.settlementBic("POSOIT22XXX")
				.reportName("S204SCTPOSOIT22230606143017000.B")
				.sendingInstitute("EBAPFRPA")
				.receivingInstitute("POSOIT22")
				.serviceId("SCT")
				.fileType("P")
				.fileSettlementDate(new Date())
				.fileLac("02")
				.build()
			);
		
		CgsLmrLacBulk cgsLmrLacBulk = CgsLmrLacBulk.builder()
				.id(BigDecimal.ONE)
				.bulkReference("BSCT230606001244ACGS000000000000001")
				.cgsLmr(list.get(0))
				.build();
		
		Optional<CgsLmrLacBulkStatement> o = Optional.of(CgsLmrLacBulkStatement.builder()
				.id(BigDecimal.ONE)
				.statementReference("BSCT230606001244ACGS000000000000001")
				.accountOwner("POSOIT22XXX")
				.openingBalance(new BigDecimal(10))
				.openingBalanceSide('C')
				.closingBalance(new BigDecimal(20))
				.closingBalanceSide('C')
				.settlementDate(new Date())
				.cgsLmrLacBulk(cgsLmrLacBulk)
				.build()
			);
		
		Mockito.when(this.cgsLmrRepository.findByFileSettlementDate(Mockito.any(Date.class))).thenReturn(list);
		Mockito.when(this.cgsLmrLacBulkStatementRepository.findByLmrId(Mockito.any(BigDecimal.class))).thenReturn(o);
		
		List<CgsLacDto> result = this.service.getCurrentDateLac("01-09-2023");
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getLacNumber()).isEqualTo(list.get(0).getFileLac());
		assertThat(result.get(1).getLacNumber()).isEqualTo(list.get(1).getFileLac());
		
		verify(this.cgsLmrRepository, times(1)).findByFileSettlementDate(Mockito.any(Date.class));
		verify(this.cgsLmrLacBulkStatementRepository, times(2)).findByLmrId(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(29)
	void testGetCurrentDateLacNoCgsLmr() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		List<CgsLmr> list = Arrays.asList();
		Mockito.when(this.cgsLmrRepository.findByFileSettlementDate(Mockito.any(Date.class))).thenReturn(list);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getCurrentDateLac("01-09-2023");
		});
		
		verify(this.cgsLmrRepository, times(1)).findByFileSettlementDate(Mockito.any(Date.class));
		verify(this.cgsLmrLacBulkStatementRepository, times(0)).findByLmrId(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(30)
	void testGetCurrentDateLacNoCgsLmrBulkStatement() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		List<CgsLmr> list = Arrays.asList(CgsLmr.builder()
				.id(BigDecimal.ONE)
				.settlementBic("POSOIT22XXX")
				.reportName("S204SCTPOSOIT22230606143016000.B")
				.sendingInstitute("EBAPFRPA")
				.receivingInstitute("POSOIT22")
				.serviceId("SCT")
				.fileType("P")
				.fileSettlementDate(new Date())
				.fileLac("01")
				.build(), CgsLmr.builder()
				.id(new BigDecimal(2))
				.settlementBic("POSOIT22XXX")
				.reportName("S204SCTPOSOIT22230606143017000.B")
				.sendingInstitute("EBAPFRPA")
				.receivingInstitute("POSOIT22")
				.serviceId("SCT")
				.fileType("P")
				.fileSettlementDate(new Date())
				.fileLac("02")
				.build()
			);
		
		Optional<CgsLmrLacBulkStatement> o = Optional.empty();
		Mockito.when(this.cgsLmrRepository.findByFileSettlementDate(Mockito.any(Date.class))).thenReturn(list);
		Mockito.when(this.cgsLmrLacBulkStatementRepository.findByLmrId(Mockito.any(BigDecimal.class))).thenReturn(o);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getCurrentDateLac("01-09-2023");
		});
		
		verify(this.cgsLmrRepository, times(1)).findByFileSettlementDate(Mockito.any(Date.class));
		verify(this.cgsLmrLacBulkStatementRepository, times(1)).findByLmrId(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(31)
	void testGetLacDetail() throws ResourceNotFoundException {
		
		CgsLmr cgsLmr = CgsLmr.builder()
				.id(BigDecimal.ONE)
				.settlementBic("POSOIT22XXX")
				.reportName("S204SCTPOSOIT22230606143016000.B")
				.sendingInstitute("EBAPFRPA")
				.receivingInstitute("POSOIT22")
				.serviceId("SCT")
				.fileType("P")
				.fileSettlementDate(new Date())
				.fileLac("01")
				.build();
		
		CgsLmrLacBulk cgsLmrLacBulk = CgsLmrLacBulk.builder()
				.id(BigDecimal.ONE)
				.bulkReference("BSCT230606001244ACGS000000000000001")
				.cgsLmr(cgsLmr)
				.build();
		
		Optional<CgsLmrLacBulkStatement> o = Optional.of(CgsLmrLacBulkStatement.builder()
				.id(BigDecimal.ONE)
				.statementReference("BSCT230606001244ACGS000000000000001")
				.accountOwner("POSOIT22XXX")
				.openingBalance(new BigDecimal(10))
				.openingBalanceSide('C')
				.closingBalance(new BigDecimal(20))
				.closingBalanceSide('C')
				.creditLiquidityTransfer(new BigDecimal(100))
				.debitLiquidityTransfer(new BigDecimal(50))
				.creditPayments(new BigDecimal(200))
				.debitPayments(new BigDecimal(150))
				.settlementDate(new Date())
				.cgsLmrLacBulk(cgsLmrLacBulk)
				.build()
			);
		
		List<CgsLmrPlcrBulkStatement> list = Arrays.asList(CgsLmrPlcrBulkStatement.builder()
				.statementReference("BSCT230606000990CSCT004000000000005")
				.accountOwner("POSOIT22XXX")
				.creditPayments(new BigDecimal(100))
				.debitPayments(new BigDecimal(50))
				.pendingPayments(BigDecimal.ZERO)
				.settlementDate(new Date())
				.service("SCT")
				.build(), CgsLmrPlcrBulkStatement.builder()
				.statementReference("BSCT230606000990CSCT004000000000006")
				.accountOwner("POSOIT22XXX")
				.creditPayments(new BigDecimal(200))
				.debitPayments(new BigDecimal(80))
				.pendingPayments(BigDecimal.ZERO)
				.settlementDate(new Date())
				.service("COR")
				.build(), CgsLmrPlcrBulkStatement.builder()
				.statementReference("BSCT230606000990CSCT004000000000007")
				.accountOwner("POSOIT22XXX")
				.creditPayments(new BigDecimal(300))
				.debitPayments(new BigDecimal(170))
				.pendingPayments(BigDecimal.ZERO)
				.settlementDate(new Date())
				.service("B2B")
				.build()
			);
		
		Mockito.when(this.cgsLmrLacBulkStatementRepository.findByLmrId(Mockito.any(BigDecimal.class))).thenReturn(o);
		Mockito.when(this.cgsLmrPlcrBulkStatementRepository.findByLmrId(Mockito.any(BigDecimal.class))).thenReturn(list);
		
		CgsLacDetailDto result = this.service.getLacDetail(BigDecimal.ONE);
		assertThat(result).isNotNull();
		assertThat(result.getLacNumber()).isEqualTo(cgsLmr.getFileLac());
		assertThat(result.getSctCreditPmntAmount()).isEqualTo(list.get(0).getCreditPayments());
		assertThat(result.getCorCreditPmntAmount()).isEqualTo(list.get(1).getCreditPayments());
		assertThat(result.getB2bDebitPmntAmount()).isEqualTo(list.get(2).getDebitPayments());
		
		verify(this.cgsLmrLacBulkStatementRepository, times(1)).findByLmrId(Mockito.any(BigDecimal.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, times(1)).findByLmrId(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(32)
	void testGetLacDetailNoCgsLmrLacBulkStatement() throws ResourceNotFoundException {
		
		Optional<CgsLmrLacBulkStatement> o = Optional.empty();
		Mockito.when(this.cgsLmrLacBulkStatementRepository.findByLmrId(Mockito.any(BigDecimal.class))).thenReturn(o);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getLacDetail(BigDecimal.ONE);
		});
		
		verify(this.cgsLmrLacBulkStatementRepository, times(1)).findByLmrId(Mockito.any(BigDecimal.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, times(0)).findByLmrId(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(33)
	void testGetLacDetailNoCgsLmrPlcrBulkStatement() {
		
		CgsLmr cgsLmr = CgsLmr.builder()
				.id(BigDecimal.ONE)
				.settlementBic("POSOIT22XXX")
				.reportName("S204SCTPOSOIT22230606143016000.B")
				.sendingInstitute("EBAPFRPA")
				.receivingInstitute("POSOIT22")
				.serviceId("SCT")
				.fileType("P")
				.fileSettlementDate(new Date())
				.fileLac("01")
				.build();
		
		CgsLmrLacBulk cgsLmrLacBulk = CgsLmrLacBulk.builder()
				.id(BigDecimal.ONE)
				.bulkReference("BSCT230606001244ACGS000000000000001")
				.cgsLmr(cgsLmr)
				.build();
		
		Optional<CgsLmrLacBulkStatement> o = Optional.of(CgsLmrLacBulkStatement.builder()
				.id(BigDecimal.ONE)
				.statementReference("BSCT230606001244ACGS000000000000001")
				.accountOwner("POSOIT22XXX")
				.openingBalance(new BigDecimal(10))
				.openingBalanceSide('C')
				.closingBalance(new BigDecimal(20))
				.closingBalanceSide('C')
				.creditLiquidityTransfer(new BigDecimal(100))
				.debitLiquidityTransfer(new BigDecimal(50))
				.creditPayments(new BigDecimal(200))
				.debitPayments(new BigDecimal(150))
				.settlementDate(new Date())
				.cgsLmrLacBulk(cgsLmrLacBulk)
				.build()
			);
		
		List<CgsLmrPlcrBulkStatement> list = Arrays.asList();
		
		Mockito.when(this.cgsLmrLacBulkStatementRepository.findByLmrId(Mockito.any(BigDecimal.class))).thenReturn(o);
		Mockito.when(this.cgsLmrPlcrBulkStatementRepository.findByLmrId(Mockito.any(BigDecimal.class))).thenReturn(list);
		
		CgsLacDetailDto result = this.service.getLacDetail(BigDecimal.ONE);
		assertThat(result).isNotNull();
		assertThat(result.getLacNumber()).isEqualTo(cgsLmr.getFileLac());
		assertThat(result.getSctCreditPmntAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getCorCreditPmntAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getB2bDebitPmntAmount()).isEqualTo(BigDecimal.ZERO);
		
		verify(this.cgsLmrLacBulkStatementRepository, times(1)).findByLmrId(Mockito.any(BigDecimal.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, times(1)).findByLmrId(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(34)
	void testMoveToBackupFolder() throws BicompException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/camt.052.xml"));
		
		Mockito.doNothing().when(this.minioService).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		Mockito.doNothing().when(this.minioService).removeFile(Mockito.anyString(), Mockito.anyString());
		
		this.service.moveToBackupFolder(list);
		
		verify(this.minioService, times(1)).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		verify(this.minioService, times(1)).removeFile(Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(35)
	void testMoveToBackupFolderMultipleFiles() throws BicompException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/camt.052.xml"), this.mockItem("src/test/resources/cgs/camt.052.xml"));
		
		Mockito.doNothing().when(this.minioService).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		Mockito.doNothing().when(this.minioService).removeFile(Mockito.anyString(), Mockito.anyString());
				
		this.service.moveToBackupFolder(list);
		
		verify(this.minioService, times(2)).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		verify(this.minioService, times(2)).removeFile(Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(36)
	void testMoveToBackupFolderCopyBicompException() throws BicompException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/camt.052.xml"));
		Mockito.doThrow(new BicompException("MinIO exception")).when(this.minioService).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		
		assertThrows(BicompException.class, () -> {
			this.service.moveToBackupFolder(list);
		});
		
		verify(this.minioService, times(1)).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		verify(this.minioService, times(0)).removeFile(Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(37)
	void testMoveToBackupFolderRemoveBicompException() throws BicompException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/camt.052.xml"));
		Mockito.doNothing().when(this.minioService).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new BicompException("MinIO exception")).when(this.minioService).removeFile(Mockito.anyString(), Mockito.anyString());
		
		assertThrows(BicompException.class, () -> {
			this.service.moveToBackupFolder(list);
		});
		
		verify(this.minioService, times(1)).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		verify(this.minioService, times(1)).removeFile(Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(38)
	void testExtractCgsLiquidityTransfer() {
		
		List<CgsLmrLtinBulkStatementEntry> list = Arrays.asList(CgsLmrLtinBulkStatementEntry.builder()
				.entryReference("CG2306050000069T")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("C-FUND")
				.build(), CgsLmrLtinBulkStatementEntry.builder()
				.entryReference("CG2306050000069V")
				.paymentAmount(new BigDecimal(200))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("C-FUND")
				.build()
			);
		
		Mockito.when(this.cgsLmrLtinBulkStatementEntryRepository.findBySettlementDate(Mockito.any(Date.class))).thenReturn(list);
		
		List<CgsLmrLtinBulkStatementEntry> result = this.service.extractCgsLiquidityTransfer(new Date());
		
		assertThat(result).isNotEmpty().hasSameSizeAs(list);
		
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(1)).findBySettlementDate(Mockito.any(Date.class));
	}
	
	@Test
	@Order(39)
	void testExtractCgsPayment() {
		
		List<CgsLmrPlcrBulkStatementEntry> list = Arrays.asList(CgsLmrPlcrBulkStatementEntry.builder()
				.entryReference("CSR2306065016069")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("LCR-SETTLEDNOTPROVIDED")
				.debitorBic("BCITITMMXXX")
				.creditorBic("POSOIT22XXX")
				.build(), CgsLmrPlcrBulkStatementEntry.builder()
				.entryReference("CSR2306065016070")
				.paymentAmount(new BigDecimal(200))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("LCR-SETTLEDNOTPROVIDED")
				.debitorBic("BCITITMMXXX")
				.creditorBic("POSOIT22XXX")
				.build()
			);
		
		Mockito.when(this.cgsLmrPlcrBulkStatementEntryRepository.findBySettlementDate(Mockito.any(Date.class))).thenReturn(list);
		
		List<CgsLmrPlcrBulkStatementEntry> result = this.service.extractCgsPayment(new Date());
		
		assertThat(result).isNotEmpty().hasSameSizeAs(list);
		
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, times(1)).findBySettlementDate(Mockito.any(Date.class));
	}
	
	@Test
	@Order(40)
	void testCreateCgsPaymentFile() throws BicompException, ParseException, IOException {
		
		CgsLmr lmr = CgsLmr.builder()
				.settlementBic("POSOIT20XXX")
				.build();
		CgsLmrLtinBulk clb = CgsLmrLtinBulk.builder()
				.cgsLmr(lmr)
				.build();
		CgsLmrLtinBulkStatement cl = CgsLmrLtinBulkStatement.builder()
				.statementReference("BSCT230606000990CSCT004000000000005")
				.accountOwner("POSOIT22XXX")
				.creditLiquidityTransfer(new BigDecimal(100))
				.debitLiquidityTransfer(new BigDecimal(50))
				.settlementDate(new Date())
				.creationDateTime(new Timestamp(System.currentTimeMillis()))
				.cgsLmrLtinBulk(clb)
				.build();
		List<CgsLmrLtinBulkStatementEntry> ltList = Arrays.asList(CgsLmrLtinBulkStatementEntry.builder()
				.entryReference("CG2306050000069T")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("C-FUND")
				.cgsLmrLtinBulkStatement(cl)
				.build(), CgsLmrLtinBulkStatementEntry.builder()
				.entryReference("CG2306050000069V")
				.paymentAmount(new BigDecimal(200))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("C-FUND")
				.cgsLmrLtinBulkStatement(cl)
				.build()
			);
		
		CgsLmrPlcrBulkStatement cp = CgsLmrPlcrBulkStatement.builder()
				.statementReference("BSCT230606000990CSCT004000000000005")
				.accountOwner("POSOIT22XXX")
				.creditPayments(new BigDecimal(100))
				.debitPayments(new BigDecimal(50))
				.pendingPayments(BigDecimal.ZERO)
				.settlementDate(new Date())
				.service("SCT")
				.creationDateTime(new Timestamp(System.currentTimeMillis()))
				.build();
		List<CgsLmrPlcrBulkStatementEntry> paymentList = Arrays.asList(CgsLmrPlcrBulkStatementEntry.builder()
				.entryReference("CSR2306065016069")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("LCR-SETTLEDNOTPROVIDED")
				.debitorBic("BCITITMMXXX")
				.creditorBic("POSOIT22XXX")
				.cgsLmrPlcrBulkStatement(cp)
				.build(), CgsLmrPlcrBulkStatementEntry.builder()
				.entryReference("CSR2306065016070")
				.paymentAmount(new BigDecimal(200))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("LCR-SETTLEDNOTPROVIDED")
				.debitorBic("BCITITMMXXX")
				.creditorBic("POSOIT22XXX")
				.cgsLmrPlcrBulkStatement(cp)
				.build()
			);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.dateToString(Mockito.any(Date.class))).thenReturn("20250729");
		Mockito.when(this.dateUtils.dateToStringFormatted(Mockito.any(Date.class))).thenReturn("20250729");
		Mockito.doNothing().when(this.minioService).uploadFile(Mockito.anyString(), Mockito.anyString(), Mockito.any(InputStream.class));
		
		String result = this.service.createCgsPaymentFile(new Date(), ltList, paymentList);
		
		assertThat(result).isNotNull();
		
		verify(this.dateUtils, times(1)).dateToString(Mockito.any(Date.class));
		verify(this.dateUtils, atLeastOnce()).dateToStringFormatted(Mockito.any(Date.class));
		verify(this.minioService, times(1)).uploadFile(Mockito.anyString(), Mockito.anyString(), Mockito.any(InputStream.class));
	}
	
	@Test
	@Order(41)
	void testCreateCgsPaymentFileBicompException() throws BicompException, ParseException {
		
		CgsLmr lmr = CgsLmr.builder()
				.settlementBic("POSOIT20XXX")
				.build();
		CgsLmrLtinBulk clb = CgsLmrLtinBulk.builder()
				.cgsLmr(lmr)
				.build();
		CgsLmrLtinBulkStatement cl = CgsLmrLtinBulkStatement.builder()
				.statementReference("BSCT230606000990CSCT004000000000005")
				.accountOwner("POSOIT22XXX")
				.creditLiquidityTransfer(new BigDecimal(100))
				.debitLiquidityTransfer(new BigDecimal(50))
				.settlementDate(new Date())
				.creationDateTime(new Timestamp(System.currentTimeMillis()))
				.cgsLmrLtinBulk(clb)
				.build();
		List<CgsLmrLtinBulkStatementEntry> ltList = Arrays.asList(CgsLmrLtinBulkStatementEntry.builder()
				.entryReference("CG2306050000069T")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("C-FUND")
				.cgsLmrLtinBulkStatement(cl)
				.build(), CgsLmrLtinBulkStatementEntry.builder()
				.entryReference("CG2306050000069V")
				.paymentAmount(new BigDecimal(200))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("C-FUND")
				.cgsLmrLtinBulkStatement(cl)
				.build()
			);
		
		CgsLmrPlcrBulkStatement cp = CgsLmrPlcrBulkStatement.builder()
				.statementReference("BSCT230606000990CSCT004000000000005")
				.accountOwner("POSOIT22XXX")
				.creditPayments(new BigDecimal(100))
				.debitPayments(new BigDecimal(50))
				.pendingPayments(BigDecimal.ZERO)
				.settlementDate(new Date())
				.service("SCT")
				.creationDateTime(new Timestamp(System.currentTimeMillis()))
				.build();
		List<CgsLmrPlcrBulkStatementEntry> paymentList = Arrays.asList(CgsLmrPlcrBulkStatementEntry.builder()
				.entryReference("CSR2306065016069")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("LCR-SETTLEDNOTPROVIDED")
				.debitorBic("BCITITMMXXX")
				.creditorBic("POSOIT22XXX")
				.cgsLmrPlcrBulkStatement(cp)
				.build(), CgsLmrPlcrBulkStatementEntry.builder()
				.entryReference("CSR2306065016070")
				.paymentAmount(new BigDecimal(200))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("LCR-SETTLEDNOTPROVIDED")
				.debitorBic("BCITITMMXXX")
				.creditorBic("POSOIT22XXX")
				.cgsLmrPlcrBulkStatement(cp)
				.build()
			);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.dateToString(Mockito.any(Date.class))).thenReturn("20250729");
		Mockito.when(this.dateUtils.dateToStringFormatted(Mockito.any(Date.class))).thenReturn("20250729");
		Mockito.doThrow(BicompException.class).when(this.minioService).uploadFile(Mockito.anyString(), Mockito.anyString(), Mockito.any(InputStream.class));
		
		assertThrows(BicompException.class, () -> {
			this.service.createCgsPaymentFile(new Date(), ltList, paymentList);
		});
		
		verify(this.dateUtils, times(1)).dateToString(Mockito.any(Date.class));
		verify(this.dateUtils, atLeastOnce()).dateToStringFormatted(Mockito.any(Date.class));
		verify(this.minioService, times(1)).uploadFile(Mockito.anyString(), Mockito.anyString(), Mockito.any(InputStream.class));
	}
	
	@Test
	@Order(42)
	void testCreateCgsPaymentFileParseException() throws BicompException, ParseException {
				
		CgsLmr lmr = CgsLmr.builder()
				.settlementBic("POSOIT20XXX")
				.build();
		CgsLmrLtinBulk clb = CgsLmrLtinBulk.builder()
				.cgsLmr(lmr)
				.build();
		CgsLmrLtinBulkStatement cl = CgsLmrLtinBulkStatement.builder()
				.statementReference("BSCT230606000990CSCT004000000000005")
				.accountOwner("POSOIT22XXX")
				.creditLiquidityTransfer(new BigDecimal(100))
				.debitLiquidityTransfer(new BigDecimal(50))
				.settlementDate(new Date())
				.creationDateTime(new Timestamp(System.currentTimeMillis()))
				.cgsLmrLtinBulk(clb)
				.build();
		List<CgsLmrLtinBulkStatementEntry> ltList = Arrays.asList(CgsLmrLtinBulkStatementEntry.builder()
				.entryReference("CG2306050000069T")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("C-FUND")
				.cgsLmrLtinBulkStatement(cl)
				.build(), CgsLmrLtinBulkStatementEntry.builder()
				.entryReference("CG2306050000069V")
				.paymentAmount(new BigDecimal(200))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("C-FUND")
				.cgsLmrLtinBulkStatement(cl)
				.build()
			);
		
		CgsLmrPlcrBulkStatement cp = CgsLmrPlcrBulkStatement.builder()
				.statementReference("BSCT230606000990CSCT004000000000005")
				.accountOwner("POSOIT22XXX")
				.creditPayments(new BigDecimal(100))
				.debitPayments(new BigDecimal(50))
				.pendingPayments(BigDecimal.ZERO)
				.settlementDate(new Date())
				.service("SCT")
				.creationDateTime(new Timestamp(System.currentTimeMillis()))
				.build();
		List<CgsLmrPlcrBulkStatementEntry> paymentList = Arrays.asList(CgsLmrPlcrBulkStatementEntry.builder()
				.entryReference("CSR2306065016069")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("LCR-SETTLEDNOTPROVIDED")
				.debitorBic("BCITITMMXXX")
				.creditorBic("POSOIT22XXX")
				.cgsLmrPlcrBulkStatement(cp)
				.build(), CgsLmrPlcrBulkStatementEntry.builder()
				.entryReference("CSR2306065016070")
				.paymentAmount(new BigDecimal(200))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("LCR-SETTLEDNOTPROVIDED")
				.debitorBic("BCITITMMXXX")
				.creditorBic("POSOIT22XXX")
				.cgsLmrPlcrBulkStatement(cp)
				.build()
			);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.dateToString(Mockito.any(Date.class))).thenThrow(ParseException.class);
		
		assertThrows(ParseException.class, () -> {
			this.service.createCgsPaymentFile(new Date(), ltList, paymentList);
		});
		
		verify(this.dateUtils, times(1)).dateToString(Mockito.any(Date.class));
		verify(this.dateUtils, times(0)).dateToStringFormatted(Mockito.any(Date.class));
		verify(this.minioService, times(0)).uploadFile(Mockito.anyString(), Mockito.anyString(), Mockito.any(InputStream.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(43)
	void testGetLiquidityTransfersAllFilter() throws ResourceNotFoundException, ParseException {
		
		Page<CgsLmrLtinBulkStatementEntry> p = new PageImpl<>(Arrays.asList(CgsLmrLtinBulkStatementEntry.builder()
				.entryReference("CG2306050000069T")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("C-FUND")
				.build(), CgsLmrLtinBulkStatementEntry.builder()
				.entryReference("CG2306050000069V")
				.paymentAmount(new BigDecimal(200))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("C-FUND")
				.build()
			));
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDateYear(Mockito.anyString())).thenReturn(new Date());
		Mockito.when(this.cgsLmrLtinBulkStatementEntryRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
			.thenReturn(p);
		
		PageableCgsEntryDto result = this.service.getLiquidityTransfers('C', new BigDecimal(0), new BigDecimal(300), "2023-06-07", "2023-06-07", "01", "BOOK", PageRequest.of(0, 10));
		assertThat(result.getEntries()).isNotEmpty().hasSameSizeAs(p.getContent());
		
		verify(this.dateUtils, times(2)).stringToDateYear(Mockito.anyString());
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(1)).findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(44)
	void testGetLiquidityTransfersNoFilter() throws ResourceNotFoundException, ParseException {
		
		Page<CgsLmrLtinBulkStatementEntry> p = new PageImpl<>(Arrays.asList(CgsLmrLtinBulkStatementEntry.builder()
				.entryReference("CG2306050000069T")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("C-FUND")
				.build(), CgsLmrLtinBulkStatementEntry.builder()
				.entryReference("CG2306050000069V")
				.paymentAmount(new BigDecimal(200))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("C-FUND")
				.build()
			));
		Mockito.when(this.cgsLmrLtinBulkStatementEntryRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
			.thenReturn(p);
		
		PageableCgsEntryDto result = this.service.getLiquidityTransfers(null, null, null, null, null, null, null, PageRequest.of(0, 10));
		assertThat(result.getEntries()).isNotEmpty().hasSameSizeAs(p.getContent());
		
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(1)).findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(45)
	void testGetLiquidityTransfersEmptyList() throws ResourceNotFoundException, ParseException {
		
		Page<CgsLmrLtinBulkStatementEntry> p = new PageImpl<>(Arrays.asList());
		
		Mockito.when(this.cgsLmrLtinBulkStatementEntryRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
		.thenReturn(p);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getLiquidityTransfers('C', null, null, null, null, null, null, PageRequest.of(0, 10));
		});
		
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(1)).findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(46)
	void testGetPaymentsAllFilter() throws ResourceNotFoundException, ParseException {
		
		CgsLmrPlcrBulkStatement c = CgsLmrPlcrBulkStatement.builder()
				.statementReference("BSCT230606000990CSCT004000000000005")
				.accountOwner("POSOIT22XXX")
				.creditPayments(new BigDecimal(100))
				.debitPayments(new BigDecimal(50))
				.pendingPayments(BigDecimal.ZERO)
				.settlementDate(new Date())
				.service("SCT")
				.build();
		Page<CgsLmrPlcrBulkStatementEntry> p = new PageImpl<>(Arrays.asList(CgsLmrPlcrBulkStatementEntry.builder()
				.entryReference("CSR2306065016069")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("LCR-SETTLEDNOTPROVIDED")
				.debitorBic("BCITITMMXXX")
				.creditorBic("POSOIT22XXX")
				.cgsLmrPlcrBulkStatement(c)
				.build(), CgsLmrPlcrBulkStatementEntry.builder()
				.entryReference("CSR2306065016070")
				.paymentAmount(new BigDecimal(200))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("LCR-SETTLEDNOTPROVIDED")
				.debitorBic("BCITITMMXXX")
				.creditorBic("POSOIT22XXX")
				.cgsLmrPlcrBulkStatement(c)
				.build())
			);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDateYear(Mockito.anyString())).thenReturn(new Date());
		Mockito.when(this.cgsLmrPlcrBulkStatementEntryRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
			.thenReturn(p);
		
		PageableCgsEntryDto result = this.service.getPayments('C', new BigDecimal(0), new BigDecimal(300), "2023-06-07", "2023-06-07", "SCT", "01", "BOOK", PageRequest.of(0, 10));
		assertThat(result.getEntries()).isNotEmpty().hasSameSizeAs(p.getContent());
		
		verify(this.dateUtils, times(2)).stringToDateYear(Mockito.anyString());
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, times(1)).findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(47)
	void testGetPaymentsNoFilter() throws ResourceNotFoundException, ParseException {
		
		CgsLmrPlcrBulkStatement c = CgsLmrPlcrBulkStatement.builder()
				.statementReference("BSCT230606000990CSCT004000000000005")
				.accountOwner("POSOIT22XXX")
				.creditPayments(new BigDecimal(100))
				.debitPayments(new BigDecimal(50))
				.pendingPayments(BigDecimal.ZERO)
				.settlementDate(new Date())
				.service("SCT")
				.build();
		Page<CgsLmrPlcrBulkStatementEntry> p = new PageImpl<>(Arrays.asList(CgsLmrPlcrBulkStatementEntry.builder()
				.entryReference("CSR2306065016069")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("LCR-SETTLEDNOTPROVIDED")
				.debitorBic("BCITITMMXXX")
				.creditorBic("POSOIT22XXX")
				.cgsLmrPlcrBulkStatement(c)
				.build(), CgsLmrPlcrBulkStatementEntry.builder()
				.entryReference("CSR2306065016070")
				.paymentAmount(new BigDecimal(200))
				.currency("EUR")
				.side('C')
				.status("BOOK")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.additionalInfo("LCR-SETTLEDNOTPROVIDED")
				.debitorBic("BCITITMMXXX")
				.creditorBic("POSOIT22XXX")
				.cgsLmrPlcrBulkStatement(c)
				.build())
			);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDateYear(Mockito.anyString())).thenReturn(new Date());
		Mockito.when(this.cgsLmrPlcrBulkStatementEntryRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
			.thenReturn(p);
		
		PageableCgsEntryDto result = this.service.getPayments(null, null, null, null, null, null, null, null, PageRequest.of(0, 10));
		assertThat(result.getEntries()).isNotEmpty().hasSameSizeAs(p.getContent());
		
		verify(this.dateUtils, times(0)).stringToDateYear(Mockito.anyString());
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, times(1)).findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(48)
	void testGetPaymentsEmptyList() throws ResourceNotFoundException, ParseException {
		
		Page<CgsLmrPlcrBulkStatementEntry> p = new PageImpl<>(Arrays.asList());
		
		Mockito.when(this.cgsLmrPlcrBulkStatementEntryRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
			.thenReturn(p);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getPayments('C', null, null, null, null, null, null, null, PageRequest.of(0, 10));
		});
		
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, times(1)).findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class));
	}
	
	@Test
	@Order(49)
	void testGetCgsLastBalanceByDateParseException() throws ResourceNotFoundException, BicompException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenThrow(ParseException.class);
		
		assertThrows(ParseException.class, () -> {
			this.service.getCgsLastBalanceByDate("01-09-2023");
		});

		verify(this.cgsLmrRepository, times(0)).findLastStatementByDate(Mockito.any(Date.class), Mockito.any(PageRequest.class));
	}
	
	@Test
	@Order(50)
	void testGetCurrentDateLacParseException() throws ResourceNotFoundException, BicompException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenThrow(ParseException.class);
		
		assertThrows(ParseException.class, () -> {
			this.service.getCurrentDateLac("01-09-2023");
		});

		verify(this.cgsLmrRepository, times(0)).findByFileSettlementDate(Mockito.any(Date.class));
	}
	
	@Test
	@Order(51)
	void testProcessLmrReportPlcrRevockedPayment() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_PlcrRevockedPayment.B"));
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_PlcrRevockedPayment.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		this.service.processLmrReport(list);
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(1)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, times(0)).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, atLeastOnce()).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(52)
	void testProcessLmrReportEmpty() throws DataIntegrityViolationException, BicompException, IOException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_Empty.B"));
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_Empty.B"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.service.processLmrReport(list);
		
		verify(this.dateUtils, times(0)).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.cgsLmrRepository, times(0)).save(Mockito.any(CgsLmr.class));
		verify(this.cgsLmrLacBulkRepository, times(0)).save(Mockito.any(CgsLmrLacBulk.class));
		verify(this.cgsLmrLacBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLacBulkStatement.class));
		verify(this.cgsLmrLtinBulkRepository, times(0)).save(Mockito.any(CgsLmrLtinBulk.class));
		verify(this.cgsLmrLtinBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatement.class));
		verify(this.cgsLmrLtinBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrLtinBulkStatementEntry.class));
		verify(this.cgsLmrPlcrBulkRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulk.class));
		verify(this.cgsLmrPlcrBulkStatementRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatement.class));
		verify(this.cgsLmrPlcrBulkStatementEntryRepository, times(0)).save(Mockito.any(CgsLmrPlcrBulkStatementEntry.class));
	}
	
	@Test
	@Order(53)
	void testGetSaldiCgs() {
		
		List<CgsLmrLacBulkStatement> list = Arrays.asList(CgsLmrLacBulkStatement.builder()
				.id(BigDecimal.ONE)
				.statementReference("BSCT230606001244ACGS000000000000001")
				.accountOwner("POSOIT22XXX")
				.openingBalance(new BigDecimal(10))
				.openingBalanceSide('C')
				.closingBalance(new BigDecimal(20))
				.closingBalanceSide('C')
				.settlementDate(new Date())
				.build());
		
		Mockito.when(this.cgsLmrLacBulkStatementRepository.findBySettlementDate(Mockito.any(Date.class))).thenReturn(list);
		
		List<CgsLmrLacBulkStatement> result = this.service.getSaldiCgs(new Date());
		
		assertThat(result).isNotNull().hasSize(1);
		verify(this.cgsLmrLacBulkStatementRepository, times(1)).findBySettlementDate(Mockito.any(Date.class));
	}
	
	@Test
	@Order(54)
	void testGetLastCgsBalance() {
		
		Page<CgsLmrLacBulkStatement> page = new PageImpl<>(Arrays.asList(CgsLmrLacBulkStatement.builder()
				.id(BigDecimal.ONE)
				.statementReference("BSCT230606001244ACGS000000000000001")
				.accountOwner("POSOIT22XXX")
				.openingBalance(new BigDecimal(10))
				.openingBalanceSide('C')
				.closingBalance(new BigDecimal(20))
				.closingBalanceSide('C')
				.settlementDate(new Date())
				.build())
		);
		
		Mockito.when(this.cgsLmrLacBulkStatementRepository.findBySettlementDate(Mockito.any(Date.class), 
				Mockito.any(PageRequest.class))).thenReturn(page);
		
		CgsLmrLacBulkStatement result = this.service.getLastCgsBalance(new Date());
		
		assertThat(result).isNotNull();
		assertThat(result.getStatementReference()).isEqualTo(page.getContent().get(0).getStatementReference());
		verify(this.cgsLmrLacBulkStatementRepository, times(1)).findBySettlementDate(Mockito.any(Date.class), 
				Mockito.any(PageRequest.class));
	}
	
	@Test
	@Order(55)
	void testGetLastCgsBalanceException() {
		
		Page<CgsLmrLacBulkStatement> page = new PageImpl<>(Arrays.asList());
		
		Mockito.when(this.cgsLmrLacBulkStatementRepository.findBySettlementDate(Mockito.any(Date.class), 
				Mockito.any(PageRequest.class))).thenReturn(page);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getLastCgsBalance(new Date());
		});
		
		verify(this.cgsLmrLacBulkStatementRepository, times(1)).findBySettlementDate(Mockito.any(Date.class), 
				Mockito.any(PageRequest.class));
	}
	
	@Test
	@Order(56)
	void testCgsServiceImplEquals() {
		
		CgsServiceImpl test = new CgsServiceImpl(this.config, this.minioService, this.cgsLmrRepository, this.cgsLmrLacBulkRepository, 
				this.cgsLmrLacBulkStatementRepository, this.cgsLmrLtinBulkRepository, this.cgsLmrLtinBulkStatementRepository, 
				this.cgsLmrLtinBulkStatementEntryRepository, this.cgsLmrPlcrBulkRepository, this.cgsLmrPlcrBulkStatementRepository, 
				this.cgsLmrPlcrBulkStatementEntryRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isTrue();
	}
	
	@Test
	@Order(57)
	void testCgsServiceImplEqualsSameInstance() {
		
		assertThat(this.service.equals(this.service)).isTrue();
	}
	
	@Test
	@Order(58)
	void testCgsServiceImplNotEqualsNotSameInstanceType() {
		
		assertThat(this.service.equals("test")).isFalse();
	}
	
	@Test
	@Order(59)
	void testCgsServiceImplEqualsNull() {
		
		this.service = new CgsServiceImpl(null, null, null, null, null, null, null, null, null, null, null);
		CgsServiceImpl test = new CgsServiceImpl(null, null, null, null, null, null, null, null, null, null, null);
		boolean result = this.service.equals(test);
		assertThat(result).isTrue();
	}
	
	@Test
	@Order(60)
	void testCgsServiceImplNotNull() {
		
		this.service = new CgsServiceImpl(null, null, null, null, null, null, null, null, null, null, null);
		CgsServiceImpl test = new CgsServiceImpl(this.config, this.minioService, this.cgsLmrRepository, this.cgsLmrLacBulkRepository, 
				this.cgsLmrLacBulkStatementRepository, this.cgsLmrLtinBulkRepository, this.cgsLmrLtinBulkStatementRepository, 
				this.cgsLmrLtinBulkStatementEntryRepository, this.cgsLmrPlcrBulkRepository, this.cgsLmrPlcrBulkStatementRepository, 
				this.cgsLmrPlcrBulkStatementEntryRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(61)
	void testCgsServiceImplNotEqualsConfigNull() {
		
		CgsServiceImpl test = new CgsServiceImpl(null, this.minioService, this.cgsLmrRepository, this.cgsLmrLacBulkRepository, 
				this.cgsLmrLacBulkStatementRepository, this.cgsLmrLtinBulkRepository, this.cgsLmrLtinBulkStatementRepository, 
				this.cgsLmrLtinBulkStatementEntryRepository, this.cgsLmrPlcrBulkRepository, this.cgsLmrPlcrBulkStatementRepository, 
				this.cgsLmrPlcrBulkStatementEntryRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(62)
	void testCgsServiceImplNotEqualsCgsLmrNull() {
		
		CgsServiceImpl test = new CgsServiceImpl(this.config, this.minioService, null, this.cgsLmrLacBulkRepository, 
				this.cgsLmrLacBulkStatementRepository, this.cgsLmrLtinBulkRepository, this.cgsLmrLtinBulkStatementRepository, 
				this.cgsLmrLtinBulkStatementEntryRepository, this.cgsLmrPlcrBulkRepository, this.cgsLmrPlcrBulkStatementRepository, 
				this.cgsLmrPlcrBulkStatementEntryRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(63)
	void testCgsServiceImplNotEqualsCgsLmrLacBulkNull() {
		
		CgsServiceImpl test = new CgsServiceImpl(this.config, this.minioService, this.cgsLmrRepository, null, 
				this.cgsLmrLacBulkStatementRepository, this.cgsLmrLtinBulkRepository, this.cgsLmrLtinBulkStatementRepository, 
				this.cgsLmrLtinBulkStatementEntryRepository, this.cgsLmrPlcrBulkRepository, this.cgsLmrPlcrBulkStatementRepository, 
				this.cgsLmrPlcrBulkStatementEntryRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(64)
	void testCgsServiceImplNotEqualsCgsLmrLacBulkStatNull() {
		
		CgsServiceImpl test = new CgsServiceImpl(this.config, this.minioService, this.cgsLmrRepository, this.cgsLmrLacBulkRepository, 
				null, this.cgsLmrLtinBulkRepository, this.cgsLmrLtinBulkStatementRepository, 
				this.cgsLmrLtinBulkStatementEntryRepository, this.cgsLmrPlcrBulkRepository, this.cgsLmrPlcrBulkStatementRepository, 
				this.cgsLmrPlcrBulkStatementEntryRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(65)
	void testCgsServiceImplNotEqualsCgsLmrLtinBulkNull() {
		
		CgsServiceImpl test = new CgsServiceImpl(this.config, this.minioService, this.cgsLmrRepository, this.cgsLmrLacBulkRepository, 
				this.cgsLmrLacBulkStatementRepository, null, this.cgsLmrLtinBulkStatementRepository, 
				this.cgsLmrLtinBulkStatementEntryRepository, this.cgsLmrPlcrBulkRepository, this.cgsLmrPlcrBulkStatementRepository, 
				this.cgsLmrPlcrBulkStatementEntryRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(66)
	void testCgsServiceImplNotEqualsCgsLmrLtinBulkStatNull() {
		
		CgsServiceImpl test = new CgsServiceImpl(this.config, this.minioService, this.cgsLmrRepository, this.cgsLmrLacBulkRepository, 
				this.cgsLmrLacBulkStatementRepository, this.cgsLmrLtinBulkRepository, null, 
				this.cgsLmrLtinBulkStatementEntryRepository, this.cgsLmrPlcrBulkRepository, this.cgsLmrPlcrBulkStatementRepository, 
				this.cgsLmrPlcrBulkStatementEntryRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(67)
	void testCgsServiceImplNotEqualsCgsLmrLtinBulkStatEntryNull() {
		
		CgsServiceImpl test = new CgsServiceImpl(this.config, this.minioService, this.cgsLmrRepository, this.cgsLmrLacBulkRepository, 
				this.cgsLmrLacBulkStatementRepository, this.cgsLmrLtinBulkRepository, this.cgsLmrLtinBulkStatementRepository, 
				null, this.cgsLmrPlcrBulkRepository, this.cgsLmrPlcrBulkStatementRepository, 
				this.cgsLmrPlcrBulkStatementEntryRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(68)
	void testCgsServiceImplNotEqualsCgsLmrPlcrBulkNull() {
		
		CgsServiceImpl test = new CgsServiceImpl(this.config, this.minioService, this.cgsLmrRepository, this.cgsLmrLacBulkRepository, 
				this.cgsLmrLacBulkStatementRepository, this.cgsLmrLtinBulkRepository, this.cgsLmrLtinBulkStatementRepository, 
				this.cgsLmrLtinBulkStatementEntryRepository, null, this.cgsLmrPlcrBulkStatementRepository, 
				this.cgsLmrPlcrBulkStatementEntryRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(69)
	void testCgsServiceImplNotEqualsCgsLmrPlcrBulkStatNull() {
		
		CgsServiceImpl test = new CgsServiceImpl(this.config, this.minioService, this.cgsLmrRepository, this.cgsLmrLacBulkRepository, 
				this.cgsLmrLacBulkStatementRepository, this.cgsLmrLtinBulkRepository, this.cgsLmrLtinBulkStatementRepository, 
				this.cgsLmrLtinBulkStatementEntryRepository, this.cgsLmrPlcrBulkRepository, null, 
				this.cgsLmrPlcrBulkStatementEntryRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(70)
	void testCgsServiceImplNotEqualsCgsLmrPlcrBulkEntryNull() {
		
		CgsServiceImpl test = new CgsServiceImpl(this.config, this.minioService, this.cgsLmrRepository, this.cgsLmrLacBulkRepository, 
				this.cgsLmrLacBulkStatementRepository, this.cgsLmrLtinBulkRepository, this.cgsLmrLtinBulkStatementRepository, 
				this.cgsLmrLtinBulkStatementEntryRepository, this.cgsLmrPlcrBulkRepository, this.cgsLmrPlcrBulkStatementRepository, 
				null);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(71)
	void testCgsServiceImplNotEqualsMinioNull() {
		
		CgsServiceImpl test = new CgsServiceImpl(this.config, null, this.cgsLmrRepository, this.cgsLmrLacBulkRepository, 
				this.cgsLmrLacBulkStatementRepository, this.cgsLmrLtinBulkRepository, this.cgsLmrLtinBulkStatementRepository, 
				this.cgsLmrLtinBulkStatementEntryRepository, this.cgsLmrPlcrBulkRepository, this.cgsLmrPlcrBulkStatementRepository, 
				this.cgsLmrPlcrBulkStatementEntryRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(72)
	void testCgsServiceImplEqualsHashCode() {
		
		CgsServiceImpl test = new CgsServiceImpl(this.config, this.minioService, this.cgsLmrRepository, this.cgsLmrLacBulkRepository, 
				this.cgsLmrLacBulkStatementRepository, this.cgsLmrLtinBulkRepository, this.cgsLmrLtinBulkStatementRepository, 
				this.cgsLmrLtinBulkStatementEntryRepository, this.cgsLmrPlcrBulkRepository, this.cgsLmrPlcrBulkStatementRepository, 
				this.cgsLmrPlcrBulkStatementEntryRepository);
		int result = this.service.hashCode();
		assertThat(result).isEqualTo(test.hashCode());
	}
	
	@Test
	@Order(73)
	void testCgsServiceImplNotEqualsHashCode() {
		
		CgsServiceImpl test = new CgsServiceImpl(null, null, null, null, null, null, null, null, null, null, null);
		int result = this.service.hashCode();
		assertThat(result).isNotEqualTo(test.hashCode());
	}
	
	@AfterEach
	public void cleanUp() {
		this.mockDateUtils.close();
	}
	
	private Item mockItem(String objectName) {
		Item item = Mockito.mock(Item.class);
		Mockito.when(item.objectName()).thenReturn(objectName);
		
		return item;
	}

}
