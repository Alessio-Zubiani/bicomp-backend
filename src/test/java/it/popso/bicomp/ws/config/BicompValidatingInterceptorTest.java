package it.popso.bicomp.ws.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.test.support.creator.SoapEnvelopeMessageCreator;
import org.xml.sax.SAXParseException;

import it.popso.bicomp.ws.exception.InputFaultException;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPException;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class BicompValidatingInterceptorTest {
	
	private Source source;
	private WebServiceMessage webServiceMessage;
	private MessageContext messageContext;
	private SaajSoapMessageFactory saajSoapMessageFactory;
	private BicompValidatingInterceptor bicompValidatingInterceptor;
	private SAXParseException[] saxParseException;
	
	
	@BeforeEach
    public void setup(TestInfo testInfo) throws IOException, SOAPException {
		
		this.bicompValidatingInterceptor = new BicompValidatingInterceptor();
		
		if(testInfo.getDisplayName().equals("testHandleRequestValidationErrorsMissingSettlementDate")) {
			
			this.source = new StreamSource(new ByteArrayInputStream(
					Files.readAllBytes(Paths.get("src/test/resources/ws/missingSettlementDate.xml")))
			);
			this.saajSoapMessageFactory = new SaajSoapMessageFactory(MessageFactory.newInstance());
			this.webServiceMessage = new SoapEnvelopeMessageCreator(this.source).createMessage(this.saajSoapMessageFactory);
			this.messageContext = new DefaultMessageContext(this.webServiceMessage, this.saajSoapMessageFactory);
			
			this.saxParseException = new SAXParseException[] { 
					new SAXParseException("Validation error: the value '' of element 'v1:SettlementDate' is not valid.", null), 
					new SAXParseException("Validation error: '' is not a valid value for 'date'.", null) 
			};
		}
		else if(testInfo.getDisplayName().equals("testHandleRequestValidationErrorsMissingIdConversazione")) {
			
			this.source = new StreamSource(new ByteArrayInputStream(
					Files.readAllBytes(Paths.get("src/test/resources/ws/missingIdConversazione.xml")))
			);
			this.saajSoapMessageFactory = new SaajSoapMessageFactory(MessageFactory.newInstance());
			this.webServiceMessage = new SoapEnvelopeMessageCreator(this.source).createMessage(this.saajSoapMessageFactory);
			this.messageContext = new DefaultMessageContext(this.webServiceMessage, this.saajSoapMessageFactory);
			
			this.saxParseException = new SAXParseException[] { 
					new SAXParseException("Validation error: The content of element 'v11:testataTecnica' is not complete. One of '{\"http://schema.testata.common.ws.popso.it/v11\":idConversazione}' is expected.", null) 
			};
		}
		else if(testInfo.getDisplayName().equals("testHandleRequestValidationErrorsMissingCodiceOperazione")) {
			
			this.source = new StreamSource(new ByteArrayInputStream(
					Files.readAllBytes(Paths.get("src/test/resources/ws/missingCodiceOperazione.xml")))
			);
			this.saajSoapMessageFactory = new SaajSoapMessageFactory(MessageFactory.newInstance());
			this.webServiceMessage = new SoapEnvelopeMessageCreator(this.source).createMessage(this.saajSoapMessageFactory);
			this.messageContext = new DefaultMessageContext(this.webServiceMessage, this.saajSoapMessageFactory);
			
			this.saxParseException = new SAXParseException[] { 
					new SAXParseException("Validation error: The content of element 'v11:idOperazione' is not complete. One of '{\"http://schema.testata.common.ws.popso.it/v11\":codiceOperazione}' is expected.", null) 
			};
		}
		else if(testInfo.getDisplayName().equals("testHandleRequestValidationErrorsMissingCodiceApplicazione")) {
			
			this.source = new StreamSource(new ByteArrayInputStream(
					Files.readAllBytes(Paths.get("src/test/resources/ws/missingCodiceApplicazione.xml")))
			);
			this.saajSoapMessageFactory = new SaajSoapMessageFactory(MessageFactory.newInstance());
			this.webServiceMessage = new SoapEnvelopeMessageCreator(this.source).createMessage(this.saajSoapMessageFactory);
			this.messageContext = new DefaultMessageContext(this.webServiceMessage, this.saajSoapMessageFactory);
			
			this.saxParseException = new SAXParseException[] { 
					new SAXParseException("Validation error: Invalid content was found starting with element '{\"http://schema.testata.common.ws.popso.it/v11\":codiceOperazione}'. One of '{\"http://schema.testata.common.ws.popso.it/v11\":codiceApplicazioneChiamante}' is expected.", null) 
			};
		}
		else if(testInfo.getDisplayName().equals("testHandleRequestValidationErrorsMissingIdOperazione")) {
			
			this.source = new StreamSource(new ByteArrayInputStream(
					Files.readAllBytes(Paths.get("src/test/resources/ws/missingIdOperazione.xml")))
			);
			this.saajSoapMessageFactory = new SaajSoapMessageFactory(MessageFactory.newInstance());
			this.webServiceMessage = new SoapEnvelopeMessageCreator(this.source).createMessage(this.saajSoapMessageFactory);
			this.messageContext = new DefaultMessageContext(this.webServiceMessage, this.saajSoapMessageFactory);
			
			this.saxParseException = new SAXParseException[] { 
					new SAXParseException("Validation error: Invalid content was found starting with element '{\"http://schema.testata.common.ws.popso.it/v11\":idConversazione}'. One of '{\"http://schema.testata.common.ws.popso.it/v11\":idOperazione}' is expected.", null) 
			};
		}
		else if(testInfo.getDisplayName().equals("testHandleRequestValidationErrorsMissingTestataTecnica")) {
			
			this.source = new StreamSource(new ByteArrayInputStream(
					Files.readAllBytes(Paths.get("src/test/resources/ws/missingTestataTecnica.xml")))
			);
			this.saajSoapMessageFactory = new SaajSoapMessageFactory(MessageFactory.newInstance());
			this.webServiceMessage = new SoapEnvelopeMessageCreator(this.source).createMessage(this.saajSoapMessageFactory);
			this.messageContext = new DefaultMessageContext(this.webServiceMessage, this.saajSoapMessageFactory);
			
			this.saxParseException = new SAXParseException[] { 
					new SAXParseException("Validation error: Invalid content was found starting with element '{\"http://schema.testata.common.ws.popso.it/v11\":testataDiContesto}'. One of '{\"http://schema.testata.common.ws.popso.it/v11\":testataTecnica}' is expected.", null) 
			};
		}
		else if(testInfo.getDisplayName().equals("testHandleRequestValidationErrorsMissingTestata")) {
			
			this.source = new StreamSource(new ByteArrayInputStream(
					Files.readAllBytes(Paths.get("src/test/resources/ws/missingTestata.xml")))
			);
			this.saajSoapMessageFactory = new SaajSoapMessageFactory(MessageFactory.newInstance());
			this.webServiceMessage = new SoapEnvelopeMessageCreator(this.source).createMessage(this.saajSoapMessageFactory);
			this.messageContext = new DefaultMessageContext(this.webServiceMessage, this.saajSoapMessageFactory);
			
			this.saxParseException = new SAXParseException[] { 
					new SAXParseException("Validation error: Invalid content was found starting with element '{\"http://schema.lettura.bicomp.ws.popso.it/v1\":saldiCgsReadRequestData}'. One of '{\"http://schema.lettura.bicomp.ws.popso.it/v1\":testata}' is expected.", null) 
			};
		}
		else if(testInfo.getDisplayName().equals("testHandleRequestValidationErrorsMissingException")) {
			
			this.source = new StreamSource(new ByteArrayInputStream(
					Files.readAllBytes(Paths.get("src/test/resources/ws/missingTestata.xml")))
			);
			this.saajSoapMessageFactory = new SaajSoapMessageFactory(MessageFactory.newInstance());
			this.webServiceMessage = new SoapEnvelopeMessageCreator(this.source).createMessage(this.saajSoapMessageFactory);
			this.messageContext = new DefaultMessageContext(this.webServiceMessage, this.saajSoapMessageFactory);
			
			this.saxParseException = new SAXParseException[] {};
		}
		else if(testInfo.getDisplayName().equals("testHandleRequestValidationErrorsEmptyIdConversazione")) {
			
			this.source = new StreamSource(new ByteArrayInputStream(
					Files.readAllBytes(Paths.get("src/test/resources/ws/emptyIdConversazione.xml")))
			);
			this.saajSoapMessageFactory = new SaajSoapMessageFactory(MessageFactory.newInstance());
			this.webServiceMessage = new SoapEnvelopeMessageCreator(this.source).createMessage(this.saajSoapMessageFactory);
			this.messageContext = new DefaultMessageContext(this.webServiceMessage, this.saajSoapMessageFactory);
			
			this.saxParseException = new SAXParseException[] { 
					new SAXParseException("Validation error: The value '' of element 'v11:idConversazione' is not valid. Value '' is not facet-valid with respect to pattern '[^ ].*' for type 'StringaNonVuota'.", null) 
			};
		}
		else if(testInfo.getDisplayName().equals("testHandleRequestValidationErrorsEmptyCodiceApplicazione")) {
			
			this.source = new StreamSource(new ByteArrayInputStream(
					Files.readAllBytes(Paths.get("src/test/resources/ws/emptyCodiceApplicazione.xml")))
			);
			this.saajSoapMessageFactory = new SaajSoapMessageFactory(MessageFactory.newInstance());
			this.webServiceMessage = new SoapEnvelopeMessageCreator(this.source).createMessage(this.saajSoapMessageFactory);
			this.messageContext = new DefaultMessageContext(this.webServiceMessage, this.saajSoapMessageFactory);
			
			this.saxParseException = new SAXParseException[] { 
					new SAXParseException("Validation error: The value '' of element 'v11:codiceApplicazioneChiamante' is not valid. Value '' with length = '0' is not facet-valid with respect to minLength '1' for type 'CodiceApplicazione'.", null) 
			};
		}
		else if(testInfo.getDisplayName().equals("testHandleRequestValidationErrorsEmptyCodiceOperazione")) {
			
			this.source = new StreamSource(new ByteArrayInputStream(
					Files.readAllBytes(Paths.get("src/test/resources/ws/emptyCodiceOperazione.xml")))
			);
			this.saajSoapMessageFactory = new SaajSoapMessageFactory(MessageFactory.newInstance());
			this.webServiceMessage = new SoapEnvelopeMessageCreator(this.source).createMessage(this.saajSoapMessageFactory);
			this.messageContext = new DefaultMessageContext(this.webServiceMessage, this.saajSoapMessageFactory);
			
			this.saxParseException = new SAXParseException[] { 
					new SAXParseException("Validation error: The value '' of element 'v11:codiceOperazione' is not valid. Value '' with length = '0' is not facet-valid with respect to minLength '1' for type 'CodiceOperazione'.", null) 
			};
		}
	}
	
	@Test
	@Order(1)
	@DisplayName("testHandleRequestValidationErrorsMissingSettlementDate")
	void testHandleRequestValidationErrorsMissingSettlementDate() {
		
		InputFaultException fault = assertThrows(InputFaultException.class, () -> {
			this.bicompValidatingInterceptor.handleRequestValidationErrors(this.messageContext, this.saxParseException);
		});
		
		assertThat(fault.getInputFault().getMessaggio()).contains("value '' of element 'v1:SettlementDate' is not valid");
		assertThat(fault.getInputFault().getMessaggio()).contains("is not a valid value for 'date'");
	}
	
	@Test
	@Order(2)
	@DisplayName("testHandleRequestValidationErrorsMissingIdConversazione")
	void testHandleRequestValidationErrorsMissingIdConversazione() {
		
		InputFaultException fault = assertThrows(InputFaultException.class, () -> {
			this.bicompValidatingInterceptor.handleRequestValidationErrors(this.messageContext, this.saxParseException);
		});
		
		assertThat(fault.getInputFault().getMessaggio()).contains("One of '{\"http://schema.testata.common.ws.popso.it/v11\":idConversazione}' is expected.");
		assertThat(fault.getInputFault().getTestata().getTestataTecnica().getIdConversazione()).isNull();
	}
	
	@Test
	@Order(3)
	@DisplayName("testHandleRequestValidationErrorsMissingCodiceOperazione")
	void testHandleRequestValidationErrorsMissingCodiceOperazione() {
		
		InputFaultException fault = assertThrows(InputFaultException.class, () -> {
			this.bicompValidatingInterceptor.handleRequestValidationErrors(this.messageContext, this.saxParseException);
		});
		
		assertThat(fault.getInputFault().getMessaggio()).contains("One of '{\"http://schema.testata.common.ws.popso.it/v11\":codiceOperazione}' is expected.");
		assertThat(fault.getInputFault().getTestata().getTestataTecnica().getIdOperazioneRichiesta().getCodiceOperazione()).isNull();
	}
	
	@Test
	@Order(4)
	@DisplayName("testHandleRequestValidationErrorsMissingCodiceApplicazione")
	void testHandleRequestValidationErrorsMissingCodiceApplicazione() {
		
		InputFaultException fault = assertThrows(InputFaultException.class, () -> {
			this.bicompValidatingInterceptor.handleRequestValidationErrors(this.messageContext, this.saxParseException);
		});
		
		assertThat(fault.getInputFault().getMessaggio()).contains("One of '{\"http://schema.testata.common.ws.popso.it/v11\":codiceApplicazioneChiamante}' is expected.");
		assertThat(fault.getInputFault().getTestata().getTestataTecnica().getIdOperazioneRichiesta().getCodiceApplicazioneChiamante()).isNull();
	}
	
	@Test
	@Order(5)
	@DisplayName("testHandleRequestValidationErrorsMissingIdOperazione")
	void testHandleRequestValidationErrorsMissingIdOperazione() {
		
		InputFaultException fault = assertThrows(InputFaultException.class, () -> {
			this.bicompValidatingInterceptor.handleRequestValidationErrors(this.messageContext, this.saxParseException);
		});
		
		assertThat(fault.getInputFault().getMessaggio()).contains("One of '{\"http://schema.testata.common.ws.popso.it/v11\":idOperazione}' is expected.");
		assertThat(fault.getInputFault().getTestata().getTestataTecnica().getIdOperazioneRichiesta()).isNull();
	}
	
	@Test
	@Order(6)
	@DisplayName("testHandleRequestValidationErrorsMissingTestataTecnica")
	void testHandleRequestValidationErrorsMissingTestataTecnica() {
		
		InputFaultException fault = assertThrows(InputFaultException.class, () -> {
			this.bicompValidatingInterceptor.handleRequestValidationErrors(this.messageContext, this.saxParseException);
		});
		
		assertThat(fault.getInputFault().getMessaggio()).contains("One of '{\"http://schema.testata.common.ws.popso.it/v11\":testataTecnica}' is expected.");
		assertThat(fault.getInputFault().getTestata().getTestataTecnica()).isNull();
	}
	
	@Test
	@Order(7)
	@DisplayName("testHandleRequestValidationErrorsMissingTestata")
	void testHandleRequestValidationErrorsMissingTestata() {
		
		InputFaultException fault = assertThrows(InputFaultException.class, () -> {
			this.bicompValidatingInterceptor.handleRequestValidationErrors(this.messageContext, this.saxParseException);
		});
		
		assertThat(fault.getInputFault().getMessaggio()).contains("One of '{\"http://schema.lettura.bicomp.ws.popso.it/v1\":testata}' is expected.");
		assertThat(fault.getInputFault().getTestata()).isNull();
	}
	
	@Test
	@Order(8)
	@DisplayName("testHandleRequestValidationErrorsMissingException")
	void testHandleRequestValidationErrorsMissingException() {
		
		InputFaultException fault = assertThrows(InputFaultException.class, () -> {
			this.bicompValidatingInterceptor.handleRequestValidationErrors(this.messageContext, this.saxParseException);
		});
		
		assertThat(fault.getInputFault().getMessaggio()).contains("Validation error");
	}
	
	@Test
	@Order(9)
	@DisplayName("testHandleRequestValidationErrorsEmptyIdConversazione")
	void testHandleRequestValidationErrorsEmptyIdConversazione() {
		
		InputFaultException fault = assertThrows(InputFaultException.class, () -> {
			this.bicompValidatingInterceptor.handleRequestValidationErrors(this.messageContext, this.saxParseException);
		});
		
		assertThat(fault.getInputFault().getMessaggio()).contains("value '' of element 'v11:idConversazione' is not valid");
		assertThat(fault.getInputFault().getMessaggio()).contains("is not facet-valid with respect to pattern '[^ ].*' for type 'StringaNonVuota'");
		assertThat(fault.getInputFault().getTestata().getTestataTecnica().getIdConversazione()).isNotNull();
	}
	
	@Test
	@Order(10)
	@DisplayName("testHandleRequestValidationErrorsEmptyCodiceApplicazione")
	void testHandleRequestValidationErrorsEmptyCodiceApplicazione() {
		
		InputFaultException fault = assertThrows(InputFaultException.class, () -> {
			this.bicompValidatingInterceptor.handleRequestValidationErrors(this.messageContext, this.saxParseException);
		});
		
		assertThat(fault.getInputFault().getMessaggio()).contains("value '' of element 'v11:codiceApplicazioneChiamante' is not valid");
		assertThat(fault.getInputFault().getMessaggio()).contains("is not facet-valid with respect to minLength '1' for type 'CodiceApplicazione'");
		assertThat(fault.getInputFault().getTestata().getTestataTecnica().getIdConversazione()).isNotNull();
	}
	
	@Test
	@Order(11)
	@DisplayName("testHandleRequestValidationErrorsEmptyCodiceOperazione")
	void testHandleRequestValidationErrorsEmptyCodiceOperazione() {
		
		InputFaultException fault = assertThrows(InputFaultException.class, () -> {
			this.bicompValidatingInterceptor.handleRequestValidationErrors(this.messageContext, this.saxParseException);
		});
		
		assertThat(fault.getInputFault().getMessaggio()).contains("value '' of element 'v11:codiceOperazione' is not valid");
		assertThat(fault.getInputFault().getMessaggio()).contains("is not facet-valid with respect to minLength '1' for type 'CodiceOperazione'");
		assertThat(fault.getInputFault().getTestata().getTestataTecnica().getIdConversazione()).isNotNull();
	}
	
	@AfterEach
	public void cleanUp() {
		this.saxParseException = new SAXParseException[] {};
	}

}
