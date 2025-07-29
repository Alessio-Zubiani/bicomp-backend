package it.popso.bicomp.ws.config;

import java.io.IOException;


import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

import it.popso.bicomp.utils.BicompConstants;
import jakarta.xml.soap.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BicompEndpointInterceptor extends EndpointInterceptorAdapter {    

	@Override
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
    	
        SaajSoapMessage soapResponse = (SaajSoapMessage) messageContext.getResponse();
        this.alterSoapEnvelope(soapResponse);
        
        return super.handleResponse(messageContext, endpoint);
    }
  
    @Override
    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        
        SaajSoapMessage soapResponse = (SaajSoapMessage) messageContext.getResponse();
        this.alterSoapEnvelope(soapResponse);
        
        return super.handleFault(messageContext, endpoint);
    }

    private void alterSoapEnvelope(SaajSoapMessage soapResponse) throws IOException {
        try {
        	SOAPMessage soapMessage = soapResponse.getSaajMessage();
        	
        	SOAPPart soapPart = soapMessage.getSOAPPart();
        	SOAPEnvelope envelope = soapPart.getEnvelope();
        	SOAPHeader header = soapMessage.getSOAPHeader();
        	SOAPBody body = soapMessage.getSOAPBody();
        	SOAPFault fault = body.getFault();
        	
        	envelope.removeNamespaceDeclaration(envelope.getPrefix());
        	envelope.addNamespaceDeclaration(BicompConstants.PREFERRED_PREFIX, BicompConstants.ENVELOPE_NAMESPACE_URI);
        	envelope.setPrefix(BicompConstants.PREFERRED_PREFIX);
        	header.setPrefix(BicompConstants.PREFERRED_PREFIX);
        	body.setPrefix(BicompConstants.PREFERRED_PREFIX);
        	
        	if(fault != null) {
        		fault.removeNamespaceDeclaration(fault.getPrefix());
        		fault.addNamespaceDeclaration(BicompConstants.FAULT_PREFIX, BicompConstants.ENVELOPE_NAMESPACE_URI);
        		fault.setPrefix(BicompConstants.PREFERRED_PREFIX);
        	}
        } 
        catch (SOAPException e) {
        	log.error("SOAPException: [{}]", e);
        }
    }
	
}
