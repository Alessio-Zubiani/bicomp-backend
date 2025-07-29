package it.popso.bicomp.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import it.popso.bicomp.dto.CNDetailDto;
import it.popso.bicomp.dto.CNDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.properties.BicompConfig;
import it.popso.bicomp.service.impl.CompensazioneNazionaleServiceImpl;
import it.popso.bicomp.slt.model.Rstbproc;
import it.popso.bicomp.slt.model.RstbprocPK;
import it.popso.bicomp.slt.model.Rstbstan;
import it.popso.bicomp.slt.model.RstbstanPK;
import it.popso.bicomp.slt.repository.RstbprocRepository;
import it.popso.bicomp.slt.repository.RstbstanRepository;
import it.popso.bicomp.t2c.model.EurRtgsOperazioni;
import it.popso.bicomp.t2c.repository.EurRtgsOperazioniRepository;
import it.popso.bicomp.utils.DateUtils;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class CompensazioneNazionaleServiceImplTest {
	
	@Mock
	private EurRtgsOperazioniRepository eurRtgsOperazioniRepository;
	
	@Mock
	private RstbstanRepository rstbstanRepository;
	
	@Mock
	private RstbprocRepository rstbprocRepository;
	
	private BicompConfig config;
	private CompensazioneNazionaleServiceImpl service;
	private DateUtils dateUtils;
	private MockedStatic<DateUtils> mockDateUtils;
	
	
	@BeforeEach
    public void setup() {
		this.config = new BicompConfig();
		this.config.setCnCodiceFamiglia("005180");
		
		this.dateUtils = Mockito.mock(DateUtils.class);
		this.mockDateUtils = Mockito.mockStatic(DateUtils.class);
		
		this.service = new CompensazioneNazionaleServiceImpl(this.config, this.eurRtgsOperazioniRepository, this.rstbstanRepository, 
				this.rstbprocRepository);
	}
	
	@Test
	@Order(1)
	void testGetCurrentDateCompensazioneNazionaleLastSettlementAllCycle() throws ResourceNotFoundException, BicompException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		List<EurRtgsOperazioni> list = Arrays.asList(EurRtgsOperazioni.builder()
				.bicIstituto("POSOIT22")
				.canale("RTGS")
				.codiContoRego("RITEURPOSOIT22XXXRTGS")
				.codiceControparte("BITAITRRCOM")
				.codiceDivisa("EUR")
				.codiceFamiglia("005180")
				.dataRegolamento(new Date())
				.idenOperazione("COM8271591215018")
				.importoOperazione(new BigDecimal(400))
				.stato("10")
				.tipoMessaggio("camt.054")
				.informazioniRmt("")
				.build()
			);
		
		Mockito.when(this.eurRtgsOperazioniRepository.findLastCompensazioneNazionaleSettlement(Mockito.any(Date.class), Mockito.anyString(), Mockito.any(PageRequest.class)))
			.thenReturn(list);
		
		List<CNDto> result = this.service.getCurrentDateCompensazioneNazionaleLastSettlement("01-09-2023");
		assertThat(result).isNotEmpty().hasSameSizeAs(list);
		assertThat(result.get(0).getCycleNumber()).isEqualTo("3");
		assertThat(result.get(0).getCycleAmount()).isEqualTo(list.get(0).getImportoOperazione());
		verify(this.eurRtgsOperazioniRepository, times(1)).findLastCompensazioneNazionaleSettlement(Mockito.any(Date.class), Mockito.anyString(), Mockito.any(PageRequest.class));
	}
	
	@Test
	@Order(2)
	void testGetCurrentDateCompensazioneNazionaleLastSettlementNotFound() throws ResourceNotFoundException, BicompException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		List<EurRtgsOperazioni> list = Arrays.asList();
		Mockito.when(this.eurRtgsOperazioniRepository.findLastCompensazioneNazionaleSettlement(Mockito.any(Date.class), Mockito.anyString(), Mockito.any(PageRequest.class)))
			.thenReturn(list);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getCurrentDateCompensazioneNazionaleLastSettlement("01-09-2023");
		});

		verify(this.eurRtgsOperazioniRepository, times(1)).findLastCompensazioneNazionaleSettlement(Mockito.any(Date.class), Mockito.anyString(), Mockito.any(PageRequest.class));
		verify(this.dateUtils, times(0)).dateToLocalDateTime(Mockito.any(Date.class));
	}
	
	@Test
	@Order(3)
	void testGetCurrentDateCompensazioneNazionaleSettlement() throws ResourceNotFoundException, BicompException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		List<EurRtgsOperazioni> list = Arrays.asList(EurRtgsOperazioni.builder()
				.bicIstituto("POSOIT22")
				.canale("RTGS")
				.codiContoRego("RITEURPOSOIT22XXXRTGS")
				.codiceControparte("BITAITRRCOM")
				.codiceDivisa("EUR")
				.codiceFamiglia("005180")
				.dataRegolamento(new Date())
				.idenOperazione("COM8271590715018")
				.importoOperazione(new BigDecimal(200))
				.stato("10")
				.tipoMessaggio("camt.054")
				.informazioniRmt("")
				.build()
			);
		
		Mockito.when(this.eurRtgsOperazioniRepository.findCompensazioneNazionaleSettlement(Mockito.any(Date.class), Mockito.anyString()))
			.thenReturn(list);
		
		List<CNDto> result = this.service.getCurrentDateCompensazioneNazionaleSettlement("01-09-2023");
		assertThat(result).isNotEmpty().hasSameSizeAs(list);
		assertThat(result.get(0).getCycleNumber()).isEqualTo("3");
		assertThat(result.get(0).getCycleAmount()).isEqualTo(list.get(0).getImportoOperazione());
		verify(this.eurRtgsOperazioniRepository, times(1)).findCompensazioneNazionaleSettlement(Mockito.any(Date.class), Mockito.anyString());
	}
	
	@Test
	@Order(4)
	void testGetCurrentDateCompensazioneNazionaleSettlementNotFound() throws ResourceNotFoundException, BicompException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		List<EurRtgsOperazioni> list = Arrays.asList();
		Mockito.when(this.eurRtgsOperazioniRepository.findCompensazioneNazionaleSettlement(Mockito.any(Date.class), Mockito.anyString()))
			.thenReturn(list);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getCurrentDateCompensazioneNazionaleSettlement("01-09-2023");
		});

		verify(this.eurRtgsOperazioniRepository, times(1)).findCompensazioneNazionaleSettlement(Mockito.any(Date.class), Mockito.anyString());
		verify(this.dateUtils, times(0)).dateToLocalDateTime(Mockito.any(Date.class));
	}
	
	@Test
	@Order(5)
	void testGetCurrentDateCompensazioneNazionaleDettaglioSettlement() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		List<Rstbstan> list = Arrays.asList(Rstbstan.builder()
				.id(RstbstanPK.builder()
						.stanData(new Date())
						.stanIstituto("05696")
						.stanMessaggio("340")
						.stanNumeroCicli(new BigDecimal(0))
						.stanTipoRete("S")
						.stanTipoVoce("905")
						.build())
				.stanTimestamp(new Date())
				.stanImpoDefDa(new BigDecimal(100))
				.stanImpoDefAv(new BigDecimal(200))
				.build(), Rstbstan.builder()
				.id(RstbstanPK.builder()
						.stanData(new Date())
						.stanIstituto("05696")
						.stanMessaggio("340")
						.stanNumeroCicli(new BigDecimal(3))
						.stanTipoRete("S")
						.stanTipoVoce("905")
						.build())
				.stanTimestamp(new Date())
				.stanImpoDefDa(new BigDecimal(50))
				.stanImpoDefAv(new BigDecimal(300))
				.build(), Rstbstan.builder()
				.id(RstbstanPK.builder()
						.stanData(new Date())
						.stanIstituto("05696")
						.stanMessaggio("340")
						.stanNumeroCicli(new BigDecimal(3))
						.stanTipoRete("S")
						.stanTipoVoce("730")
						.build())
				.stanTimestamp(new Date())
				.stanImpoDefDa(new BigDecimal(300))
				.stanImpoDefAv(new BigDecimal(400))
				.build());
		
		Optional<Rstbproc> o = Optional.of(Rstbproc.builder()
				.id(RstbprocPK.builder()
						.procTipoVoce("905")
						.build())
				.procIstituto("05696")
				.procDescrizione("REGOLAMENTI SEPA")
				.build());
		
		Mockito.when(this.rstbstanRepository.findByStanDataAndStanMessaggioAndStanNumeroCicli(Mockito.any(Date.class), Mockito.anyString(), Mockito.any(BigDecimal.class)))
			.thenReturn(list);
		Mockito.when(this.rstbprocRepository.findByProcTipoVoce(Mockito.anyString())).thenReturn(o);
		
		List<CNDetailDto> result = this.service.getCurrentDateCompensazioneNazionaleDettaglioSettlement("1", "01-09-2023");
		assertThat(result).isNotEmpty().hasSize(3);
		assertThat(result.get(0).getStanCycle()).isEqualTo("1");
		assertThat(result.get(1).getStanSettledCreditAmount()).isEqualTo(list.get(1).getStanImpoDefAv().divide(new BigDecimal(100)));
		
		verify(this.rstbstanRepository, times(1)).findByStanDataAndStanMessaggioAndStanNumeroCicli(Mockito.any(Date.class), Mockito.anyString(), Mockito.any(BigDecimal.class));
		verify(this.rstbprocRepository, times(3)).findByProcTipoVoce(Mockito.anyString());
	}
	
	@Test
	@Order(6)
	void testGetCurrentDateCompensazioneNazionaleDettaglioSettlementRstbstanNotFound() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		List<Rstbstan> list = Arrays.asList();
		Mockito.when(this.rstbstanRepository.findByStanDataAndStanMessaggioAndStanNumeroCicli(Mockito.any(Date.class), Mockito.anyString(), Mockito.any(BigDecimal.class)))
			.thenReturn(list);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getCurrentDateCompensazioneNazionaleDettaglioSettlement("1", "01-09-2023");
		});
		
		verify(this.rstbstanRepository, times(1)).findByStanDataAndStanMessaggioAndStanNumeroCicli(Mockito.any(Date.class), Mockito.anyString(), Mockito.any(BigDecimal.class));
		verify(this.rstbprocRepository, times(0)).findByProcTipoVoce(Mockito.anyString());
	}
	
	@Test
	@Order(7)
	void testGetCurrentDateCompensazioneNazionaleDettaglioSettlementRstbprocNotFound() throws ResourceNotFoundException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenReturn(new Date());
		
		List<Rstbstan> list = Arrays.asList(Rstbstan.builder()
				.id(RstbstanPK.builder()
						.stanData(new Date())
						.stanIstituto("05696")
						.stanMessaggio("340")
						.stanNumeroCicli(new BigDecimal(0))
						.stanTipoRete("S")
						.stanTipoVoce("905")
						.build())
				.stanTimestamp(new Date())
				.stanImpoDefDa(new BigDecimal(100))
				.stanImpoDefAv(new BigDecimal(200))
				.build(), Rstbstan.builder()
				.id(RstbstanPK.builder()
						.stanData(new Date())
						.stanIstituto("05696")
						.stanMessaggio("340")
						.stanNumeroCicli(new BigDecimal(3))
						.stanTipoRete("S")
						.stanTipoVoce("905")
						.build())
				.stanTimestamp(new Date())
				.stanImpoDefDa(new BigDecimal(50))
				.stanImpoDefAv(new BigDecimal(300))
				.build(), Rstbstan.builder()
				.id(RstbstanPK.builder()
						.stanData(new Date())
						.stanIstituto("05696")
						.stanMessaggio("340")
						.stanNumeroCicli(new BigDecimal(3))
						.stanTipoRete("S")
						.stanTipoVoce("730")
						.build())
				.stanTimestamp(new Date())
				.stanImpoDefDa(new BigDecimal(300))
				.stanImpoDefAv(new BigDecimal(400))
				.build());
		
		Optional<Rstbproc> o = Optional.empty();
		Mockito.when(this.rstbstanRepository.findByStanDataAndStanMessaggioAndStanNumeroCicli(Mockito.any(Date.class), Mockito.anyString(), Mockito.any(BigDecimal.class)))
			.thenReturn(list);
		Mockito.when(this.rstbprocRepository.findByProcTipoVoce(Mockito.anyString())).thenReturn(o);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.getCurrentDateCompensazioneNazionaleDettaglioSettlement("1", "01-09-2023");
		});
		
		verify(this.rstbstanRepository, times(1)).findByStanDataAndStanMessaggioAndStanNumeroCicli(Mockito.any(Date.class), Mockito.anyString(), Mockito.any(BigDecimal.class));
		verify(this.rstbprocRepository, times(1)).findByProcTipoVoce(Mockito.anyString());
	}
	
	@Test
	@Order(8)
	void testGetCurrentDateCompensazioneNazionaleLastSettlementParseException() throws ResourceNotFoundException, BicompException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenThrow(ParseException.class);
		
		assertThrows(ParseException.class, () -> {
			this.service.getCurrentDateCompensazioneNazionaleLastSettlement("01-09-2023");
		});

		verify(this.eurRtgsOperazioniRepository, times(0)).findLastCompensazioneNazionaleSettlement(Mockito.any(Date.class), Mockito.anyString(), Mockito.any(PageRequest.class));
	}
	
	@Test
	@Order(9)
	void testGetCurrentDateCompensazioneNazionaleSettlementParseException() throws ResourceNotFoundException, BicompException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenThrow(ParseException.class);
		
		assertThrows(ParseException.class, () -> {
			this.service.getCurrentDateCompensazioneNazionaleSettlement("01-09-2023");
		});

		verify(this.eurRtgsOperazioniRepository, times(0)).findLastCompensazioneNazionaleSettlement(Mockito.any(Date.class), Mockito.anyString(), Mockito.any(PageRequest.class));
	}
	
	@Test
	@Order(10)
	void testGetCurrentDateCompensazioneNazionaleDettaglioSettlementParseException() throws ResourceNotFoundException, BicompException, ParseException {
		
		this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
		Mockito.when(this.dateUtils.stringToDate(Mockito.anyString())).thenThrow(ParseException.class);
		
		assertThrows(ParseException.class, () -> {
			this.service.getCurrentDateCompensazioneNazionaleDettaglioSettlement("1", "01-09-2023");
		});

		verify(this.eurRtgsOperazioniRepository, times(0)).findLastCompensazioneNazionaleSettlement(Mockito.any(Date.class), Mockito.anyString(), Mockito.any(PageRequest.class));
	}
	
	@Test
	@Order(11)
	void testCompensazioneNazionaleServiceImplEquals() {
		
		CompensazioneNazionaleServiceImpl test = new CompensazioneNazionaleServiceImpl(this.config, this.eurRtgsOperazioniRepository, this.rstbstanRepository, 
				this.rstbprocRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isTrue();
	}
	
	@Test
	@Order(12)
	void testCompensazioneNazionaleServiceImplEqualsSameInstance() {
		
		assertThat(this.service.equals(this.service)).isTrue();
	}
	
	@Test
	@Order(13)
	void testCompensazioneNazionaleServiceImplNotEqualsNotSameInstanceType() {
		
		assertThat(this.service.equals("test")).isFalse();
	}
	
	@Test
	@Order(14)
	void testCompensazioneNazionaleServiceImplEqualsNull() {
		
		this.service = new CompensazioneNazionaleServiceImpl(null, null, null, null);
		CompensazioneNazionaleServiceImpl test = new CompensazioneNazionaleServiceImpl(null, null, null, null);
		boolean result = this.service.equals(test);
		assertThat(result).isTrue();
	}
	
	@Test
	@Order(15)
	void testCompensazioneNazionaleServiceImplNotNull() {
		
		this.service = new CompensazioneNazionaleServiceImpl(null, null, null, null);
		CompensazioneNazionaleServiceImpl test = new CompensazioneNazionaleServiceImpl(this.config, this.eurRtgsOperazioniRepository, this.rstbstanRepository, 
				this.rstbprocRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(16)
	void testCompensazioneNazionaleServiceImplNotEqualsEurRtgsNull() {
		
		CompensazioneNazionaleServiceImpl test = new CompensazioneNazionaleServiceImpl(this.config, null, this.rstbstanRepository, this.rstbprocRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(17)
	void testCompensazioneNazionaleServiceImplNotEqualsStanNull() {
		
		CompensazioneNazionaleServiceImpl test = new CompensazioneNazionaleServiceImpl(this.config, this.eurRtgsOperazioniRepository, null, this.rstbprocRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(18)
	void testCompensazioneNazionaleServiceImplNotEqualsProcNull() {
		
		CompensazioneNazionaleServiceImpl test = new CompensazioneNazionaleServiceImpl(this.config, this.eurRtgsOperazioniRepository, this.rstbstanRepository, null);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(19)
	void testCompensazioneNazionaleServiceImplNotEqualsConfigNull() {
		
		CompensazioneNazionaleServiceImpl test = new CompensazioneNazionaleServiceImpl(null, this.eurRtgsOperazioniRepository, this.rstbstanRepository, this.rstbprocRepository);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(20)
	void testCompensazioneNazionaleServiceImplEqualsHashCode() {
		
		CompensazioneNazionaleServiceImpl test = new CompensazioneNazionaleServiceImpl(this.config, this.eurRtgsOperazioniRepository, this.rstbstanRepository, 
				this.rstbprocRepository);
		int result = this.service.hashCode();
		assertThat(result).isEqualTo(test.hashCode());
	}
	
	@Test
	@Order(21)
	void testCompensazioneNazionaleServiceImplNotEqualsHashCode() {
		
		CompensazioneNazionaleServiceImpl test = new CompensazioneNazionaleServiceImpl(null, null, null, null);
		int result = this.service.hashCode();
		assertThat(result).isNotEqualTo(test.hashCode());
	}
	
	@AfterEach
	public void cleanUp() {
		this.mockDateUtils.close();
	}

}
