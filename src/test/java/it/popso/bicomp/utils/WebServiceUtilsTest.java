package it.popso.bicomp.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import it.popso.ws.bicomp.lettura.schema.v1.ObjectFactory;
import it.popso.ws.bicomp.lettura.schema.v1.SaldiCgsReadRequest;
import it.popso.ws.bicomp.lettura.schema.v1.SaldiCgsReadRequestData;
import it.popso.ws.bicomp.lettura.schema.v1.SaldiTipsReadRequest;
import it.popso.ws.bicomp.lettura.schema.v1.SaldiTipsReadRequestData;
import it.popso.ws.bicomp.lettura.schema.v1.UltimoPagamentoTipsReadRequest;
import it.popso.ws.bicomp.lettura.schema.v1.UltimoPagamentoTipsReadRequestData;
import it.popso.ws.bicomp.lettura.schema.v1.UltimoSaldoCgsReadRequest;
import it.popso.ws.bicomp.lettura.schema.v1.UltimoSaldoCgsReadRequestData;
import it.popso.ws.bicomp.scrittura.schema.v1.ReportTipsUpdateRequest;
import it.popso.ws.bicomp.scrittura.schema.v1.ReportTipsUpdateRequestData;
import it.popso.ws.common.testata.fault.schema.v11.ApplicationFault;
import it.popso.ws.common.testata.fault.schema.v11.DatiTestataFault;
import it.popso.ws.common.testata.schema.v11.IdOperazione;
import it.popso.ws.common.testata.schema.v11.IdentificazioneSoggetti;
import it.popso.ws.common.testata.schema.v11.TestataDiContesto;
import it.popso.ws.common.testata.schema.v11.TestataInput;
import it.popso.ws.common.testata.schema.v11.TestataTecnicaInput;
import it.popso.ws.common.testata.tipi.schema.v11.CodiceCanale;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class WebServiceUtilsTest {
	
	private static final String ERROR_MESSAGE = "Error";
	
	private WebServiceUtils webServiceUtils;
	private ObjectFactory letturaFactory = new ObjectFactory();
	private it.popso.ws.bicomp.scrittura.schema.v1.ObjectFactory scritturaFactory = new it.popso.ws.bicomp.scrittura.schema.v1.ObjectFactory();
	
	
	@BeforeEach
	public void setup() {
		
		this.webServiceUtils = WebServiceUtils.webServiceUtils();
	}
	
	@Test
	@Order(1)
	void testCreateDatiTestataFaultSaldiCgsReadRequest() {
		
		SaldiCgsReadRequestData requestData = this.letturaFactory.createSaldiCgsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		
		SaldiCgsReadRequest request = this.letturaFactory.createSaldiCgsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setSaldiCgsReadRequestData(requestData);
		
		DatiTestataFault fault = this.webServiceUtils.createDatiTestataFault(request, 400, ERROR_MESSAGE);
		
		assertThat(fault.getMessaggio()).isEqualTo(ERROR_MESSAGE);
		assertThat(fault.getTestata().getTestataTecnica().getIdOperazioneRichiesta().getCodiceApplicazioneChiamante())
			.isEqualTo(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante());
		assertThat(fault.getTestata().getTestataTecnica().getCodiceOperazioneRisposta()).isEqualTo("SALDI_CGS_READ");
	}
	
	@Test
	@Order(2)
	void testCreateDatiTestataFaultSaldiTipsReadRequest() {
		
		SaldiTipsReadRequestData requestData = this.letturaFactory.createSaldiTipsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		
		SaldiTipsReadRequest request = this.letturaFactory.createSaldiTipsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setSaldiTipsReadRequestData(requestData);
		
		DatiTestataFault fault = this.webServiceUtils.createDatiTestataFault(request, 400, ERROR_MESSAGE);
		
		assertThat(fault.getMessaggio()).isEqualTo(ERROR_MESSAGE);
		assertThat(fault.getTestata().getTestataTecnica().getIdOperazioneRichiesta().getCodiceApplicazioneChiamante())
			.isEqualTo(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante());
		assertThat(fault.getTestata().getTestataTecnica().getCodiceOperazioneRisposta()).isEqualTo("SALDI_TIPS_READ");
	}
	
	@Test
	@Order(3)
	void testCreateDatiTestataFaultUltimoSaldoCgsReadRequest() {
		
		UltimoSaldoCgsReadRequestData requestData = this.letturaFactory.createUltimoSaldoCgsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		
		UltimoSaldoCgsReadRequest request = this.letturaFactory.createUltimoSaldoCgsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setUltimoSaldoCgsReadRequestData(requestData);
		
		DatiTestataFault fault = this.webServiceUtils.createDatiTestataFault(request, 400, ERROR_MESSAGE);
		
		assertThat(fault.getMessaggio()).isEqualTo(ERROR_MESSAGE);
		assertThat(fault.getTestata().getTestataTecnica().getIdOperazioneRichiesta().getCodiceApplicazioneChiamante())
			.isEqualTo(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante());
		assertThat(fault.getTestata().getTestataTecnica().getCodiceOperazioneRisposta()).isEqualTo("ULTIMO_SALDO_CGS_READ");
	}
	
	@Test
	@Order(4)
	void testCreateDatiTestataFaultUltimoPagamentoTipsReadRequest() {
		
		UltimoPagamentoTipsReadRequestData requestData = this.letturaFactory.createUltimoPagamentoTipsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		
		UltimoPagamentoTipsReadRequest request = this.letturaFactory.createUltimoPagamentoTipsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setUltimoPagamentoTipsReadRequestData(requestData);
		
		DatiTestataFault fault = this.webServiceUtils.createDatiTestataFault(request, 400, ERROR_MESSAGE);
		
		assertThat(fault.getMessaggio()).isEqualTo(ERROR_MESSAGE);
		assertThat(fault.getTestata().getTestataTecnica().getIdOperazioneRichiesta().getCodiceApplicazioneChiamante())
			.isEqualTo(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante());
		assertThat(fault.getTestata().getTestataTecnica().getCodiceOperazioneRisposta()).isEqualTo("ULTIMO_PAGAMENTO_TIPS_READ");
	}
	
	@Test
	@Order(5)
	void testCreateDatiTestataFaultReportTipsUpdateRequest() {
		
		ReportTipsUpdateRequestData requestData = this.scritturaFactory.createReportTipsUpdateRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		
		ReportTipsUpdateRequest request = this.scritturaFactory.createReportTipsUpdateRequest();
		request.setTestata(this.createTestataInput());
		request.setReportTipsUpdateRequestData(requestData);
		
		DatiTestataFault fault = this.webServiceUtils.createDatiTestataFault(request, 400, ERROR_MESSAGE);
		
		assertThat(fault.getMessaggio()).isEqualTo(ERROR_MESSAGE);
		assertThat(fault.getTestata().getTestataTecnica().getIdOperazioneRichiesta().getCodiceApplicazioneChiamante())
			.isEqualTo(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante());
		assertThat(fault.getTestata().getTestataTecnica().getCodiceOperazioneRisposta()).isEqualTo("TIPS_REPORT_UPDATE");
	}
	
	@Test
	@Order(6)
	void testCreateApplicationFaultSaldiCgsReadRequest() {
		
		SaldiCgsReadRequestData requestData = this.letturaFactory.createSaldiCgsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		
		SaldiCgsReadRequest request = this.letturaFactory.createSaldiCgsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setSaldiCgsReadRequestData(requestData);
		
		ApplicationFault fault = this.webServiceUtils.createApplicationFault(request, 400, ERROR_MESSAGE);
		
		assertThat(fault.getMessaggio()).isEqualTo(ERROR_MESSAGE);
		assertThat(fault.getTestata().getTestataTecnica().getIdOperazioneRichiesta().getCodiceApplicazioneChiamante())
			.isEqualTo(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante());
		assertThat(fault.getTestata().getTestataTecnica().getCodiceOperazioneRisposta()).isEqualTo("SALDI_CGS_READ");
	}
	
	@Test
	@Order(7)
	void testCreateApplicationFaultSaldiTipsReadRequest() {
		
		SaldiTipsReadRequestData requestData = this.letturaFactory.createSaldiTipsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		
		SaldiTipsReadRequest request = this.letturaFactory.createSaldiTipsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setSaldiTipsReadRequestData(requestData);
		
		ApplicationFault fault = this.webServiceUtils.createApplicationFault(request, 400, ERROR_MESSAGE);
		
		assertThat(fault.getMessaggio()).isEqualTo(ERROR_MESSAGE);
		assertThat(fault.getTestata().getTestataTecnica().getIdOperazioneRichiesta().getCodiceApplicazioneChiamante())
			.isEqualTo(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante());
		assertThat(fault.getTestata().getTestataTecnica().getCodiceOperazioneRisposta()).isEqualTo("SALDI_TIPS_READ");
	}
	
	@Test
	@Order(8)
	void testCreateApplicationFaultUltimoSaldoCgsReadRequest() {
		
		UltimoSaldoCgsReadRequestData requestData = this.letturaFactory.createUltimoSaldoCgsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		
		UltimoSaldoCgsReadRequest request = this.letturaFactory.createUltimoSaldoCgsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setUltimoSaldoCgsReadRequestData(requestData);
		
		ApplicationFault fault = this.webServiceUtils.createApplicationFault(request, 400, ERROR_MESSAGE);
		
		assertThat(fault.getMessaggio()).isEqualTo(ERROR_MESSAGE);
		assertThat(fault.getTestata().getTestataTecnica().getIdOperazioneRichiesta().getCodiceApplicazioneChiamante())
			.isEqualTo(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante());
		assertThat(fault.getTestata().getTestataTecnica().getCodiceOperazioneRisposta()).isEqualTo("ULTIMO_SALDO_CGS_READ");
	}
	
	@Test
	@Order(9)
	void testCreateApplicationFaultUltimoPagamentoTipsReadRequest() {
		
		UltimoPagamentoTipsReadRequestData requestData = this.letturaFactory.createUltimoPagamentoTipsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		
		UltimoPagamentoTipsReadRequest request = this.letturaFactory.createUltimoPagamentoTipsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setUltimoPagamentoTipsReadRequestData(requestData);
		
		ApplicationFault fault = this.webServiceUtils.createApplicationFault(request, 400, ERROR_MESSAGE);
		
		assertThat(fault.getMessaggio()).isEqualTo(ERROR_MESSAGE);
		assertThat(fault.getTestata().getTestataTecnica().getIdOperazioneRichiesta().getCodiceApplicazioneChiamante())
			.isEqualTo(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante());
		assertThat(fault.getTestata().getTestataTecnica().getCodiceOperazioneRisposta()).isEqualTo("ULTIMO_PAGAMENTO_TIPS_READ");
	}
	
	@Test
	@Order(10)
	void testCreateApplicationFaultReportTipsUpdateRequest() {
		
		ReportTipsUpdateRequestData requestData = this.scritturaFactory.createReportTipsUpdateRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		
		ReportTipsUpdateRequest request = this.scritturaFactory.createReportTipsUpdateRequest();
		request.setTestata(this.createTestataInput());
		request.setReportTipsUpdateRequestData(requestData);
		
		ApplicationFault fault = this.webServiceUtils.createApplicationFault(request, 400, ERROR_MESSAGE);
		
		assertThat(fault.getMessaggio()).isEqualTo(ERROR_MESSAGE);
		assertThat(fault.getTestata().getTestataTecnica().getIdOperazioneRichiesta().getCodiceApplicazioneChiamante())
			.isEqualTo(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante());
		assertThat(fault.getTestata().getTestataTecnica().getCodiceOperazioneRisposta()).isEqualTo("TIPS_REPORT_UPDATE");
	}
	
	@Test
	@Order(11)
	void testXmlToString() {
		
		SaldiCgsReadRequestData requestData = this.letturaFactory.createSaldiCgsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		
		SaldiCgsReadRequest request = this.letturaFactory.createSaldiCgsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setSaldiCgsReadRequestData(requestData);
		
		String result = this.webServiceUtils.xmlToString(request);
		
		assertThat(result).isNotNull().contains(requestData.getCorrelationKey());
	}
	
	@Test
	@Order(12)
	void testIsCodiceAppliazioneChiamanteValid() {
		
		TestataInput t = this.createTestataInput();
		
		boolean result = this.webServiceUtils.isCodiceAppliazioneChiamanteValid(t);
		
		assertThat(result).isTrue();
	}
	
	@Test
	@Order(13)
	void testIsCodiceAppliazioneChiamanteNotValid() {
		
		TestataInput t = this.createTestataInput();
		t.getTestataTecnica().getIdOperazione().setCodiceApplicazioneChiamante("PROVA");
		
		boolean result = this.webServiceUtils.isCodiceAppliazioneChiamanteValid(t);
		
		assertThat(result).isFalse();
	}
	
	private TestataInput createTestataInput() {

		it.popso.ws.common.testata.schema.v11.ObjectFactory objectFactory = new it.popso.ws.common.testata.schema.v11.ObjectFactory();
		TestataInput testataInput = objectFactory.createTestataInput();
		TestataTecnicaInput testataTecnicaInput = objectFactory.createTestataTecnicaInput();
		IdOperazione idOperazione = objectFactory.createIdOperazione();
		
		idOperazione.setCodiceApplicazioneChiamante("CONTCONS");
		idOperazione.setCodiceOperazione(this.generateRandomString());
		
		testataTecnicaInput.setIdOperazione(idOperazione);
		testataTecnicaInput.setIdConversazione(this.generateRandomString());
		testataInput.setTestataTecnica(testataTecnicaInput);
		
		TestataDiContesto testataDiContesto = objectFactory.createTestataDiContesto();
		testataDiContesto.setCodiceApplicazione("CONTCONS");
		testataDiContesto.setCodiceCanale(CodiceCanale.ITR);
		testataDiContesto.setCodiceIstituto("05696");
		testataDiContesto.setCodiceFilialeContabile("1");
		testataDiContesto.setCodiceDipendenza("1");
		
		IdentificazioneSoggetti identificazioneSoggetti = objectFactory.createIdentificazioneSoggetti();
		testataDiContesto.setIdentificazioneSoggetti(identificazioneSoggetti);
		
		testataInput.setTestataDiContesto(testataDiContesto);
		
		return testataInput;
	}
	
	private String generateRandomString() {

		String alphaNumeric = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvxyz0123456789";
		StringBuilder s = new StringBuilder(20);

		for (int i = 0; i < 20; i++) {
			int ch = (int)(alphaNumeric.length() * Math.random());
			s.append(alphaNumeric.charAt(ch));
		}

	    return s.toString();
	}
	
}
