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
import java.time.LocalDate;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.minio.messages.Item;
import it.popso.bicomp.dto.PageableRt1EntryDto;
import it.popso.bicomp.dto.Rt1LacDto;
import it.popso.bicomp.dto.Rt1TotalDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.FileManagerException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.flat.PsrFileHeader;
import it.popso.bicomp.flat.PsrParticipantBody;
import it.popso.bicomp.flat.PsrParticipantHeader;
import it.popso.bicomp.flat.PsrSettlementBicHeader;
import it.popso.bicomp.model.IsoExternalCodeSet;
import it.popso.bicomp.model.Rt1Bulk;
import it.popso.bicomp.model.Rt1BulkPayment;
import it.popso.bicomp.model.Rt1PsrFileHeader;
import it.popso.bicomp.model.Rt1PsrParticipantBody;
import it.popso.bicomp.model.Rt1PsrParticipantHeader;
import it.popso.bicomp.model.Rt1PsrSettlementBic;
import it.popso.bicomp.properties.Access;
import it.popso.bicomp.properties.BicompConfig;
import it.popso.bicomp.properties.Bucket;
import it.popso.bicomp.properties.Minio;
import it.popso.bicomp.properties.Rt1;
import it.popso.bicomp.repository.IsoExternalCodeSetRepository;
import it.popso.bicomp.repository.Rt1BulkPaymentRepository;
import it.popso.bicomp.repository.Rt1BulkRepository;
import it.popso.bicomp.repository.Rt1PsrFileHeaderRepository;
import it.popso.bicomp.repository.Rt1PsrParticipantBodyRepository;
import it.popso.bicomp.repository.Rt1PsrParticipantHeaderRepository;
import it.popso.bicomp.repository.Rt1PsrSettlementBicRepository;
import it.popso.bicomp.service.impl.Rt1ServiceImpl;
import it.popso.bicomp.utils.DateUtils;
import it.popso.bicomp.utils.FileManager;
import it.popso.bicomp.utils.GenericSpecification;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class Rt1ServiceImplTest {
	
	@Mock
	private MinioService minioService;
	
	@Mock
	private Rt1BulkRepository rt1BulkRepository;
	
	@Mock
	private Rt1BulkPaymentRepository rt1BulkPaymentRepository;
	
	@Mock
	private Rt1PsrFileHeaderRepository rt1PsrFileHeaderRepository;
	
	@Mock
	private Rt1PsrSettlementBicRepository rt1PsrSettlementBicRepository;
	
	@Mock
	private Rt1PsrParticipantHeaderRepository rt1PsrParticipantHeaderRepository;
	
	@Mock
	private Rt1PsrParticipantBodyRepository rt1PsrParticipantBodyRepository;
	
	@Mock
	private IsoExternalCodeSetRepository isoExternalCodeSetRepository;
	
	private BicompConfig config;
	private Rt1ServiceImpl service;
	private DateUtils dateUtils;
	private MockedStatic<DateUtils> mockDateUtils;
	private FileManager fileManager;
	private MockedStatic<FileManager> mockFileManager;
	
	
	@BeforeEach
    public void setup() {
		Rt1 rt1 = new Rt1();
		rt1.setShare("rt1");
		rt1.setBulkPrefix("RT02SCIPOSOIT");
		rt1.setBulkSuffix(".S");
		rt1.setPsrPrefix("RT02SCIPOSOIT");
		rt1.setPsrSuffix(".P");
		
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
		this.config.setRt1(rt1);
		this.config.setMinio(minio);
		
		this.dateUtils = Mockito.mock(DateUtils.class);
		this.mockDateUtils = Mockito.mockStatic(DateUtils.class);
		this.fileManager = Mockito.mock(FileManager.class);
		this.mockFileManager = Mockito.mockStatic(FileManager.class);
		
		this.service = new Rt1ServiceImpl(this.config, this.minioService, this.rt1BulkRepository, this.rt1BulkPaymentRepository, 
				this.rt1PsrFileHeaderRepository, this.rt1PsrSettlementBicRepository, this.rt1PsrParticipantHeaderRepository, 
				this.rt1PsrParticipantBodyRepository, this.isoExternalCodeSetRepository);
	}
	
	@Test
	@Order(1)
	void testProcessBulkReportNoReport() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of();
		
		this.service.processBulkReport(list);
		
		verify(this.minioService, times(0)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.rt1BulkRepository, times(0)).save(Mockito.any(Rt1Bulk.class));
		verify(this.rt1BulkPaymentRepository, times(0)).save(Mockito.any(Rt1BulkPayment.class));
	}
	
	@Test
	@Order(2)
	void testProcessBulkReportMultiplePayments() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22_multiple_payments.S"));
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/rt1/RT02SCIPOSOIT22_multiple_payments.S"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		this.service.processBulkReport(list);
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.rt1BulkRepository, times(1)).save(Mockito.any(Rt1Bulk.class));
		verify(this.rt1BulkPaymentRepository, atLeastOnce()).save(Mockito.any(Rt1BulkPayment.class));
	}
	
	@Test
	@Order(3)
	void testProcessBulkReportOnePayment() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22_one_payment.S"));
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/rt1/RT02SCIPOSOIT22_one_payment.S"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		this.service.processBulkReport(list);
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.rt1BulkRepository, times(1)).save(Mockito.any(Rt1Bulk.class));
		verify(this.rt1BulkPaymentRepository, times(1)).save(Mockito.any(Rt1BulkPayment.class));
	}
	
	@Test
	@Order(4)
	void testProcessBulkReportDataIntegrityViolationException() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22_multiple_payments.S"));
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/rt1/RT02SCIPOSOIT22_multiple_payments.S"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		Mockito.when(this.rt1BulkRepository.save(Mockito.any(Rt1Bulk.class))).thenThrow(new DataIntegrityViolationException("Rt1Bulk already exists"));
		
		assertThrows(DataIntegrityViolationException.class, () -> {
			this.service.processBulkReport(list);
		});
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.rt1BulkRepository, times(1)).save(Mockito.any(Rt1Bulk.class));
		verify(this.rt1BulkPaymentRepository, times(0)).save(Mockito.any(Rt1BulkPayment.class));
	}
	
	@Test
	@Order(5)
	void testProcessBulkReportBicompException() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22_bicomp_exception.S"));
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/rt1/RT02SCIPOSOIT22_bicomp_exception.S"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		assertThrows(BicompException.class, () -> {
			this.service.processBulkReport(list);
		});
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.rt1BulkRepository, times(1)).save(Mockito.any(Rt1Bulk.class));
		verify(this.rt1BulkPaymentRepository, times(0)).save(Mockito.any(Rt1BulkPayment.class));
	}
	
	@Test
	@Order(6)
	void testProcessBulkReportNullAcceptanceDateTime() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22_null_acceptance_date_time.S"));
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/rt1/RT02SCIPOSOIT22_null_acceptance_date_time.S"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		this.service.processBulkReport(list);
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.rt1BulkRepository, times(1)).save(Mockito.any(Rt1Bulk.class));
		verify(this.rt1BulkPaymentRepository, times(1)).save(Mockito.any(Rt1BulkPayment.class));
	}
	
	@Test
	@Order(7)
	void testProcessBulkReportRejectPayment() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22_reject_payment.S"));
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/rt1/RT02SCIPOSOIT22_reject_payment.S"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		this.service.processBulkReport(list);
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.rt1BulkRepository, times(1)).save(Mockito.any(Rt1Bulk.class));
		verify(this.rt1BulkPaymentRepository, times(1)).save(Mockito.any(Rt1BulkPayment.class));
	}
	
	@Test
	@Order(8)
	void testProcessBulkReportDebitCreditPayment() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22_debit_credit_payments.S"));
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/rt1/RT02SCIPOSOIT22_debit_credit_payments.S"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		this.service.processBulkReport(list);
		
		verify(this.dateUtils, atLeastOnce()).gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class));
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.rt1BulkRepository, atLeastOnce()).save(Mockito.any(Rt1Bulk.class));
		verify(this.rt1BulkPaymentRepository, atLeastOnce()).save(Mockito.any(Rt1BulkPayment.class));
	}
	
	@Test
	@Order(9)
	void testGetBulkReportEmpty() throws FileManagerException, IOException {
		
		List<Item> list = List.of();
		Mockito.when(this.minioService.getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
			.thenReturn(list);
		
		List<Item> result = this.service.getBulkReport();
		
		assertThat(result).isEmpty();
		
		verify(this.minioService, times(1)).getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());
	}
	
	@Test
	@Order(10)
	void testGetBulkReport() throws FileManagerException, IOException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22_multiple_payments.S"));
		Mockito.when(this.minioService.getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
			.thenReturn(list);
		
		List<Item> result = this.service.getBulkReport();
		
		assertThat(result).hasSize(1);
		
		verify(this.minioService, times(1)).getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());
	}
	
	@Test
	@Order(11)
	void testGetBulkReportBicompException() throws BicompException {
		
		Mockito.when(this.minioService.getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
			.thenThrow(new BicompException("Minio exception"));
		
		assertThrows(BicompException.class, () -> {
			this.service.getBulkReport();
		});
		
		verify(this.minioService, times(1)).getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());
	}
	
	@Test
	@Order(12)
	void testMoveToBackupFolder() throws FileManagerException, IOException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22_multiple_payments.S"));
		
		Mockito.doNothing().when(this.minioService).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		Mockito.doNothing().when(this.minioService).removeFile(Mockito.anyString(), Mockito.anyString());
		
		this.service.moveToBackupFolder(list);
		
		verify(this.minioService, times(1)).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		verify(this.minioService, times(1)).removeFile(Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(13)
	void testMoveToBackupFolderMultipleFiles() throws FileManagerException, IOException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22_multiple_payments.S"), 
				this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22_multiple_payments.S"));
		
		this.service.moveToBackupFolder(list);
		
		verify(this.minioService, times(2)).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		verify(this.minioService, times(2)).removeFile(Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(14)
	void testMoveToBackupFolderIOException() throws FileManagerException, IOException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22_multiple_payments.S"));
		Mockito.doThrow(new BicompException("MinIO exception")).when(this.minioService).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		
		assertThrows(BicompException.class, () -> {
			this.service.moveToBackupFolder(list);
		});
		
		verify(this.minioService, times(1)).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		verify(this.minioService, times(0)).removeFile(Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(15)
	void testGetPsrReportEmpty() throws FileManagerException, IOException {
		
		List<Item> list = List.of();
		Mockito.when(this.minioService.getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
			.thenReturn(list);
		
		List<Item> result = this.service.getPsrReport();
		
		assertThat(result).isEmpty();
		
		verify(this.minioService, times(1)).getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());
	}
	
	@Test
	@Order(16)
	void testGetPsrReport() throws FileManagerException, IOException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22.P"));
		Mockito.when(this.minioService.getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
			.thenReturn(list);
		
		List<Item> result = this.service.getPsrReport();
		
		assertThat(result).hasSize(1);
		
		verify(this.minioService, times(1)).getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());
	}
	
	@Test
	@Order(17)
	void testGetPsrReportIOException() throws FileManagerException, IOException {
		
		Mockito.when(this.minioService.getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
			.thenThrow(new BicompException("Minio exception"));
	
		assertThrows(BicompException.class, () -> {
			this.service.getPsrReport();
		});
		
		verify(this.minioService, times(1)).getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());
	}
	
	@Test
	@Order(18)
	void testProcessPsrReportNoReport() throws DataIntegrityViolationException, IOException, BicompException, FileManagerException, ParseException {
		
		List<Item> list = List.of();
		this.service.processPreSettlementReport(list);
		
		verify(this.minioService, times(0)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.rt1PsrFileHeaderRepository, times(0)).save(Mockito.any(Rt1PsrFileHeader.class));
	}
	
	@Test
	@Order(19)
	void testProcessPsrReport() throws DataIntegrityViolationException, IOException, BicompException, FileManagerException, ParseException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22.P"));
		InputStream inputStream = new FileInputStream(new File("src/test/resources/rt1/RT02SCIPOSOIT22.P"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
				
		Rt1PsrFileHeader rt1PsrFileHeader = Rt1PsrFileHeader.builder().build();
		PsrFileHeader hpsr = PsrFileHeader.builder()
				.dateTime("230627130230")
				.fileType("PSR")
				.lac("03")
				.receivingInstitution("POSOIT22")
				.numberOfRecord("000007")
				.senderFileReference("PSCI230627000214")
				.sendingInstitution("IPSTFRPP")
				.serviceIdentifier("SCI")
				.settlementDate("230627")
				.testCode("P")
				.build();
		
		Rt1PsrSettlementBic rt1PsrSettlementBic = Rt1PsrSettlementBic.builder().build();
		PsrSettlementBicHeader psbh = PsrSettlementBicHeader.builder()
				.recordType("PSBH")
				.settlementBic("POSOIT22XXX")
				.initialPositionIndicator("CR")
				.initialLiquidityPosition("000000000000010,00")
				.finalPositionIndicator("CR")
				.finalLiquidityPosition("000000000000020,00")
				.build();
		
		Rt1PsrParticipantHeader rt1PsrParticipantHeader = Rt1PsrParticipantHeader.builder().build();
		PsrParticipantHeader pdph = PsrParticipantHeader.builder()
				.recordType("PDPH")
				.settlementBic("POSOIT22")
				.initialParticipantPositionIndicator("CR")
				.initialParticipantPosition("000000000000010,00")
				.finalParticipantPositionIndicator("CR")
				.finalParticipantPosition("000000000000020,00")
				.receivedFundingAmount("000000000000005,00")
				.requestedFundingAmount("000000000000000,00")
				.requestedDefundingAmount("000000000000000,00")
				.rejectedDefundingAmount("000000000000000,00")
				.creditLiquidityTransferAmount("000000000000005,00")
				.debitLiquidityTransferAmount("000000000000000,00")
				.instantPrrSentAmount("000000000000000,00")
				.instantPrrReceivedAmount("000000000000000,00")
				.instantPrrPendingAmount("000000000000000,00")
				.rejectedAmountPreviousInstantPrrPending("000000000000000,00")
				.acceptedAmountPreviousInstantPrrPending("000000000000000,00")
				.instantPrrRt1aSentAmount("000000000000000,00")
				.instantPrrRt1aReceivedAmount("000000000000000,00")
				.instantPrrRt1aPendingAmount("000000000000000,00")
				.rejectedAmountPreviousInstantPrrRt1aPending("000000000000000,00")
				.acceptedAmountPreviousInstantPrrRt1aPending("000000000000000,00")
				.build();
		
		Rt1PsrParticipantBody rt1PsrParticipantBody = Rt1PsrParticipantBody.builder().build();
		PsrParticipantBody pdpb = PsrParticipantBody.builder()
				.recordType("PDPB")
				.operationType("CLRR")
				.liquidityInstructionReference("IPSTFRPP0627l9LmlqeCR72qslKtz9Aeuou")
				.liquidityOperationAmount("000000000000005,00")
				.liquidityInstructionStatus("STD")
				.build();
		Object[] o = new Object[] { Arrays.asList(pdph), Arrays.asList(pdpb) };
		
		this.mockFileManager.when(() -> FileManager.fileManager()).thenReturn(this.fileManager);
		Mockito.when(this.fileManager.getPsrString(Mockito.any(InputStream.class)))
			.thenReturn("HPSRSCIPSRIPSTFRPPPSCI230627000214230627130230PPOSOIT2223062703000007PSBHPOSOIT22XXXCR000000016533447,50CR000000012926545,92PDPHPOSOIT22CR000000016533447,50CR000000012926545,92000000000000000,00000000000000000,00000000004831171,23000000000000000,00000000000000000,00000000000000000,00000000000065399,16000000001289668,81000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00PDPBCLRRIPSTFRPP0627l9LmlqeCR72qslKtz9Aeuou000000004831171,23STDPDPTPSBTTPSR");
		Mockito.when(this.fileManager.parseHpsr(Mockito.anyString())).thenReturn(hpsr);
		Mockito.when(this.fileManager.parsePsbh(Mockito.anyString(), Mockito.anyInt())).thenReturn(psbh);
		Mockito.when(this.fileManager.parsePdph(Mockito.anyString(), Mockito.anyInt())).thenReturn(o);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDateTime(Mockito.anyString())).thenReturn(new Date());
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		Mockito.when(this.rt1PsrFileHeaderRepository.save(Mockito.any(Rt1PsrFileHeader.class))).thenReturn(rt1PsrFileHeader);
		Mockito.when(this.rt1PsrSettlementBicRepository.save(Mockito.any(Rt1PsrSettlementBic.class))).thenReturn(rt1PsrSettlementBic);
		Mockito.when(this.rt1PsrParticipantHeaderRepository.save(Mockito.any(Rt1PsrParticipantHeader.class))).thenReturn(rt1PsrParticipantHeader);
		Mockito.when(this.rt1PsrParticipantBodyRepository.save(Mockito.any(Rt1PsrParticipantBody.class))).thenReturn(rt1PsrParticipantBody);
		
		this.service.processPreSettlementReport(list);
		
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.fileManager, atLeastOnce()).getPsrString(Mockito.any(InputStream.class));
		verify(this.fileManager, atLeastOnce()).parseHpsr(Mockito.anyString());
		verify(this.fileManager, atLeastOnce()).parsePsbh(Mockito.anyString(), Mockito.anyInt());
		verify(this.fileManager, atLeastOnce()).parsePdph(Mockito.anyString(), Mockito.anyInt());
		verify(this.dateUtils, atLeastOnce()).stringToDateTime(Mockito.anyString());
		verify(this.dateUtils, atLeastOnce()).stringToDate(Mockito.anyString());
		verify(this.rt1PsrFileHeaderRepository, times(1)).save(Mockito.any(Rt1PsrFileHeader.class));
		verify(this.rt1PsrSettlementBicRepository, times(1)).save(Mockito.any(Rt1PsrSettlementBic.class));
		verify(this.rt1PsrParticipantHeaderRepository, times(1)).save(Mockito.any(Rt1PsrParticipantHeader.class));
		verify(this.rt1PsrParticipantBodyRepository, times(1)).save(Mockito.any(Rt1PsrParticipantBody.class));
	}
	
	@Test
	@Order(20)
	void testProcessPsrReportGetPsrStringFileManagerException() throws DataIntegrityViolationException, IOException, BicompException, FileManagerException, ParseException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22.P"));
		InputStream inputStream = new FileInputStream(new File("src/test/resources/rt1/RT02SCIPOSOIT22.P"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockFileManager.when(() -> FileManager.fileManager()).thenReturn(this.fileManager);
		Mockito.when(this.fileManager.getPsrString(Mockito.any(InputStream.class))).thenThrow(new FileManagerException("FileManagerException error"));
		
		assertThrows(FileManagerException.class, () -> {
			this.service.processPreSettlementReport(list);
		});
		
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.fileManager, times(1)).getPsrString(Mockito.any(InputStream.class));
		verify(this.fileManager, times(0)).parseHpsr(Mockito.anyString());
		verify(this.dateUtils, times(0)).stringToDateTime(Mockito.anyString());
		verify(this.dateUtils, times(0)).stringToDate(Mockito.anyString());
		verify(this.rt1PsrFileHeaderRepository, times(0)).save(Mockito.any(Rt1PsrFileHeader.class));
	}
	
	@Test
	@Order(21)
	void testProcessPsrReportParseHpsrFileManagerException() throws DataIntegrityViolationException, IOException, BicompException, FileManagerException, ParseException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22.P"));
		InputStream inputStream = new FileInputStream(new File("src/test/resources/rt1/RT02SCIPOSOIT22.P"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockFileManager.when(() -> FileManager.fileManager()).thenReturn(this.fileManager);
		Mockito.when(this.fileManager.getPsrString(Mockito.any(InputStream.class)))
			.thenReturn("HPSRSCIPSRIPSTFRPPPSCI230627000214230627130230PPOSOIT2223062703000007PSBHPOSOIT22XXXCR000000016533447,50CR000000012926545,92PDPHPOSOIT22CR000000016533447,50CR000000012926545,92000000000000000,00000000000000000,00000000004831171,23000000000000000,00000000000000000,00000000000000000,00000000000065399,16000000001289668,81000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00PDPBCLRRIPSTFRPP0627l9LmlqeCR72qslKtz9Aeuou000000004831171,23STDPDPTPSBTTPSR");
		Mockito.when(this.fileManager.parseHpsr(Mockito.anyString())).thenThrow(new FileManagerException("FileManagerException error"));
	
		assertThrows(FileManagerException.class, () -> {
			this.service.processPreSettlementReport(list);
		});
		
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.fileManager, times(1)).getPsrString(Mockito.any(InputStream.class));
		verify(this.fileManager, times(1)).parseHpsr(Mockito.anyString());
		verify(this.dateUtils, times(0)).stringToDateTime(Mockito.anyString());
		verify(this.dateUtils, times(0)).stringToDate(Mockito.anyString());
		verify(this.rt1PsrFileHeaderRepository, times(0)).save(Mockito.any(Rt1PsrFileHeader.class));
	}
	
	@Test
	@Order(22)
	void testProcessPsrReportStringToDateTimeParseException() throws DataIntegrityViolationException, IOException, BicompException, FileManagerException, ParseException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22.P"));
		InputStream inputStream = new FileInputStream(new File("src/test/resources/rt1/RT02SCIPOSOIT22.P"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		PsrFileHeader hpsr = PsrFileHeader.builder()
				.dateTime("230627130230")
				.fileType("PSR")
				.lac("03")
				.receivingInstitution("POSOIT22")
				.numberOfRecord("000007")
				.senderFileReference("PSCI230627000214")
				.sendingInstitution("IPSTFRPP")
				.serviceIdentifier("SCI")
				.settlementDate("230627")
				.testCode("P")
				.build();
		
		this.mockFileManager.when(() -> FileManager.fileManager()).thenReturn(this.fileManager);
		Mockito.when(this.fileManager.getPsrString(Mockito.any(InputStream.class)))
			.thenReturn("HPSRSCIPSRIPSTFRPPPSCI230627000214230627130230PPOSOIT2223062703000007PSBHPOSOIT22XXXCR000000016533447,50CR000000012926545,92PDPHPOSOIT22CR000000016533447,50CR000000012926545,92000000000000000,00000000000000000,00000000004831171,23000000000000000,00000000000000000,00000000000000000,00000000000065399,16000000001289668,81000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00PDPBCLRRIPSTFRPP0627l9LmlqeCR72qslKtz9Aeuou000000004831171,23STDPDPTPSBTTPSR");
		Mockito.when(this.fileManager.parseHpsr(Mockito.anyString())).thenReturn(hpsr);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDateTime(Mockito.anyString())).thenThrow(new ParseException("ParseException error", 0));
		
		assertThrows(ParseException.class, () -> {
			this.service.processPreSettlementReport(list);
		});
		
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.fileManager, atLeastOnce()).getPsrString(Mockito.any(InputStream.class));
		verify(this.fileManager, atLeastOnce()).parseHpsr(Mockito.anyString());
		verify(this.dateUtils, atLeastOnce()).stringToDateTime(Mockito.anyString());
		verify(this.dateUtils, times(0)).stringToDate(Mockito.anyString());
		verify(this.rt1PsrFileHeaderRepository, times(0)).save(Mockito.any(Rt1PsrFileHeader.class));
	}
	
	@Test
	@Order(23)
	void testProcessPsrReportStringToDateParseException() throws DataIntegrityViolationException, IOException, BicompException, FileManagerException, ParseException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22.P"));
		InputStream inputStream = new FileInputStream(new File("src/test/resources/rt1/RT02SCIPOSOIT22.P"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		PsrFileHeader hpsr = PsrFileHeader.builder()
				.dateTime("230627130230")
				.fileType("PSR")
				.lac("03")
				.receivingInstitution("POSOIT22")
				.numberOfRecord("000007")
				.senderFileReference("PSCI230627000214")
				.sendingInstitution("IPSTFRPP")
				.serviceIdentifier("SCI")
				.settlementDate("230627")
				.testCode("P")
				.build();
		
		this.mockFileManager.when(() -> FileManager.fileManager()).thenReturn(this.fileManager);
		Mockito.when(this.fileManager.getPsrString(Mockito.any(InputStream.class)))
			.thenReturn("HPSRSCIPSRIPSTFRPPPSCI230627000214230627130230PPOSOIT2223062703000007PSBHPOSOIT22XXXCR000000016533447,50CR000000012926545,92PDPHPOSOIT22CR000000016533447,50CR000000012926545,92000000000000000,00000000000000000,00000000004831171,23000000000000000,00000000000000000,00000000000000000,00000000000065399,16000000001289668,81000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00PDPBCLRRIPSTFRPP0627l9LmlqeCR72qslKtz9Aeuou000000004831171,23STDPDPTPSBTTPSR");
		Mockito.when(this.fileManager.parseHpsr(Mockito.anyString())).thenReturn(hpsr);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDateTime(Mockito.anyString())).thenReturn(new Date());
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenThrow(new ParseException("ParseException error", 0));
		
		assertThrows(ParseException.class, () -> {
			this.service.processPreSettlementReport(list);
		});
		
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.fileManager, atLeastOnce()).getPsrString(Mockito.any(InputStream.class));
		verify(this.fileManager, atLeastOnce()).parseHpsr(Mockito.anyString());
		verify(this.dateUtils, atLeastOnce()).stringToDateTime(Mockito.anyString());
		verify(this.dateUtils, atLeastOnce()).stringToDate(Mockito.anyString());
		verify(this.rt1PsrFileHeaderRepository, times(0)).save(Mockito.any(Rt1PsrFileHeader.class));
	}
	
	@Test
	@Order(24)
	void testProcessPsrReportDataIntegrityViolationExceptionHpsr() throws DataIntegrityViolationException, IOException, BicompException, FileManagerException, ParseException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22.P"));
		InputStream inputStream = new FileInputStream(new File("src/test/resources/rt1/RT02SCIPOSOIT22.P"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		PsrFileHeader hpsr = PsrFileHeader.builder()
				.dateTime("230627130230")
				.fileType("PSR")
				.lac("03")
				.receivingInstitution("POSOIT22")
				.numberOfRecord("000007")
				.senderFileReference("PSCI230627000214")
				.sendingInstitution("IPSTFRPP")
				.serviceIdentifier("SCI")
				.settlementDate("230627")
				.testCode("P")
				.build();
		
		this.mockFileManager.when(() -> FileManager.fileManager()).thenReturn(this.fileManager);
		Mockito.when(this.fileManager.getPsrString(Mockito.any(InputStream.class)))
			.thenReturn("HPSRSCIPSRIPSTFRPPPSCI230627000214230627130230PPOSOIT2223062703000007PSBHPOSOIT22XXXCR000000016533447,50CR000000012926545,92PDPHPOSOIT22CR000000016533447,50CR000000012926545,92000000000000000,00000000000000000,00000000004831171,23000000000000000,00000000000000000,00000000000000000,00000000000065399,16000000001289668,81000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00PDPBCLRRIPSTFRPP0627l9LmlqeCR72qslKtz9Aeuou000000004831171,23STDPDPTPSBTTPSR");
		Mockito.when(this.fileManager.parseHpsr(Mockito.anyString())).thenReturn(hpsr);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDateTime(Mockito.anyString())).thenReturn(new Date());
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		Mockito.when(this.rt1PsrFileHeaderRepository.save(Mockito.any(Rt1PsrFileHeader.class))).thenThrow(new DataIntegrityViolationException("DataIntegrityViolationException error"));
		
		assertThrows(DataIntegrityViolationException.class, () -> {
			this.service.processPreSettlementReport(list);
		});
		
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.fileManager, atLeastOnce()).getPsrString(Mockito.any(InputStream.class));
		verify(this.fileManager, atLeastOnce()).parseHpsr(Mockito.anyString());
		verify(this.dateUtils, atLeastOnce()).stringToDateTime(Mockito.anyString());
		verify(this.dateUtils, atLeastOnce()).stringToDate(Mockito.anyString());
		verify(this.rt1PsrFileHeaderRepository, times(1)).save(Mockito.any(Rt1PsrFileHeader.class));
	}
	
	@Test
	@Order(25)
	void testProcessPsrReportParsePsbhFileManagerException() throws DataIntegrityViolationException, IOException, BicompException, FileManagerException, ParseException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22.P"));
		InputStream inputStream = new FileInputStream(new File("src/test/resources/rt1/RT02SCIPOSOIT22.P"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		Rt1PsrFileHeader rt1PsrFileHeader = Rt1PsrFileHeader.builder().build();
		PsrFileHeader hpsr = PsrFileHeader.builder()
				.dateTime("230627130230")
				.fileType("PSR")
				.lac("03")
				.receivingInstitution("POSOIT22")
				.numberOfRecord("000007")
				.senderFileReference("PSCI230627000214")
				.sendingInstitution("IPSTFRPP")
				.serviceIdentifier("SCI")
				.settlementDate("230627")
				.testCode("P")
				.build();
		
		this.mockFileManager.when(() -> FileManager.fileManager()).thenReturn(this.fileManager);
		Mockito.when(this.fileManager.getPsrString(Mockito.any(InputStream.class)))
			.thenReturn("HPSRSCIPSRIPSTFRPPPSCI230627000214230627130230PPOSOIT2223062703000007PSBHPOSOIT22XXXCR000000016533447,50CR000000012926545,92PDPHPOSOIT22CR000000016533447,50CR000000012926545,92000000000000000,00000000000000000,00000000004831171,23000000000000000,00000000000000000,00000000000000000,00000000000065399,16000000001289668,81000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00PDPBCLRRIPSTFRPP0627l9LmlqeCR72qslKtz9Aeuou000000004831171,23STDPDPTPSBTTPSR");
		Mockito.when(this.fileManager.parseHpsr(Mockito.anyString())).thenReturn(hpsr);
		Mockito.when(this.fileManager.parsePsbh(Mockito.anyString(), Mockito.anyInt())).thenThrow(new FileManagerException("FileManagerException error"));
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDateTime(Mockito.anyString())).thenReturn(new Date());
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		Mockito.when(this.rt1PsrFileHeaderRepository.save(Mockito.any(Rt1PsrFileHeader.class))).thenReturn(rt1PsrFileHeader);
		
		assertThrows(FileManagerException.class, () -> {
			this.service.processPreSettlementReport(list);
		});
		
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.fileManager, times(1)).getPsrString(Mockito.any(InputStream.class));
		verify(this.fileManager, times(1)).parseHpsr(Mockito.anyString());
		verify(this.fileManager, times(1)).parsePsbh(Mockito.anyString(), Mockito.anyInt());
		verify(this.dateUtils, atLeastOnce()).stringToDateTime(Mockito.anyString());
		verify(this.dateUtils, atLeastOnce()).stringToDate(Mockito.anyString());
		verify(this.rt1PsrFileHeaderRepository, times(1)).save(Mockito.any(Rt1PsrFileHeader.class));
		verify(this.rt1PsrSettlementBicRepository, times(0)).save(Mockito.any(Rt1PsrSettlementBic.class));
	}
	
	@Test
	@Order(26)
	void testProcessPsrReportDataIntegrityViolationExceptionPsbh() throws DataIntegrityViolationException, IOException, BicompException, FileManagerException, ParseException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22.P"));
		InputStream inputStream = new FileInputStream(new File("src/test/resources/rt1/RT02SCIPOSOIT22.P"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		Rt1PsrFileHeader rt1PsrFileHeader = Rt1PsrFileHeader.builder().build();
		PsrFileHeader hpsr = PsrFileHeader.builder()
				.dateTime("230627130230")
				.fileType("PSR")
				.lac("03")
				.receivingInstitution("POSOIT22")
				.numberOfRecord("000007")
				.senderFileReference("PSCI230627000214")
				.sendingInstitution("IPSTFRPP")
				.serviceIdentifier("SCI")
				.settlementDate("230627")
				.testCode("P")
				.build();
		
		PsrSettlementBicHeader psbh = PsrSettlementBicHeader.builder()
				.recordType("PSBH")
				.settlementBic("POSOIT22XXX")
				.initialPositionIndicator("CR")
				.initialLiquidityPosition("000000000000010,00")
				.finalPositionIndicator("CR")
				.finalLiquidityPosition("000000000000020,00")
				.build();
		
		this.mockFileManager.when(() -> FileManager.fileManager()).thenReturn(this.fileManager);
		Mockito.when(this.fileManager.getPsrString(Mockito.any(InputStream.class)))
			.thenReturn("HPSRSCIPSRIPSTFRPPPSCI230627000214230627130230PPOSOIT2223062703000007PSBHPOSOIT22XXXCR000000016533447,50CR000000012926545,92PDPHPOSOIT22CR000000016533447,50CR000000012926545,92000000000000000,00000000000000000,00000000004831171,23000000000000000,00000000000000000,00000000000000000,00000000000065399,16000000001289668,81000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00000000000000000,00PDPBCLRRIPSTFRPP0627l9LmlqeCR72qslKtz9Aeuou000000004831171,23STDPDPTPSBTTPSR");
		Mockito.when(this.fileManager.parseHpsr(Mockito.anyString())).thenReturn(hpsr);
		Mockito.when(this.fileManager.parsePsbh(Mockito.anyString(), Mockito.anyInt())).thenReturn(psbh);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDateTime(Mockito.anyString())).thenReturn(new Date());
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		Mockito.when(this.rt1PsrFileHeaderRepository.save(Mockito.any(Rt1PsrFileHeader.class))).thenReturn(rt1PsrFileHeader);
		Mockito.when(this.rt1PsrSettlementBicRepository.save(Mockito.any(Rt1PsrSettlementBic.class))).thenThrow(new DataIntegrityViolationException("DataIntegrityViolationException error"));
		
		assertThrows(DataIntegrityViolationException.class, () -> {
			this.service.processPreSettlementReport(list);
		});
		
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.fileManager, times(1)).getPsrString(Mockito.any(InputStream.class));
		verify(this.fileManager, times(1)).parseHpsr(Mockito.anyString());
		verify(this.fileManager, times(1)).parsePsbh(Mockito.anyString(), Mockito.anyInt());
		verify(this.dateUtils, atLeastOnce()).stringToDateTime(Mockito.anyString());
		verify(this.dateUtils, atLeastOnce()).stringToDate(Mockito.anyString());
		verify(this.rt1PsrFileHeaderRepository, times(1)).save(Mockito.any(Rt1PsrFileHeader.class));
		verify(this.rt1PsrSettlementBicRepository, times(1)).save(Mockito.any(Rt1PsrSettlementBic.class));
	}
	
	@Test
	@Order(27)
	void testGetRt1LastBalanceByDate() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		Rt1PsrFileHeader header = Rt1PsrFileHeader.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.serviceIdentifier("SCI")
				.fileType("PSR")
				.sendingInstitution("IPSTFRPP")
				.senderFileReference("PSCI231011000222")
				.testCode("P")
				.receivingInstitution("POSOIT22")
				.settlementDate(new Date())
				.lac("01")
				.recordNumber(new BigDecimal(1))
				.reportName("RT02SCIPOSOIT22010123000001000.P")
				.build();
		
		Rt1PsrSettlementBic settlementBic = Rt1PsrSettlementBic.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.settlementBic("POSOIT22XXX")
				.initialPositionIndicator("CR")
				.initialLiquidityPosition(new BigDecimal(100))
				.finalPositionIndicator("CR")
				.finalLiquidityPosition(new BigDecimal(200))
				.rt1PsrFileHeader(header)
				.build();
		
		Mockito.when(this.rt1PsrSettlementBicRepository.findLastLacByDate(Mockito.any(Date.class), Mockito.any(PageRequest.class)))
			.thenReturn(Optional.of(settlementBic));
		
		List<Rt1LacDto> listDtos = this.service.getRt1LastBalanceByDate("230101");
		
		verify(this.dateUtils, atLeastOnce()).stringToDate(Mockito.anyString());
		verify(this.rt1PsrSettlementBicRepository, times(1)).findLastLacByDate(Mockito.any(Date.class), Mockito.any(PageRequest.class));
		
		assertThat(listDtos).isNotEmpty();
		assertThat(listDtos.get(0).getLacNumber()).isEqualTo(header.getLac());
	}
	
	@Test
	@Order(28)
	void testGetRt1LastBalanceByDateException() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		Mockito.when(this.rt1PsrSettlementBicRepository.findLastLacByDate(Mockito.any(Date.class), Mockito.any(PageRequest.class)))
			.thenReturn(Optional.empty());
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getRt1LastBalanceByDate("230101");
		});
		
		verify(this.dateUtils, atLeastOnce()).stringToDate(Mockito.anyString());
		verify(this.rt1PsrSettlementBicRepository, times(1)).findLastLacByDate(Mockito.any(Date.class), Mockito.any(PageRequest.class));
	}
	
	@Test
	@Order(39)
	void testGetRt1LastBalanceByDateParseException() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenThrow(new ParseException("ParseException error", 0));
		
		assertThrows(ParseException.class, () -> {
			this.service.getRt1LastBalanceByDate("230101");
		});
		
		verify(this.dateUtils, atLeastOnce()).stringToDate(Mockito.anyString());
		verify(this.rt1PsrSettlementBicRepository, times(0)).findLastLacByDate(Mockito.any(Date.class), Mockito.any(PageRequest.class));
	}
	
	@Test
	@Order(30)
	void testGetRt1DailyLac() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		Rt1PsrFileHeader header1 = Rt1PsrFileHeader.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.serviceIdentifier("SCI")
				.fileType("PSR")
				.sendingInstitution("IPSTFRPP")
				.senderFileReference("PSCI231011000222")
				.testCode("P")
				.receivingInstitution("POSOIT22")
				.settlementDate(new Date())
				.lac("01")
				.recordNumber(new BigDecimal(1))
				.reportName("RT02SCIPOSOIT22010123000001000.P")
				.build();
		Rt1PsrFileHeader header2 = Rt1PsrFileHeader.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.serviceIdentifier("SCI")
				.fileType("PSR")
				.sendingInstitution("IPSTFRPP")
				.senderFileReference("PSCI231011000222")
				.testCode("P")
				.receivingInstitution("POSOIT22")
				.settlementDate(new Date())
				.lac("02")
				.recordNumber(new BigDecimal(2))
				.reportName("RT02SCIPOSOIT22010123000002000.P")
				.build();
		
		Rt1PsrSettlementBic settlementBic1 = Rt1PsrSettlementBic.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.settlementBic("POSOIT22XXX")
				.initialPositionIndicator("CR")
				.initialLiquidityPosition(new BigDecimal(100))
				.finalPositionIndicator("CR")
				.finalLiquidityPosition(new BigDecimal(200))
				.rt1PsrFileHeader(header1)
				.build();
		Rt1PsrSettlementBic settlementBic2 = Rt1PsrSettlementBic.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.settlementBic("POSOIT22XXX")
				.initialPositionIndicator("CR")
				.initialLiquidityPosition(new BigDecimal(100))
				.finalPositionIndicator("CR")
				.finalLiquidityPosition(new BigDecimal(200))
				.rt1PsrFileHeader(header2)
				.build();
		
		List<Rt1PsrSettlementBic> list = Arrays.asList(settlementBic1, settlementBic2);
		Mockito.when(this.rt1PsrSettlementBicRepository.findLacsByDate(Mockito.any(Date.class))).thenReturn(list);
		
		List<Rt1LacDto> listDtos = this.service.getRt1DailyLac("230101");
		
		verify(this.dateUtils, atLeastOnce()).stringToDate(Mockito.anyString());
		verify(this.rt1PsrSettlementBicRepository, times(1)).findLacsByDate(Mockito.any(Date.class));
		
		assertThat(listDtos).isNotEmpty().hasSameSizeAs(list);
		assertThat(listDtos.get(0).getLacNumber()).isEqualTo(header1.getLac());
		assertThat(listDtos.get(1).getLacNumber()).isEqualTo(header2.getLac());
	}
	
	@Test
	@Order(31)
	void testGetRt1DailyLacException() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		List<Rt1PsrSettlementBic> list = Arrays.asList();
		Mockito.when(this.rt1PsrSettlementBicRepository.findLacsByDate(Mockito.any(Date.class))).thenReturn(list);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getRt1DailyLac("230101");
		});
		
		verify(this.dateUtils, atLeastOnce()).stringToDate(Mockito.anyString());
		verify(this.rt1PsrSettlementBicRepository, times(1)).findLacsByDate(Mockito.any(Date.class));
	}
	
	@Test
	@Order(32)
	void testGetRt1DailyLacParseException() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenThrow(new ParseException("ParseException error", 0));
		
		assertThrows(ParseException.class, () -> {
			this.service.getRt1DailyLac("230101");
		});
		
		verify(this.dateUtils, atLeastOnce()).stringToDate(Mockito.anyString());
		verify(this.rt1PsrSettlementBicRepository, times(0)).findLacsByDate(Mockito.any(Date.class));
	}
	
	@Test
	@Order(33)
	void testGetRt1LacDetail() throws ResourceNotFoundException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.dateToLocalDate(Mockito.any(Date.class))).thenReturn(LocalDate.now());
		
		Rt1PsrFileHeader header = Rt1PsrFileHeader.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.serviceIdentifier("SCI")
				.fileType("PSR")
				.sendingInstitution("IPSTFRPP")
				.senderFileReference("PSCI231011000222")
				.testCode("P")
				.receivingInstitution("POSOIT22")
				.settlementDate(new Date())
				.lac("01")
				.recordNumber(new BigDecimal(1))
				.reportName("RT02SCIPOSOIT22010123000001000.P")
				.build();
		
		Rt1PsrSettlementBic settlementBic = Rt1PsrSettlementBic.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.settlementBic("POSOIT22XXX")
				.initialPositionIndicator("CR")
				.initialLiquidityPosition(new BigDecimal(100))
				.finalPositionIndicator("CR")
				.finalLiquidityPosition(new BigDecimal(200))
				.rt1PsrFileHeader(header)
				.build();
		
		Rt1PsrParticipantBody body1 = Rt1PsrParticipantBody.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.operationType("CLRR")
				.paymentReference("IPSTFRPP1011n4lCh3tuhyW3aXbBfs1yXu4")
				.paymentAmount(new BigDecimal(3000))
				.paymentStatus("STD")
				.build();
		Rt1PsrParticipantBody body2 = Rt1PsrParticipantBody.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.operationType("CPFR")
				.paymentReference("IPSTFRPP1011n4lCh3tuhyW3aXbBfs1yXu5")
				.paymentAmount(new BigDecimal(500))
				.paymentStatus("STD")
				.build();
		List<Rt1PsrParticipantBody> list = Arrays.asList(body1, body2);
		
		Object[] debitRejected = new Object[] { 'D', "RJCT", new BigDecimal(100) };
		Object[] creditRejected = new Object[] { 'C', "RJCT", new BigDecimal(150) };
		Object[] debit = new Object[] { 'D', "ACSC", new BigDecimal(1000) };
		Object[] credit = new Object[] { 'C', "ACSC", new BigDecimal(2000) };
		List<Object[]> total = Arrays.asList(debitRejected, creditRejected, debit, credit);
		
		Mockito.when(this.rt1PsrSettlementBicRepository.findById(Mockito.any(BigDecimal.class)))
				.thenReturn(Optional.of(settlementBic));
		Mockito.when(this.rt1PsrParticipantBodyRepository.findDailyLiquidityTransfer(Mockito.any(BigDecimal.class)))
				.thenReturn(list);
		Mockito.when(this.rt1BulkPaymentRepository.findTotalBySettlementDateAndLac(Mockito.any(Date.class), Mockito.anyString()))
				.thenReturn(total);
		
		Rt1TotalDto result = this.service.getRt1LacDetail(BigDecimal.ONE);
		
		verify(this.rt1PsrSettlementBicRepository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.rt1PsrParticipantBodyRepository, times(1)).findDailyLiquidityTransfer(Mockito.any(BigDecimal.class));
		verify(this.rt1BulkPaymentRepository, times(1)).findTotalBySettlementDateAndLac(Mockito.any(Date.class), Mockito.anyString());
		
		assertThat(result.getCreditLtAmount()).isEqualTo(body2.getPaymentAmount());
		assertThat(result.getDebitLtAmount()).isEqualTo(body1.getPaymentAmount());
		assertThat(result.getCreditPmntAmount()).isEqualTo(credit[2]);
		assertThat(result.getDebitPmntAmount()).isEqualTo(debit[2]);
		assertThat(result.getRejectedCreditPmntAmount()).isEqualTo(creditRejected[2]);
		assertThat(result.getRejectedDebitPmntAmount()).isEqualTo(debitRejected[2]);
	}
	
	@Test
	@Order(34)
	void testGetRt1LacDetailException() throws ResourceNotFoundException {
		
		Mockito.when(this.rt1PsrSettlementBicRepository.findById(Mockito.any(BigDecimal.class)))
				.thenReturn(Optional.empty());
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getRt1LacDetail(BigDecimal.ONE);
		});
		
		verify(this.rt1PsrSettlementBicRepository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.rt1PsrParticipantBodyRepository, times(0)).findDailyLiquidityTransfer(Mockito.any(BigDecimal.class));
		verify(this.rt1BulkPaymentRepository, times(0)).findTotalBySettlementDateAndLac(Mockito.any(Date.class), Mockito.anyString());
	}
	
	@Test
	@Order(35)
	void testGetRt1LacDetailNoAmount() throws ResourceNotFoundException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.dateToLocalDate(Mockito.any(Date.class))).thenReturn(LocalDate.now());
		
		Rt1PsrFileHeader header = Rt1PsrFileHeader.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.serviceIdentifier("SCI")
				.fileType("PSR")
				.sendingInstitution("IPSTFRPP")
				.senderFileReference("PSCI231011000222")
				.testCode("P")
				.receivingInstitution("POSOIT22")
				.settlementDate(new Date())
				.lac("01")
				.recordNumber(new BigDecimal(1))
				.reportName("RT02SCIPOSOIT22010123000001000.P")
				.build();
		
		Rt1PsrSettlementBic settlementBic = Rt1PsrSettlementBic.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.settlementBic("POSOIT22XXX")
				.initialPositionIndicator("CR")
				.initialLiquidityPosition(new BigDecimal(100))
				.finalPositionIndicator("CR")
				.finalLiquidityPosition(new BigDecimal(200))
				.rt1PsrFileHeader(header)
				.build();
		
		List<Rt1PsrParticipantBody> list = Arrays.asList();
		List<Object[]> total = Arrays.asList();
		
		Mockito.when(this.rt1PsrSettlementBicRepository.findById(Mockito.any(BigDecimal.class)))
				.thenReturn(Optional.of(settlementBic));
		Mockito.when(this.rt1PsrParticipantBodyRepository.findDailyLiquidityTransfer(Mockito.any(BigDecimal.class)))
				.thenReturn(list);
		Mockito.when(this.rt1BulkPaymentRepository.findTotalBySettlementDateAndLac(Mockito.any(Date.class), Mockito.anyString()))
				.thenReturn(total);
		
		Rt1TotalDto result = this.service.getRt1LacDetail(BigDecimal.ONE);
		
		verify(this.rt1PsrSettlementBicRepository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.rt1PsrParticipantBodyRepository, times(1)).findDailyLiquidityTransfer(Mockito.any(BigDecimal.class));
		verify(this.rt1BulkPaymentRepository, times(1)).findTotalBySettlementDateAndLac(Mockito.any(Date.class), Mockito.anyString());
		
		assertThat(result.getCreditLtAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getDebitLtAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getCreditPmntAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getDebitPmntAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getRejectedCreditPmntAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getRejectedDebitPmntAmount()).isEqualTo(BigDecimal.ZERO);
	}
	
	@Test
	@Order(36)
	void testProcessBulkReportEmptyReport() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22_empty.S"));
		InputStream inputStream = new FileInputStream(new File("src/test/resources/rt1/RT02SCIPOSOIT22_empty.S"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.service.processBulkReport(list);
		
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.rt1BulkRepository, times(0)).save(Mockito.any(Rt1Bulk.class));
		verify(this.rt1BulkPaymentRepository, times(0)).save(Mockito.any(Rt1BulkPayment.class));
	}
	
	@Test
	@Order(37)
	void testProcessPsrReportEmptyReport() throws DataIntegrityViolationException, IOException, BicompException, FileManagerException, ParseException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22_empty.P"));
		InputStream inputStream = new FileInputStream(new File("src/test/resources/rt1/RT02SCIPOSOIT22_empty.P"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.service.processPreSettlementReport(list);
		
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.rt1PsrFileHeaderRepository, times(0)).save(Mockito.any(Rt1PsrFileHeader.class));
		verify(this.rt1PsrSettlementBicRepository, times(0)).save(Mockito.any(Rt1PsrSettlementBic.class));
		verify(this.rt1PsrParticipantHeaderRepository, times(0)).save(Mockito.any(Rt1PsrParticipantHeader.class));
		verify(this.rt1PsrParticipantBodyRepository, times(0)).save(Mockito.any(Rt1PsrParticipantBody.class));
	}
	
	/*@SuppressWarnings("unchecked")
	@Test
	@Order(39)
	void testGetDebitCreditPaymentAllFilter() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToTimestamp(Mockito.anyString())).thenReturn(new Timestamp(System.currentTimeMillis()));
		
		Page<Rt1BulkPayment> p = new PageImpl<>(Arrays.asList(Rt1BulkPayment.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.msgId("SSCI240821000348L000000000000000002")
				.creationDateTime(new Timestamp(System.currentTimeMillis()))
				.originalMsgName("pacs.008")
				.originalTransactionNumber(BigDecimal.ONE)
				.originalAmount(new BigDecimal(100))
				.status("ACSC")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.debitor("CCRTIT2T76A")
				.creditor("POSOIT22XXX")
				.build()
			));
		Mockito.when(this.rt1BulkPaymentRepository.findAll(Mockito.any(GenericSpecification.class), 
				Mockito.any(Pageable.class))).thenReturn(p);
		
		PageableRt1EntryDto result = this.service.getPayments('C', "BOOK", new BigDecimal(0), 
				new BigDecimal(300), "2023-06-07T10:00", "2023-06-07T12:00", "01", PageRequest.of(0, 10));
		
		assertThat(result).isNotNull();
		assertThat(result.getEntries()).isNotEmpty();
		assertThat(result.getTotalElements()).isEqualTo(new BigDecimal(1));
		assertThat(result.getEntries().get(0).getPaymentAmount()).isEqualTo(p.getContent().get(0).getPaymentAmount());
		assertThat(result.getEntries().get(0).getRejectReasonCode()).isNull();
		
		verify(this.rt1BulkPaymentRepository, times(1)).findAll(Mockito.any(GenericSpecification.class), 
				Mockito.any(Pageable.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(40)
	void testGetPaymentsAmountToNull() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToTimestamp(Mockito.anyString())).thenReturn(new Timestamp(System.currentTimeMillis()));
		
		Page<Rt1BulkPayment> p = new PageImpl<>(Arrays.asList(Rt1BulkPayment.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.msgId("SSCI240821000348L000000000000000002")
				.creationDateTime(new Timestamp(System.currentTimeMillis()))
				.originalMsgName("pacs.008")
				.originalTransactionNumber(BigDecimal.ONE)
				.originalAmount(new BigDecimal(100))
				.status("ACSC")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.debitor("CCRTIT2T76A")
				.creditor("POSOIT22XXX")
				.build()
			));
		Mockito.when(this.rt1BulkPaymentRepository.findAll(Mockito.any(GenericSpecification.class), 
				Mockito.any(Pageable.class))).thenReturn(p);
		
		PageableRt1EntryDto result = this.service.getPayments('C', "BOOK", new BigDecimal(0), null, 
				"2023-06-07T10:00", "2023-06-07T12:00", "01", PageRequest.of(0, 10));
		
		assertThat(result).isNotNull();
		assertThat(result.getEntries()).isNotEmpty();
		assertThat(result.getTotalElements()).isEqualTo(new BigDecimal(1));
		assertThat(result.getEntries().get(0).getPaymentAmount()).isEqualTo(p.getContent().get(0).getPaymentAmount());
		assertThat(result.getEntries().get(0).getRejectReasonCode()).isNull();
		
		verify(this.rt1BulkPaymentRepository, times(1)).findAll(Mockito.any(GenericSpecification.class), 
				Mockito.any(Pageable.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(41)
	void testGetPaymentsAmountFromNull() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToTimestamp(Mockito.anyString())).thenReturn(new Timestamp(System.currentTimeMillis()));
		
		Page<Rt1BulkPayment> p = new PageImpl<>(Arrays.asList(Rt1BulkPayment.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.msgId("SSCI240821000348L000000000000000002")
				.creationDateTime(new Timestamp(System.currentTimeMillis()))
				.originalMsgName("pacs.008")
				.originalTransactionNumber(BigDecimal.ONE)
				.originalAmount(new BigDecimal(100))
				.status("ACSC")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.debitor("CCRTIT2T76A")
				.creditor("POSOIT22XXX")
				.build()
			));
		Mockito.when(this.rt1BulkPaymentRepository.findAll(Mockito.any(GenericSpecification.class), 
				Mockito.any(Pageable.class))).thenReturn(p);
		
		PageableRt1EntryDto result = this.service.getPayments('C', "BOOK", null, new BigDecimal(0), 
				"2023-06-07T10:00", "2023-06-07T12:00", "01", PageRequest.of(0, 10));
		
		assertThat(result).isNotNull();
		assertThat(result.getEntries()).isNotEmpty();
		assertThat(result.getTotalElements()).isEqualTo(new BigDecimal(1));
		assertThat(result.getEntries().get(0).getPaymentAmount()).isEqualTo(p.getContent().get(0).getPaymentAmount());
		assertThat(result.getEntries().get(0).getRejectReasonCode()).isNull();
		
		verify(this.rt1BulkPaymentRepository, times(1)).findAll(Mockito.any(GenericSpecification.class), 
				Mockito.any(Pageable.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(42)
	void testGetPaymentsAmountFromAndAmountToNull() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToTimestamp(Mockito.anyString())).thenReturn(new Timestamp(System.currentTimeMillis()));
		
		Page<Rt1BulkPayment> p = new PageImpl<>(Arrays.asList(Rt1BulkPayment.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.msgId("SSCI240821000348L000000000000000002")
				.creationDateTime(new Timestamp(System.currentTimeMillis()))
				.originalMsgName("pacs.008")
				.originalTransactionNumber(BigDecimal.ONE)
				.originalAmount(new BigDecimal(100))
				.status("ACSC")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.debitor("CCRTIT2T76A")
				.creditor("POSOIT22XXX")
				.build()
			));
		Mockito.when(this.rt1BulkPaymentRepository.findAll(Mockito.any(GenericSpecification.class), 
				Mockito.any(Pageable.class))).thenReturn(p);
		
		PageableRt1EntryDto result = this.service.getPayments('C', "BOOK", null, null, 
				"2023-06-07T10:00", "2023-06-07T12:00", "01", PageRequest.of(0, 10));
		
		assertThat(result).isNotNull();
		assertThat(result.getEntries()).isNotEmpty();
		assertThat(result.getTotalElements()).isEqualTo(new BigDecimal(1));
		assertThat(result.getEntries().get(0).getPaymentAmount()).isEqualTo(p.getContent().get(0).getPaymentAmount());
		assertThat(result.getEntries().get(0).getRejectReasonCode()).isNull();
		
		verify(this.rt1BulkPaymentRepository, times(1)).findAll(Mockito.any(GenericSpecification.class), 
				Mockito.any(Pageable.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(43)
	void testGetPaymentsSettlementDateTimeFromNull() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToTimestamp(Mockito.anyString())).thenReturn(new Timestamp(System.currentTimeMillis()));
		
		Page<Rt1BulkPayment> p = new PageImpl<>(Arrays.asList(Rt1BulkPayment.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.msgId("SSCI240821000348L000000000000000002")
				.creationDateTime(new Timestamp(System.currentTimeMillis()))
				.originalMsgName("pacs.008")
				.originalTransactionNumber(BigDecimal.ONE)
				.originalAmount(new BigDecimal(100))
				.status("ACSC")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.debitor("CCRTIT2T76A")
				.creditor("POSOIT22XXX")
				.build()
			));
		Mockito.when(this.rt1BulkPaymentRepository.findAll(Mockito.any(GenericSpecification.class), 
				Mockito.any(Pageable.class))).thenReturn(p);
		
		PageableRt1EntryDto result = this.service.getPayments('C', "BOOK", new BigDecimal(0), new BigDecimal(0), 
				null, "2023-06-07T12:00", "01", PageRequest.of(0, 10));
		
		assertThat(result).isNotNull();
		assertThat(result.getEntries()).isNotEmpty();
		assertThat(result.getTotalElements()).isEqualTo(new BigDecimal(1));
		assertThat(result.getEntries().get(0).getPaymentAmount()).isEqualTo(p.getContent().get(0).getPaymentAmount());
		assertThat(result.getEntries().get(0).getRejectReasonCode()).isNull();
		
		verify(this.rt1BulkPaymentRepository, times(1)).findAll(Mockito.any(GenericSpecification.class), 
				Mockito.any(Pageable.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(44)
	void testGetPaymentsSettlementDateTimeToNull() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToTimestamp(Mockito.anyString())).thenReturn(new Timestamp(System.currentTimeMillis()));
		
		Page<Rt1BulkPayment> p = new PageImpl<>(Arrays.asList(Rt1BulkPayment.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.msgId("SSCI240821000348L000000000000000002")
				.creationDateTime(new Timestamp(System.currentTimeMillis()))
				.originalMsgName("pacs.008")
				.originalTransactionNumber(BigDecimal.ONE)
				.originalAmount(new BigDecimal(100))
				.status("ACSC")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.debitor("CCRTIT2T76A")
				.creditor("POSOIT22XXX")
				.build()
			));
		Mockito.when(this.rt1BulkPaymentRepository.findAll(Mockito.any(GenericSpecification.class), 
				Mockito.any(Pageable.class))).thenReturn(p);
		
		PageableRt1EntryDto result = this.service.getPayments('C', "BOOK", new BigDecimal(0), new BigDecimal(0), 
				"2023-06-07T10:00", null, "01", PageRequest.of(0, 10));
		
		assertThat(result).isNotNull();
		assertThat(result.getEntries()).isNotEmpty();
		assertThat(result.getTotalElements()).isEqualTo(new BigDecimal(1));
		assertThat(result.getEntries().get(0).getPaymentAmount()).isEqualTo(p.getContent().get(0).getPaymentAmount());
		assertThat(result.getEntries().get(0).getRejectReasonCode()).isNull();
		
		verify(this.rt1BulkPaymentRepository, times(1)).findAll(Mockito.any(GenericSpecification.class), 
				Mockito.any(Pageable.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(45)
	void testGetPaymentsSettlementDateTimeFromAndSettlementDateTimeToNull() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToTimestamp(Mockito.anyString())).thenReturn(new Timestamp(System.currentTimeMillis()));
		
		Page<Rt1BulkPayment> p = new PageImpl<>(Arrays.asList(Rt1BulkPayment.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.msgId("SSCI240821000348L000000000000000002")
				.creationDateTime(new Timestamp(System.currentTimeMillis()))
				.originalMsgName("pacs.008")
				.originalTransactionNumber(BigDecimal.ONE)
				.originalAmount(new BigDecimal(100))
				.status("ACSC")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.debitor("CCRTIT2T76A")
				.creditor("POSOIT22XXX")
				.build()
			));
		Mockito.when(this.rt1BulkPaymentRepository.findAll(Mockito.any(GenericSpecification.class), 
				Mockito.any(Pageable.class))).thenReturn(p);
		
		PageableRt1EntryDto result = this.service.getPayments('C', "BOOK", new BigDecimal(0), new BigDecimal(0), 
				null, null, "01", PageRequest.of(0, 10));
		
		assertThat(result).isNotNull();
		assertThat(result.getEntries()).isNotEmpty();
		assertThat(result.getTotalElements()).isEqualTo(new BigDecimal(1));
		assertThat(result.getEntries().get(0).getPaymentAmount()).isEqualTo(p.getContent().get(0).getPaymentAmount());
		assertThat(result.getEntries().get(0).getRejectReasonCode()).isNull();
		
		verify(this.rt1BulkPaymentRepository, times(1)).findAll(Mockito.any(GenericSpecification.class), 
				Mockito.any(Pageable.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(49)
	void testGetPaymentsParseException() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToTimestamp(Mockito.anyString())).thenThrow(new ParseException("ParseException error", 0));
		
		Page<Rt1BulkPayment> p = new PageImpl<>(Arrays.asList(Rt1BulkPayment.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.msgId("SSCI240821000348L000000000000000002")
				.creationDateTime(new Timestamp(System.currentTimeMillis()))
				.originalMsgName("pacs.008")
				.originalTransactionNumber(BigDecimal.ONE)
				.originalAmount(new BigDecimal(100))
				.status("ACSC")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.debitor("CCRTIT2T76A")
				.creditor("POSOIT22XXX")
				.build()
			));
		Mockito.when(this.rt1BulkPaymentRepository.findAll(Mockito.any(GenericSpecification.class), 
				Mockito.any(Pageable.class))).thenReturn(p);
		
		assertThrows(ParseException.class, () -> {
			this.service.getPayments('C', "BOOK", new BigDecimal(0), 
					new BigDecimal(300), "2023-06-07T10:00", "2023-06-07T12:00", "01", PageRequest.of(0, 10));
		});
		
		verify(this.rt1BulkPaymentRepository, times(0)).findAll(Mockito.any(GenericSpecification.class), 
				Mockito.any(Pageable.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(50)
	void testGetPaymentsWithRejectionReasonCodeSetExists() throws ResourceNotFoundException, ParseException {
		
		Page<Rt1BulkPayment> p = new PageImpl<>(Arrays.asList(Rt1BulkPayment.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.msgId("SSCI240821000348L000000000000000002")
				.creationDateTime(new Timestamp(System.currentTimeMillis()))
				.originalMsgName("pacs.008")
				.originalTransactionNumber(BigDecimal.ONE)
				.originalAmount(new BigDecimal(100))
				.status("RJCT")
				.rejectReason("B09")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.debitor("CCRTIT2T76A")
				.creditor("POSOIT22XXX")
				.build()
			));
		
		List<IsoExternalCodeSet> iso = Arrays.asList(IsoExternalCodeSet.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.codeValue("AB09")
				.codeName("ErrorCreditorAgent")
				.codeDescription("Transaction stopped due to error at the Creditor Agent.")
				.build());
		
		Mockito.when(this.rt1BulkPaymentRepository.findAll(Mockito.any(GenericSpecification.class), 
				Mockito.any(Pageable.class))).thenReturn(p);
		Mockito.when(this.isoExternalCodeSetRepository.findByCodeValue(Mockito.anyString())).thenReturn(iso);
		
		PageableRt1EntryDto result = this.service.getPayments('C', "BOOK", null, null, null, null, "01", PageRequest.of(0, 10));
		
		assertThat(result).isNotNull();
		assertThat(result.getEntries()).isNotEmpty();
		assertThat(result.getTotalElements()).isEqualTo(new BigDecimal(1));
		assertThat(result.getEntries().get(0).getPaymentAmount()).isEqualTo(p.getContent().get(0).getPaymentAmount());
		assertThat(iso.get(0).getCodeValue()).contains(result.getEntries().get(0).getRejectReasonCode());
		assertThat(result.getEntries().get(0).getRejectReasonDescription()).isNotNull();
		
		verify(this.rt1BulkPaymentRepository, times(1)).findAll(Mockito.any(GenericSpecification.class), 
				Mockito.any(Pageable.class));
		verify(this.isoExternalCodeSetRepository, times(1)).findByCodeValue(Mockito.anyString());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(51)
	void testGetPaymentsWithRejectionReasonCodeSetNotExists() throws ResourceNotFoundException, ParseException {
		
		Page<Rt1BulkPayment> p = new PageImpl<>(Arrays.asList(Rt1BulkPayment.builder()
				.id(BigDecimal.ONE)
				.tmsInsert(new Date())
				.msgId("SSCI240821000348L000000000000000002")
				.creationDateTime(new Timestamp(System.currentTimeMillis()))
				.originalMsgName("pacs.008")
				.originalTransactionNumber(BigDecimal.ONE)
				.originalAmount(new BigDecimal(100))
				.status("RJCT")
				.rejectReason("B09")
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.side('C')
				.debitor("CCRTIT2T76A")
				.creditor("POSOIT22XXX")
				.build()
			));
		
		List<IsoExternalCodeSet> iso = Arrays.asList();
		
		Mockito.when(this.rt1BulkPaymentRepository.findAll(Mockito.any(GenericSpecification.class), 
				Mockito.any(Pageable.class))).thenReturn(p);
		Mockito.when(this.isoExternalCodeSetRepository.findByCodeValue(Mockito.anyString())).thenReturn(iso);
		
		PageableRt1EntryDto result = this.service.getPayments('C', "BOOK", null, null, null, null, "01", PageRequest.of(0, 10));
		
		assertThat(result).isNotNull();
		assertThat(result.getEntries()).isNotEmpty();
		assertThat(result.getTotalElements()).isEqualTo(new BigDecimal(1));
		assertThat(result.getEntries().get(0).getPaymentAmount()).isEqualTo(p.getContent().get(0).getPaymentAmount());
		assertThat(result.getEntries().get(0).getRejectReasonCode()).isNotNull();
		assertThat(result.getEntries().get(0).getRejectReasonDescription()).isNull();
		
		verify(this.rt1BulkPaymentRepository, times(1)).findAll(Mockito.any(GenericSpecification.class), 
				Mockito.any(Pageable.class));
		verify(this.isoExternalCodeSetRepository, times(1)).findByCodeValue(Mockito.anyString());
	}*/
	
	@Test
	@Order(52)
	void testRt1ServiceImplEquals() {
		
		Rt1ServiceImpl test = new Rt1ServiceImpl(this.config, this.minioService, this.rt1BulkRepository, this.rt1BulkPaymentRepository, 
				this.rt1PsrFileHeaderRepository, this.rt1PsrSettlementBicRepository, this.rt1PsrParticipantHeaderRepository, 
				this.rt1PsrParticipantBodyRepository, this.isoExternalCodeSetRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isTrue();
	}
	
	@Test
	@Order(53)
	void testRt1ServiceImplEqualsSameInstance() {
		
		assertThat(this.service.equals(this.service)).isTrue();
	}
	
	@Test
	@Order(54)
	void testRt1ServiceImplNotEqualsNotSameInstanceType() {
		
		assertThat(this.service.equals("test")).isFalse();
	}
	
	@Test
	@Order(55)
	void testRt1ServiceImplEqualsNull() {
		
		this.service = new Rt1ServiceImpl(null, null, null, null, null, null, null, null, null);
		Rt1ServiceImpl test = new Rt1ServiceImpl(null, null, null, null, null, null, null, null, null);
		boolean result = this.service.equals(test);
		assertThat(result).isTrue();
	}
	
	@Test
	@Order(56)
	void testRt1ServiceImplNotNull() {
		
		this.service = new Rt1ServiceImpl(null, null, null, null, null, null, null, null, null);
		Rt1ServiceImpl test = new Rt1ServiceImpl(this.config, this.minioService, this.rt1BulkRepository, this.rt1BulkPaymentRepository, 
				this.rt1PsrFileHeaderRepository, this.rt1PsrSettlementBicRepository, this.rt1PsrParticipantHeaderRepository, 
				this.rt1PsrParticipantBodyRepository, this.isoExternalCodeSetRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(57)
	void testRt1ServiceImplNotEqualsConfigNull() {
		
		Rt1ServiceImpl test = new Rt1ServiceImpl(null, this.minioService, this.rt1BulkRepository, this.rt1BulkPaymentRepository, 
				this.rt1PsrFileHeaderRepository, this.rt1PsrSettlementBicRepository, this.rt1PsrParticipantHeaderRepository, 
				this.rt1PsrParticipantBodyRepository, this.isoExternalCodeSetRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(58)
	void testRt1ServiceImplNotEqualsRt1BulkNull() {
		
		Rt1ServiceImpl test = new Rt1ServiceImpl(this.config, this.minioService, null, this.rt1BulkPaymentRepository, 
				this.rt1PsrFileHeaderRepository, this.rt1PsrSettlementBicRepository, this.rt1PsrParticipantHeaderRepository, 
				this.rt1PsrParticipantBodyRepository, this.isoExternalCodeSetRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(59)
	void testRt1ServiceImplNotEqualsRt1BulkPaymentNull() {
		
		Rt1ServiceImpl test = new Rt1ServiceImpl(this.config, this.minioService, this.rt1BulkRepository, null, 
				this.rt1PsrFileHeaderRepository, this.rt1PsrSettlementBicRepository, this.rt1PsrParticipantHeaderRepository, 
				this.rt1PsrParticipantBodyRepository, this.isoExternalCodeSetRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(60)
	void testRt1ServiceImplNotEqualsRt1PsrFileNull() {
		
		Rt1ServiceImpl test = new Rt1ServiceImpl(this.config, this.minioService, this.rt1BulkRepository, this.rt1BulkPaymentRepository, 
				null, this.rt1PsrSettlementBicRepository, this.rt1PsrParticipantHeaderRepository, 
				this.rt1PsrParticipantBodyRepository, this.isoExternalCodeSetRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(61)
	void testRt1ServiceImplNotEqualsRt1PsrSettlementBicNull() {
		
		Rt1ServiceImpl test = new Rt1ServiceImpl(this.config, this.minioService, this.rt1BulkRepository, this.rt1BulkPaymentRepository, 
				this.rt1PsrFileHeaderRepository, null, this.rt1PsrParticipantHeaderRepository, 
				this.rt1PsrParticipantBodyRepository, this.isoExternalCodeSetRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(62)
	void testRt1ServiceImplNotEqualsRt1PsrPartNull() {
		
		Rt1ServiceImpl test = new Rt1ServiceImpl(this.config, this.minioService, this.rt1BulkRepository, this.rt1BulkPaymentRepository, 
				this.rt1PsrFileHeaderRepository, this.rt1PsrSettlementBicRepository, null, 
				this.rt1PsrParticipantBodyRepository, this.isoExternalCodeSetRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(63)
	void testRt1ServiceImplNotEqualsRt1PsrPartBodyNull() {
		
		Rt1ServiceImpl test = new Rt1ServiceImpl(this.config, this.minioService, this.rt1BulkRepository, this.rt1BulkPaymentRepository, 
				this.rt1PsrFileHeaderRepository, this.rt1PsrSettlementBicRepository, this.rt1PsrParticipantHeaderRepository, 
				null, this.isoExternalCodeSetRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(64)
	void testRt1ServiceImplNotEqualsIsoExternalCodeSetNull() {
		
		Rt1ServiceImpl test = new Rt1ServiceImpl(this.config, this.minioService, this.rt1BulkRepository, this.rt1BulkPaymentRepository, 
				this.rt1PsrFileHeaderRepository, this.rt1PsrSettlementBicRepository, this.rt1PsrParticipantHeaderRepository, 
				this.rt1PsrParticipantBodyRepository, null);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(65)
	void testRt1ServiceImplNotEqualsMinioNull() {
		
		Rt1ServiceImpl test = new Rt1ServiceImpl(this.config, null, this.rt1BulkRepository, this.rt1BulkPaymentRepository, 
				this.rt1PsrFileHeaderRepository, this.rt1PsrSettlementBicRepository, this.rt1PsrParticipantHeaderRepository, 
				this.rt1PsrParticipantBodyRepository, this.isoExternalCodeSetRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(66)
	void testRt1ServiceImplEqualsHashCode() {
		
		Rt1ServiceImpl test = new Rt1ServiceImpl(this.config, this.minioService, this.rt1BulkRepository, this.rt1BulkPaymentRepository, 
				this.rt1PsrFileHeaderRepository, this.rt1PsrSettlementBicRepository, this.rt1PsrParticipantHeaderRepository, 
				this.rt1PsrParticipantBodyRepository, this.isoExternalCodeSetRepository);
		int result = this.service.hashCode();
		assertThat(result).isEqualTo(test.hashCode());
	}
	
	@Test
	@Order(67)
	void testRt1ServiceImplNotEqualsHashCode() {
		
		Rt1ServiceImpl test = new Rt1ServiceImpl(null, null, null, null, null, null, null, null, null);
		int result = this.service.hashCode();
		assertThat(result).isNotEqualTo(test.hashCode());
	}
	
	@AfterEach
	public void cleanUp() {
		this.mockFileManager.close();
		this.mockDateUtils.close();
	}
	
	private Item mockItem(String objectName) {
		Item item = Mockito.mock(Item.class);
		Mockito.when(item.objectName()).thenReturn(objectName);
		
		return item;
	}

}
