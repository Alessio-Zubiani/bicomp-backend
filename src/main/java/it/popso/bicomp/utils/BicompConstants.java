package it.popso.bicomp.utils;

import java.util.Arrays;
import java.util.List;

import it.popso.ws.common.testata.schema.v11.ObjectFactory;

public final class BicompConstants {
	
	private BicompConstants() {}
	
	
	/*
	 * Aspect static final variable
	 */
	public static final String BEFORE_METHOD_LOG_FORMAT = "Starting [{}]";
	public static final String AFTER_METHOD_LOG_FORMAT = "Finished [{}]";
	public static final String BEFORE_METHOD_NO_LOG_FORMAT = "Starting [{}] {}";
	public static final String ERROR_LOG_FORMAT = "Exception [{}] - {}";
	public static final String MDC_UUID = "uuid";
	public static final String MDC_ID_USER = "id_user";
	public static final String MDC_IP_ADDRESS = "ip_address";
	public static final String MDC_BANCA = "banca";
	public static final String MDC_APPLICAZIONE = "applicazione";
	public static final String MDC_NODO = "nodo";
	public static final String MDC_ID_CONVERSAZIONE = "idConversazione";
	
	
	
	/*
	 * Service and Controller static final variable
	 */
	public static final String SM_USER = "sm_user";
	public static final String POPSO_SSO_PROFILO = "popso.sso.profilo";
	public static final String LOG_CRLF = "[\r\n]";
	public static final String SETTLEMENT_DATE_TIME_SORTING = "settlementDateTime";
	public static final String SETTLEMENT_DATE_SORTING = "settlementDate";
	public static final String CGS_PLCR_BULK_STATEMENT = "cgsLmrPlcrBulkStatement";
	public static final String CGS_LTIN_BULK_STATEMENT = "cgsLmrLtinBulkStatement";
	public static final String RT1_PARTICIPANT_HEADER = "rt1PsrParticipantHeader";
	public static final String RT1_SETTLEMENT_BIC = "rt1PsrSettlementBic";
	public static final String RT1_FILE_HEADER = "rt1PsrFileHeader";
	public static final String SETTLEMENT_DATE = "settlementDate";
	public static final String SIDE = "side";
	public static final String FILE_LAC = "fileLac";
	public static final String STATUS = "status";
	public static final String ACCEPTANCE_DATE_TIME = "acceptanceDateTime";
	public static final String PAYMENT_AMOUNT = "paymentAmount";
	public static final String SETTLEMENT_DATE_TIME = "settlementDateTime";
	
	
	/*
	 * WebService static final variable
	 */
	public static final String ENVELOPE_NAMESPACE_URI = "http://schemas.xmlsoap.org/soap/envelope/";
	public static final String PREFERRED_PREFIX = "soapenv";
	public static final String FAULT_PREFIX = "fault";
	public static final ObjectFactory TESTATA_FACTORY = new ObjectFactory();
	public static final it.popso.ws.common.testata.fault.schema.v11.ObjectFactory TESTATA_FAULT_FACTORY = new it.popso.ws.common.testata.fault.schema.v11.ObjectFactory();
	public static final String LETTURA_NAMESPACE_URI = "http://schema.lettura.bicomp.ws.popso.it/v1";
	public static final String SCRITTURA_NAMESPACE_URI = "http://schema.scrittura.bicomp.ws.popso.it/v1";
	public static final String CODICE_APPLICAZIONE_NON_VALIDO_MESSAGE = " non è un CodiceApplicazioneChiamante valido";
	public static final String SETTLEMENT_DATE_NON_VALIDA_MESSAGE = " non e' una SettlementDate valida";
	public static final it.popso.ws.bicomp.lettura.schema.v1.ObjectFactory BICOMP_LETTURA_FACTORY = new it.popso.ws.bicomp.lettura.schema.v1.ObjectFactory();
	public static final it.popso.ws.bicomp.scrittura.schema.v1.ObjectFactory BICOMP_SCRITTURA_FACTORY = new it.popso.ws.bicomp.scrittura.schema.v1.ObjectFactory();
	
	
	/*
	 * DateUtils static final variable
	 */
	public static final String ZONE_ID = "Europe/Rome";
	
	
	/*
	 * FileManager static final variable
	 */
	public static final String HPSR = "HPSR";
	public static final String TPSR = "TPSR";
	public static final String PSBH = "PSBH";
	public static final String PSBT = "PSBT";
	public static final String PDPH = "PDPH";
	public static final int PDPH_LENGTH = 340;
	public static final String PDPT = "PDPT";
	public static final int PDPT_LENGTH = 4;
	public static final String PDPB = "PDPB";
	public static final int PDPB_LENGTH = 64;
	
	
	/*
	 * WebServiceUtils static final variable
	 */
	public static final List<String> CODICE_APPLICAZIONE_CHIAMANTE = Arrays.asList("CONTCONS", "FOREAQUA");
	public static final String SALDI_CGS_READ = "SALDI_CGS_READ";
	public static final String SALDI_TIPS_READ = "SALDI_TIPS_READ";
	public static final String ULTIMO_SALDO_CGS_READ = "ULTIMO_SALDO_CGS_READ";
	public static final String ULTIMO_PAGAMENTO_TIPS_READ = "ULTIMO_PAGAMENTO_TIPS_READ";
	public static final String TIPS_REPORT_UPDATE = "TIPS_REPORT_UPDATE";
	
	
	/*
	 * Job bean static final variable
	 */
	public static final String FAILURE = "FAILURE";
	public static final String BACKUP_FOLDER = "backup";
	public static final String JOBNAME = "jobName";
	
	
	/*
	 * File type
	 */
	public static final String CODE_SET = "CODE_SET";
	public static final String RT1 = "RT1";
	public static final String TIPS_CAMT052 = "TIPS_CAMT052";
	public static final String TIPS_CAMT053 = "TIPS_CAMT053";
	public static final String CGS = "CGS";
	
	
	/*
	 * Delete files string
	 */
	public static final String SUCCESSFUL_DELETING = "Successfully deleted following files: ";
	
	
	/*
	 * Service static final variables
	 */
	public static final String ALREADY_EXISTS = "] already exists";
	
}
