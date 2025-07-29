package it.popso.bicomp.ws.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadValidatingInterceptor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import it.popso.bicomp.utils.BicompConstants;
import it.popso.bicomp.ws.exception.InputFaultException;
import it.popso.ws.common.testata.fault.schema.v11.InputFault;
import it.popso.ws.common.testata.schema.v11.IdOperazione;
import it.popso.ws.common.testata.schema.v11.TestataTecnicaOutput;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BicompValidatingInterceptor extends PayloadValidatingInterceptor {
	private InputFault inputFault;
	

	@Override
	protected boolean handleRequestValidationErrors(MessageContext messageContext, SAXParseException[] errors) {
		
		List<SAXParseException> errorList = Arrays.asList(errors);
		SoapMessage request = (SoapMessage) messageContext.getRequest();
        this.inputFault = BicompConstants.TESTATA_FAULT_FACTORY.createInputFault();
		this.parseRequest(request);
		
		this.inputFault.setCodice("INP 001");
		this.inputFault.setMessaggio(this.parseError(errorList));
		this.inputFault.setLayer("BICOMP");
		throw new InputFaultException(this.inputFault);
	}

	private void parseRequest(SoapMessage request) {
		
		NodeList list = request.getDocument().getDocumentElement().getChildNodes();
		this.iterateNode(list);
	}
	
	private void iterateNode(NodeList list) {
		
		for(int i = 0; i < list.getLength(); ++i) {
			
			Node currentNode = list.item(i);			
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {				
				switch(currentNode.getLocalName()) {
					case "testata":
						this.createTestataOutput();
						break;
					case "testataTecnica":
						this.createTestataTecnicaOutput();
						break;
					case "idOperazione":
						this.createIdOperazione();
						break;
					case "codiceApplicazioneChiamante":
						this.createCodiceApplicazione(currentNode);
						break;
					case "codiceOperazione":
						this.createCodiceOperazione(currentNode);
						break;
					case "idConversazione":
						this.createIdConversazione(currentNode);
						break;
					default:
						break;
				}
			}
		
			this.iterateNode(currentNode.getChildNodes());
		}
	}
	
	private void createTestataOutput() {
		
		this.inputFault.setTestata(BicompConstants.TESTATA_FACTORY.createTestataOutput());
	}
	
	private void createTestataTecnicaOutput() {
		
		TestataTecnicaOutput testataTecnicaOutput = BicompConstants.TESTATA_FACTORY.createTestataTecnicaOutput();
		this.inputFault.getTestata().setTestataTecnica(testataTecnicaOutput);
	}
	
	private void createIdOperazione() {
		
		IdOperazione idOperazione = BicompConstants.TESTATA_FACTORY.createIdOperazione();
		this.inputFault.getTestata().getTestataTecnica().setIdOperazioneRichiesta(idOperazione);
	}
	
	private void createCodiceApplicazione(Node node) {
		
		String codiceApplicazioneChiamante = (!node.getTextContent().isEmpty()) ? node.getTextContent() : "";
		this.inputFault.getTestata().getTestataTecnica().getIdOperazioneRichiesta().setCodiceApplicazioneChiamante(codiceApplicazioneChiamante);
	}
	
	private void createCodiceOperazione(Node node) {
		
		String codiceOperazione = (!node.getTextContent().isEmpty()) ? node.getTextContent() : "";
		this.inputFault.getTestata().getTestataTecnica().getIdOperazioneRichiesta().setCodiceOperazione(codiceOperazione);
	}
	
	private void createIdConversazione(Node node) {
		
		String idConversazione = (!node.getTextContent().isEmpty()) ? node.getTextContent() : "";
		this.inputFault.getTestata().getTestataTecnica().setIdConversazione(idConversazione);
	}
	
	private String parseError(List<SAXParseException> errorList) {
		
		errorList.forEach(e -> log.error("", e));
		
		String validation;
		String type;
		if(errorList.size() > 1) {
			validation = (errorList.get(0).getMessage().split(": ")[1]).substring(0, 1).toUpperCase() + 
					(errorList.get(0).getMessage().split(": ")[1]).substring(1);
			type = (errorList.get(1).getMessage().split(": ")[1]).substring(0, 1).toUpperCase() + 
					(errorList.get(1).getMessage().split(": ")[1]).substring(1);
		}
		else if(errorList.size() == 1) {
			validation = "";
			type = (errorList.get(0).getMessage().split(": ")[1]).substring(0, 1).toUpperCase() + 
					(errorList.get(0).getMessage().split(": ")[1]).substring(1);
		}
		else {
			validation = "";
			type = "Validation error";
		}

		return new StringBuilder(type).append(" ").append(validation).toString();
	}

}
