package it.popso.bicomp.ws.endpoint;

import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.CgsLmrLacBulkStatement;
import it.popso.bicomp.model.TipsCamt052BankAccountReport;
import it.popso.bicomp.model.TipsCamt053BankAccountStatementEntry;
import it.popso.bicomp.service.CgsService;
import it.popso.bicomp.service.TipsService;
import it.popso.bicomp.utils.BicompConstants;
import it.popso.bicomp.utils.DateUtils;
import it.popso.bicomp.utils.WebServiceUtils;
import it.popso.bicomp.ws.exception.ApplicationFaultException;
import it.popso.bicomp.ws.exception.DatiTestataFaultException;
import it.popso.ws.bicomp.lettura.schema.v1.CgsLac;
import it.popso.ws.bicomp.lettura.schema.v1.SaldiCgsReadRequest;
import it.popso.ws.bicomp.lettura.schema.v1.SaldiCgsReadResponse;
import it.popso.ws.bicomp.lettura.schema.v1.SaldiCgsReadResponseData;
import it.popso.ws.bicomp.lettura.schema.v1.SaldiTipsReadRequest;
import it.popso.ws.bicomp.lettura.schema.v1.SaldiTipsReadResponse;
import it.popso.ws.bicomp.lettura.schema.v1.SaldiTipsReadResponseData;
import it.popso.ws.bicomp.lettura.schema.v1.TipsPayment;
import it.popso.ws.bicomp.lettura.schema.v1.TipsReport;
import it.popso.ws.bicomp.lettura.schema.v1.UltimoPagamentoTipsReadRequest;
import it.popso.ws.bicomp.lettura.schema.v1.UltimoPagamentoTipsReadResponse;
import it.popso.ws.bicomp.lettura.schema.v1.UltimoPagamentoTipsReadResponseData;
import it.popso.ws.bicomp.lettura.schema.v1.UltimoSaldoCgsReadRequest;
import it.popso.ws.bicomp.lettura.schema.v1.UltimoSaldoCgsReadResponse;
import it.popso.ws.bicomp.lettura.schema.v1.UltimoSaldoCgsReadResponseData;
import it.popso.ws.bicomp.schema.v1.TService;
import it.popso.ws.bicomp.schema.v1.TSide;
import it.popso.ws.bicomp.schema.v1.TTransactionCode;
import it.popso.ws.bicomp.schema.v1.TTransactionCodeFamily;
import jakarta.xml.bind.JAXBElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Endpoint
@RequiredArgsConstructor
public class BicompLetturaEndpoint {
	
