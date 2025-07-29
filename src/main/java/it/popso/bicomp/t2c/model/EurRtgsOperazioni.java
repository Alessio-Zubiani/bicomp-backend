package it.popso.bicomp.t2c.model;

import java.io.Serializable;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "EUR_RTGS_OPERAZIONI", schema = "TLM_T2C")
public class EurRtgsOperazioni implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="PROGRESSIVO_OPERAZIONE")
	private BigDecimal progressivoOperazione;

	@Column(name="BIC_ISTITUTO")
	private String bicIstituto;

	@Column(name="CANALE")
	private String canale;

	@Column(name="CODI_CONTO_REGO")
	private String codiContoRego;

	@Column(name="CODICE_CONTROPARTE")
	private String codiceControparte;

	@Column(name="CODICE_CONTROPARTE_SCAMBIANTE")
	private String codiceControparteScambiante;

	@Column(name="CODICE_DIVISA")
	private String codiceDivisa;

	@Column(name="CODICE_FAMIGLIA")
	private String codiceFamiglia;

	@Column(name="CODICE_FAMIGLIA_BASE")
	private String codiceFamigliaBase;

	@Column(name="CODICE_FILIALE")
	private String codiceFiliale;

	@Column(name="CODICE_TIPOLOGIA")
	private String codiceTipologia;

	@Column(name="CODICE_TRAMITATA")
	private String codiceTramitata;

	@Column(name="CODICE_TRAMITATA_CONTROPARTE")
	private String codiceTramitataControparte;

	@Column(name="CONFERMA_REGOLAMENTO")
	private String confermaRegolamento;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CREATION_TS")
	private Date creationTs;

	@Column(name="CREDITOR_NAME")
	private String creditorName;

	@Column(name="DATA_ENTRY")
	private String dataEntry;

	@Temporal(TemporalType.DATE)
	@Column(name="DATA_REGOLAMENTO")
	private Date dataRegolamento;

	@Column(name="DATI_APPL_BANCARIA")
	private String datiApplBancaria;

	@Column(name="DEBITOR_NAME")
	private String debitorName;

	private String delivered;

	private String descrizione;

	@Column(name="FLAG_CONTABILIZZAZIONE")
	private String flagContabilizzazione;

	@Column(name="FLAG_DAILY_EVENTS")
	private String flagDailyEvents;

	@Column(name="FLAG_ELABORATO")
	private String flagElaborato;

	@Column(name="FLAG_RILASCIO_ESITI_KB")
	private String flagRilascioEsitiKb;

	@Column(name="FLAG_TIPO_PAGAMENTI")
	private String flagTipoPagamenti;

	@Column(name="FORECAST_ABBINATO")
	private String forecastAbbinato;

	@Column(name="FORMATO_MSG_PAGAMENTI")
	private String formatoMsgPagamenti;

	@Column(name="IDEN_OPERAZIONE")
	private String idenOperazione;

	@Column(name="IDEN_OPERAZIONE_ORIG")
	private String idenOperazioneOrig;

	@Column(name="IDEN_TRANSAZIONE")
	private String idenTransazione;

	@Column(name="IMPORTO_OPERAZIONE")
	private BigDecimal importoOperazione;

	private String informazioni;

	@Column(name="INFORMAZIONI_CDTR")
	private String informazioniCdtr;

	@Column(name="INFORMAZIONI_RMT")
	private String informazioniRmt;

	private String inoltrato;

	@Column(name="LOCAL_INSTR_CD")
	private String localInstrCd;

	@Column(name="LOCAL_INSTR_PRTRY")
	private String localInstrPrtry;

	private String priorita;

	@Column(name="PROGRESSIVO_PREV_DETTAGLIO")
	private BigDecimal progressivoPrevDettaglio;

	@Column(name="PRTRY_CD")
	private String prtryCd;

	@Column(name="SETTLEMENT_CODEWORD")
	private String settlementCodeword;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="SETTLEMENT_TIME_FROM")
	private Date settlementTimeFrom;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="SETTLEMENT_TIME_TO")
	private Date settlementTimeTo;

	private String stato;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="TIMBRO_REGOLAMENTO")
	private Date timbroRegolamento;

	@Column(name="TIMED_PAYMENT")
	private String timedPayment;

	@Column(name="TIPO_MESSAGGIO")
	private String tipoMessaggio;

	@Column(name="TIPO_TOTALE")
	private String tipoTotale;

	private String uetr;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="UPDATE_TS")
	private Date updateTs;

}