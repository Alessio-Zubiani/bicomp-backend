package it.popso.bicomp.ws.endpoint;

import javax.xml.datatype.DatatypeConfigurationException;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.service.TipsService;
import it.popso.bicomp.utils.BicompConstants;
import it.popso.bicomp.utils.DateUtils;
import it.popso.bicomp.utils.WebServiceUtils;
import it.popso.bicomp.ws.exception.ApplicationFaultException;
import it.popso.bicomp.ws.exception.DatiTestataFaultException;
import it.popso.ws.bicomp.scrittura.schema.v1.ReportTipsUpdateRequest;
import it.popso.ws.bicomp.scrittura.schema.v1.ReportTipsUpdateResponse;
import it.popso.ws.bicomp.scrittura.schema.v1.ReportTipsUpdateResponseData;
import jakarta.xml.bind.JAXBElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Endpoint
@RequiredArgsConstructor
public class BicompScritturaEndpoint {
	
	private final TipsService tipsService;
	
	
	@PayloadRoot(namespace = BicompConstants.SCRITTURA_NAMESPACE_URI, localPart = "reportTipsUpdateRequest")
	@ResponsePayload
	public JAXBElement<ReportTipsUpdateResponse> tipsReportUpdate(@RequestPayload JAXBElement<ReportTipsUpdateRequest> req) throws DatatypeConfigurationException {
	
		ReportTipsUpdateRequest request = req.getValue();
		this.setIdConversazione(request.getTestata().getTestataTecnica().getIdConversazione());
		log.info("Called tipsReportUpdate with request value: [{}]", WebServiceUtils.webServiceUtils().xmlToString(request));
		
		if(!WebServiceUtils.webServiceUtils().isCodiceAppliazioneChiamanteValid(request.getTestata())) {
			throw new DatiTestataFaultException(WebServiceUtils.webServiceUtils().createDatiTestataFault(req.getValue(), HttpStatus.BAD_REQUEST.value(), 
				new StringBuilder(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante())
					.append(" non è un CodiceApplicazioneChiamante valido").toString()));
		}
		
		String messageId = request.getReportTipsUpdateRequestData().getMessageId();
		try {
			this.tipsService.updateFlagElaborato(messageId);
		}
		catch(ResourceNotFoundException e) {
			throw new ApplicationFaultException(WebServiceUtils.webServiceUtils().createApplicationFault(req.getValue(), 
					HttpStatus.BAD_REQUEST.value(), e.getMessage()));
		}
		
		ReportTipsUpdateResponse response = BicompConstants.BICOMP_SCRITTURA_FACTORY.createReportTipsUpdateResponse();
		response.setTestata(WebServiceUtils.webServiceUtils().createTestataOutput(req.getValue()));
		
		ReportTipsUpdateResponseData responseData = BicompConstants.BICOMP_SCRITTURA_FACTORY.createReportTipsUpdateResponseData();
		responseData.setResponseTimestamp(DateUtils.dateUtils().currentTimestamp());
		responseData.setCorrelationKey(request.getReportTipsUpdateRequestData().getCorrelationKey());
		responseData.setMessageId(messageId);
		responseData.setReturnCode("0");
		responseData.setReturnDescription("OK");
		
		response.setReportTipsUpdateResponseData(responseData);

		return BicompConstants.BICOMP_SCRITTURA_FACTORY.createReportTipsUpdateResponse(response);
	}
	
	private void setIdConversazione(String idConversazione) {
		MDC.put(BicompConstants.MDC_ID_CONVERSAZIONE, idConversazione);
	}

}
