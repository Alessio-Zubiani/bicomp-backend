package it.popso.bicomp.ws.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.CgsLmr;
import it.popso.bicomp.model.CgsLmrLacBulk;
import it.popso.bicomp.model.CgsLmrLacBulkStatement;
import it.popso.bicomp.model.TipsCamt052BankAccountReport;
import it.popso.bicomp.model.TipsCamt053BankAccountStatementEntry;
import it.popso.bicomp.service.CgsService;
import it.popso.bicomp.service.TipsService;
import it.popso.bicomp.utils.DateUtils;
import it.popso.bicomp.utils.WebServiceUtils;
import it.popso.bicomp.ws.exception.ApplicationFaultException;
import it.popso.bicomp.ws.exception.DatiTestataFaultException;
import it.popso.ws.bicomp.lettura.schema.v1.ObjectFactory;
import it.popso.ws.bicomp.lettura.schema.v1.SaldiCgsReadRequest;
import it.popso.ws.bicomp.lettura.schema.v1.SaldiCgsReadRequestData;
import it.popso.ws.bicomp.lettura.schema.v1.SaldiCgsReadResponse;
import it.popso.ws.bicomp.lettura.schema.v1.SaldiTipsReadRequest;
import it.popso.ws.bicomp.lettura.schema.v1.SaldiTipsReadRequestData;
import it.popso.ws.bicomp.lettura.schema.v1.SaldiTipsReadResponse;
import it.popso.ws.bicomp.lettura.schema.v1.UltimoPagamentoTipsReadRequest;
import it.popso.ws.bicomp.lettura.schema.v1.UltimoPagamentoTipsReadRequestData;
import it.popso.ws.bicomp.lettura.schema.v1.UltimoPagamentoTipsReadResponse;
import it.popso.ws.bicomp.lettura.schema.v1.UltimoSaldoCgsReadRequest;
import it.popso.ws.bicomp.lettura.schema.v1.UltimoSaldoCgsReadRequestData;
import it.popso.ws.bicomp.lettura.schema.v1.UltimoSaldoCgsReadResponse;
import it.popso.ws.bicomp.schema.v1.TTransactionCodeFamily;
import it.popso.ws.common.testata.schema.v11.IdOperazione;
import it.popso.ws.common.testata.schema.v11.IdentificazioneSoggetti;
import it.popso.ws.common.testata.schema.v11.TestataDiContesto;
import it.popso.ws.common.testata.schema.v11.TestataInput;
import it.popso.ws.common.testata.schema.v11.TestataTecnicaInput;
import it.popso.ws.common.testata.tipi.schema.v11.CodiceCanale;
import jakarta.xml.bind.JAXB;
import jakarta.xml.bind.JAXBElement;

