package it.popso.bicomp.ws.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
import it.popso.bicomp.service.TipsService;
import it.popso.bicomp.utils.DateUtils;
import it.popso.bicomp.utils.WebServiceUtils;
import it.popso.bicomp.ws.exception.ApplicationFaultException;
import it.popso.bicomp.ws.exception.DatiTestataFaultException;
import it.popso.ws.bicomp.scrittura.schema.v1.ObjectFactory;
import it.popso.ws.bicomp.scrittura.schema.v1.ReportTipsUpdateRequest;
import it.popso.ws.bicomp.scrittura.schema.v1.ReportTipsUpdateRequestData;
import it.popso.ws.bicomp.scrittura.schema.v1.ReportTipsUpdateResponse;
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
class BicompScritturaEndpointTest {
	
	@Mock
	private TipsService tipsService;
	
	private BicompScritturaEndpoint endpoint;
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
		
		this.endpoint = new BicompScritturaEndpoint(this.tipsService);
	}
	
	@Test
	@Order(1)
	void testReportTipsUpdate() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
		
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    Mockito.when(this.dateUtils.xmlGregorianToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(this.xmlGregorianToDate(false));
	    
	    Mockito.when(this.tipsService.updateFlagElaborato(Mockito.anyString())).thenReturn("OK");
	    
	    ReportTipsUpdateRequestData requestData = this.objectFactory.createReportTipsUpdateRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setMessageId("SAT-F89p18z-N9xl7jl-9ezjop");
		
		ReportTipsUpdateRequest request = this.objectFactory.createReportTipsUpdateRequest();
		request.setTestata(this.createTestataInput());
		request.setReportTipsUpdateRequestData(requestData);		
		
		JAXBElement<ReportTipsUpdateRequest> jaxbRequest = this.objectFactory.createReportTipsUpdateRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		JAXBElement<ReportTipsUpdateResponse> jaxbResponse = this.endpoint.tipsReportUpdate(jaxbRequest);
		
		assertThat(jaxbResponse).isNotNull();
		assertThat(jaxbResponse.getValue().getReportTipsUpdateResponseData().getMessageId()).isEqualTo(requestData.getMessageId());
		
		verify(this.tipsService, times(1)).updateFlagElaborato(Mockito.anyString());
	}
	
	@Test
	@Order(2)
	void testReportTipsUpdateDatiTestataFaultException() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(false);
		
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    Mockito.when(this.dateUtils.xmlGregorianToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(this.xmlGregorianToDate(false));
	    
	    ReportTipsUpdateRequestData requestData = this.objectFactory.createReportTipsUpdateRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setMessageId("SAT-F89p18z-N9xl7jl-9ezjop");
		
		ReportTipsUpdateRequest request = this.objectFactory.createReportTipsUpdateRequest();
		request.setTestata(this.createTestataInput());
		request.setReportTipsUpdateRequestData(requestData);		
		
		JAXBElement<ReportTipsUpdateRequest> jaxbRequest = this.objectFactory.createReportTipsUpdateRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		assertThrows(DatiTestataFaultException.class, () -> {
			this.endpoint.tipsReportUpdate(jaxbRequest);
		});
		
		verify(this.tipsService, times(0)).updateFlagElaborato(Mockito.anyString());
	}
	
	@Test
	@Order(3)
	void testReportTipsUpdateResourceNotFoundException() throws DatatypeConfigurationException {
		
		this.mockWebServiceUtils.when(() -> WebServiceUtils.webServiceUtils()).thenReturn(this.webServiceUtils);
	    Mockito.when(this.webServiceUtils.isCodiceAppliazioneChiamanteValid(Mockito.any(TestataInput.class))).thenReturn(true);
		
	    this.mockDateUtils.when(() -> DateUtils.dateUtils()).thenReturn(this.dateUtils);
	    Mockito.when(this.dateUtils.xmlGregorianToDate(Mockito.any(XMLGregorianCalendar.class))).thenReturn(this.xmlGregorianToDate(false));
	    
	    Mockito.when(this.tipsService.updateFlagElaborato(Mockito.anyString())).thenThrow(
	    		new ResourceNotFoundException("ResourceNotFoundException")
	    );
	    
	    ReportTipsUpdateRequestData requestData = this.objectFactory.createReportTipsUpdateRequestData();
		requestData.setCorrelationKey("CORRELATION_KEY");
		requestData.setCreationTimestamp(this.dateToXmlGregorianCalendar(true, false));
		requestData.setAccountOwnerBIC("POSOIT22XXX");
		requestData.setMessageId("SAT-F89p18z-N9xl7jl-9ezjop");
		
		ReportTipsUpdateRequest request = this.objectFactory.createReportTipsUpdateRequest();
		request.setTestata(this.createTestataInput());
		request.setReportTipsUpdateRequestData(requestData);		
		
		JAXBElement<ReportTipsUpdateRequest> jaxbRequest = this.objectFactory.createReportTipsUpdateRequest(request);
		Mockito.when(this.webServiceUtils.xmlToString(Mockito.any(Object.class))).thenReturn(this.xmlToString(request));
		
		assertThrows(ApplicationFaultException.class, () -> {
			this.endpoint.tipsReportUpdate(jaxbRequest);
		});
		
		verify(this.tipsService, times(1)).updateFlagElaborato(Mockito.anyString());
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