	private final CgsService cgsService;
	private final TipsService tipsService;
	
	
	@PayloadRoot(namespace = BicompConstants.LETTURA_NAMESPACE_URI, localPart = "saldiCgsReadRequest")
	@ResponsePayload
	public JAXBElement<SaldiCgsReadResponse> saldiCgsRead(@RequestPayload JAXBElement<SaldiCgsReadRequest> req) throws DatatypeConfigurationException {
	
		SaldiCgsReadRequest request = req.getValue();
		this.setIdConversazione(request.getTestata().getTestataTecnica().getIdConversazione());
		log.info("Called saldiCgsRead with request value: [{}]", WebServiceUtils.webServiceUtils().xmlToString(request));
		
		if(!WebServiceUtils.webServiceUtils().isCodiceAppliazioneChiamanteValid(request.getTestata())) {
			throw new DatiTestataFaultException(WebServiceUtils.webServiceUtils().createDatiTestataFault(req.getValue(), HttpStatus.BAD_REQUEST.value(), 
				new StringBuilder(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante())
					.append(BicompConstants.CODICE_APPLICAZIONE_NON_VALIDO_MESSAGE).toString()));
		}
		
		if(!this.isSettlementDateValid(request.getSaldiCgsReadRequestData().getSettlementDate())) {
			throw new ApplicationFaultException(WebServiceUtils.webServiceUtils().createApplicationFault(req.getValue(), HttpStatus.BAD_REQUEST.value(), 
					new StringBuilder(request.getSaldiCgsReadRequestData().getSettlementDate().toString())
						.append(BicompConstants.SETTLEMENT_DATE_NON_VALIDA_MESSAGE).toString()));
		}
		
		Date settlementDate = DateUtils.dateUtils().xmlGregorianToDate(request.getSaldiCgsReadRequestData().getSettlementDate());
		List<CgsLmrLacBulkStatement> list = this.cgsService.getSaldiCgs(settlementDate);
		
		SaldiCgsReadResponse response = BicompConstants.BICOMP_LETTURA_FACTORY.createSaldiCgsReadResponse();
		response.setTestata(WebServiceUtils.webServiceUtils().createTestataOutput(req.getValue()));
		
		SaldiCgsReadResponseData responseData = BicompConstants.BICOMP_LETTURA_FACTORY.createSaldiCgsReadResponseData();
		responseData.setResponseTimestamp(DateUtils.dateUtils().currentTimestamp());
		responseData.setCorrelationKey(request.getSaldiCgsReadRequestData().getCorrelationKey());
		
		if(list.isEmpty()) {
			log.warn("Nessun saldo CGS trovato per SettlementDate: [{}]", request.getSaldiCgsReadRequestData().getSettlementDate());
		}
		else {
			list.forEach(c -> {
				log.debug("[{}]", c);
				
				try {
					responseData.getCgsLac().add(this.createCgsLac(c));
				}
				catch(DatatypeConfigurationException e) {
					log.error("", e);
					throw new ApplicationFaultException(WebServiceUtils.webServiceUtils().createApplicationFault(req.getValue(), 
							HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
				}
			});
		}
		
		responseData.setReturnCode("0");
		responseData.setReturnDescription("OK");
		
		response.setSaldiCgsReadResponseData(responseData);

		return BicompConstants.BICOMP_LETTURA_FACTORY.createSaldiCgsReadResponse(response);
	}
	
	@PayloadRoot(namespace = BicompConstants.LETTURA_NAMESPACE_URI, localPart = "saldiTipsReadRequest")
	@ResponsePayload
	public JAXBElement<SaldiTipsReadResponse> saldiTipsRead(@RequestPayload JAXBElement<SaldiTipsReadRequest> req) throws DatatypeConfigurationException {
	
		SaldiTipsReadRequest request = req.getValue();
		this.setIdConversazione(request.getTestata().getTestataTecnica().getIdConversazione());
		log.info("Called saldiTipsRead with request value: [{}]", WebServiceUtils.webServiceUtils().xmlToString(request));
		
		if(!WebServiceUtils.webServiceUtils().isCodiceAppliazioneChiamanteValid(request.getTestata())) {
			throw new DatiTestataFaultException(WebServiceUtils.webServiceUtils().createDatiTestataFault(req.getValue(), HttpStatus.BAD_REQUEST.value(), 
				new StringBuilder(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante())
					.append(BicompConstants.CODICE_APPLICAZIONE_NON_VALIDO_MESSAGE).toString()));
		}
		
		List<TipsCamt052BankAccountReport> list = this.tipsService.getSaldiTips();
		
		SaldiTipsReadResponse response = BicompConstants.BICOMP_LETTURA_FACTORY.createSaldiTipsReadResponse();
		response.setTestata(WebServiceUtils.webServiceUtils().createTestataOutput(req.getValue()));
		
		SaldiTipsReadResponseData responseData = BicompConstants.BICOMP_LETTURA_FACTORY.createSaldiTipsReadResponseData();
		responseData.setResponseTimestamp(DateUtils.dateUtils().currentTimestamp());
		responseData.setCorrelationKey(request.getSaldiTipsReadRequestData().getCorrelationKey());
		
		if(list.isEmpty()) {
			log.warn("Nessun report TIPS trovato");
		}
		else {
			list.forEach(t -> {
				log.debug("[{}]", t);
				
				try {
					TipsReport tipsReport = BicompConstants.BICOMP_LETTURA_FACTORY.createTipsReport();
					tipsReport.setMessageId(t.getMsgId());
					tipsReport.setMessageCreationDateTime(DateUtils.dateUtils().dateToXmlGregorian(t.getCreationDateTime()));
					tipsReport.setReportName(t.getReportName());
					tipsReport.setReportCreationDateTime(DateUtils.dateUtils().dateToXmlGregorian(t.getCreationDateTime()));
					tipsReport.setAccountId(t.getAccountId());
					tipsReport.setAccountCurrency(t.getCurrency());
					tipsReport.setAccountOwner(t.getAccountOwner());
					tipsReport.setOpeningBalance(t.getOpeningBalance().setScale(2, RoundingMode.HALF_UP));
					tipsReport.setOpeningBalanceCurrency(t.getCurrency());
					tipsReport.setOpeningBalanceSide(t.getOpeningBalanceSide() == 'C' ? TSide.C : TSide.D);
					tipsReport.setOpeningBalanceDate(DateUtils.dateUtils().dateToXmlGregorian(t.getSettlementDate()));
					tipsReport.setClosingBalance(t.getClosingBalance().setScale(2, RoundingMode.HALF_UP));
					tipsReport.setClosingBalanceCurrency(t.getCurrency());
					tipsReport.setClosingBalanceSide(t.getClosingBalanceSide() == 'C' ? TSide.C : TSide.D);
					tipsReport.setClosingBalanceDate(DateUtils.dateUtils().dateToXmlGregorian(t.getSettlementDate()));
					tipsReport.setTotalCredit(t.getTotalCreditOperation().setScale(2, RoundingMode.HALF_UP));
					tipsReport.setTotalDebit(t.getTotalDebitOperation().setScale(2, RoundingMode.HALF_UP));
					
					responseData.getTipsReport().add(tipsReport);
				}
				catch(DatatypeConfigurationException e) {
					log.error("", e);
					throw new ApplicationFaultException(WebServiceUtils.webServiceUtils().createApplicationFault(req.getValue(), 
							HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
				}
			});
		}
		
		responseData.setReturnCode("0");
		responseData.setReturnDescription("OK");
		
		response.setSaldiTipsReadResponseData(responseData);

		return BicompConstants.BICOMP_LETTURA_FACTORY.createSaldiTipsReadResponse(response);
	}
	
	@PayloadRoot(namespace = BicompConstants.LETTURA_NAMESPACE_URI, localPart = "ultimoSaldoCgsReadRequest")
	@ResponsePayload
	public JAXBElement<UltimoSaldoCgsReadResponse> ultimoSaldoCgsRead(@RequestPayload JAXBElement<UltimoSaldoCgsReadRequest> req) throws DatatypeConfigurationException {
	
		UltimoSaldoCgsReadRequest request = req.getValue();
		this.setIdConversazione(request.getTestata().getTestataTecnica().getIdConversazione());
		log.info("Called utlimoSaldoCgsRead with request value: [{}]", WebServiceUtils.webServiceUtils().xmlToString(request));
		
		if(!WebServiceUtils.webServiceUtils().isCodiceAppliazioneChiamanteValid(request.getTestata())) {
			throw new DatiTestataFaultException(WebServiceUtils.webServiceUtils().createDatiTestataFault(req.getValue(), HttpStatus.BAD_REQUEST.value(), 
				new StringBuilder(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante())
					.append(BicompConstants.CODICE_APPLICAZIONE_NON_VALIDO_MESSAGE).toString()));
		}
		
		if(!this.isSettlementDateValid(request.getUltimoSaldoCgsReadRequestData().getSettlementDate())) {
			throw new ApplicationFaultException(WebServiceUtils.webServiceUtils().createApplicationFault(req.getValue(), HttpStatus.BAD_REQUEST.value(), 
					new StringBuilder(request.getUltimoSaldoCgsReadRequestData().getSettlementDate().toString())
						.append(BicompConstants.SETTLEMENT_DATE_NON_VALIDA_MESSAGE).toString()));
		}
		
		Date settlementDate = DateUtils.dateUtils().xmlGregorianToDate(request.getUltimoSaldoCgsReadRequestData().getSettlementDate());
		CgsLmrLacBulkStatement c;
		try {
			c = this.cgsService.getLastCgsBalance(settlementDate);
		}
		catch(ResourceNotFoundException e) {
			throw new ApplicationFaultException(WebServiceUtils.webServiceUtils().createApplicationFault(req.getValue(), HttpStatus.NOT_FOUND.value(), 
					new StringBuilder("Nessun saldo CGS trovato per SettlementDate: [")
						.append(request.getUltimoSaldoCgsReadRequestData().getSettlementDate())
						.append("]").toString()));
		}
		
		UltimoSaldoCgsReadResponse response = BicompConstants.BICOMP_LETTURA_FACTORY.createUltimoSaldoCgsReadResponse();
		response.setTestata(WebServiceUtils.webServiceUtils().createTestataOutput(req.getValue()));
		
		UltimoSaldoCgsReadResponseData responseData = BicompConstants.BICOMP_LETTURA_FACTORY.createUltimoSaldoCgsReadResponseData();
		responseData.setResponseTimestamp(DateUtils.dateUtils().currentTimestamp());
		responseData.setCorrelationKey(request.getUltimoSaldoCgsReadRequestData().getCorrelationKey());
		
		CgsLac cgsLac = this.createCgsLac(c);
		responseData.setCgsLac(cgsLac);
		
		responseData.setReturnCode("0");
		responseData.setReturnDescription("OK");
		
		response.setUltimoSaldoCgsReadResponseData(responseData);

		return BicompConstants.BICOMP_LETTURA_FACTORY.createUltimoSaldoCgsReadResponse(response);
	}
	
	@PayloadRoot(namespace = BicompConstants.LETTURA_NAMESPACE_URI, localPart = "ultimoPagamentoTipsReadRequest")
	@ResponsePayload
	public JAXBElement<UltimoPagamentoTipsReadResponse> ultimoPagamentoTipsRead(@RequestPayload JAXBElement<UltimoPagamentoTipsReadRequest> req) throws DatatypeConfigurationException {
	
		UltimoPagamentoTipsReadRequest request = req.getValue();
		this.setIdConversazione(request.getTestata().getTestataTecnica().getIdConversazione());
		log.info("Called ultimoPagamentoTipsRead with request value: [{}]", WebServiceUtils.webServiceUtils().xmlToString(request));
		
		if(!WebServiceUtils.webServiceUtils().isCodiceAppliazioneChiamanteValid(request.getTestata())) {
			throw new DatiTestataFaultException(WebServiceUtils.webServiceUtils().createDatiTestataFault(req.getValue(), HttpStatus.BAD_REQUEST.value(), 
				new StringBuilder(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante())
					.append(BicompConstants.CODICE_APPLICAZIONE_NON_VALIDO_MESSAGE).toString()));
		}
		
		if(!this.isSettlementDateValid(request.getUltimoPagamentoTipsReadRequestData().getSettlementDate())) {
			throw new ApplicationFaultException(WebServiceUtils.webServiceUtils().createApplicationFault(req.getValue(), HttpStatus.BAD_REQUEST.value(), 
					new StringBuilder(request.getUltimoPagamentoTipsReadRequestData().getSettlementDate().toString())
						.append(BicompConstants.SETTLEMENT_DATE_NON_VALIDA_MESSAGE).toString()));
		}
		
		Date settlementDate = DateUtils.dateUtils().xmlGregorianToDate(request.getUltimoPagamentoTipsReadRequestData().getSettlementDate());
		TipsCamt053BankAccountStatementEntry t;
		TipsPayment tipsPayment = BicompConstants.BICOMP_LETTURA_FACTORY.createTipsPayment();
		try {
			t = this.tipsService.getLastTipsPaymentByDate(settlementDate);
			tipsPayment.setEntryReference(t.getEntryReference());
			tipsPayment.setSettlementDateTime(DateUtils.dateUtils().dateToXmlGregorian(t.getSettlementDateTime()));
			tipsPayment.setPaymentAmount(t.getPaymentAmount().setScale(2, RoundingMode.HALF_UP));
			tipsPayment.setCurrency(t.getCurrency());
			tipsPayment.setStatus(t.getStatus());
			tipsPayment.setSide(t.getSide() == 'C' ? TSide.C : TSide.D);
			tipsPayment.setTransactionCode(t.getBankTransactionCode().equals("PMNT") ? TTransactionCode.PMNT : null);
			tipsPayment.setTransactionCodeFamily(this.getTransactionCodeFamily(t.getBankTransactionCodeFamily()));
			tipsPayment.setDebitor(t.getDebitorBic());
			tipsPayment.setCreditor(t.getCreditorBic());
		}
		catch(ResourceNotFoundException e) {
			log.warn("Nessun pagamento TIPS trovato per SettlementDate: [{}]", request.getUltimoPagamentoTipsReadRequestData().getSettlementDate());
		}
		
		UltimoPagamentoTipsReadResponse response = BicompConstants.BICOMP_LETTURA_FACTORY.createUltimoPagamentoTipsReadResponse();
		response.setTestata(WebServiceUtils.webServiceUtils().createTestataOutput(req.getValue()));
		
		UltimoPagamentoTipsReadResponseData responseData = BicompConstants.BICOMP_LETTURA_FACTORY.createUltimoPagamentoTipsReadResponseData();
		responseData.setResponseTimestamp(DateUtils.dateUtils().currentTimestamp());
		responseData.setCorrelationKey(request.getUltimoPagamentoTipsReadRequestData().getCorrelationKey());
		responseData.setTipsPayment(tipsPayment);
		responseData.setReturnCode("0");
		responseData.setReturnDescription("OK");
		
		response.setUltimoPagamentoTipsReadResponseData(responseData);

		return BicompConstants.BICOMP_LETTURA_FACTORY.createUltimoPagamentoTipsReadResponse(response);
	}
	
	private boolean isSettlementDateValid(XMLGregorianCalendar xgc) {
		
		Date date = DateUtils.dateUtils().xmlGregorianToDate(xgc);
		log.debug("Date: [{}]", date);
		
		Calendar calendar = Calendar.getInstance();
	    calendar.set(Calendar.HOUR_OF_DAY, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MILLISECOND, 0);
		
		return date.compareTo(calendar.getTime()) <= 0;
	}
	
	private TTransactionCodeFamily getTransactionCodeFamily(String transactionCodeFamily) {
		
		switch(transactionCodeFamily) {
			case "IRCT":
				return TTransactionCodeFamily.IRCT;
			case "RCDT":
				return TTransactionCodeFamily.RCDT;
			case "ICDT":
				return TTransactionCodeFamily.ICDT;
			case "RRCT":
				return TTransactionCodeFamily.RRCT;
			default:
				return null;
		}
	}
	
	private CgsLac createCgsLac(CgsLmrLacBulkStatement c) throws DatatypeConfigurationException {
		
		CgsLac cgsLac = BicompConstants.BICOMP_LETTURA_FACTORY.createCgsLac();
		cgsLac.setSendingInstitute(c.getCgsLmrLacBulk().getCgsLmr().getSendingInstitute());
		cgsLac.setReceivingInstitute(c.getCgsLmrLacBulk().getCgsLmr().getReceivingInstitute());
		cgsLac.setServiceId(TService.SCT);
		cgsLac.setSettlementDate(DateUtils.dateUtils().dateToXmlGregorian(c.getCgsLmrLacBulk().getCgsLmr().getFileSettlementDate()));
		cgsLac.setLac(c.getCgsLmrLacBulk().getCgsLmr().getFileLac());
		cgsLac.setFileCreationDateTime(DateUtils.dateUtils().timestampToXmlGregorian(c.getCreationDateTime()));
		cgsLac.setMessageId(c.getCgsLmrLacBulk().getBulkReference());
		cgsLac.setAccountOwnerBIC(c.getAccountOwner());
		cgsLac.setOpeningBalance(c.getOpeningBalance().setScale(2, RoundingMode.HALF_UP));
		cgsLac.setOpeningBalanceSide(c.getOpeningBalanceSide() == 'C' ? TSide.C : TSide.D);
		cgsLac.setClosingBalance(c.getClosingBalance().setScale(2, RoundingMode.HALF_UP));
		cgsLac.setClosingBalanceSide(c.getClosingBalanceSide() == 'C' ? TSide.C : TSide.D);
		
		return cgsLac;
	}
	
	private void setIdConversazione(String idConversazione) {
		MDC.put(BicompConstants.MDC_ID_CONVERSAZIONE, idConversazione);
	}

}
