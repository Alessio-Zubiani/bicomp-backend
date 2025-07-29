package it.popso.bicomp.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.minio.messages.Item;
import it.popso.bicomp.dto.PageableTipsEntryDto;
import it.popso.bicomp.dto.PageableTipsReportDto;
import it.popso.bicomp.dto.TipsBalanceDto;
import it.popso.bicomp.dto.TipsTotalDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.TipsCamt052BankAccountReport;
import it.popso.bicomp.model.TipsCamt053BankAccountStatement;
import it.popso.bicomp.model.TipsCamt053BankAccountStatementEntry;
import it.popso.bicomp.model.TipsCamt053BankStatement;
import it.popso.bicomp.properties.Access;
import it.popso.bicomp.properties.BicompConfig;
import it.popso.bicomp.properties.Bucket;
import it.popso.bicomp.properties.Minio;
import it.popso.bicomp.properties.Tips;
import it.popso.bicomp.repository.TipsCamt052BankAccountReportRepository;
import it.popso.bicomp.repository.TipsCamt053BankAccountStatementEntryRepository;
import it.popso.bicomp.repository.TipsCamt053BankAccountStatementRepository;
import it.popso.bicomp.repository.TipsCamt053BankStatementRepository;
import it.popso.bicomp.service.impl.TipsServiceImpl;
import it.popso.bicomp.utils.DateUtils;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class TipsServiceImplTest {
	
	@Mock
	private MinioService minioService;
	
	@Mock
	private TipsCamt052BankAccountReportRepository tipsCamt052BankAccountReportRepository;
	
	@Mock
	private TipsCamt053BankStatementRepository tipsCamt053BankStatementRepository;
	
	@Mock
	private TipsCamt053BankAccountStatementRepository tipsCamt053BankAccountStatementRepository;
	
	@Mock
	private TipsCamt053BankAccountStatementEntryRepository tipsCamt053BankAccountStatementEntryRepository;
	
	private BicompConfig config;
	private TipsServiceImpl service;
	private DateUtils dateUtils;
	private MockedStatic<DateUtils> mockDateUtils;
	
	
	@BeforeEach
    public void setup() {
		Tips tips = new Tips();
		tips.setCamt052Share("tips/camt_052");
		tips.setCamt052Prefix("camt.052");
		tips.setCamt052Suffix(".xml");
		tips.setCamt053Share("tips/camt_053");
		tips.setCamt053Prefix("camt.053");
		tips.setCamt053Suffix(".xml");
		tips.setPaymentReportFolder("tips");
		tips.setPaymentReportPrefix("TIPS_PAYMENT_");
		tips.setPaymentReportSuffix(".txt");
		
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
		this.config.setTips(tips);
		this.config.setMinio(minio);
		
		this.dateUtils = Mockito.mock(DateUtils.class);
		this.mockDateUtils = Mockito.mockStatic(DateUtils.class);
		
		this.service = new TipsServiceImpl(this.config, this.minioService, this.tipsCamt052BankAccountReportRepository, this.tipsCamt053BankStatementRepository, 
				this.tipsCamt053BankAccountStatementRepository, this.tipsCamt053BankAccountStatementEntryRepository);
	}
	
	@Test
	@Order(1)
	void testGetCamt052ReportEmpty() throws IOException {
		
		List<Item> list = List.of();
		Mockito.when(this.minioService.getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
			.thenReturn(list);
		
		List<Item> result = this.service.getCamt052Report();
		
		assertThat(result).isEmpty();
		verify(this.minioService, times(1)).getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());
	}
	
	@Test
	@Order(2)
	void testGetCamt052Report() throws IOException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/tips/camt.052_20250101.xml"));
		Mockito.when(this.minioService.getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
			.thenReturn(list);
		
		List<Item> result = this.service.getCamt052Report();
		
		assertThat(result).hasSize(1);
		verify(this.minioService, times(1)).getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());
	}
	
	@Test
	@Order(3)
	void testGetCamt052ReportBicompException() throws IOException {
		
		Mockito.when(this.minioService.getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
			.thenThrow(new BicompException("Minio exception"));
		
		assertThrows(BicompException.class, () -> {
			this.service.getCamt052Report();
		});
		
		verify(this.minioService, times(1)).getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());
	}
	
	@Test
	@Order(4)
	void testGetCamt053ReportEmpty() throws IOException {
		
		List<Item> list = List.of();
		Mockito.when(this.minioService.getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
			.thenReturn(list);
		
		List<Item> result = this.service.getCamt053Report();
		
		assertThat(result).isEmpty();
		verify(this.minioService, times(1)).getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());
	}
	
	@Test
	@Order(5)
	void testGetCamt053Report() throws IOException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/tips/camt.053_20250101.xml"));
		Mockito.when(this.minioService.getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
			.thenReturn(list);
		
		List<Item> result = this.service.getCamt053Report();
		
		assertThat(result).hasSize(1);
		verify(this.minioService, times(1)).getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());
	}
	
	@Test
	@Order(6)
	void testGetCamt053ReportIOException() throws IOException {
		
		Mockito.when(this.minioService.getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
			.thenThrow(new BicompException("Minio exception"));
	
		assertThrows(BicompException.class, () -> {
			this.service.getCamt053Report();
		});
		verify(this.minioService, times(1)).getObjectsByPrefixAndSuffix(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());
	}
	
	@Test
	@Order(7)
	void testProcessCamt052Report() throws IOException, JAXBException, DataIntegrityViolationException, BicompException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/tips/camt.052_20250101.xml"));
		InputStream inputStream = new FileInputStream(new File("src/test/resources/tips/camt.052_20250101.xml"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
				
		this.service.processCamt052Report(list);
		
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.tipsCamt052BankAccountReportRepository, times(1)).save(Mockito.any(TipsCamt052BankAccountReport.class));
	}
	
	@Test
	@Order(8)
	void testProcessCamt052ReportBicompException() throws IOException, JAXBException, DataIntegrityViolationException, BicompException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/tips/camt.052_BicompException.xml"));
		InputStream inputStream = new FileInputStream(new File("src/test/resources/tips/camt.052_BicompException.xml"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		assertThrows(BicompException.class, () -> {
			this.service.processCamt052Report(list);
		});
		
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.tipsCamt052BankAccountReportRepository, times(0)).save(Mockito.any(TipsCamt052BankAccountReport.class));
	}
	
	@Test
	@Order(9)
	void testProcessCamt052ReportDataIntegrityViolationException() throws IOException, JAXBException, DataIntegrityViolationException, BicompException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/tips/camt.052_20250101.xml"));
		InputStream inputStream = new FileInputStream(new File("src/test/resources/tips/camt.052_20250101.xml"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		Mockito.when(this.tipsCamt052BankAccountReportRepository.save(Mockito.any(TipsCamt052BankAccountReport.class)))
			.thenThrow(new DataIntegrityViolationException("DataIntegrityViolationException error"));
		
		assertThrows(DataIntegrityViolationException.class, () -> {
			this.service.processCamt052Report(list);
		});
		
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.tipsCamt052BankAccountReportRepository, times(1)).save(Mockito.any(TipsCamt052BankAccountReport.class));
	}
	
	@Test
	@Order(10)
	void testProcessCamt053Report() throws IOException, JAXBException, DataIntegrityViolationException, BicompException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/tips/camt.053_20250101.xml"));
		InputStream inputStream = new FileInputStream(new File("src/test/resources/tips/camt.053_20250101.xml"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		this.service.processCamt053Report(list);
		
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.tipsCamt053BankStatementRepository, times(1)).save(Mockito.any(TipsCamt053BankStatement.class));
		verify(this.tipsCamt053BankAccountStatementRepository, times(1)).save(Mockito.any(TipsCamt053BankAccountStatement.class));
		verify(this.tipsCamt053BankAccountStatementEntryRepository, atLeastOnce()).save(Mockito.any(TipsCamt053BankAccountStatementEntry.class));
	}
	
	@Test
	@Order(11)
	void testProcessCamt053ReportDataIntegrityViolationException() throws IOException, JAXBException, DataIntegrityViolationException, BicompException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/tips/camt.053_20250101.xml"));
		InputStream inputStream = new FileInputStream(new File("src/test/resources/tips/camt.053_20250101.xml"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		Mockito.when(this.tipsCamt053BankStatementRepository.save(Mockito.any(TipsCamt053BankStatement.class)))
			.thenThrow(new DataIntegrityViolationException("DataIntegrityViolationException error"));
		
		assertThrows(DataIntegrityViolationException.class, () -> {
			this.service.processCamt053Report(list);
		});
		
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.tipsCamt053BankStatementRepository, times(1)).save(Mockito.any(TipsCamt053BankStatement.class));
		verify(this.tipsCamt053BankAccountStatementRepository, times(0)).save(Mockito.any(TipsCamt053BankAccountStatement.class));
		verify(this.tipsCamt053BankAccountStatementEntryRepository, times(0)).save(Mockito.any(TipsCamt053BankAccountStatementEntry.class));
	}
	
	@Test
	@Order(12)
	void testProcessCamt053ReportNullAgent() throws IOException, JAXBException, DataIntegrityViolationException, BicompException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/tips/camt.053_NullAgent.xml"));
		InputStream inputStream = new FileInputStream(new File("src/test/resources/tips/camt.053_NullAgent.xml"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.gregorianCalendarToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(new Date());
		
		this.service.processCamt053Report(list);
		
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.tipsCamt053BankStatementRepository, times(1)).save(Mockito.any(TipsCamt053BankStatement.class));
		verify(this.tipsCamt053BankAccountStatementRepository, times(1)).save(Mockito.any(TipsCamt053BankAccountStatement.class));
		verify(this.tipsCamt053BankAccountStatementEntryRepository, atLeastOnce()).save(Mockito.any(TipsCamt053BankAccountStatementEntry.class));
	}
	
	@Test
	@Order(13)
	void testMoveToBackupFolder052() throws IOException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/tips/camt.052_20250101.xml"));
		Mockito.doNothing().when(this.minioService).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		Mockito.doNothing().when(this.minioService).removeFile(Mockito.anyString(), Mockito.anyString());
		
		this.service.moveToBackupFolder052(list);
		
		verify(this.minioService, times(1)).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		verify(this.minioService, times(1)).removeFile(Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(14)
	void testMoveToBackupFolder052CopyBicompException() {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/tips/camt.052_20250101.xml"));
		Mockito.doThrow(new BicompException("MinIO exception")).when(this.minioService).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		
		assertThrows(BicompException.class, () -> {
			this.service.moveToBackupFolder052(list);
		});
		
		verify(this.minioService, times(1)).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		verify(this.minioService, times(0)).removeFile(Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(15)
	void testMoveToBackupFolder052RemoveBicompException() {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/tips/camt.052_20250101.xml"));
		Mockito.doNothing().when(this.minioService).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new BicompException("MinIO exception")).when(this.minioService).removeFile(Mockito.anyString(), Mockito.anyString());
				
		assertThrows(BicompException.class, () -> {
			this.service.moveToBackupFolder052(list);
		});
		
		verify(this.minioService, times(1)).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		verify(this.minioService, times(1)).removeFile(Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(16)
	void testMoveToBackupFolder053() throws IOException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/tips/camt.053_20250101.xml"));
		Mockito.doNothing().when(this.minioService).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		Mockito.doNothing().when(this.minioService).removeFile(Mockito.anyString(), Mockito.anyString());
		
		this.service.moveToBackupFolder053(list);
		
		verify(this.minioService, times(1)).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		verify(this.minioService, times(1)).removeFile(Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(17)
	void testMoveToBackupFolder053CopyBicompException() {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/tips/camt.053_20250101.xml"));
		Mockito.doThrow(new BicompException("MinIO exception")).when(this.minioService).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		
		assertThrows(BicompException.class, () -> {
			this.service.moveToBackupFolder052(list);
		});
		
		verify(this.minioService, times(1)).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		verify(this.minioService, times(0)).removeFile(Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(18)
	void testMoveToBackupFolder053RemoveBicompException() {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/tips/camt.053_20250101.xml"));
		Mockito.doNothing().when(this.minioService).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new BicompException("MinIO exception")).when(this.minioService).removeFile(Mockito.anyString(), Mockito.anyString());
				
		assertThrows(BicompException.class, () -> {
			this.service.moveToBackupFolder052(list);
		});
		
		verify(this.minioService, times(1)).copyFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		verify(this.minioService, times(1)).removeFile(Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(19)
	void testGetTipsLastBalanceByDate() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		Optional<TipsCamt053BankAccountStatement> o = Optional.of(TipsCamt053BankAccountStatement.builder()
				.accountNumber("IITEURPOSOIT22XXXTIPS")
				.accountOwner("POSOIT22XXX")
				.openingBalanceIndicator('C')
				.openingBalance(BigDecimal.ZERO)
				.closingBalanceIndicator('C')
				.closingBalance(BigDecimal.TEN)
				.currency("EUR")
				.stmtId("1")
				.settlementDate(new Date())
				.build()
			);
		Mockito.when(this.tipsCamt053BankAccountStatementRepository.findLastStatementByDate(Mockito.any(Date.class), Mockito.any(PageRequest.class)))
			.thenReturn(o);
		
		List<TipsBalanceDto> list = this.service.getTipsLastBalanceByDate("01-09-2023");
		assertThat(list).isNotEmpty();
		assertThat(list.get(0).getClosingBalance()).isEqualTo(o.get().getClosingBalance());
		verify(this.tipsCamt053BankAccountStatementRepository, times(1)).findLastStatementByDate(Mockito.any(Date.class), Mockito.any(PageRequest.class));
	}
	
	@Test
	@Order(20)
	void testGetTipsLastBalanceByDateException() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		Optional<TipsCamt053BankAccountStatement> o = Optional.empty();
		Mockito.when(this.tipsCamt053BankAccountStatementRepository.findLastStatementByDate(Mockito.any(Date.class), Mockito.any(PageRequest.class)))
			.thenReturn(o);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getTipsLastBalanceByDate("01-09-2023");
		});
		
		verify(this.tipsCamt053BankAccountStatementRepository, times(1)).findLastStatementByDate(Mockito.any(Date.class), Mockito.any(PageRequest.class));
	}
	
	@Test
	@Order(21)
	void testGetTipsDailyReport() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		TipsCamt053BankStatement t = TipsCamt053BankStatement.builder()
				.msgId("SA-D3lgaa-N11j8j-8czuop")
				.reportName("camt.053_20230606_171317371.xml")
				.build();
		
		Page<TipsCamt053BankAccountStatement> p = new PageImpl<>(Arrays.asList(TipsCamt053BankAccountStatement.builder()
				.accountNumber("IITEURPOSOIT22XXXTIPS")
				.accountOwner("POSOIT22XXX")
				.openingBalanceIndicator('C')
				.openingBalance(BigDecimal.ZERO)
				.closingBalanceIndicator('C')
				.closingBalance(BigDecimal.TEN)
				.currency("EUR")
				.stmtId("1")
				.settlementDate(new Date())
				.tipsCamt053BankStatement(t)
				.build(), 
				TipsCamt053BankAccountStatement.builder()
				.accountNumber("IITEURPOSOIT22XXXTIPS")
				.accountOwner("POSOIT22XXX")
				.openingBalanceIndicator('C')
				.openingBalance(BigDecimal.ZERO)
				.closingBalanceIndicator('C')
				.closingBalance(new BigDecimal(20))
				.currency("EUR")
				.stmtId("1")
				.settlementDate(new Date())
				.tipsCamt053BankStatement(t)
				.build()
			));
		Mockito.when(this.tipsCamt053BankAccountStatementRepository.findBySettlementDate(Mockito.any(Date.class), Mockito.any(Pageable.class))).thenReturn(p);
		
		PageableTipsReportDto result = this.service.getTipsDailyReport("01-09-2023", PageRequest.of(0, 10));
		assertThat(result.getReports()).isNotEmpty().hasSameSizeAs(p.getContent());
		assertThat(result.getReports().get(0).getClosingBalance()).isEqualTo(p.getContent().get(0).getClosingBalance());
		verify(this.tipsCamt053BankAccountStatementRepository, times(1)).findBySettlementDate(Mockito.any(Date.class), Mockito.any(Pageable.class));
	}
	
	@Test
	@Order(22)
	void testGetTipsDailyReportPagingNull() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		TipsCamt053BankStatement t = TipsCamt053BankStatement.builder()
				.msgId("SA-D3lgaa-N11j8j-8czuop")
				.reportName("camt.053_20230606_171317371.xml")
				.build();
		
		List<TipsCamt053BankAccountStatement> list = Arrays.asList(TipsCamt053BankAccountStatement.builder()
				.accountNumber("IITEURPOSOIT22XXXTIPS")
				.accountOwner("POSOIT22XXX")
				.openingBalanceIndicator('C')
				.openingBalance(BigDecimal.ZERO)
				.closingBalanceIndicator('C')
				.closingBalance(BigDecimal.TEN)
				.currency("EUR")
				.stmtId("1")
				.settlementDate(new Date())
				.tipsCamt053BankStatement(t)
				.build(), 
				TipsCamt053BankAccountStatement.builder()
				.accountNumber("IITEURPOSOIT22XXXTIPS")
				.accountOwner("POSOIT22XXX")
				.openingBalanceIndicator('C')
				.openingBalance(BigDecimal.ZERO)
				.closingBalanceIndicator('C')
				.closingBalance(new BigDecimal(20))
				.currency("EUR")
				.stmtId("1")
				.settlementDate(new Date())
				.tipsCamt053BankStatement(t)
				.build()
			);
		Mockito.when(this.tipsCamt053BankAccountStatementRepository.findBySettlementDateNoPagination(Mockito.any(Date.class))).thenReturn(list);
		
		PageableTipsReportDto result = this.service.getTipsDailyReport("01-09-2023", null);
		assertThat(result.getReports()).isNotEmpty().hasSameSizeAs(list);
		assertThat(result.getReports().get(0).getClosingBalance()).isEqualTo(list.get(0).getClosingBalance());
		
		verify(this.tipsCamt053BankAccountStatementRepository, times(1)).findBySettlementDateNoPagination(Mockito.any(Date.class));
	}
	
	@Test
	@Order(23)
	void testGetTipsDailyReportPagingNullEmptyList() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		Mockito.when(this.tipsCamt053BankAccountStatementRepository.findBySettlementDateNoPagination(Mockito.any(Date.class))).thenReturn(Arrays.asList());
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getTipsDailyReport("01-09-2023", null);
		});

		verify(this.tipsCamt053BankAccountStatementRepository, times(1)).findBySettlementDateNoPagination(Mockito.any(Date.class));
	}
	
	@Test
	@Order(24)
	void testGetTipsDailyReportException() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		Page<TipsCamt053BankAccountStatement> p = new PageImpl<>(Arrays.asList());
		Mockito.when(this.tipsCamt053BankAccountStatementRepository.findBySettlementDate(Mockito.any(Date.class), Mockito.any(Pageable.class))).thenReturn(p);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getTipsDailyReport("01-09-2023", PageRequest.of(0, 10));
		});
		
		verify(this.tipsCamt053BankAccountStatementRepository, times(1)).findBySettlementDate(Mockito.any(Date.class), Mockito.any(Pageable.class));
	}
	
	@Test
	@Order(25)
	void testGetTipsReportDetail() throws ResourceNotFoundException {
		
		TipsCamt053BankAccountStatement t = TipsCamt053BankAccountStatement.builder()
				.stmtId("1")
				.accountNumber("IITEURPOSOIT22XXXTIPS")
				.accountOwner("POSOIT22XXX")
				.settlementDate(new Date())
				.openingBalanceIndicator('C')
				.openingBalance(BigDecimal.ZERO)
				.closingBalanceIndicator('C')
				.currency("EUR")
				.build();
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.localDateToDate(Mockito.any(LocalDate.class))).thenReturn(new Date());
		Mockito.when(this.dateUtils.getWorkingDay(Mockito.any(Date.class))).thenReturn(new Date());
		Mockito.when(this.dateUtils.dateToLocalDate(Mockito.any(Date.class))).thenReturn(LocalDate.now());
		Mockito.when(this.tipsCamt053BankAccountStatementRepository.findById(Mockito.any(BigDecimal.class)))
			.thenReturn(Optional.of(t));
		
		List<Object[]> list = Arrays.asList(new Object[] { 'D', new BigDecimal(10) }, new Object[] { 'C', new BigDecimal(20) });
		Mockito.when(this.tipsCamt053BankAccountStatementEntryRepository.findTotalByReportId(Mockito.any(BigDecimal.class))).thenReturn(list);
		
		TipsTotalDto result = this.service.getTipsReportDetail(BigDecimal.ONE);
		assertThat(result).isNotNull();
		assertThat(result.getDebitPmntAmount()).isEqualTo(list.get(0)[1]);
		assertThat(result.getCreditPmntAmount()).isEqualTo(list.get(1)[1]);
		verify(this.tipsCamt053BankAccountStatementRepository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.tipsCamt053BankAccountStatementEntryRepository, times(1)).findTotalByReportId(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(26)
	void testGetTipsReportDetailEmpty() {
		
		TipsCamt053BankAccountStatement t = TipsCamt053BankAccountStatement.builder()
				.stmtId("1")
				.accountNumber("IITEURPOSOIT22XXXTIPS")
				.accountOwner("POSOIT22XXX")
				.settlementDate(new Date())
				.openingBalanceIndicator('C')
				.openingBalance(BigDecimal.ZERO)
				.closingBalanceIndicator('C')
				.closingBalance(new BigDecimal(100))
				.currency("EUR")
				.build();
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.localDateToDate(Mockito.any(LocalDate.class))).thenReturn(new Date());
		Mockito.when(this.dateUtils.getWorkingDay(Mockito.any(Date.class))).thenReturn(new Date());
		Mockito.when(this.dateUtils.dateToLocalDate(Mockito.any(Date.class))).thenReturn(LocalDate.now());
		Mockito.when(this.tipsCamt053BankAccountStatementRepository.findById(Mockito.any(BigDecimal.class)))
			.thenReturn(Optional.of(t));
		
		List<Object[]> list = Arrays.asList();
		Mockito.when(this.tipsCamt053BankAccountStatementEntryRepository.findTotalByReportId(Mockito.any(BigDecimal.class))).thenReturn(list);
		
		TipsTotalDto result = this.service.getTipsReportDetail(BigDecimal.ONE);
		assertThat(result).isNotNull();
		assertThat(result.getDebitPmntAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getCreditPmntAmount()).isEqualTo(BigDecimal.ZERO);
		verify(this.tipsCamt053BankAccountStatementRepository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.tipsCamt053BankAccountStatementEntryRepository, times(1)).findTotalByReportId(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(27)
	void testProcessCamt052ReportJaxbException() throws IOException, JAXBException, DataIntegrityViolationException, BicompException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/tips/camt.052_JaxbException.xml"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/tips/camt.052_JaxbException.xml"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		assertThrows(JAXBException.class, () -> {
			this.service.processCamt052Report(list);
		});
		
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.tipsCamt052BankAccountReportRepository, times(0)).save(Mockito.any(TipsCamt052BankAccountReport.class));
	}
	
	@Test
	@Order(28)
	void testProcessCamt053ReportJaxbException() throws IOException, JAXBException, DataIntegrityViolationException, BicompException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/tips/camt.053_JaxbException.xml"));		
		
		InputStream inputStream = new FileInputStream(new File("src/test/resources/tips/camt.053_JaxbException.xml"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		assertThrows(JAXBException.class, () -> {
			this.service.processCamt053Report(list);
		});
		
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.tipsCamt053BankStatementRepository, times(0)).save(Mockito.any(TipsCamt053BankStatement.class));
		verify(this.tipsCamt053BankAccountStatementRepository, times(0)).save(Mockito.any(TipsCamt053BankAccountStatement.class));
		verify(this.tipsCamt053BankAccountStatementEntryRepository, times(0)).save(Mockito.any(TipsCamt053BankAccountStatementEntry.class));
	}
	
	@Test
	@Order(29)
	void testExtractTipsPayment() {
		
		List<TipsCamt053BankAccountStatementEntry> list = Arrays.asList(TipsCamt053BankAccountStatementEntry.builder()
				.entryReference("A102586329601030483417032550IT")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.status("BOOK")
				.side('C')
				.bankTransactionCode("PMNT")
				.bankTransactionCodeFamily("RRCT")
				.debitorBic("UNCRITMMXXX")
				.creditorBic("POSOIT22XXX")
				.build(), TipsCamt053BankAccountStatementEntry.builder()
				.entryReference("IP23060807590363483955000043IT")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.paymentAmount(new BigDecimal(50))
				.currency("EUR")
				.status("BOOK")
				.side('D')
				.bankTransactionCode("PMNT")
				.bankTransactionCodeFamily("IRCT")
				.debitorBic("POSOIT22XXX")
				.creditorBic("UNCRITMMXXX")
				.build());
		Mockito.when(this.tipsCamt053BankAccountStatementEntryRepository.findBySettlementDate(Mockito.any(Date.class))).thenReturn(list);
		
		List<TipsCamt053BankAccountStatementEntry> result = this.service.extractTipsPayment(new Date());
		
		assertThat(result).isNotEmpty().hasSameSizeAs(list);
		verify(this.tipsCamt053BankAccountStatementEntryRepository, times(1)).findBySettlementDate(Mockito.any(Date.class));
	}
	
	@Test
	@Order(30)
	void testCreateTipsPaymentFile() throws BicompException, ParseException {
		
		TipsCamt053BankAccountStatement t = TipsCamt053BankAccountStatement.builder()
				.stmtId("1")
				.accountNumber("IITEURPOSOIT22XXXTIPS")
				.accountOwner("POSOIT22XXX")
				.settlementDate(new Date())
				.openingBalanceIndicator('C')
				.openingBalance(BigDecimal.ZERO)
				.closingBalanceIndicator('C')
				.closingBalance(new BigDecimal(100))
				.currency("EUR")
				.creationDateTime(new Date())
				.build();
		
		List<TipsCamt053BankAccountStatementEntry> list = Arrays.asList(TipsCamt053BankAccountStatementEntry.builder()
				.entryReference("A102586329601030483417032550IT")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.status("BOOK")
				.side('C')
				.bankTransactionCode("PMNT")
				.bankTransactionCodeFamily("RRCT")
				.debitorBic("UNCRITMMXXX")
				.creditorBic("POSOIT22XXX")
				.tipsCamt053BankAccountStatement(t)
				.build(), TipsCamt053BankAccountStatementEntry.builder()
				.entryReference("IP23060807590363483955000043IT")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.paymentAmount(new BigDecimal(50))
				.currency("EUR")
				.status("BOOK")
				.side('D')
				.bankTransactionCode("PMNT")
				.bankTransactionCodeFamily("IRCT")
				.debitorBic("POSOIT22XXX")
				.creditorBic("UNCRITMMXXX")
				.tipsCamt053BankAccountStatement(t)
				.build());
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.dateToString(Mockito.any(Date.class))).thenReturn("20250729");
		Mockito.doNothing().when(this.minioService).uploadFile(Mockito.anyString(), Mockito.anyString(), Mockito.any(InputStream.class));
		
		String result = this.service.createTipsPaymentFile(new Date(), list);
		
		assertThat(result).isNotNull();
		
		verify(this.dateUtils, times(1)).dateToString(Mockito.any(Date.class));
		verify(this.minioService, times(1)).uploadFile(Mockito.anyString(), Mockito.anyString(), Mockito.any(InputStream.class));
	}
	
	@Test
	@Order(31)
	void testCreateTipsPaymentFileBicompException() throws BicompException, ParseException {
		
		TipsCamt053BankAccountStatement t = TipsCamt053BankAccountStatement.builder()
				.stmtId("1")
				.accountNumber("IITEURPOSOIT22XXXTIPS")
				.accountOwner("POSOIT22XXX")
				.settlementDate(new Date())
				.openingBalanceIndicator('C')
				.openingBalance(BigDecimal.ZERO)
				.closingBalanceIndicator('C')
				.closingBalance(new BigDecimal(100))
				.currency("EUR")
				.creationDateTime(new Date())
				.build();
		
		List<TipsCamt053BankAccountStatementEntry> list = Arrays.asList(TipsCamt053BankAccountStatementEntry.builder()
				.entryReference("A102586329601030483417032550IT")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.status("BOOK")
				.side('C')
				.bankTransactionCode("PMNT")
				.bankTransactionCodeFamily("RRCT")
				.debitorBic("UNCRITMMXXX")
				.creditorBic("POSOIT22XXX")
				.tipsCamt053BankAccountStatement(t)
				.build(), TipsCamt053BankAccountStatementEntry.builder()
				.entryReference("IP23060807590363483955000043IT")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.paymentAmount(new BigDecimal(50))
				.currency("EUR")
				.status("BOOK")
				.side('D')
				.bankTransactionCode("PMNT")
				.bankTransactionCodeFamily("IRCT")
				.debitorBic("POSOIT22XXX")
				.creditorBic("UNCRITMMXXX")
				.tipsCamt053BankAccountStatement(t)
				.build());
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.dateToString(Mockito.any(Date.class))).thenReturn("20250729");
		Mockito.doThrow(BicompException.class).when(this.minioService).uploadFile(Mockito.anyString(), Mockito.anyString(), Mockito.any(InputStream.class));
		
		assertThrows(BicompException.class, () -> {
			this.service.createTipsPaymentFile(new Date(), list);
		});
		
		verify(this.dateUtils, times(1)).dateToString(Mockito.any(Date.class));
		verify(this.minioService, times(1)).uploadFile(Mockito.anyString(), Mockito.anyString(), Mockito.any(InputStream.class));
	}
	
	@Test
	@Order(32)
	void testCreateTipsPaymentFileParseException() throws BicompException, ParseException {
				
		List<TipsCamt053BankAccountStatementEntry> list = Arrays.asList(TipsCamt053BankAccountStatementEntry.builder()
				.entryReference("A102586329601030483417032550IT")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.status("BOOK")
				.side('C')
				.bankTransactionCode("PMNT")
				.bankTransactionCodeFamily("RRCT")
				.debitorBic("UNCRITMMXXX")
				.creditorBic("POSOIT22XXX")
				.build(), TipsCamt053BankAccountStatementEntry.builder()
				.entryReference("IP23060807590363483955000043IT")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.paymentAmount(new BigDecimal(50))
				.currency("EUR")
				.status("BOOK")
				.side('D')
				.bankTransactionCode("PMNT")
				.bankTransactionCodeFamily("IRCT")
				.debitorBic("POSOIT22XXX")
				.creditorBic("UNCRITMMXXX")
				.build());
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.dateToString(Mockito.any(Date.class))).thenThrow(ParseException.class);
		
		assertThrows(ParseException.class, () -> {
			this.service.createTipsPaymentFile(new Date(), list);
		});
		
		verify(this.dateUtils, times(1)).dateToString(Mockito.any(Date.class));
		verify(this.minioService, times(0)).uploadFile(Mockito.anyString(), Mockito.anyString(), Mockito.any(InputStream.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(33)
	void testGetPaymentsAllFilter() throws ResourceNotFoundException, ParseException {
		
		Page<TipsCamt053BankAccountStatementEntry> p = new PageImpl<>(Arrays.asList(TipsCamt053BankAccountStatementEntry.builder()
				.entryReference("A102586329601030483417032550IT")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.status("BOOK")
				.side('C')
				.bankTransactionCode("PMNT")
				.bankTransactionCodeFamily("RRCT")
				.debitorBic("UNCRITMMXXX")
				.creditorBic("POSOIT22XXX")
				.build(), TipsCamt053BankAccountStatementEntry.builder()
				.entryReference("IP23060807590363483955000043IT")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.paymentAmount(new BigDecimal(50))
				.currency("EUR")
				.status("BOOK")
				.side('D')
				.bankTransactionCode("PMNT")
				.bankTransactionCodeFamily("IRCT")
				.debitorBic("POSOIT22XXX")
				.creditorBic("UNCRITMMXXX")
				.build())
			);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDateYear(Mockito.anyString())).thenReturn(new Date());
		Mockito.when(this.tipsCamt053BankAccountStatementEntryRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
			.thenReturn(p);
		
		PageableTipsEntryDto result = this.service.getPayments('C', "BOOK", new BigDecimal(0), new BigDecimal(300), 
				"2023-06-07", "2023-06-07", PageRequest.of(0, 10));
		
		assertThat(result).isNotNull();
		assertThat(result.getEntries()).isNotEmpty();
		assertThat(result.getTotalElements()).isEqualTo(new BigDecimal(2));
		assertThat(result.getEntries().get(0).getPaymentAmount()).isEqualTo(p.getContent().get(0).getPaymentAmount());
		
		verify(this.dateUtils, times(2)).stringToDateYear(Mockito.anyString());
		verify(this.tipsCamt053BankAccountStatementEntryRepository, times(1)).findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(34)
	void testGetPaymentsNoFilter() throws ResourceNotFoundException, ParseException {
		
		Page<TipsCamt053BankAccountStatementEntry> p = new PageImpl<>(Arrays.asList(TipsCamt053BankAccountStatementEntry.builder()
				.entryReference("A102586329601030483417032550IT")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.status("BOOK")
				.side('C')
				.bankTransactionCode("PMNT")
				.bankTransactionCodeFamily("RRCT")
				.debitorBic("UNCRITMMXXX")
				.creditorBic("POSOIT22XXX")
				.build(), TipsCamt053BankAccountStatementEntry.builder()
				.entryReference("IP23060807590363483955000043IT")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.paymentAmount(new BigDecimal(50))
				.currency("EUR")
				.status("BOOK")
				.side('D')
				.bankTransactionCode("PMNT")
				.bankTransactionCodeFamily("IRCT")
				.debitorBic("POSOIT22XXX")
				.creditorBic("UNCRITMMXXX")
				.build())
			);
		
		Mockito.when(this.tipsCamt053BankAccountStatementEntryRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
			.thenReturn(p);
		
		PageableTipsEntryDto result = this.service.getPayments(null, null, null, null, null, null, PageRequest.of(0, 10));
		
		assertThat(result).isNotNull();
		assertThat(result.getEntries()).isNotEmpty();
		assertThat(result.getTotalElements()).isEqualTo(new BigDecimal(2));
		assertThat(result.getEntries().get(0).getPaymentAmount()).isEqualTo(p.getContent().get(0).getPaymentAmount());
		
		verify(this.dateUtils, times(0)).stringToDateYear(Mockito.anyString());
		verify(this.tipsCamt053BankAccountStatementEntryRepository, times(1)).findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(35)
	void testGetPaymentsEmptyList() throws ResourceNotFoundException, ParseException {
		
		Page<TipsCamt053BankAccountStatementEntry> p = new PageImpl<>(Arrays.asList());
		
		Mockito.when(this.tipsCamt053BankAccountStatementEntryRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
			.thenReturn(p);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getPayments('C', "BOOK", null, null, null, null, PageRequest.of(0, 10));
		});
		
		verify(this.tipsCamt053BankAccountStatementEntryRepository, times(1)).findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class));
	}
	
	@Test
	@Order(36)
	void testGetTipsLastBalanceByDateParseException() throws ResourceNotFoundException, BicompException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenThrow(ParseException.class);
		
		assertThrows(ParseException.class, () -> {
			this.service.getTipsLastBalanceByDate("01-09-2023");
		});

		verify(this.tipsCamt053BankAccountStatementRepository, times(0)).findLastStatementByDate(Mockito.any(Date.class), Mockito.any(PageRequest.class));
	}
	
	@Test
	@Order(37)
	void testGetTipsDailyReportParseException() throws ResourceNotFoundException, BicompException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenThrow(ParseException.class);
		
		assertThrows(ParseException.class, () -> {
			this.service.getTipsDailyReport("01-09-2023", PageRequest.of(0, 10));
		});

		verify(this.tipsCamt053BankAccountStatementRepository, times(0)).findBySettlementDate(Mockito.any(Date.class), Mockito.any(Pageable.class));
	}
	
	@Test
	@Order(38)
	void testGetTipsReportDetailException() {
		
		Mockito.when(this.tipsCamt053BankAccountStatementRepository.findById(Mockito.any(BigDecimal.class)))
			.thenReturn(Optional.empty());
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getTipsReportDetail(BigDecimal.ONE);
		});
		
		verify(this.tipsCamt053BankAccountStatementRepository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.tipsCamt053BankAccountStatementEntryRepository, times(0)).findTotalByReportId(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(39)
	void testProcessCamt052ReportEmptyReport() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/tips/camt.052_empty.xml"));
		InputStream inputStream = new FileInputStream(new File("src/test/resources/tips/camt.052_empty.xml"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.service.processCamt052Report(list);
		
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.tipsCamt052BankAccountReportRepository, times(0)).save(Mockito.any(TipsCamt052BankAccountReport.class));
	}
	
	@Test
	@Order(40)
	void testProcessCamt053ReportEmptyReport() throws DataIntegrityViolationException, IOException, BicompException, JAXBException {
		
		List<Item> list = List.of(this.mockItem("src/test/resources/tips/camt.053_empty.xml"));
		InputStream inputStream = new FileInputStream(new File("src/test/resources/tips/camt.053_empty.xml"));
		Mockito.when(this.minioService.getObject(Mockito.anyString(), Mockito.anyString())).thenReturn(inputStream);
		
		this.service.processCamt053Report(list);
		
		verify(this.minioService, times(1)).getObject(Mockito.anyString(), Mockito.anyString());
		verify(this.tipsCamt053BankStatementRepository, times(0)).save(Mockito.any(TipsCamt053BankStatement.class));
		verify(this.tipsCamt053BankAccountStatementRepository, times(0)).save(Mockito.any(TipsCamt053BankAccountStatement.class));
		verify(this.tipsCamt053BankAccountStatementEntryRepository, times(0)).save(Mockito.any(TipsCamt053BankAccountStatementEntry.class));
	}
	
	@Test
	@Order(41)
	void testGetSaldiTips() {
		
		List<TipsCamt052BankAccountReport> list = Arrays.asList(TipsCamt052BankAccountReport.builder()
	    		.id(BigDecimal.ONE)
	    		.tmsInsert(new Date())
	    		.creationDateTime(new Date())
	    		.accountId("IITEURPOSOIT22XXXTIPS")
	    		.accountOwner("POSOIT22XXX")
	    		.closingBalance(new BigDecimal(20))
	    		.closingBalanceSide('C')
	    		.currency("EUR")
	    		.flagElaborato('N')
	    		.msgId("SAT-F89p18z-N9xl7jl-9ezjop")
	    		.openingBalance(new BigDecimal(0))
	    		.openingBalanceSide('C')
	    		.reportName("camt.052_20231204_171921559.xml")
	    		.settlementDate(new Date())
	    		.totalCreditOperation(new BigDecimal(30))
	    		.totalDebitOperation(new BigDecimal(10))
	    		.build()
	    );
		Mockito.when(this.tipsCamt052BankAccountReportRepository.findByFlagElaboratoOrderBySettlementDateAsc('N'))
			.thenReturn(list);
		
		List<TipsCamt052BankAccountReport> result = this.service.getSaldiTips();
		
		assertThat(result).hasSameSizeAs(list);
		
		verify(this.tipsCamt052BankAccountReportRepository, times(1))
			.findByFlagElaboratoOrderBySettlementDateAsc((Mockito.any(Character.class)));
	}
	
	@Test
	@Order(42)
	void testGetLastTipsPaymentByDate() {
		
		Page<TipsCamt053BankAccountStatementEntry> p = new PageImpl<>(Arrays.asList(TipsCamt053BankAccountStatementEntry.builder()
				.entryReference("A102586329601030483417032550IT")
				.settlementDateTime(new Timestamp(System.currentTimeMillis()))
				.paymentAmount(new BigDecimal(100))
				.currency("EUR")
				.status("BOOK")
				.side('C')
				.bankTransactionCode("PMNT")
				.bankTransactionCodeFamily("RRCT")
				.debitorBic("UNCRITMMXXX")
				.creditorBic("POSOIT22XXX")
				.build()
			));
		Mockito.when(this.tipsCamt053BankAccountStatementEntryRepository.findLastPaymentBySettlementDate(Mockito.any(Date.class), 
				Mockito.any(PageRequest.class))).thenReturn(p);
		
		TipsCamt053BankAccountStatementEntry result = this.service.getLastTipsPaymentByDate(new Date());
		
		assertThat(result).isNotNull();
		assertThat(result.getEntryReference()).isEqualTo(p.getContent().get(0).getEntryReference());
		
		verify(this.tipsCamt053BankAccountStatementEntryRepository, times(1))
			.findLastPaymentBySettlementDate(Mockito.any(Date.class), Mockito.any(PageRequest.class));
	}
	
	@Test
	@Order(43)
	void testGetLastTipsPaymentByDateResourceNotFoundException() {
		
		Page<TipsCamt053BankAccountStatementEntry> p = new PageImpl<>(Arrays.asList());
		Mockito.when(this.tipsCamt053BankAccountStatementEntryRepository.findLastPaymentBySettlementDate(Mockito.any(Date.class), 
				Mockito.any(PageRequest.class))).thenReturn(p);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getLastTipsPaymentByDate(new Date());
		});
		
		verify(this.tipsCamt053BankAccountStatementEntryRepository, times(1))
			.findLastPaymentBySettlementDate(Mockito.any(Date.class), Mockito.any(PageRequest.class));
	}
	
	@Test
	@Order(44)
	void testUpdateFlagElaborato() {
		
		Optional<TipsCamt052BankAccountReport> o = Optional.of(TipsCamt052BankAccountReport.builder()
	    		.id(BigDecimal.ONE)
	    		.tmsInsert(new Date())
	    		.creationDateTime(new Date())
	    		.accountId("IITEURPOSOIT22XXXTIPS")
	    		.accountOwner("POSOIT22XXX")
	    		.closingBalance(new BigDecimal(20))
	    		.closingBalanceSide('C')
	    		.currency("EUR")
	    		.flagElaborato('N')
	    		.msgId("SAT-F89p18z-N9xl7jl-9ezjop")
	    		.openingBalance(new BigDecimal(0))
	    		.openingBalanceSide('C')
	    		.reportName("camt.052_20231204_171921559.xml")
	    		.settlementDate(new Date())
	    		.totalCreditOperation(new BigDecimal(30))
	    		.totalDebitOperation(new BigDecimal(10))
	    		.build()
	    );
		Mockito.when(this.tipsCamt052BankAccountReportRepository.findByMsgId(Mockito.anyString())).thenReturn(o);
		Mockito.doNothing().when(this.tipsCamt052BankAccountReportRepository).updateFlagElaborato(Mockito.anyString(), Mockito.anyChar());
		
		String result = this.service.updateFlagElaborato("PROVA");
		
		assertThat(result).isEqualTo("PROVA");
		
		verify(this.tipsCamt052BankAccountReportRepository, times(1)).findByMsgId(Mockito.anyString());
		verify(this.tipsCamt052BankAccountReportRepository, times(1)).updateFlagElaborato(Mockito.anyString(), Mockito.anyChar());
	}
	
	@Test
	@Order(45)
	void testUpdateFlagElaboratoResourceNotFoundException() {
		
		Optional<TipsCamt052BankAccountReport> o = Optional.empty();
		Mockito.when(this.tipsCamt052BankAccountReportRepository.findByMsgId(Mockito.anyString())).thenReturn(o);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.updateFlagElaborato("PROVA");
		});
		
		verify(this.tipsCamt052BankAccountReportRepository, times(1)).findByMsgId(Mockito.anyString());
		verify(this.tipsCamt052BankAccountReportRepository, times(0)).updateFlagElaborato(Mockito.anyString(), Mockito.anyChar());
	}
	
	@Test
	@Order(46)
	void testTipsServiceImplEquals() {
		
		TipsServiceImpl test = new TipsServiceImpl(this.config, this.minioService, this.tipsCamt052BankAccountReportRepository, 
				this.tipsCamt053BankStatementRepository, this.tipsCamt053BankAccountStatementRepository, 
				this.tipsCamt053BankAccountStatementEntryRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isTrue();
	}
	
	@Test
	@Order(47)
	void testTipsServiceImplEqualsSameInstance() {
		
		assertThat(this.service.equals(this.service)).isTrue();
	}
	
	@Test
	@Order(48)
	void testTipsServiceImplNotEqualsNotSameInstanceType() {
		
		assertThat(this.service.equals("test")).isFalse();
	}
	
	@Test
	@Order(49)
	void testTipsServiceImplEqualsNull() {
		
		this.service = new TipsServiceImpl(null, null, null, null, null, null);
		TipsServiceImpl test = new TipsServiceImpl(null, null, null, null, null, null);
		boolean result = this.service.equals(test);
		assertThat(result).isTrue();
	}
	
	@Test
	@Order(50)
	void testTipsServiceImplNotNull() {
		
		this.service = new TipsServiceImpl(null, null, null, null, null, null);
		TipsServiceImpl test = new TipsServiceImpl(this.config, this.minioService, this.tipsCamt052BankAccountReportRepository, 
				this.tipsCamt053BankStatementRepository, this.tipsCamt053BankAccountStatementRepository, 
				this.tipsCamt053BankAccountStatementEntryRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(51)
	void testTipsServiceImplNotEqualsConfigNull() {
		
		TipsServiceImpl test = new TipsServiceImpl(null, this.minioService, this.tipsCamt052BankAccountReportRepository, 
				this.tipsCamt053BankStatementRepository, this.tipsCamt053BankAccountStatementRepository, 
				this.tipsCamt053BankAccountStatementEntryRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(52)
	void testTipsServiceImplNotEqualsCamt052Null() {
		
		TipsServiceImpl test = new TipsServiceImpl(this.config, this.minioService, null, this.tipsCamt053BankStatementRepository, 
				this.tipsCamt053BankAccountStatementRepository,	this.tipsCamt053BankAccountStatementEntryRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(53)
	void testTipsServiceImplNotEqualsCamt053BankNull() {
		
		TipsServiceImpl test = new TipsServiceImpl(this.config, this.minioService, this.tipsCamt052BankAccountReportRepository, 
				null, this.tipsCamt053BankAccountStatementRepository, this.tipsCamt053BankAccountStatementEntryRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(54)
	void testTipsServiceImplNotEqualsCamt053BankAccountNull() {
		
		TipsServiceImpl test = new TipsServiceImpl(this.config, this.minioService, this.tipsCamt052BankAccountReportRepository, 
				this.tipsCamt053BankStatementRepository, null, this.tipsCamt053BankAccountStatementEntryRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(55)
	void testTipsServiceImplNotEqualsCamt053BankAccountEntryNull() {
		
		TipsServiceImpl test = new TipsServiceImpl(this.config, this.minioService, this.tipsCamt052BankAccountReportRepository, 
				this.tipsCamt053BankStatementRepository, this.tipsCamt053BankAccountStatementRepository, null);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(56)
	void testTipsServiceImplNotEqualsMinioNull() {
		
		TipsServiceImpl test = new TipsServiceImpl(this.config, null, this.tipsCamt052BankAccountReportRepository, 
				this.tipsCamt053BankStatementRepository, this.tipsCamt053BankAccountStatementRepository, 
				this.tipsCamt053BankAccountStatementEntryRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(57)
	void testTipsServiceImplEqualsHashCode() {
		
		TipsServiceImpl test = new TipsServiceImpl(this.config, this.minioService, this.tipsCamt052BankAccountReportRepository, 
				this.tipsCamt053BankStatementRepository, this.tipsCamt053BankAccountStatementRepository, 
				this.tipsCamt053BankAccountStatementEntryRepository);
		int result = this.service.hashCode();
		assertThat(result).isEqualTo(test.hashCode());
	}
	
	@Test
	@Order(58)
	void testTipsServiceImplNotEqualsHashCode() {
		
		TipsServiceImpl test = new TipsServiceImpl(null, null, null, null, null, null);
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
