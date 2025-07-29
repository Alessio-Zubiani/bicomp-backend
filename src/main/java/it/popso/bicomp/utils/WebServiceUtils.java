package it.popso.bicomp.utils;

import java.io.StringWriter;

import jakarta.xml.bind.JAXB;

import it.popso.ws.bicomp.lettura.schema.v1.SaldiCgsReadRequest;
import it.popso.ws.bicomp.lettura.schema.v1.SaldiTipsReadRequest;
import it.popso.ws.bicomp.lettura.schema.v1.UltimoPagamentoTipsReadRequest;
import it.popso.ws.bicomp.lettura.schema.v1.UltimoSaldoCgsReadRequest;
import it.popso.ws.bicomp.scrittura.schema.v1.ReportTipsUpdateRequest;
import it.popso.ws.common.testata.fault.schema.v11.ApplicationFault;
import it.popso.ws.common.testata.fault.schema.v11.DatiTestataFault;
import it.popso.ws.common.testata.schema.v11.IdOperazione;
import it.popso.ws.common.testata.schema.v11.TestataInput;
import it.popso.ws.common.testata.schema.v11.TestataOutput;
import it.popso.ws.common.testata.schema.v11.TestataTecnicaOutput;

public class WebServiceUtils {
	
	public static WebServiceUtils webServiceUtils() {
		return new WebServiceUtils();
	}

	
	public DatiTestataFault createDatiTestataFault(Object o, int errorCode, String errorMessage) {
		
		it.popso.ws.common.testata.fault.schema.v11.ObjectFactory objectFactory = new it.popso.ws.common.testata.fault.schema.v11.ObjectFactory();
		DatiTestataFault datiTestataFault = objectFactory.createDatiTestataFault();
		
		if(o instanceof SaldiCgsReadRequest) {
			SaldiCgsReadRequest request = (SaldiCgsReadRequest) o;
			datiTestataFault.setTestata(this.createTestataOutput(request));
		} 
		else if(o instanceof SaldiTipsReadRequest) {
			SaldiTipsReadRequest request = (SaldiTipsReadRequest) o;
			datiTestataFault.setTestata(this.createTestataOutput(request));
		}
		else if(o instanceof UltimoSaldoCgsReadRequest) {
			UltimoSaldoCgsReadRequest request = (UltimoSaldoCgsReadRequest) o;
			datiTestataFault.setTestata(this.createTestataOutput(request));
		}
		else if(o instanceof UltimoPagamentoTipsReadRequest) {
			UltimoPagamentoTipsReadRequest request = (UltimoPagamentoTipsReadRequest) o;
			datiTestataFault.setTestata(this.createTestataOutput(request));
		}
		else if(o instanceof ReportTipsUpdateRequest) {
			ReportTipsUpdateRequest request = (ReportTipsUpdateRequest) o;
			datiTestataFault.setTestata(this.createTestataOutput(request));
		}
		
		datiTestataFault.setCodice(String.valueOf(errorCode));
		datiTestataFault.setMessaggio(errorMessage);
		
		return datiTestataFault;
	}
	
	public ApplicationFault createApplicationFault(Object o, int errorCode, String errorMessage) {
		
		it.popso.ws.common.testata.fault.schema.v11.ObjectFactory objectFactory = new it.popso.ws.common.testata.fault.schema.v11.ObjectFactory();
		ApplicationFault applicationFault = objectFactory.createApplicationFault();
		
		if(o instanceof SaldiCgsReadRequest) {
			SaldiCgsReadRequest request = (SaldiCgsReadRequest) o;
			applicationFault.setTestata(this.createTestataOutput(request));
		}
		else if(o instanceof SaldiTipsReadRequest) {
			SaldiTipsReadRequest request = (SaldiTipsReadRequest) o;
			applicationFault.setTestata(this.createTestataOutput(request));
		}
		else if(o instanceof UltimoSaldoCgsReadRequest) {
			UltimoSaldoCgsReadRequest request = (UltimoSaldoCgsReadRequest) o;
			applicationFault.setTestata(this.createTestataOutput(request));
		}
		else if(o instanceof UltimoPagamentoTipsReadRequest) {
			UltimoPagamentoTipsReadRequest request = (UltimoPagamentoTipsReadRequest) o;
			applicationFault.setTestata(this.createTestataOutput(request));
		}
		else if(o instanceof ReportTipsUpdateRequest) {
			ReportTipsUpdateRequest request = (ReportTipsUpdateRequest) o;
			applicationFault.setTestata(this.createTestataOutput(request));
		}
		
		applicationFault.setCodice(String.valueOf(errorCode));
		applicationFault.setMessaggio(errorMessage);
		
		return applicationFault;
	}
	
