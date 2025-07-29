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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import it.popso.bicomp.dto.CNDetailDto;
import it.popso.bicomp.dto.CNDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.service.CompensazioneNazionaleService;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class CompensazioneNazionaleControllerTest {
	
	@Mock
	private CompensazioneNazionaleService compensazioneNazionaleService;
	
	private CompensazioneNazionaleController controller;
	

	@BeforeEach
	public void setup() {
		this.controller = new CompensazioneNazionaleController(this.compensazioneNazionaleService);
	}
	
	@Test
	@Order(1)
	void testGetLastCompensazioneNazionaleLastSettlement() throws ResourceNotFoundException, ParseException {
		
		List<CNDto> list = Arrays.asList(CNDto.builder()
				.cycleId(BigDecimal.ONE)
				.cycleNumber("1")
				.cycleDateTime(LocalDateTime.now())
				.cycleAmount(new BigDecimal(100))
				.currency("EUR")
				.build()
			);
		Mockito.when(this.compensazioneNazionaleService.getCurrentDateCompensazioneNazionaleLastSettlement(Mockito.anyString())).thenReturn(list);
		
		List<CNDto> result = (List<CNDto>) this.controller.getLastCompensazioneNazionaleSettlement("01-09-2023").getBody().getResponse();
		
		assertThat(result).isNotEmpty().hasSameSizeAs(list);
		assertThat(result.get(0).getCycleNumber()).isEqualTo(list.get(0).getCycleNumber());
		
		verify(this.compensazioneNazionaleService, times(1)).getCurrentDateCompensazioneNazionaleLastSettlement(Mockito.anyString());
	}
	
	@Test
	@Order(2)
	void testGetLastCompensazioneNazionaleLastSettlementNotFound() throws ResourceNotFoundException, ParseException {
		
		Mockito.when(this.compensazioneNazionaleService.getCurrentDateCompensazioneNazionaleLastSettlement(Mockito.anyString())).thenThrow(ResourceNotFoundException.class);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.getLastCompensazioneNazionaleSettlement("01-09-2023");
		});
		
		verify(this.compensazioneNazionaleService, times(1)).getCurrentDateCompensazioneNazionaleLastSettlement(Mockito.anyString());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(3)
	void testCompensazioneNazionaleSettlement() throws ResourceNotFoundException, ParseException {
		
		List<CNDto> list = Arrays.asList(CNDto.builder()
				.cycleId(BigDecimal.ONE)
				.cycleNumber("1")
				.cycleDateTime(LocalDateTime.now())
				.cycleAmount(new BigDecimal(100))
				.currency("EUR")
				.build(), CNDto.builder()
				.cycleId(BigDecimal.ONE)
				.cycleNumber("2")
				.cycleDateTime(LocalDateTime.now())
				.cycleAmount(new BigDecimal(200))
				.currency("EUR")
				.build()
			);
		Mockito.when(this.compensazioneNazionaleService.getCurrentDateCompensazioneNazionaleSettlement(Mockito.anyString())).thenReturn(list);
		
		List<CNDto> result = (List<CNDto>) this.controller.getCompensazioneNazionaleSettlementOrDetail("01-09-2023", Optional.empty()).getBody().getResponse();
		
		assertThat(result).isNotEmpty().hasSameSizeAs(list);
		assertThat(result.get(1).getCycleNumber()).isEqualTo(list.get(1).getCycleNumber());
		assertThat(result.get(1).getCycleAmount()).isEqualTo(list.get(1).getCycleAmount());
		
		verify(this.compensazioneNazionaleService, times(1)).getCurrentDateCompensazioneNazionaleSettlement(Mockito.anyString());
	}
	
	@Test
	@Order(4)
	void testCompensazioneNazionaleSettlementNotFound() throws ResourceNotFoundException, ParseException {
		
		Mockito.when(this.compensazioneNazionaleService.getCurrentDateCompensazioneNazionaleSettlement(Mockito.anyString())).thenThrow(ResourceNotFoundException.class);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.getCompensazioneNazionaleSettlementOrDetail("01-09-2023", Optional.empty());
		});
		
		verify(this.compensazioneNazionaleService, times(1)).getCurrentDateCompensazioneNazionaleSettlement(Mockito.anyString());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Order(5)
	void testCurrentDateCompensazioneNazionaleDettaglioSettlement() throws ResourceNotFoundException, ParseException {
		
		List<CNDetailDto> list = Arrays.asList(CNDetailDto.builder()
				.stanCycle("1")
				.stanTipoMessaggio("340")
				.stanTipoVoce("950")
				.stanDescrizioneVoce("REGOLAMENTI SEPA")
				.stanSettlementDate(LocalDate.now())
				.stanSettlementDateTime(LocalDateTime.now())
				.stanSettledCreditAmount(new BigDecimal(100))
				.stanSettledDebitAmount(new BigDecimal(50))
				.currency("EUR")
				.build(), CNDetailDto.builder()
				.stanCycle("2")
				.stanTipoMessaggio("340")
				.stanTipoVoce("950")
				.stanDescrizioneVoce("REGOLAMENTI SEPA")
				.stanSettlementDate(LocalDate.now())
				.stanSettlementDateTime(LocalDateTime.now())
				.stanSettledCreditAmount(new BigDecimal(200))
				.stanSettledDebitAmount(new BigDecimal(50))
				.currency("EUR")
				.build()
			);
		Mockito.when(this.compensazioneNazionaleService.getCurrentDateCompensazioneNazionaleDettaglioSettlement(Mockito.anyString(), Mockito.anyString()))
			.thenReturn(list);
		
		List<CNDetailDto> result = (List<CNDetailDto>) this.controller.getCompensazioneNazionaleSettlementOrDetail("01-09-2023", Optional.of("1")).getBody().getResponse();
		
		assertThat(result).isNotEmpty().hasSameSizeAs(list);
		assertThat(result.get(0).getStanCycle()).isEqualTo(list.get(0).getStanCycle());
		assertThat(result.get(1).getStanSettledCreditAmount()).isEqualTo(list.get(1).getStanSettledCreditAmount());
		
		verify(this.compensazioneNazionaleService, times(1)).getCurrentDateCompensazioneNazionaleDettaglioSettlement(Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(6)
	void testCurrentDateCompensazioneNazionaleDettaglioSettlementNotFound() throws ResourceNotFoundException, ParseException {
		
		Mockito.when(this.compensazioneNazionaleService.getCurrentDateCompensazioneNazionaleDettaglioSettlement(Mockito.anyString(), Mockito.anyString())).thenThrow(ResourceNotFoundException.class);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.controller.getCompensazioneNazionaleSettlementOrDetail("01-09-2023", Optional.of("1"));
		});
		
		verify(this.compensazioneNazionaleService, times(1)).getCurrentDateCompensazioneNazionaleDettaglioSettlement(Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(7)
	void testGetLastCompensazioneNazionaleSettlementParseException() throws ResourceNotFoundException, ParseException {
		
		Mockito.when(this.compensazioneNazionaleService.getCurrentDateCompensazioneNazionaleLastSettlement(Mockito.anyString())).thenThrow(ParseException.class);
		
		assertThrows(BicompException.class, () -> {
			this.controller.getLastCompensazioneNazionaleSettlement("01-09-2023");
		});
		
		verify(this.compensazioneNazionaleService, times(1)).getCurrentDateCompensazioneNazionaleLastSettlement(Mockito.anyString());
	}
	
	@Test
	@Order(8)
	void testCompensazioneNazionaleSettlementParseException() throws ResourceNotFoundException, ParseException {
		
		Mockito.when(this.compensazioneNazionaleService.getCurrentDateCompensazioneNazionaleSettlement(Mockito.anyString())).thenThrow(ParseException.class);
		
		assertThrows(BicompException.class, () -> {
			this.controller.getCompensazioneNazionaleSettlementOrDetail("01-09-2023", Optional.empty());
		});
		
		verify(this.compensazioneNazionaleService, times(1)).getCurrentDateCompensazioneNazionaleSettlement(Mockito.anyString());
	}
	
	@Test
	@Order(9)
	void testCurrentDateCompensazioneNazionaleDettaglioSettlementParseException() throws ResourceNotFoundException, ParseException {
		
		Mockito.when(this.compensazioneNazionaleService.getCurrentDateCompensazioneNazionaleDettaglioSettlement(Mockito.anyString(), Mockito.anyString())).thenThrow(ParseException.class);
		
		assertThrows(BicompException.class, () -> {
			this.controller.getCompensazioneNazionaleSettlementOrDetail("01-09-2023", Optional.of("1"));
		});
		
		verify(this.compensazioneNazionaleService, times(1)).getCurrentDateCompensazioneNazionaleDettaglioSettlement(Mockito.anyString(), Mockito.anyString());
	}

}
