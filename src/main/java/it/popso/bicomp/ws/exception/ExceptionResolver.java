package it.popso.bicomp.ws.exception;

import java.util.Locale;

import javax.xml.transform.Result;

import org.springframework.stereotype.Component;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.AbstractEndpointExceptionResolver;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapMessage;

import it.popso.bicomp.utils.BicompConstants;
import it.popso.ws.common.testata.fault.schema.v11.ApplicationFault;
import it.popso.ws.common.testata.fault.schema.v11.DatiTestataFault;
import it.popso.ws.common.testata.fault.schema.v11.InputFault;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Component
public class ExceptionResolver extends AbstractEndpointExceptionResolver {

    public ExceptionResolver() throws JAXBException {
    	// Empty constructor
    }
    
    
    @Override
	protected boolean resolveExceptionInternal(MessageContext messageContext, Object endpoint, Exception e) {
    	
    	JAXBContext jaxbContext;
    	Marshaller marshaller;
    	log.error("[{}]", e.getMessage());
    	
    	try {
    		if(e instanceof ApplicationFaultException) {
        		
    			jaxbContext = JAXBContext.newInstance(ApplicationFault.class);
    	    	marshaller = jaxbContext.createMarshaller();
    			
        		final ApplicationFaultException applicationFaultException = (ApplicationFaultException) e;
        		
        		final ApplicationFault applicationFault = applicationFaultException.getApplicationFault();
        		
        		final SoapMessage response = (SoapMessage) messageContext.getResponse();
        		final SoapBody soapBody = response.getSoapBody();
        		
        		final SoapFault soapFault = soapBody.addServerOrReceiverFault("Application Fault Error", Locale.ITALIAN);
        		
        		final SoapFaultDetail faultDetail = soapFault.addFaultDetail();
        		final Result result = faultDetail.getResult();
        		
        		marshaller.marshal(BicompConstants.TESTATA_FAULT_FACTORY.createApplicationFault(applicationFault), result);
        			
    			// We have handled the Exception.
    			return true;
        	}
        	else if(e instanceof DatiTestataFaultException) {
        		
        		jaxbContext = JAXBContext.newInstance(DatiTestataFault.class);
    	    	marshaller = jaxbContext.createMarshaller();
        		
        		final DatiTestataFaultException datiTestataFaultException = (DatiTestataFaultException) e;
        		
        		final DatiTestataFault datiTestataFault = datiTestataFaultException.getDatiTestataFault();
        		
        		final SoapMessage response = (SoapMessage) messageContext.getResponse();
        		final SoapBody soapBody = response.getSoapBody();
        		
        		final SoapFault soapFault = soapBody.addClientOrSenderFault("DatiTestata Fault Error", Locale.ITALIAN);
        		
        		final SoapFaultDetail faultDetail = soapFault.addFaultDetail();
        		final Result result = faultDetail.getResult();
        		
        		marshaller.marshal(BicompConstants.TESTATA_FAULT_FACTORY.createDatiTestataFault(datiTestataFault), result);
        			
    			// We have handled the Exception.
    			return true;
        	}
        	else if(e instanceof InputFaultException) {
        		
        		jaxbContext = JAXBContext.newInstance(InputFault.class);
    	    	marshaller = jaxbContext.createMarshaller();
        		
        		final InputFaultException inputFaultException = (InputFaultException) e;
        		
        		final InputFault inputFault = inputFaultException.getInputFault();
        		
        		final SoapMessage response = (SoapMessage) messageContext.getResponse();
        		final SoapBody soapBody = response.getSoapBody();
        		
        		final SoapFault soapFault = soapBody.addClientOrSenderFault("Input Fault Error", Locale.ITALIAN);
        		
        		final SoapFaultDetail faultDetail = soapFault.addFaultDetail();
        		final Result result = faultDetail.getResult();
        		
        		marshaller.marshal(BicompConstants.TESTATA_FAULT_FACTORY.createInputFault(inputFault), result);
        		
    			// We have handled the Exception.
    			return true;
        	}
    	}
    	catch(final JAXBException j) {
            // Mention what went wrong, but don't fallback or something. Spring will take care of this.
            log.error("Marshalling error: ", j);
        }
    	
		return false;
	}

}
