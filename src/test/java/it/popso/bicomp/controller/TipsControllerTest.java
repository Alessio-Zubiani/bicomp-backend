package it.popso.bicomp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import it.popso.bicomp.dto.PageableTipsReportDto;
import it.popso.bicomp.dto.TipsBalanceDto;
import it.popso.bicomp.dto.TipsReportDto;
import it.popso.bicomp.dto.TipsTotalDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.service.TipsService;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class TipsControllerTest {
	
	@Mock
	private TipsService tipsService;
	
	private TipsController controller;
	

	@BeforeEach
	public void setup() {
		this.controller = new TipsController(this.tipsService);
	}
	
	@Test
	@Order(1)
	void testGetLastTipsBalance() throws ResourceNotFoundException, ParseException {
		
		List<TipsBalanceDto> list = Arrays.asList(TipsBalanceDto.builder()
				.id(new BigDecimal(1))
				.fromDateTime(LocalDateTime.now())
				.toDateTime(LocalDateTime.now())
				.closingBalance(new BigDecimal(200))
				.currency("EUR")
				.build()
			);
		Mockito.when(this.tipsService.getTipsLastBalanceByDate(Mockito.anyString())).thenReturn(list);
		
		List<TipsBalanceDto> result = (List<TipsBalanceDto>) this.controller.getLastTipsReport("230901").getBody().getResponse();
		
		assertThat(result).isNotEmpty().hasSameSizeAs(list);
		assertThat(result.get(0).getClosingBalance()).isEqualTo(list.get(0).getClosingBalance());
		
		verify(this.tipsService, times(1)).getTipsLastBalanceByDate(Mockito.anyString());
	}
	
	@Test
	@Order(2)
	void testGetLastTipsBalanceException() throws ResourceNotFoundException, ParseException {
		
		Mockito.when(this.tipsService.getTipsLastBalanceByDate(Mockito.anyString())).thenThrow(ResourceNotFoundException.class);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.getLastTipsReport("230901");
		});
		
		verify(this.tipsService, times(1)).getTipsLastBalanceByDate(Mockito.anyString());
	}
	
	@Test
	@Order(3)
	void testGetTipsDailyReports() throws ResourceNotFoundException, ParseException {
		
		PageableTipsReportDto p = PageableTipsReportDto.builder()
				.reports(Arrays.asList(TipsReportDto.builder()
					.reportId(new BigDecimal(1))
					.fromDateTime(LocalDateTime.now())
					.toDateTime(LocalDateTime.now())
					.closingBalance(new BigDecimal(200))
					.currency("EUR")
					.build(), TipsReportDto.builder()
					.reportId(new BigDecimal(2))
					.fromDateTime(LocalDateTime.now())
					.toDateTime(LocalDateTime.now())
					.closingBalance(new BigDecimal(300))
					.currency("EUR")
					.build()
				))
				.totalElements(new BigDecimal(2))
				.build();
				
		Mockito.when(this.tipsService.getTipsDailyReport(Mockito.anyString(), Mockito.any(Pageable.class))).thenReturn(p);
		
		PageableTipsReportDto result = (PageableTipsReportDto) this.controller.getTipsDailyReports("230901", Optional.of(0)).getBody().getResponse();
		
		assertThat(result.getReports()).isNotEmpty().hasSameSizeAs(p.getReports());
		assertThat(result.getReports().get(1).getClosingBalance()).isEqualTo(p.getReports().get(1).getClosingBalance());
		
		verify(this.tipsService, times(1)).getTipsDailyReport(Mockito.anyString(), Mockito.any(Pageable.class));
	}
	
	@Test
	@Order(4)
	void testGetTipsDailyReportsException() throws ResourceNotFoundException, ParseException {
		
		Mockito.when(this.tipsService.getTipsDailyReport(Mockito.anyString(), Mockito.any(Pageable.class))).thenThrow(ResourceNotFoundException.class);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.getTipsDailyReports("230901", Optional.of(0));
		});
		
		verify(this.tipsService, times(1)).getTipsDailyReport(Mockito.anyString(), Mockito.any(Pageable.class));
	}
	
	@Test
	@Order(5)
	void testGetTipsReportDetail() {
		
		TipsTotalDto tipsTotalDto = TipsTotalDto.builder()
				.settlementDate(LocalDate.now())
				.creditPmntAmount(new BigDecimal(200))
				.debitPmntAmount(new BigDecimal(100))
				.currency("EUR")
				.build();
		Mockito.when(this.tipsService.getTipsReportDetail(Mockito.any(BigDecimal.class))).thenReturn(tipsTotalDto);
		
		TipsTotalDto result = (TipsTotalDto) this.controller.getTipsReportDetail(BigDecimal.ONE).getBody().getResponse();
		
		assertThat(result).isNotNull();
		assertThat(result.getCreditPmntAmount()).isEqualTo(tipsTotalDto.getCreditPmntAmount());
		verify(this.tipsService, times(1)).getTipsReportDetail(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(6)
	void testGetLastTipsBalanceParseException() throws ResourceNotFoundException, ParseException {
		
		Mockito.when(this.tipsService.getTipsLastBalanceByDate(Mockito.anyString())).thenThrow(ParseException.class);
		
		assertThrows(BicompException.class, () -> {
			this.controller.getLastTipsReport("01-09-2023");
		});
		
		verify(this.tipsService, times(1)).getTipsLastBalanceByDate(Mockito.anyString());
	}
	
	@Test
	@Order(7)
	void testGetTipsDailyReportsParseException() throws ResourceNotFoundException, ParseException {
		
		Mockito.when(this.tipsService.getTipsDailyReport(Mockito.anyString(), Mockito.any(Pageable.class))).thenThrow(ParseException.class);
		
		assertThrows(BicompException.class, () -> {
			this.controller.getTipsDailyReports("01-09-2023", Optional.of(0));
		});
		
		verify(this.tipsService, times(1)).getTipsDailyReport(Mockito.anyString(), Mockito.any(Pageable.class));
	}
	
}