@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class BicompLetturaEndpointTest {
	
	@Mock
	private CgsService cgsService;
	
	@Mock
	private TipsService tipsService;
	
	private BicompLetturaEndpoint endpoint;
	private DateUtils dateUtils;
	private MockedStatic<DateUtils> mockDateUtils;
	private WebServiceUtils webServiceUtils;
	private MockedStatic<WebServiceUtils> mockWebServiceUtils;
	private ObjectFactory objectFactory = new ObjectFactory();
	
	
	@BeforeEach
    public void setup() {
		this.dateUtils = Mockito.mock(DateUtils.class);
		this.mockDateUtils = Mockito.mockStatic(DateUtils.class);
		this.webServiceUtils = Mockito.mock(WebServiceUtils.class);
		this.mockWebServiceUtils = Mockito.mockStatic(WebServiceUtils.class);
		
		this.endpoint = new BicompLetturaEndpoint(this.cgsService, this.tipsService);
	}
	
	@Test
	@Order(1)
	void testSaldiCgsRead() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
		
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    Mockito.when(this.dateUtils.xmlGregorianToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(this.xmlGregorianToDate(false));
	    
	    CgsLmr lmr = CgsLmr.builder()
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
		
		CgsLmrLacBulk bulk = CgsLmrLacBulk.builder()
				.id(BigDecimal.ONE)
				.bulkReference("BSCT230606001244ACGS000000000000001")
				.cgsLmr(lmr)
				.build();
	    
	    List<CgsLmrLacBulkStatement> list = Arrays.asList(CgsLmrLacBulkStatement.builder()
				.id(BigDecimal.ONE)
				.statementReference("BSCT230606001244ACGS000000000000001")
				.accountOwner("POSOIT22XXX")
				.openingBalance(new BigDecimal(10))
				.openingBalanceSide('C')
				.closingBalance(new BigDecimal(20))
				.closingBalanceSide('C')
				.settlementDate(new Date())
				.cgsLmrLacBulk(bulk)
				.build());
	    Mockito.when(this.cgsService.getSaldiCgs(Mockito.any(Date.class))).thenReturn(list);
	    
		SaldiCgsReadRequestData requestData = this.objectFactory.createSaldiCgsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setSettlementDate(this.dateToXmlGregorianCalendar(false, false));
		
		SaldiCgsReadRequest request = this.objectFactory.createSaldiCgsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setSaldiCgsReadRequestData(requestData);		
		
		JAXBElement<SaldiCgsReadRequest> jaxbRequest = this.objectFactory.createSaldiCgsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		JAXBElement<SaldiCgsReadResponse> jaxbResponse = this.endpoint.saldiCgsRead(jaxbRequest);
		
		assertThat(jaxbResponse).isNotNull();
		assertThat(jaxbResponse.getValue().getSaldiCgsReadResponseData().getCgsLac()).hasSameSizeAs(list);
		
		verify(this.cgsService, times(1)).getSaldiCgs(Mockito.any(Date.class));
	}
	
	@Test
	@Order(2)
	void testSaldiCgsReadEmpty() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
		
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    Mockito.when(this.dateUtils.xmlGregorianToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(this.xmlGregorianToDate(false));
	    	    
	    List<CgsLmrLacBulkStatement> list = Arrays.asList();
	    Mockito.when(this.cgsService.getSaldiCgs(Mockito.any(Date.class))).thenReturn(list);
	    
		SaldiCgsReadRequestData requestData = this.objectFactory.createSaldiCgsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setSettlementDate(this.dateToXmlGregorianCalendar(false,false));
		
		SaldiCgsReadRequest request = this.objectFactory.createSaldiCgsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setSaldiCgsReadRequestData(requestData);		
		
		JAXBElement<SaldiCgsReadRequest> jaxbRequest = this.objectFactory.createSaldiCgsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		JAXBElement<SaldiCgsReadResponse> jaxbResponse = this.endpoint.saldiCgsRead(jaxbRequest);
		
		assertThat(jaxbResponse).isNotNull();
		assertThat(jaxbResponse.getValue().getSaldiCgsReadResponseData().getCgsLac()).hasSameSizeAs(list);
		
		verify(this.cgsService, times(1)).getSaldiCgs(Mockito.any(Date.class));
	}
	
	@Test
	@Order(3)
	void testSaldiCgsReadDatatypeConfigurationException() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
		
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    Mockito.when(this.dateUtils.xmlGregorianToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(this.xmlGregorianToDate(false));
	    
	    CgsLmr lmr = CgsLmr.builder()
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
		
		CgsLmrLacBulk bulk = CgsLmrLacBulk.builder()
				.id(BigDecimal.ONE)
				.bulkReference("BSCT230606001244ACGS000000000000001")
				.cgsLmr(lmr)
				.build();
	    
	    List<CgsLmrLacBulkStatement> list = Arrays.asList(CgsLmrLacBulkStatement.builder()
				.id(BigDecimal.ONE)
				.statementReference("BSCT230606001244ACGS000000000000001")
				.accountOwner("POSOIT22XXX")
				.openingBalance(new BigDecimal(10))
				.openingBalanceSide('C')
				.closingBalance(new BigDecimal(20))
				.closingBalanceSide('C')
				.settlementDate(new Date())
				.cgsLmrLacBulk(bulk)
				.build());
	    Mockito.when(this.cgsService.getSaldiCgs(Mockito.any(Date.class))).thenReturn(list);
	    
		SaldiCgsReadRequestData requestData = this.objectFactory.createSaldiCgsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setSettlementDate(this.dateToXmlGregorianCalendar(false, false));
		
		SaldiCgsReadRequest request = this.objectFactory.createSaldiCgsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setSaldiCgsReadRequestData(requestData);		
		
		JAXBElement<SaldiCgsReadRequest> jaxbRequest = this.objectFactory.createSaldiCgsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		Mockito.when(this.dateUtils.dateToXmlGregorian(Mockito.any(Date.class)))
			.thenThrow(new DatatypeConfigurationException("DatatypeConfigurationException"));
		
		assertThrows(ApplicationFaultException.class, () -> {
	    	this.endpoint.saldiCgsRead(jaxbRequest);
		});
		
		verify(this.cgsService, times(1)).getSaldiCgs(Mockito.any(Date.class));
	}
	
	@Test
	@Order(4)
	void testSaldiCgsReadDatiTestataFaultException() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(false);
		
		SaldiCgsReadRequestData requestData = this.objectFactory.createSaldiCgsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setSettlementDate(this.dateToXmlGregorianCalendar(false, false));
		
		SaldiCgsReadRequest request = this.objectFactory.createSaldiCgsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setSaldiCgsReadRequestData(requestData);		
		
		JAXBElement<SaldiCgsReadRequest> jaxbRequest = this.objectFactory.createSaldiCgsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		assertThrows(DatiTestataFaultException.class, () -> {
	    	this.endpoint.saldiCgsRead(jaxbRequest);
		});
		
		verify(this.cgsService, times(0)).getSaldiCgs(Mockito.any(Date.class));
	}
	
	@Test
	@Order(5)
	void testSaldiCgsReadApplicationFaultException() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
		
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    Mockito.when(this.dateUtils.xmlGregorianToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(this.xmlGregorianToDate(true));
	    
		SaldiCgsReadRequestData requestData = this.objectFactory.createSaldiCgsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setSettlementDate(this.dateToXmlGregorianCalendar(false, true));
		
		SaldiCgsReadRequest request = this.objectFactory.createSaldiCgsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setSaldiCgsReadRequestData(requestData);		
		
		JAXBElement<SaldiCgsReadRequest> jaxbRequest = this.objectFactory.createSaldiCgsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		assertThrows(ApplicationFaultException.class, () -> {
	    	this.endpoint.saldiCgsRead(jaxbRequest);
		});
		
		verify(this.cgsService, times(0)).getSaldiCgs(Mockito.any(Date.class));
	}
	
	@Test
	@Order(6)
	void testSaldiCgsReadNegativeBalance() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
		
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    Mockito.when(this.dateUtils.xmlGregorianToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(this.xmlGregorianToDate(false));
	    
	    CgsLmr lmr = CgsLmr.builder()
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
		
		CgsLmrLacBulk bulk = CgsLmrLacBulk.builder()
				.id(BigDecimal.ONE)
				.bulkReference("BSCT230606001244ACGS000000000000001")
				.cgsLmr(lmr)
				.build();
	    
	    List<CgsLmrLacBulkStatement> list = Arrays.asList(CgsLmrLacBulkStatement.builder()
				.id(BigDecimal.ONE)
				.statementReference("BSCT230606001244ACGS000000000000001")
				.accountOwner("POSOIT22XXX")
				.openingBalance(new BigDecimal(10))
				.openingBalanceSide('D')
				.closingBalance(new BigDecimal(20))
				.closingBalanceSide('D')
				.settlementDate(new Date())
				.cgsLmrLacBulk(bulk)
				.build());
	    Mockito.when(this.cgsService.getSaldiCgs(Mockito.any(Date.class))).thenReturn(list);
	    
		SaldiCgsReadRequestData requestData = this.objectFactory.createSaldiCgsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setSettlementDate(this.dateToXmlGregorianCalendar(false, false));
		
		SaldiCgsReadRequest request = this.objectFactory.createSaldiCgsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setSaldiCgsReadRequestData(requestData);		
		
		JAXBElement<SaldiCgsReadRequest> jaxbRequest = this.objectFactory.createSaldiCgsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		JAXBElement<SaldiCgsReadResponse> jaxbResponse = this.endpoint.saldiCgsRead(jaxbRequest);
		
		assertThat(jaxbResponse).isNotNull();
		assertThat(jaxbResponse.getValue().getSaldiCgsReadResponseData().getCgsLac()).hasSameSizeAs(list);
		
		verify(this.cgsService, times(1)).getSaldiCgs(Mockito.any(Date.class));
	}
	
	@Test
	@Order(7)
	void testSaldiTipsRead() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
	    
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    
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
	    Mockito.when(this.tipsService.getSaldiTips()).thenReturn(list);
	    
		SaldiTipsReadRequestData requestData = this.objectFactory.createSaldiTipsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		
		SaldiTipsReadRequest request = this.objectFactory.createSaldiTipsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setSaldiTipsReadRequestData(requestData);		
		
		JAXBElement<SaldiTipsReadRequest> jaxbRequest = this.objectFactory.createSaldiTipsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		JAXBElement<SaldiTipsReadResponse> jaxbResponse = this.endpoint.saldiTipsRead(jaxbRequest);
		
		assertThat(jaxbResponse).isNotNull();
		assertThat(jaxbResponse.getValue().getSaldiTipsReadResponseData().getTipsReport()).hasSameSizeAs(list);
		
		verify(this.tipsService, times(1)).getSaldiTips();
	}
	
	@Test
	@Order(8)
	void testSaldiTipsReadEmpty() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
	    
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    
	    List<TipsCamt052BankAccountReport> list = Arrays.asList();
	    Mockito.when(this.tipsService.getSaldiTips()).thenReturn(list);
	    
		SaldiTipsReadRequestData requestData = this.objectFactory.createSaldiTipsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		
		SaldiTipsReadRequest request = this.objectFactory.createSaldiTipsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setSaldiTipsReadRequestData(requestData);		
		
		JAXBElement<SaldiTipsReadRequest> jaxbRequest = this.objectFactory.createSaldiTipsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		JAXBElement<SaldiTipsReadResponse> jaxbResponse = this.endpoint.saldiTipsRead(jaxbRequest);
		
		assertThat(jaxbResponse).isNotNull();
		assertThat(jaxbResponse.getValue().getSaldiTipsReadResponseData().getTipsReport()).hasSameSizeAs(list);
		
		verify(this.tipsService, times(1)).getSaldiTips();
	}
	
	@Test
	@Order(9)
	void testSaldiTipsReadDatatypeConfigurationException() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
		
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    Mockito.when(this.dateUtils.xmlGregorianToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(this.xmlGregorianToDate(false));
	    
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
	    Mockito.when(this.tipsService.getSaldiTips()).thenReturn(list);
	    
		SaldiTipsReadRequestData requestData = this.objectFactory.createSaldiTipsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		
		SaldiTipsReadRequest request = this.objectFactory.createSaldiTipsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setSaldiTipsReadRequestData(requestData);		
		
		JAXBElement<SaldiTipsReadRequest> jaxbRequest = this.objectFactory.createSaldiTipsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		Mockito.when(this.dateUtils.dateToXmlGregorian(Mockito.any(Date.class)))
			.thenThrow(new DatatypeConfigurationException("DatatypeConfigurationException"));
	
		assertThrows(ApplicationFaultException.class, () -> {
	    	this.endpoint.saldiTipsRead(jaxbRequest);
		});
		
		verify(this.tipsService, times(1)).getSaldiTips();
	}
	
	@Test
	@Order(10)
	void testSaldiTipsReadDatiTestataFaultException() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(false);
		
	    SaldiTipsReadRequestData requestData = this.objectFactory.createSaldiTipsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		
		SaldiTipsReadRequest request = this.objectFactory.createSaldiTipsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setSaldiTipsReadRequestData(requestData);		
		
		JAXBElement<SaldiTipsReadRequest> jaxbRequest = this.objectFactory.createSaldiTipsReadRequest(request);		
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		assertThrows(DatiTestataFaultException.class, () -> {
	    	this.endpoint.saldiTipsRead(jaxbRequest);
		});
		
		verify(this.tipsService, times(0)).getSaldiTips();
	}
	
	@Test
	@Order(11)
	void testSaldiTipsReadNegativeBalance() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
	    
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    
	    List<TipsCamt052BankAccountReport> list = Arrays.asList(TipsCamt052BankAccountReport.builder()
	    		.id(BigDecimal.ONE)
	    		.tmsInsert(new Date())
	    		.creationDateTime(new Date())
	    		.accountId("IITEURPOSOIT22XXXTIPS")
	    		.accountOwner("POSOIT22XXX")
	    		.closingBalance(new BigDecimal(20).negate())
	    		.closingBalanceSide('D')
	    		.currency("EUR")
	    		.flagElaborato('N')
	    		.msgId("SAT-F89p18z-N9xl7jl-9ezjop")
	    		.openingBalance(new BigDecimal(10).negate())
	    		.openingBalanceSide('D')
	    		.reportName("camt.052_20231204_171921559.xml")
	    		.settlementDate(new Date())
	    		.totalCreditOperation(new BigDecimal(30))
	    		.totalDebitOperation(new BigDecimal(10))
	    		.build()
	    );
	    Mockito.when(this.tipsService.getSaldiTips()).thenReturn(list);
	    
		SaldiTipsReadRequestData requestData = this.objectFactory.createSaldiTipsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		
		SaldiTipsReadRequest request = this.objectFactory.createSaldiTipsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setSaldiTipsReadRequestData(requestData);		
		
		JAXBElement<SaldiTipsReadRequest> jaxbRequest = this.objectFactory.createSaldiTipsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		JAXBElement<SaldiTipsReadResponse> jaxbResponse = this.endpoint.saldiTipsRead(jaxbRequest);
		
		assertThat(jaxbResponse).isNotNull();
		assertThat(jaxbResponse.getValue().getSaldiTipsReadResponseData().getTipsReport()).hasSameSizeAs(list);
		
		verify(this.tipsService, times(1)).getSaldiTips();
	}
	
	@Test
	@Order(12)
	void testUltimoSaldoCgsRead() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
		
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    Mockito.when(this.dateUtils.xmlGregorianToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(this.xmlGregorianToDate(false));
	    
	    CgsLmr lmr = CgsLmr.builder()
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
		
		CgsLmrLacBulk bulk = CgsLmrLacBulk.builder()
				.id(BigDecimal.ONE)
				.bulkReference("BSCT230606001244ACGS000000000000001")
				.cgsLmr(lmr)
				.build();
	    
	    CgsLmrLacBulkStatement c = CgsLmrLacBulkStatement.builder()
				.id(BigDecimal.ONE)
				.statementReference("BSCT230606001244ACGS000000000000001")
				.accountOwner("POSOIT22XXX")
				.openingBalance(new BigDecimal(10))
				.openingBalanceSide('C')
				.closingBalance(new BigDecimal(20))
				.closingBalanceSide('C')
				.settlementDate(new Date())
				.cgsLmrLacBulk(bulk)
				.build();
	    Mockito.when(this.cgsService.getLastCgsBalance(Mockito.any(Date.class))).thenReturn(c);
	    
		UltimoSaldoCgsReadRequestData requestData = this.objectFactory.createUltimoSaldoCgsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setSettlementDate(this.dateToXmlGregorianCalendar(false, false));
		
		UltimoSaldoCgsReadRequest request = this.objectFactory.createUltimoSaldoCgsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setUltimoSaldoCgsReadRequestData(requestData);		
		
		JAXBElement<UltimoSaldoCgsReadRequest> jaxbRequest = this.objectFactory.createUltimoSaldoCgsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		JAXBElement<UltimoSaldoCgsReadResponse> jaxbResponse = this.endpoint.ultimoSaldoCgsRead(jaxbRequest);
		
		assertThat(jaxbResponse).isNotNull();
		assertThat(jaxbResponse.getValue().getUltimoSaldoCgsReadResponseData().getCgsLac().getMessageId()).isEqualTo(c.getCgsLmrLacBulk().getBulkReference());
		
		verify(this.cgsService, times(1)).getLastCgsBalance(Mockito.any(Date.class));
	}
	
	@Test
	@Order(13)
	void testUltimoSaldoCgsReadResourceNotFoundException() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
		
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    Mockito.when(this.dateUtils.xmlGregorianToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(this.xmlGregorianToDate(false));
	    
	    Mockito.when(this.cgsService.getLastCgsBalance(Mockito.any(Date.class))).thenThrow(
	    		new ResourceNotFoundException("ResourceNotFoundException")
	    );
	    
	    UltimoSaldoCgsReadRequestData requestData = this.objectFactory.createUltimoSaldoCgsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setSettlementDate(this.dateToXmlGregorianCalendar(false, false));
		
		UltimoSaldoCgsReadRequest request = this.objectFactory.createUltimoSaldoCgsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setUltimoSaldoCgsReadRequestData(requestData);		
		
		JAXBElement<UltimoSaldoCgsReadRequest> jaxbRequest = this.objectFactory.createUltimoSaldoCgsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		assertThrows(ApplicationFaultException.class, () -> {
	    	this.endpoint.ultimoSaldoCgsRead(jaxbRequest);
		});
		
		verify(this.cgsService, times(1)).getLastCgsBalance(Mockito.any(Date.class));
	}
	
	@Test
	@Order(14)
	void testUltimoSaldoCgsReadDatiTestataFaultException() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(false);
		
	    UltimoSaldoCgsReadRequestData requestData = this.objectFactory.createUltimoSaldoCgsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setSettlementDate(this.dateToXmlGregorianCalendar(false, false));
		
		UltimoSaldoCgsReadRequest request = this.objectFactory.createUltimoSaldoCgsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setUltimoSaldoCgsReadRequestData(requestData);		
		
		JAXBElement<UltimoSaldoCgsReadRequest> jaxbRequest = this.objectFactory.createUltimoSaldoCgsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		assertThrows(DatiTestataFaultException.class, () -> {
	    	this.endpoint.ultimoSaldoCgsRead(jaxbRequest);
		});
		
		verify(this.cgsService, times(0)).getLastCgsBalance(Mockito.any(Date.class));
	}
	
	@Test
	@Order(15)
	void testUltimoSaldoCgsReadApplicationFaultException() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
		
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    Mockito.when(this.dateUtils.xmlGregorianToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(this.xmlGregorianToDate(true));
	    
	    UltimoSaldoCgsReadRequestData requestData = this.objectFactory.createUltimoSaldoCgsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setSettlementDate(this.dateToXmlGregorianCalendar(false, true));
		
		UltimoSaldoCgsReadRequest request = this.objectFactory.createUltimoSaldoCgsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setUltimoSaldoCgsReadRequestData(requestData);		
		
		JAXBElement<UltimoSaldoCgsReadRequest> jaxbRequest = this.objectFactory.createUltimoSaldoCgsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		assertThrows(ApplicationFaultException.class, () -> {
	    	this.endpoint.ultimoSaldoCgsRead(jaxbRequest);
		});
		
		verify(this.cgsService, times(0)).getLastCgsBalance(Mockito.any(Date.class));
	}
	
	@Test
	@Order(16)
	void testUltimoPagamentoTipsRead() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
		
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    Mockito.when(this.dateUtils.xmlGregorianToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(this.xmlGregorianToDate(false));
	    
	    TipsCamt053BankAccountStatementEntry t = TipsCamt053BankAccountStatementEntry.builder()
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
			.build();
	    Mockito.when(this.tipsService.getLastTipsPaymentByDate(Mockito.any(Date.class))).thenReturn(t);
	    
		UltimoPagamentoTipsReadRequestData requestData = this.objectFactory.createUltimoPagamentoTipsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setSettlementDate(this.dateToXmlGregorianCalendar(false, false));
		
		UltimoPagamentoTipsReadRequest request = this.objectFactory.createUltimoPagamentoTipsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setUltimoPagamentoTipsReadRequestData(requestData);		
		
		JAXBElement<UltimoPagamentoTipsReadRequest> jaxbRequest = this.objectFactory.createUltimoPagamentoTipsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		JAXBElement<UltimoPagamentoTipsReadResponse> jaxbResponse = this.endpoint.ultimoPagamentoTipsRead(jaxbRequest);
		
		assertThat(jaxbResponse).isNotNull();
		assertThat(jaxbResponse.getValue().getUltimoPagamentoTipsReadResponseData().getTipsPayment().getEntryReference())
			.isEqualTo(t.getEntryReference());
		assertThat(jaxbResponse.getValue().getUltimoPagamentoTipsReadResponseData().getTipsPayment().getTransactionCodeFamily())
			.isEqualTo(TTransactionCodeFamily.RRCT);
		
		verify(this.tipsService, times(1)).getLastTipsPaymentByDate(Mockito.any(Date.class));
	}
	
	@Test
	@Order(17)
	void testUltimoPagamentoTipsReadResourceNotFoundException() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
		
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    Mockito.when(this.dateUtils.xmlGregorianToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(this.xmlGregorianToDate(false));
	    
	    Mockito.when(this.tipsService.getLastTipsPaymentByDate(Mockito.any(Date.class))).thenThrow(
	    		new ResourceNotFoundException("ResourceNotFoundException")
	    );
	    
		UltimoPagamentoTipsReadRequestData requestData = this.objectFactory.createUltimoPagamentoTipsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setSettlementDate(this.dateToXmlGregorianCalendar(false, false));
		
		UltimoPagamentoTipsReadRequest request = this.objectFactory.createUltimoPagamentoTipsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setUltimoPagamentoTipsReadRequestData(requestData);		
		
		JAXBElement<UltimoPagamentoTipsReadRequest> jaxbRequest = this.objectFactory.createUltimoPagamentoTipsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		JAXBElement<UltimoPagamentoTipsReadResponse> jaxbResponse = this.endpoint.ultimoPagamentoTipsRead(jaxbRequest);
		
		assertThat(jaxbResponse).isNotNull();
		assertThat(jaxbResponse.getValue().getUltimoPagamentoTipsReadResponseData().getTipsPayment().getEntryReference()).isNull();
		
		verify(this.tipsService, times(1)).getLastTipsPaymentByDate(Mockito.any(Date.class));
	}
	
	@Test
	@Order(18)
	void testUltimoPagamentoTipsReadDatiTestataFaultException() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(false);
		
	    UltimoPagamentoTipsReadRequestData requestData = this.objectFactory.createUltimoPagamentoTipsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setSettlementDate(this.dateToXmlGregorianCalendar(false, false));
		
		UltimoPagamentoTipsReadRequest request = this.objectFactory.createUltimoPagamentoTipsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setUltimoPagamentoTipsReadRequestData(requestData);
		
		JAXBElement<UltimoPagamentoTipsReadRequest> jaxbRequest = this.objectFactory.createUltimoPagamentoTipsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		assertThrows(DatiTestataFaultException.class, () -> {
	    	this.endpoint.ultimoPagamentoTipsRead(jaxbRequest);
		});
		
		verify(this.tipsService, times(0)).getLastTipsPaymentByDate(Mockito.any(Date.class));
	}
	
	@Test
	@Order(19)
	void testUltimoPagamentoTipsReadApplicationFaultException() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
		
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    Mockito.when(this.dateUtils.xmlGregorianToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(this.xmlGregorianToDate(true));
	    
	    UltimoPagamentoTipsReadRequestData requestData = this.objectFactory.createUltimoPagamentoTipsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setSettlementDate(this.dateToXmlGregorianCalendar(false, true));
		
		UltimoPagamentoTipsReadRequest request = this.objectFactory.createUltimoPagamentoTipsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setUltimoPagamentoTipsReadRequestData(requestData);
		
		JAXBElement<UltimoPagamentoTipsReadRequest> jaxbRequest = this.objectFactory.createUltimoPagamentoTipsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		assertThrows(ApplicationFaultException.class, () -> {
	    	this.endpoint.ultimoPagamentoTipsRead(jaxbRequest);
		});
		
		verify(this.tipsService, times(0)).getLastTipsPaymentByDate(Mockito.any(Date.class));
	}
	
	@Test
	@Order(20)
	void testUltimoPagamentoTipsReadTransactionCodeNotPmnt() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
		
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    Mockito.when(this.dateUtils.xmlGregorianToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(this.xmlGregorianToDate(false));
	    
	    TipsCamt053BankAccountStatementEntry t = TipsCamt053BankAccountStatementEntry.builder()
			.entryReference("A102586329601030483417032550IT")
			.settlementDateTime(new Timestamp(System.currentTimeMillis()))
			.paymentAmount(new BigDecimal(100))
			.currency("EUR")
			.status("BOOK")
			.side('D')
			.bankTransactionCode("LIQT")
			.bankTransactionCodeFamily("IRCT")
			.debitorBic("POSOIT22XXX")
			.creditorBic("UNCRITMMXXX")
			.build();
	    Mockito.when(this.tipsService.getLastTipsPaymentByDate(Mockito.any(Date.class))).thenReturn(t);
	    
		UltimoPagamentoTipsReadRequestData requestData = this.objectFactory.createUltimoPagamentoTipsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setSettlementDate(this.dateToXmlGregorianCalendar(false, false));
		
		UltimoPagamentoTipsReadRequest request = this.objectFactory.createUltimoPagamentoTipsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setUltimoPagamentoTipsReadRequestData(requestData);		
		
		JAXBElement<UltimoPagamentoTipsReadRequest> jaxbRequest = this.objectFactory.createUltimoPagamentoTipsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		JAXBElement<UltimoPagamentoTipsReadResponse> jaxbResponse = this.endpoint.ultimoPagamentoTipsRead(jaxbRequest);
		
		assertThat(jaxbResponse).isNotNull();
		assertThat(jaxbResponse.getValue().getUltimoPagamentoTipsReadResponseData().getTipsPayment().getEntryReference())
			.isEqualTo(t.getEntryReference());
		assertThat(jaxbResponse.getValue().getUltimoPagamentoTipsReadResponseData().getTipsPayment().getTransactionCode()).isNull();
		
		verify(this.tipsService, times(1)).getLastTipsPaymentByDate(Mockito.any(Date.class));
	}
	
	@Test
	@Order(21)
	void testUltimoPagamentoTipsReadNegativeAmount() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
		
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    Mockito.when(this.dateUtils.xmlGregorianToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(this.xmlGregorianToDate(false));
	    
	    TipsCamt053BankAccountStatementEntry t = TipsCamt053BankAccountStatementEntry.builder()
			.entryReference("A102586329601030483417032550IT")
			.settlementDateTime(new Timestamp(System.currentTimeMillis()))
			.paymentAmount(new BigDecimal(100))
			.currency("EUR")
			.status("BOOK")
			.side('D')
			.bankTransactionCode("PMNT")
			.bankTransactionCodeFamily("IRCT")
			.debitorBic("POSOIT22XXX")
			.creditorBic("UNCRITMMXXX")
			.build();
	    Mockito.when(this.tipsService.getLastTipsPaymentByDate(Mockito.any(Date.class))).thenReturn(t);
	    
		UltimoPagamentoTipsReadRequestData requestData = this.objectFactory.createUltimoPagamentoTipsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setSettlementDate(this.dateToXmlGregorianCalendar(false, false));
		
		UltimoPagamentoTipsReadRequest request = this.objectFactory.createUltimoPagamentoTipsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setUltimoPagamentoTipsReadRequestData(requestData);		
		
		JAXBElement<UltimoPagamentoTipsReadRequest> jaxbRequest = this.objectFactory.createUltimoPagamentoTipsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		JAXBElement<UltimoPagamentoTipsReadResponse> jaxbResponse = this.endpoint.ultimoPagamentoTipsRead(jaxbRequest);
		
		assertThat(jaxbResponse).isNotNull();
		assertThat(jaxbResponse.getValue().getUltimoPagamentoTipsReadResponseData().getTipsPayment().getEntryReference())
			.isEqualTo(t.getEntryReference());
		assertThat(jaxbResponse.getValue().getUltimoPagamentoTipsReadResponseData().getTipsPayment().getTransactionCodeFamily())
			.isEqualTo(TTransactionCodeFamily.IRCT);
		
		verify(this.tipsService, times(1)).getLastTipsPaymentByDate(Mockito.any(Date.class));
	}
	
	@Test
	@Order(22)
	void testUltimoPagamentoTipsReadRcdt() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
		
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    Mockito.when(this.dateUtils.xmlGregorianToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(this.xmlGregorianToDate(false));
	    
	    TipsCamt053BankAccountStatementEntry t = TipsCamt053BankAccountStatementEntry.builder()
			.entryReference("A102586329601030483417032550IT")
			.settlementDateTime(new Timestamp(System.currentTimeMillis()))
			.paymentAmount(new BigDecimal(100))
			.currency("EUR")
			.status("BOOK")
			.side('C')
			.bankTransactionCode("PMNT")
			.bankTransactionCodeFamily("RCDT")
			.debitorBic("UNCRITMMXXX")
			.creditorBic("POSOIT22XXX")
			.build();
	    Mockito.when(this.tipsService.getLastTipsPaymentByDate(Mockito.any(Date.class))).thenReturn(t);
	    
		UltimoPagamentoTipsReadRequestData requestData = this.objectFactory.createUltimoPagamentoTipsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setSettlementDate(this.dateToXmlGregorianCalendar(false, false));
		
		UltimoPagamentoTipsReadRequest request = this.objectFactory.createUltimoPagamentoTipsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setUltimoPagamentoTipsReadRequestData(requestData);		
		
		JAXBElement<UltimoPagamentoTipsReadRequest> jaxbRequest = this.objectFactory.createUltimoPagamentoTipsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		JAXBElement<UltimoPagamentoTipsReadResponse> jaxbResponse = this.endpoint.ultimoPagamentoTipsRead(jaxbRequest);
		
		assertThat(jaxbResponse).isNotNull();
		assertThat(jaxbResponse.getValue().getUltimoPagamentoTipsReadResponseData().getTipsPayment().getEntryReference())
			.isEqualTo(t.getEntryReference());
		assertThat(jaxbResponse.getValue().getUltimoPagamentoTipsReadResponseData().getTipsPayment().getTransactionCodeFamily())
			.isEqualTo(TTransactionCodeFamily.RCDT);
		
		verify(this.tipsService, times(1)).getLastTipsPaymentByDate(Mockito.any(Date.class));
	}
	
	@Test
	@Order(23)
	void testUltimoPagamentoTipsReadIcdt() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
		
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    Mockito.when(this.dateUtils.xmlGregorianToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(this.xmlGregorianToDate(false));
	    
	    TipsCamt053BankAccountStatementEntry t = TipsCamt053BankAccountStatementEntry.builder()
			.entryReference("A102586329601030483417032550IT")
			.settlementDateTime(new Timestamp(System.currentTimeMillis()))
			.paymentAmount(new BigDecimal(100))
			.currency("EUR")
			.status("BOOK")
			.side('D')
			.bankTransactionCode("PMNT")
			.bankTransactionCodeFamily("ICDT")
			.debitorBic("POSOIT22XXX")
			.creditorBic("UNCRITMMXXX")
			.build();
	    Mockito.when(this.tipsService.getLastTipsPaymentByDate(Mockito.any(Date.class))).thenReturn(t);
	    
		UltimoPagamentoTipsReadRequestData requestData = this.objectFactory.createUltimoPagamentoTipsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setSettlementDate(this.dateToXmlGregorianCalendar(false, false));
		
		UltimoPagamentoTipsReadRequest request = this.objectFactory.createUltimoPagamentoTipsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setUltimoPagamentoTipsReadRequestData(requestData);		
		
		JAXBElement<UltimoPagamentoTipsReadRequest> jaxbRequest = this.objectFactory.createUltimoPagamentoTipsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		JAXBElement<UltimoPagamentoTipsReadResponse> jaxbResponse = this.endpoint.ultimoPagamentoTipsRead(jaxbRequest);
		
		assertThat(jaxbResponse).isNotNull();
		assertThat(jaxbResponse.getValue().getUltimoPagamentoTipsReadResponseData().getTipsPayment().getEntryReference())
			.isEqualTo(t.getEntryReference());
		assertThat(jaxbResponse.getValue().getUltimoPagamentoTipsReadResponseData().getTipsPayment().getTransactionCodeFamily())
			.isEqualTo(TTransactionCodeFamily.ICDT);
		
		verify(this.tipsService, times(1)).getLastTipsPaymentByDate(Mockito.any(Date.class));
	}
	
	@Test
	@Order(24)
	void testUltimoPagamentoTipsReadTransactionCodeFamilyNotManaged() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
		
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    Mockito.when(this.dateUtils.xmlGregorianToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(this.xmlGregorianToDate(false));
	    
	    TipsCamt053BankAccountStatementEntry t = TipsCamt053BankAccountStatementEntry.builder()
			.entryReference("A102586329601030483417032550IT")
			.settlementDateTime(new Timestamp(System.currentTimeMillis()))
			.paymentAmount(new BigDecimal(100))
			.currency("EUR")
			.status("BOOK")
			.side('D')
			.bankTransactionCode("PMNT")
			.bankTransactionCodeFamily("PROVA")
			.debitorBic("POSOIT22XXX")
			.creditorBic("UNCRITMMXXX")
			.build();
	    Mockito.when(this.tipsService.getLastTipsPaymentByDate(Mockito.any(Date.class))).thenReturn(t);
	    
		UltimoPagamentoTipsReadRequestData requestData = this.objectFactory.createUltimoPagamentoTipsReadRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setSettlementDate(this.dateToXmlGregorianCalendar(false, false));
		
		UltimoPagamentoTipsReadRequest request = this.objectFactory.createUltimoPagamentoTipsReadRequest();
		request.setTestata(this.createTestataInput());
		request.setUltimoPagamentoTipsReadRequestData(requestData);		
		
		JAXBElement<UltimoPagamentoTipsReadRequest> jaxbRequest = this.objectFactory.createUltimoPagamentoTipsReadRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		JAXBElement<UltimoPagamentoTipsReadResponse> jaxbResponse = this.endpoint.ultimoPagamentoTipsRead(jaxbRequest);
		
		assertThat(jaxbResponse).isNotNull();
		assertThat(jaxbResponse.getValue().getUltimoPagamentoTipsReadResponseData().getTipsPayment().getEntryReference())
			.isEqualTo(t.getEntryReference());
		assertThat(jaxbResponse.getValue().getUltimoPagamentoTipsReadResponseData().getTipsPayment().getTransactionCodeFamily()).isNull();
		
		verify(this.tipsService, times(1)).getLastTipsPaymentByDate(Mockito.any(Date.class));
	}
	
	@AfterEach
	public void cleanUp() {
		this.mockDateUtils.close();
		this.mockWebServiceUtils.close();
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
	
	private XMLGregorianCalendar dateToXmlGregorianCalendar(boolean isWithTime, boolean isFuture) throws DatatypeConfigurationException {
		
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		
		if(!isWithTime) {
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
		}
		
		if(isFuture) {
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}
		
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
	}
	
	private Date xmlGregorianToDate(boolean isFuture) throws DatatypeConfigurationException {
		
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		
		if(isFuture) {
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}
		
		XMLGregorianCalendar xgc = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
		xgc.setHour(0);
		xgc.setMinute(0);
		xgc.setSecond(0);
		xgc.setMillisecond(0);
		
		return xgc.toGregorianCalendar().getTime();
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
	
	private String xmlToString(Object o) {
		
		StringWriter stringWriter = new StringWriter();
		JAXB.marshal(o, stringWriter);
		
		return stringWriter.toString();
	}

}
