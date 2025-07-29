package it.popso.bicomp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import it.popso.bicomp.dto.CgsLacDetailDto;
import it.popso.bicomp.dto.CgsLacDto;
import it.popso.bicomp.dto.CgsLacEntryDto;
import it.popso.bicomp.dto.PageableCgsEntryDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.CgsLmrLtinBulkStatementEntry;
import it.popso.bicomp.model.CgsLmrPlcrBulkStatementEntry;
import it.popso.bicomp.service.CgsService;
import it.popso.bicomp.utils.DateUtils;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class CgsControllerTest {
	
	@Mock
	private CgsService cgsService;
	
	private CgsController controller;
	private DateUtils dateUtils;
	private MockedStatic<DateUtils> mockDateUtils;
	

	@BeforeEach
	public void setup() {
		this.controller = new CgsController(this.cgsService);
		this.dateUtils = Mockito.mock(DateUtils.class);
		this.mockDateUtils = Mockito.mockStatic(DateUtils.class);
	}
	
	@Test
	@Order(1)
	void testGetLastCgsBalance() throws ResourceNotFoundException, ParseException {
		
		List<CgsLacDto> list = Arrays.asList(CgsLacDto.builder()
				.lacId(new BigDecimal(1))
				.lacNumber("01")
				.fromDateTime(LocalDateTime.now())
				.toDateTime(LocalDateTime.now())
				.openingBalance(new BigDecimal(100))
				.closingBalance(new BigDecimal(200))
				.currency("EUR")
				.build()
			);
		Mockito.when(this.cgsService.getCgsLastBalanceByDate(Mockito.anyString())).thenReturn(list);
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.getCurrentTimestamp()).thenReturn(LocalDateTime.now());
		
		List<CgsLacDto> result = (List<CgsLacDto>) this.controller.getLastCgsLac("01-09-2023").getBody().getResponse();
		
		assertThat(result).isNotEmpty().hasSameSizeAs(list);
		assertThat(result.get(0).getLacNumber()).isEqualTo(list.get(0).getLacNumber());
		
		verify(this.cgsService, times(1)).getCgsLastBalanceByDate(Mockito.anyString());
	}
	
	@Test
	@Order(2)
	void testGetLastCgsBalanceException() throws ResourceNotFoundException, ParseException {
		
		Mockito.when(this.cgsService.getCgsLastBalanceByDate(Mockito.anyString())).thenThrow(ResourceNotFoundException.class);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.getLastCgsLac("01-09-2023");
		});
		
		verify(this.cgsService, times(1)).getCgsLastBalanceByDate(Mockito.anyString());
	}
	
	@Test
	@Order(3)
	void testGetCgsLacs() throws ResourceNotFoundException, ParseException {
		
		List<CgsLacDto> list = Arrays.asList(CgsLacDto.builder()
				.lacId(new BigDecimal(1))
				.lacNumber("01")
				.fromDateTime(LocalDateTime.now())
				.toDateTime(LocalDateTime.now())
				.openingBalance(new BigDecimal(100))
				.closingBalance(new BigDecimal(200))
				.currency("EUR")
				.build(), CgsLacDto.builder()
				.lacId(new BigDecimal(2))
				.lacNumber("02")
				.fromDateTime(LocalDateTime.now())
				.toDateTime(LocalDateTime.now())
				.openingBalance(new BigDecimal(300))
				.closingBalance(new BigDecimal(200))
				.currency("EUR")
				.build()
			);
		Mockito.when(this.cgsService.getCurrentDateLac(Mockito.anyString())).thenReturn(list);
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.getCurrentTimestamp()).thenReturn(LocalDateTime.now());
		
		List<CgsLacDto> result = (List<CgsLacDto>) this.controller.getCgsLacs("01-09-2023").getBody().getResponse();
		
		assertThat(result).isNotEmpty().hasSameSizeAs(list);
		assertThat(result.get(1).getLacNumber()).isEqualTo(list.get(1).getLacNumber());
		
		verify(this.cgsService, times(1)).getCurrentDateLac(Mockito.anyString());
	}
	
	@Test
	@Order(4)
	void testGetCgsLacsException() throws ResourceNotFoundException, ParseException {
		
		Mockito.when(this.cgsService.getCurrentDateLac(Mockito.anyString())).thenThrow(ResourceNotFoundException.class);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.getCgsLacs("01-09-2023");
		});
		
		verify(this.cgsService, times(1)).getCurrentDateLac(Mockito.anyString());
	}
	
	@Test
	@Order(5)
	void testGetCgsLacDetail() {
		
		CgsLacDetailDto cgsLacDetailDto = CgsLacDetailDto .builder()
				.lacId(new BigDecimal(1))
				.lacNumber("01")
				.creditLtinAmount(new BigDecimal(100))
				.debitLtinAmount(BigDecimal.ZERO)
				.sctCreditPmntAmount(new BigDecimal(300))
				.sctDebitPmntAmount(new BigDecimal(200))
				.b2bCreditPmntAmount(new BigDecimal(100))
				.b2bDebitPmntAmount(BigDecimal.ZERO)
				.corCreditPmntAmount(new BigDecimal(50))
				.corDebitPmntAmount(new BigDecimal(150))
				.pendingDebitPmntAmount(BigDecimal.ZERO)
				.currency("EUR")
				.build();
		Mockito.when(this.cgsService.getLacDetail(Mockito.any(BigDecimal.class))).thenReturn(cgsLacDetailDto);
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.getCurrentTimestamp()).thenReturn(LocalDateTime.now());
		
		CgsLacDetailDto result = (CgsLacDetailDto) this.controller.getCgsLacDetail(BigDecimal.ONE).getBody().getResponse();
		
		assertThat(result).isNotNull();
		assertThat(result.getLacNumber()).isEqualTo(cgsLacDetailDto.getLacNumber());
		assertThat(result.getSctCreditPmntAmount()).isEqualTo(cgsLacDetailDto.getSctCreditPmntAmount());
		
		verify(this.cgsService, times(1)).getLacDetail(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(6)
	void testGetCgsLacDetailException() {
		
		Mockito.when(this.cgsService.getLacDetail(Mockito.any(BigDecimal.class))).thenThrow(ResourceNotFoundException.class);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.getCgsLacDetail(BigDecimal.ONE);
		});
		
		verify(this.cgsService, times(1)).getLacDetail(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(7)
	void testGetLiquidityTransfers() throws ParseException {
		
		PageableCgsEntryDto p = PageableCgsEntryDto.builder()
				.totalElements(new BigDecimal(2))
				.entries(Arrays.asList(CgsLacEntryDto.builder()
						.entryId(new BigDecimal(1))
						.entryReference("CG2306050000069T")
						.paymentAmount(new BigDecimal(100))
						.currency("EUR")
						.side('C')
						.service("SCT")
						.status("BOOK")
						.settlementDateTime(new Date())
						.additionalInfo("C-FUND")
						.build(), CgsLacEntryDto.builder()
						.entryReference("CG2306050000069V")
						.paymentAmount(new BigDecimal(200))
						.currency("EUR")
						.side('C')
						.service("SCT")
						.status("BOOK")
						.settlementDateTime(new Date())
						.additionalInfo("C-FUND")
						.build())
				)
				.build();
		Mockito.when(this.cgsService.getLiquidityTransfers(Mockito.anyChar(), Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), 
				Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Pageable.class)))
			.thenReturn(p);
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.getCurrentTimestamp()).thenReturn(LocalDateTime.now());
		
		PageableCgsEntryDto result = (PageableCgsEntryDto) this.controller.getLiquidityTransfers('C', "BOOK", new BigDecimal(10), new BigDecimal(100), "2023-06-09", "2023-06-09", "01", 0, 10)
				.getBody().getResponse();
		
		assertThat(result).isNotNull();
		assertThat(result.getEntries()).hasSameSizeAs(p.getEntries());
		assertThat(result.getEntries().get(1).getEntryReference()).isEqualTo(p.getEntries().get(1).getEntryReference());
		
		verify(this.cgsService, times(1)).getLiquidityTransfers(Mockito.anyChar(), Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), 
				Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Pageable.class));
	}
	
	@Test
	@Order(8)
	void testGetLiquidityTransfersResourceNotFoundException() throws ParseException {
		
		Mockito.when(this.cgsService.getLiquidityTransfers(Mockito.anyChar(), Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), 
				Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Pageable.class)))
			.thenThrow(ResourceNotFoundException.class);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.getLiquidityTransfers('C', "BOOK", new BigDecimal(10), new BigDecimal(100), "2023-06-09", "2023-06-09", "01", 0, 10);
		});
		
		verify(this.cgsService, times(1)).getLiquidityTransfers(Mockito.anyChar(), Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), 
				Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Pageable.class));
	}
	
	@Test
	@Order(9)
	void testGetLiquidityTransfersBicompException() throws ParseException {
		
		Mockito.when(this.cgsService.getLiquidityTransfers(Mockito.anyChar(), Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), 
				Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Pageable.class)))
			.thenThrow(ParseException.class);
		
		assertThrows(BicompException.class, () -> {
			this.controller.getLiquidityTransfers('C', "BOOK", new BigDecimal(10), new BigDecimal(100), "2023-06-09", "2023-06-09", "01", 0, 10);
		});
		
		verify(this.cgsService, times(1)).getLiquidityTransfers(Mockito.anyChar(), Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), 
				Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Pageable.class));
	}
	
	@Test
	@Order(10)
	void testGetPayments() throws ParseException {
		
		PageableCgsEntryDto p = PageableCgsEntryDto.builder()
				.totalElements(new BigDecimal(2))
				.entries(Arrays.asList(CgsLacEntryDto.builder()
						.entryId(new BigDecimal(1))
						.entryReference("CSR2306065016070")
						.paymentAmount(new BigDecimal(100))
						.currency("EUR")
						.side('C')
						.service("SCT")
						.status("BOOK")
						.settlementDateTime(new Date())
						.debitor("BCITITMMXXX")
						.creditor("POSOIT22XXX")
						.additionalInfo("LCR-SETTLEDNOTPROVIDED")
						.build(), CgsLacEntryDto.builder()
						.entryReference("CSR2306065016071")
						.paymentAmount(new BigDecimal(200))
						.currency("EUR")
						.side('C')
						.service("SCT")
						.status("BOOK")
						.settlementDateTime(new Date())
						.debitor("BCITITMMXXX")
						.creditor("POSOIT22XXX")
						.additionalInfo("LCR-SETTLEDNOTPROVIDED")
						.build())
				)
				.build();
		Mockito.when(this.cgsService.getPayments(Mockito.anyChar(), Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), 
				Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Pageable.class)))
			.thenReturn(p);
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.getCurrentTimestamp()).thenReturn(LocalDateTime.now());
		
		PageableCgsEntryDto result = (PageableCgsEntryDto) this.controller.getPayments('C', "BOOK", new BigDecimal(10), new BigDecimal(100), "2023-06-09", "2023-06-09", "SCT", "01", 0, 10)
				.getBody().getResponse();
		
		assertThat(result).isNotNull();
		assertThat(result.getEntries()).hasSameSizeAs(p.getEntries());
		assertThat(result.getEntries().get(1).getEntryReference()).isEqualTo(p.getEntries().get(1).getEntryReference());
		
		verify(this.cgsService, times(1)).getPayments(Mockito.anyChar(), Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), 
				Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Pageable.class));
	}
	
	@Test
	@Order(11)
	void testGetPaymentsResourceNotFoundException() throws ParseException {
		
		Mockito.when(this.cgsService.getPayments(Mockito.anyChar(), Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), 
				Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Pageable.class)))
			.thenThrow(ResourceNotFoundException.class);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.getPayments('C', "BOOK", new BigDecimal(10), new BigDecimal(100), "2023-06-09", "2023-06-09", "SCT", "01", 0, 10);
		});
		
		verify(this.cgsService, times(1)).getPayments(Mockito.anyChar(), Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), 
				Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Pageable.class));
	}
	
	@Test
	@Order(12)
	void testGetPaymentsBicompException() throws ParseException {
		
		Mockito.when(this.cgsService.getPayments(Mockito.anyChar(), Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), 
				Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Pageable.class)))
			.thenThrow(ParseException.class);
		
		assertThrows(BicompException.class, () -> {
			this.controller.getPayments('C', "BOOK", new BigDecimal(10), new BigDecimal(100), "2023-06-09", "2023-06-09", "SCT", "01", 0, 10);
		});
		
		verify(this.cgsService, times(1)).getPayments(Mockito.anyChar(), Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), 
				Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Pageable.class));
	}
	
	@Test
	@Order(13)
	void testGetLastCgsBalanceParseException() throws ResourceNotFoundException, ParseException {
		
		Mockito.when(this.cgsService.getCgsLastBalanceByDate(Mockito.anyString())).thenThrow(ParseException.class);
		
		assertThrows(BicompException.class, () -> {
			this.controller.getLastCgsLac("01-09-2023");
		});
		
		verify(this.cgsService, times(1)).getCgsLastBalanceByDate(Mockito.anyString());
	}
	
	@Test
	@Order(14)
	void testGetCgsLacsParseException() throws ResourceNotFoundException, ParseException {
		
		Mockito.when(this.cgsService.getCurrentDateLac(Mockito.anyString())).thenThrow(ParseException.class);
		
		assertThrows(BicompException.class, () -> {
			this.controller.getCgsLacs("01-09-2023");
		});
		
		verify(this.cgsService, times(1)).getCurrentDateLac(Mockito.anyString());
	}
	
	@Test
	@Order(15)
	void testCgsExtemporaryExtractor() throws ParseException, BicompException, IOException {
		
		List<CgsLmrLtinBulkStatementEntry> ltList = (Arrays.asList(CgsLmrLtinBulkStatementEntry.builder()
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
						.build())
				);
		List<CgsLmrPlcrBulkStatementEntry> paymentList = (Arrays.asList(CgsLmrPlcrBulkStatementEntry.builder()
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
						.build())
				);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(LocalDate.now().toDate());
		
		Mockito.when(this.cgsService.extractCgsLiquidityTransfer(Mockito.any(Date.class))).thenReturn(ltList);
		Mockito.when(this.cgsService.extractCgsPayment(Mockito.any(Date.class))).thenReturn(paymentList);
		Mockito.when(this.cgsService.createCgsPaymentFile(Mockito.any(Date.class),Mockito.anyList(), Mockito.anyList()))
			.thenReturn("FILE");
		
		String result = this.controller.cgsExtemporaryExtractor("250729").getBody().getResponse();
		
		assertThat(result).isEqualTo("FILE");
		
		verify(this.dateUtils, times(1)).stringToDate(Mockito.anyString());
		verify(this.cgsService, times(1)).extractCgsLiquidityTransfer(Mockito.any(Date.class));
		verify(this.cgsService, times(1)).extractCgsPayment(Mockito.any(Date.class));
		verify(this.cgsService, times(1)).createCgsPaymentFile(Mockito.any(Date.class),Mockito.anyList(), Mockito.anyList());
	}
	
	@Test
	@Order(16)
	void testCgsExtemporaryExtractorIOException() throws ParseException, BicompException, IOException {
		
		List<CgsLmrLtinBulkStatementEntry> ltList = (Arrays.asList(CgsLmrLtinBulkStatementEntry.builder()
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
						.build())
				);
		List<CgsLmrPlcrBulkStatementEntry> paymentList = (Arrays.asList(CgsLmrPlcrBulkStatementEntry.builder()
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
						.build())
				);
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(LocalDate.now().toDate());
		
		Mockito.when(this.cgsService.extractCgsLiquidityTransfer(Mockito.any(Date.class))).thenReturn(ltList);
		Mockito.when(this.cgsService.extractCgsPayment(Mockito.any(Date.class))).thenReturn(paymentList);
		Mockito.when(this.cgsService.createCgsPaymentFile(Mockito.any(Date.class),Mockito.anyList(), Mockito.anyList()))
			.thenThrow(IOException.class);
		
		assertThrows(BicompException.class, () -> {
			this.controller.cgsExtemporaryExtractor("250729").getBody().getResponse();
		});
		
		verify(this.dateUtils, times(1)).stringToDate(Mockito.anyString());
		verify(this.cgsService, times(1)).extractCgsLiquidityTransfer(Mockito.any(Date.class));
		verify(this.cgsService, times(1)).extractCgsPayment(Mockito.any(Date.class));
		verify(this.cgsService, times(1)).createCgsPaymentFile(Mockito.any(Date.class),Mockito.anyList(), Mockito.anyList());
	}
	
	@Test
	@Order(17)
	void testCgsExtemporaryExtractorBicompException() throws ParseException, BicompException, IOException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(LocalDate.now().toDate());
		
		Mockito.when(this.cgsService.extractCgsLiquidityTransfer(Mockito.any(Date.class))).thenThrow(BicompException.class);
		
		assertThrows(BicompException.class, () -> {
			this.controller.cgsExtemporaryExtractor("250729").getBody().getResponse();
		});
		
		verify(this.dateUtils, times(1)).stringToDate(Mockito.anyString());
		verify(this.cgsService, times(1)).extractCgsLiquidityTransfer(Mockito.any(Date.class));
		verify(this.cgsService, times(0)).extractCgsPayment(Mockito.any(Date.class));
		verify(this.cgsService, times(0)).createCgsPaymentFile(Mockito.any(Date.class),Mockito.anyList(), Mockito.anyList());
	}
	
	@Test
	@Order(18)
	void testCgsExtemporaryExtractorParseException() throws ParseException, BicompException, IOException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenThrow(ParseException.class);
		
		assertThrows(BicompException.class, () -> {
			this.controller.cgsExtemporaryExtractor("250729").getBody().getResponse();
		});
		
		verify(this.dateUtils, times(1)).stringToDate(Mockito.anyString());
		verify(this.cgsService, times(0)).extractCgsLiquidityTransfer(Mockito.any(Date.class));
		verify(this.cgsService, times(0)).extractCgsPayment(Mockito.any(Date.class));
		verify(this.cgsService, times(0)).createCgsPaymentFile(Mockito.any(Date.class),Mockito.anyList(), Mockito.anyList());
	}
	
	@AfterEach
	public void cleanUp() {
		this.mockDateUtils.close();
	}
	
}