	public TestataOutput createTestataOutput(Object o) {

		it.popso.ws.common.testata.schema.v11.ObjectFactory objectFactory = new it.popso.ws.common.testata.schema.v11.ObjectFactory();
		TestataOutput testataOutput = objectFactory.createTestataOutput();
		TestataTecnicaOutput testataTecnicaOutput = objectFactory.createTestataTecnicaOutput();
		IdOperazione idOperazione = objectFactory.createIdOperazione();
		
		if(o instanceof SaldiCgsReadRequest) {
			SaldiCgsReadRequest request = (SaldiCgsReadRequest) o;
			idOperazione.setCodiceApplicazioneChiamante(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante());
			idOperazione.setCodiceOperazione(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceOperazione());
			
			testataTecnicaOutput.setIdOperazioneRichiesta(idOperazione);
			testataTecnicaOutput.setIdConversazione(request.getTestata().getTestataTecnica().getIdConversazione());
			testataTecnicaOutput.setCodiceOperazioneRisposta(BicompConstants.SALDI_CGS_READ);
		}
		else if(o instanceof SaldiTipsReadRequest) {
			SaldiTipsReadRequest request = (SaldiTipsReadRequest) o;
			idOperazione.setCodiceApplicazioneChiamante(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante());
			idOperazione.setCodiceOperazione(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceOperazione());
			
			testataTecnicaOutput.setIdOperazioneRichiesta(idOperazione);
			testataTecnicaOutput.setIdConversazione(request.getTestata().getTestataTecnica().getIdConversazione());
			testataTecnicaOutput.setCodiceOperazioneRisposta(BicompConstants.SALDI_TIPS_READ);
		}
		else if(o instanceof UltimoSaldoCgsReadRequest) {
			UltimoSaldoCgsReadRequest request = (UltimoSaldoCgsReadRequest) o;
			idOperazione.setCodiceApplicazioneChiamante(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante());
			idOperazione.setCodiceOperazione(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceOperazione());
			
			testataTecnicaOutput.setIdOperazioneRichiesta(idOperazione);
			testataTecnicaOutput.setIdConversazione(request.getTestata().getTestataTecnica().getIdConversazione());
			testataTecnicaOutput.setCodiceOperazioneRisposta(BicompConstants.ULTIMO_SALDO_CGS_READ);
		}
		else if(o instanceof UltimoPagamentoTipsReadRequest) {
			UltimoPagamentoTipsReadRequest request = (UltimoPagamentoTipsReadRequest) o;
			idOperazione.setCodiceApplicazioneChiamante(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante());
			idOperazione.setCodiceOperazione(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceOperazione());
			
			testataTecnicaOutput.setIdOperazioneRichiesta(idOperazione);
			testataTecnicaOutput.setIdConversazione(request.getTestata().getTestataTecnica().getIdConversazione());
			testataTecnicaOutput.setCodiceOperazioneRisposta(BicompConstants.ULTIMO_PAGAMENTO_TIPS_READ);
		}
		else if(o instanceof ReportTipsUpdateRequest) {
			ReportTipsUpdateRequest request = (ReportTipsUpdateRequest) o;
			idOperazione.setCodiceApplicazioneChiamante(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante());
			idOperazione.setCodiceOperazione(request.getTestata().getTestataTecnica().getIdOperazione().getCodiceOperazione());
			
			testataTecnicaOutput.setIdOperazioneRichiesta(idOperazione);
			testataTecnicaOutput.setIdConversazione(request.getTestata().getTestataTecnica().getIdConversazione());
			testataTecnicaOutput.setCodiceOperazioneRisposta(BicompConstants.TIPS_REPORT_UPDATE);
		}
		
		testataOutput.setTestataTecnica(testataTecnicaOutput);
		
		return testataOutput;
	}
	
	public String xmlToString(Object o) {
		
		StringWriter stringWriter = new StringWriter();
		JAXB.marshal(o, stringWriter);
		
		return stringWriter.toString();
	}
	
	public boolean isCodiceAppliazioneChiamanteValid(TestataInput testataInput) {
		
		return BicompConstants.CODICE_APPLICAZIONE_CHIAMANTE.contains(testataInput.getTestataTecnica().getIdOperazione().getCodiceApplicazioneChiamante());
	}
	
}
