package it.popso.bicomp.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.minio.messages.Item;
import it.popso.bicomp.dto.InputStreamWithLength;
import it.popso.bicomp.dto.PageableTipsEntryDto;
import it.popso.bicomp.dto.PageableTipsReportDto;
import it.popso.bicomp.dto.TipsBalanceDto;
import it.popso.bicomp.dto.TipsEntryDto;
import it.popso.bicomp.dto.TipsReportDto;
import it.popso.bicomp.dto.TipsTotalDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.TipsCamt052BankAccountReport;
import it.popso.bicomp.model.TipsCamt053BankAccountStatement;
import it.popso.bicomp.model.TipsCamt053BankAccountStatementEntry;
import it.popso.bicomp.model.TipsCamt053BankStatement;
import it.popso.bicomp.properties.BicompConfig;
import it.popso.bicomp.repository.TipsCamt052BankAccountReportRepository;
import it.popso.bicomp.repository.TipsCamt053BankAccountStatementEntryRepository;
import it.popso.bicomp.repository.TipsCamt053BankAccountStatementRepository;
import it.popso.bicomp.repository.TipsCamt053BankStatementRepository;
import it.popso.bicomp.service.MinioService;
import it.popso.bicomp.service.TipsService;
import it.popso.bicomp.utils.BicompConstants;
import it.popso.bicomp.utils.DateUtils;
import it.popso.bicomp.utils.GenericSpecification;
import it.popso.bicomp.utils.XMLUtils;
import it.popso.bicomp.xjc.tips.camt052.BankToCustomerAccountReportV06;
import it.popso.bicomp.xjc.tips.camt053.AccountStatement61;
import it.popso.bicomp.xjc.tips.camt053.BalanceType12Code1;
import it.popso.bicomp.xjc.tips.camt053.BankToCustomerStatementV06;
import it.popso.bicomp.xjc.tips.camt053.CashBalance71;
import it.popso.bicomp.xjc.tips.camt053.Document;
import it.popso.bicomp.xjc.tips.camt053.ReportEntry81;
import jakarta.xml.bind.JAXBException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@Scope("prototype")
@RequiredArgsConstructor
public class TipsServiceImpl extends BaseServiceImpl implements TipsService {
	
	private final BicompConfig config;
	private final MinioService minioService;
	private final TipsCamt052BankAccountReportRepository tipsCamt052BankAccountReportRepository;
	private final TipsCamt053BankStatementRepository tipsCamt053BankStatementRepository;
	private final TipsCamt053BankAccountStatementRepository tipsCamt053BankAccountStatementRepository;
	private final TipsCamt053BankAccountStatementEntryRepository tipsCamt053BankAccountStatementEntryRepository;
	
	
	@Override
	public List<Item> getCamt052Report() throws IOException {
		
		log.info("Getting list of CAMT.052 files in: [{}]", this.config.getTips().getCamt052Share());
		List<Item> list = this.minioService.getObjectsByPrefixAndSuffix(
				this.config.getMinio().getBucket().getName(), 
				this.config.getTips().getCamt052Share(), 
				this.config.getTips().getCamt052Prefix(), 
				this.config.getTips().getCamt052Suffix(), 
				true);
				
		log.info("Trovati [{}] files da elaborare", list.size());
		
		return list;
	}
	
	@Override
	public List<Item> getCamt053Report() throws IOException {
		
		log.info("Getting list of CAMT.053 files in: [{}]", this.config.getTips().getCamt053Share());
		List<Item> list = this.minioService.getObjectsByPrefixAndSuffix(
				this.config.getMinio().getBucket().getName(), 
				this.config.getTips().getCamt053Share(), 
				this.config.getTips().getCamt053Prefix(), 
				this.config.getTips().getCamt053Suffix(), 
				true);
				
		log.info("Trovati [{}] files da elaborare", list.size());
		
		return list;
	}

	@Override
	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRES_NEW, 
		rollbackFor = { IOException.class, JAXBException.class, DataIntegrityViolationException.class, BicompException.class, JpaSystemException.class }
	)
	public void processCamt052Report(List<Item> list) throws IOException, JAXBException, DataIntegrityViolationException, BicompException, JpaSystemException {

		for(Item i : list) {
			InputStream inputStream = this.minioService.getObject(this.config.getMinio().getBucket().getName(), i.objectName());
			log.info("Elaboro flusso: [{}]", i.objectName());
			
			InputStreamWithLength inputStreamWithLength = this.fromByteArraytoInputStream(inputStream);
			
			if(inputStreamWithLength.getLength() > 0) {
				it.popso.bicomp.xjc.tips.camt052.Document document = XMLUtils.unmarshall(inputStreamWithLength.getInputStream(), it.popso.bicomp.xjc.tips.camt052.Document.class);
				BankToCustomerAccountReportV06 bankReport = document.getBkToCstmrAcctRpt();
				log.info("Report with Message ID: [{}]", bankReport.getGrpHdr().getMsgId());
				
				if(bankReport.getRpt().size() > 1) {
					log.error(new StringBuilder("Attenzione... Presenti [").append(bankReport.getRpt().size()).append("] report. Ammesso solo 1 report per POSOIT22XXX").toString());
					throw new BicompException(new StringBuilder("Attenzione... Presenti [").append(bankReport.getRpt().size()).append("] report. Ammesso solo 1 report per POSOIT22XXX").toString());
				}

				Optional<TipsCamt052BankAccountReport> oReport = this.tipsCamt052BankAccountReportRepository.findByReportName(i.objectName());
				this.isPresent(oReport, new StringBuilder("Report with reportName [").append(i.objectName()).append(BicompConstants.ALREADY_EXISTS).toString());
				
				// Creazione entity TipsCamt052BankAccountReport
				TipsCamt052BankAccountReport tipsCamt052BankAccountReport = TipsCamt052BankAccountReport.builder()
						.creationDateTime(DateUtils.dateUtils().gregorianCalendarToDate(bankReport.getGrpHdr().getCreDtTm()))
						.msgId(bankReport.getGrpHdr().getMsgId())
						.reportName(i.objectName())
						.accountId(bankReport.getRpt().get(0).getAcct().getId().getOthr().getId())
						.accountOwner(bankReport.getRpt().get(0).getAcct().getOwnr().getId().getOrgId().getAnyBIC())
						.currency(bankReport.getRpt().get(0).getAcct().getCcy())
						.flagElaborato('N')
						.settlementDate(DateUtils.dateUtils().gregorianCalendarToDate(bankReport.getRpt().get(0).getBal().get(0).getDt().getDt()))
						.openingBalance(bankReport.getRpt().get(0).getBal().get(0).getAmt().getValue())
						.openingBalanceSide(this.debitOrCredit(bankReport.getRpt().get(0).getBal().get(0).getCdtDbtInd().value()))
						.closingBalance(bankReport.getRpt().get(0).getBal().get(1).getAmt().getValue())
						.closingBalanceSide(this.debitOrCredit(bankReport.getRpt().get(0).getBal().get(1).getCdtDbtInd().value()))
						.totalCreditOperation(bankReport.getRpt().get(0).getTxsSummry().getTtlCdtNtries().getSum())
						.totalDebitOperation(bankReport.getRpt().get(0).getTxsSummry().getTtlDbtNtries().getSum())
						.build();
				
				this.tipsCamt052BankAccountReportRepository.save(tipsCamt052BankAccountReport);
			} else {
				log.warn("Size is 0. Nothing to do");
			}
		}
	}
	
	@Override
	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRES_NEW, 
		rollbackFor = { IOException.class, JAXBException.class, DataIntegrityViolationException.class, BicompException.class, JpaSystemException.class }
	)
	public void processCamt053Report(List<Item> list) throws IOException, JAXBException, DataIntegrityViolationException, BicompException, JpaSystemException {
		
		for(Item i : list) {
			InputStream inputStream = this.minioService.getObject(this.config.getMinio().getBucket().getName(), i.objectName());
			log.info("Elaboro flusso: [{}]", i.objectName());
			
			InputStreamWithLength inputStreamWithLength = this.fromByteArraytoInputStream(inputStream);
			
			if(inputStreamWithLength.getLength() > 0) {
				Document document = XMLUtils.unmarshall(inputStreamWithLength.getInputStream(), Document.class);
				BankToCustomerStatementV06 bankStatement = document.getBkToCstmrStmt();
				log.info("Report with Message ID: [{}]", bankStatement.getGrpHdr().getMsgId());
				
				Optional<TipsCamt053BankStatement> oStatement = this.tipsCamt053BankStatementRepository.findByMsgId(bankStatement.getGrpHdr().getMsgId());
				this.isPresent(oStatement, new StringBuilder("Statement with msgId [").append(bankStatement.getGrpHdr().getMsgId()).append(BicompConstants.ALREADY_EXISTS).toString());
				
				// Creazione entity TipsCamt053BankStatement
				TipsCamt053BankStatement tipsCamt053BankStatement = TipsCamt053BankStatement.builder()
						.creationDateTime(DateUtils.dateUtils().gregorianCalendarToDate(bankStatement.getGrpHdr().getCreDtTm()))
						.msgId(bankStatement.getGrpHdr().getMsgId())
						.reportName(i.objectName())
						.build();
				
				this.tipsCamt053BankStatementRepository.save(tipsCamt053BankStatement);
				
				this.saveTipsCamt053BankAccountStatement(tipsCamt053BankStatement, bankStatement.getStmt());
			}
			else {
				log.warn("Size is 0. Nothing to do");
			}
		}
	}

	@Override
	public List<TipsCamt053BankAccountStatementEntry> extractTipsPayment(Date date) {

		List<TipsCamt053BankAccountStatementEntry> list = this.tipsCamt053BankAccountStatementEntryRepository.findBySettlementDate(date);
		log.info("Extracted [{}] TIPS payment with settlement date [{}]", list.size(), date);

		return list;
	}

	@Override
	public String createTipsPaymentFile(Date date, List<TipsCamt053BankAccountStatementEntry> list) throws BicompException, ParseException {

		String fileName = this.config.getTips().getPaymentReportPrefix().concat(DateUtils.dateUtils().dateToString(date)).concat(this.config.getTips().getPaymentReportSuffix());
		String objectName = this.config.getTips().getPaymentReportFolder().concat(fileName);
		
		log.info("Scrittura dati su file: [{}]", objectName);

		StringBuilder sb = new StringBuilder();
		for(TipsCamt053BankAccountStatementEntry t : list) {
			sb = sb.append(t.getTipsCamt053BankAccountStatement().getAccountOwner().substring(0, 8)).append(";")
				.append("TIPS").append(";")
				.append(t.getTipsCamt053BankAccountStatement().getAccountNumber()).append(";")
				.append(t.getCurrency()).append(";")
				.append("000000").append(";")
				.append(DateUtils.dateUtils().dateToStringFormatted(t.getTipsCamt053BankAccountStatement().getSettlementDate())).append(";")
				.append(t.getEntryReference()).append(";")
				.append(t.getEntryReference()).append(";")
				.append(t.getEntryReference()).append(";")
				.append(t.getEntryReference()).append(";")
				.append(t.getPaymentAmount().setScale(2, RoundingMode.HALF_UP).toString()).append(";")
				.append(t.getSide()).append(";")
				.append(t.getStatus()).append(";")
				.append("pacs.008").append(";")
				.append(t.getCreditorBic()).append(";")
				.append(t.getDebitorBic()).append(";")
				.append(t.getSettlementDateTime().toString()).append(";")
				.append(t.getTipsCamt053BankAccountStatement().getCreationDateTime().toString())
				.append(System.lineSeparator());
		}
		
		InputStream inputStream = new ByteArrayInputStream(sb.toString().getBytes());
		this.minioService.uploadFile(
				this.config.getMinio().getBucket().getName(), 
				objectName, 
				inputStream);
		
		log.info("Scrittura [{}] eseguita con successo", objectName);
		return objectName;
	}
	
	@Override
	public void moveToBackupFolder052(List<Item> list) throws IOException {

		for(Item i : list) {
			String fileName = i.objectName().substring(this.config.getTips().getCamt052Share().length());
			String year = this.getYearFromFile(fileName);
			
			log.info("Copio [{}] nella cartella di backup: [{}]", i.objectName(), this.config.getTips().getCamt053Share().concat(year));
			this.minioService.copyFile(this.config.getMinio().getBucket().getName(), 
					i.objectName(),
					this.config.getMinio().getBucket().getName(), 
					this.config.getTips().getCamt052Share().concat(year).concat("/").concat(fileName)
			);
			
			log.info("Rimuovo file: [{}]", i.objectName());
			this.minioService.removeFile(this.config.getMinio().getBucket().getName(), i.objectName());
		}
	}
	
	@Override
	public void moveToBackupFolder053(List<Item> list) throws IOException {
		
		for(Item i : list) {
			String fileName = i.objectName().substring(this.config.getTips().getCamt053Share().length());
			String year = this.getYearFromFile(fileName);
			
			log.info("Copio [{}] nella cartella di backup: [{}]", i.objectName(), this.config.getTips().getCamt053Share().concat(year));
			this.minioService.copyFile(this.config.getMinio().getBucket().getName(), 
					i.objectName(),
					this.config.getMinio().getBucket().getName(), 
					this.config.getTips().getCamt053Share().concat(year).concat("/").concat(fileName)
			);
			
			log.info("Rimuovo file: [{}]", i.objectName());
			this.minioService.removeFile(this.config.getMinio().getBucket().getName(), i.objectName());
		}
	}
	
	@Override
	public List<TipsBalanceDto> getTipsLastBalanceByDate(String date) throws ResourceNotFoundException, ParseException {
		
		log.info("Getting TIPS last closing balance by date");
		Date dataRegolamento = DateUtils.dateUtils().stringToDate(date);
    	log.info("Current date is: [{}]", dataRegolamento);
    	
    	Optional<TipsCamt053BankAccountStatement> accountStatement = this.tipsCamt053BankAccountStatementRepository.findLastStatementByDate(dataRegolamento, PageRequest.of(0, 1));
		if(!accountStatement.isPresent()) {
			throw new ResourceNotFoundException(new StringBuilder("No TIPS statement found for date: [").append(dataRegolamento).append("]").toString());
		}
		
		log.info("Retrieved [{}] statements", accountStatement);
		
		List<TipsBalanceDto> listDtos = new ArrayList<>();
		listDtos.add(TipsBalanceDto.builder()
				.id(accountStatement.get().getId())
				.closingBalance(accountStatement.get().getClosingBalance())
				.currency(accountStatement.get().getCurrency())
				.fromDateTime(DateUtils.dateUtils().dateToLocalDateTime(accountStatement.get().getFromDateTime()))
				.toDateTime(DateUtils.dateUtils().dateToLocalDateTime(accountStatement.get().getToDateTime()))
				.build());
		
		return listDtos;
	}
	
	@Override
	public PageableTipsReportDto getTipsDailyReport(String date, Pageable paging) throws ResourceNotFoundException, ParseException {
		
		Date dataRegolamento = DateUtils.dateUtils().stringToDate(date);
		log.info("Getting Reports for date: [{}]", dataRegolamento);
    	log.info("Current date is: [{}]", dataRegolamento);
    	
    	if(paging == null) {
    		List<TipsCamt053BankAccountStatement> list = this.tipsCamt053BankAccountStatementRepository.findBySettlementDateNoPagination(dataRegolamento);
    		if(list.isEmpty()) {
    			throw new ResourceNotFoundException(new StringBuilder("No reports found for date: [").append(dataRegolamento).append("]").toString());
    		}
    		
    		return this.buildPageableTipsReportDto(list, list.size());
    	}
    	else {
    		Page<TipsCamt053BankAccountStatement> page = this.tipsCamt053BankAccountStatementRepository.findBySettlementDate(dataRegolamento, paging);
    		if(page.isEmpty()) {
    			throw new ResourceNotFoundException(new StringBuilder("No reports found for date: [").append(dataRegolamento).append("]").toString());
    		}
    		
    		return this.buildPageableTipsReportDto(page.getContent(), Math.toIntExact(page.getTotalElements()));
    	}
	}
	
	@Override
	public TipsTotalDto getTipsReportDetail(BigDecimal id) throws ResourceNotFoundException {
		
		log.info("Getting TIPS totals by reportId");
    	
		TipsCamt053BankAccountStatement statement = this.tipsCamt053BankAccountStatementRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(new StringBuilder("No TIPS Report found for ID: [").append(id).append("]").toString()));
		
		TipsTotalDto t = TipsTotalDto.builder()
    			.settlementDate(DateUtils.dateUtils().dateToLocalDate(statement.getSettlementDate()))
    			.creditPmntAmount(BigDecimal.ZERO)
    			.debitPmntAmount(BigDecimal.ZERO)
    			.currency("EUR")
    			.build();
		
    	List<Object[]> list = this.tipsCamt053BankAccountStatementEntryRepository.findTotalByReportId(id);
    	if(!list.isEmpty()) {
    		for(Object[] obj : list) {
        		if(obj[0].toString().charAt(0) == 'C') {
        			t.setCreditPmntAmount((BigDecimal) obj[1]);
        		}
        		else {
        			t.setDebitPmntAmount((BigDecimal) obj[1]);
        		}
        	}
    	}
    	
    	return t;
	}
	
	@Override
	public PageableTipsEntryDto getPayments(Character side, String status, BigDecimal amountFrom, BigDecimal amountTo, String settlementDateFrom, String settlementDateTo, Pageable paging) throws ResourceNotFoundException, ParseException {
		
		log.info("Getting TIPS payments with custom filters");
		
		Specification<TipsCamt053BankAccountStatementEntry> specification = Specification.where(null);
		if(side != null) {
			specification = specification.and(GenericSpecification.tipsPaymentHasSide(side));
		}
		if(amountFrom != null) {
			specification = specification.and(GenericSpecification.tipsPaymentHasAmountFrom(amountFrom));
		}
		if(amountTo != null) {
			specification = specification.and(GenericSpecification.tipsPaymentHasAmountTo(amountTo));
		}
		if(settlementDateFrom != null) {
			Date dateFrom = DateUtils.dateUtils().stringToDateYear(settlementDateFrom);
			specification = specification.and(GenericSpecification.tipsPaymentHasSettlementDateFrom(dateFrom));
		}
		if(settlementDateTo != null) {
			Date dateTo = DateUtils.dateUtils().stringToDateYear(settlementDateTo);
			specification = specification.and(GenericSpecification.tipsPaymentHasSettlementDateTo(dateTo));
		}
		if(status != null) {
			specification = specification.and(GenericSpecification.tipsPaymentHasStatus(status));
		}
		
		specification = specification.and(GenericSpecification.tipsPaymentSortBySettlementDateTimeAsc());
		
		Page<TipsCamt053BankAccountStatementEntry> page = this.tipsCamt053BankAccountStatementEntryRepository.findAll(specification, paging);
		
		if(page.isEmpty()) {
			throw new ResourceNotFoundException(new StringBuilder("No Payment found with filter: [side = ").append(side)
					.append("] [amountFrom = ").append(amountFrom).append("] [amountTo = ").append(amountTo).append("] [settlementDateFrom = ")
					.append(settlementDateFrom).append("] [settlementDateTo = ").append(settlementDateTo).append("] [status = ")
					.append(status).append("] [page = ").append(paging.getPageNumber()).append("] [size = ").append(paging.getPageSize()).append("]").toString());
		}
		
		log.info("List of payment: {}", Arrays.toString(page.getContent().toArray()));
		List<TipsEntryDto> dtos = new ArrayList<>();
		
		PageableTipsEntryDto p = PageableTipsEntryDto.builder().build();
		page.getContent().forEach(e -> {
			log.info("Reference: [{}]", e.getEntryReference());
			
			dtos.add(TipsEntryDto.builder()
					.entryId(e.getId())
					.entryReference(e.getEntryReference())
					.paymentAmount(e.getPaymentAmount())
					.currency(e.getCurrency())
					.settlementDateTime(e.getSettlementDateTime())
					.side(e.getSide())
					.status(e.getStatus())
					.debitor(e.getDebitorBic())
					.creditor(e.getCreditorBic())
					.build()
			);
		});		
		p.setTotalElements(new BigDecimal(page.getTotalElements()));
		p.setEntries(dtos);
		
		return p;
	}
	
	@Override
	public List<TipsCamt052BankAccountReport> getSaldiTips() throws ResourceNotFoundException {
		
		return this.tipsCamt052BankAccountReportRepository.findByFlagElaboratoOrderBySettlementDateAsc('N');
	}
	
	@Override
	public TipsCamt053BankAccountStatementEntry getLastTipsPaymentByDate(Date date) throws ResourceNotFoundException {
		
		Page<TipsCamt053BankAccountStatementEntry> p = this.tipsCamt053BankAccountStatementEntryRepository.findLastPaymentBySettlementDate(date, PageRequest.of(0, 1));
		if(p.getContent().isEmpty()) {
			throw new ResourceNotFoundException(new StringBuilder("No Payments found for SettlementDate: [").append(date)
					.append("]").toString());
		}
		
		return p.getContent().get(0);
	}
	
	@Override
	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRES_NEW, 
			rollbackFor = { ResourceNotFoundException.class }
	)
	public String updateFlagElaborato(String messageId) throws ResourceNotFoundException {
		
		Optional<TipsCamt052BankAccountReport> o = this.tipsCamt052BankAccountReportRepository.findByMsgId(messageId);
		if(o.isEmpty()) {
			throw new ResourceNotFoundException(new StringBuilder("No Report found for MessageId: [").append(messageId)
					.append("]").toString());
		}
		
		this.tipsCamt052BankAccountReportRepository.updateFlagElaborato(messageId, 'Y');
		return messageId;
	}
	
	private char debitOrCredit(String creditDebitCode) {
		return creditDebitCode.equals("CRDT") ? 'C' : 'D';
	}
	
	private Object[] readOpeningBalance(List<CashBalance71> balances) {
		Object[] o = new Object[] {null, null};
		for(CashBalance71 b : balances) {
			if(b.getTp().getCdOrPrtry().getCd().equals(BalanceType12Code1.OPBD)) {
				o[0] = b.getAmt().getValue();
				o[1] = this.debitOrCredit(b.getCdtDbtInd().value());
			}
		}
		
		return o;
	}
	
	private Object[] readClosingBalance(List<CashBalance71> balances) {
		Object[] o = new Object[] {null, null};
		for(CashBalance71 b : balances) {
			if(b.getTp().getCdOrPrtry().getCd().equals(BalanceType12Code1.CLBD)) {
				o[0] = b.getAmt().getValue();
				o[1] = this.debitOrCredit(b.getCdtDbtInd().value());
			}
		}
		
		return o;
	}
	
	private PageableTipsReportDto buildPageableTipsReportDto(List<TipsCamt053BankAccountStatement> list, int totalElements) {
		
		PageableTipsReportDto p = PageableTipsReportDto.builder().build();
		List<TipsReportDto> listTipsReportDto = new ArrayList<>();
		list.forEach(c -> {
			log.info("Report: [{}]", c.getTipsCamt053BankStatement().getReportName());
			listTipsReportDto.add(TipsReportDto.builder()
					.reportId(c.getId())
					.fromDateTime(DateUtils.dateUtils().dateToLocalDateTime(c.getFromDateTime()))
					.toDateTime(DateUtils.dateUtils().dateToLocalDateTime(c.getToDateTime()))
					.openingBalance(c.getOpeningBalance())
					.closingBalance(c.getClosingBalance())
					.currency(c.getCurrency())
					.build()
			);
		});
		p.setTotalElements(new BigDecimal(totalElements));
		p.setReports(listTipsReportDto);
		
		return p;
	}

	private void isPresent(Optional<?> o, String errorMessage) {
		if(o.isPresent()) {
			log.error(errorMessage);
			throw new BicompException(errorMessage);
		}
	}
	
	private String getYearFromFile(String file) {
		
		log.info("Estraggo anno dal nome file: [{}]", file);
		return file.substring(9, 13);
	}
	
	private void saveTipsCamt053BankAccountStatement(TipsCamt053BankStatement t, List<AccountStatement61> list) {
		
		for(AccountStatement61 a : list) {
			
			Object[] ob = this.readOpeningBalance(a.getBal());
			Object[] cb = this.readClosingBalance(a.getBal());
			
			TipsCamt053BankAccountStatement tipsCamt053BankAccountStatement = TipsCamt053BankAccountStatement.builder()
					.accountNumber(a.getAcct().getId().getOthr().getId())
					.accountOwner(a.getAcct().getOwnr().getId().getOrgId().getAnyBIC())
					.openingBalanceIndicator(ob[1] != null ? ob[1].toString().charAt(0) : null)
					.openingBalance(ob[0] != null ? (BigDecimal) ob[0] : null)
					.closingBalanceIndicator(cb[1] != null ? cb[1].toString().charAt(0) : null)
					.closingBalance(cb[0] != null ? (BigDecimal) cb[0] : null)
					.currency(a.getAcct().getCcy())
					.stmtId(a.getId())
					.creationDateTime(DateUtils.dateUtils().gregorianCalendarToDate(a.getCreDtTm() ))
					.fromDateTime(DateUtils.dateUtils().gregorianCalendarToDate(a.getFrToDt().getFrDtTm()))
					.toDateTime(DateUtils.dateUtils().gregorianCalendarToDate(a.getFrToDt().getToDtTm()))
					.settlementDate(DateUtils.dateUtils().gregorianCalendarToDate(a.getBal().get(0).getDt().getDt()))
					.tipsCamt053BankStatement(t)
					.build();
			
			this.tipsCamt053BankAccountStatementRepository.save(tipsCamt053BankAccountStatement);
			
			this.saveTipsCamt053BankAccountStatementEntry(tipsCamt053BankAccountStatement, a.getNtry());
		}
	}
	
	private void saveTipsCamt053BankAccountStatementEntry(TipsCamt053BankAccountStatement t, List<ReportEntry81> list) {
		
		for(ReportEntry81 r : list) {
			log.info("Entry with ID: [{}]", r.getNtryRef());

			Optional<TipsCamt053BankAccountStatementEntry> oEntry = this.tipsCamt053BankAccountStatementEntryRepository.findByEntryReferenceAndSide(r.getNtryRef(), this.debitOrCredit(r.getCdtDbtInd().value()));
			this.isPresent(oEntry, new StringBuilder("Entry with reference [").append(r.getNtryRef()).append("] and side [").append(this.debitOrCredit(r.getCdtDbtInd().value())).append(BicompConstants.ALREADY_EXISTS).toString());
			
			TipsCamt053BankAccountStatementEntry tipsCamt053BankAccountStatementEntry = TipsCamt053BankAccountStatementEntry.builder()
					.entryReference(r.getNtryRef())
					.settlementDateTime(DateUtils.dateUtils().gregorianCalendarToDate(r.getBookgDt().getDtTm()))
					.paymentAmount(r.getAmt().getValue())
					.currency(r.getAmt().getCcy())
					.status(r.getSts().value())
					.side(this.debitOrCredit(r.getCdtDbtInd().value()))
					.bankTransactionCode(r.getBkTxCd().getDomn().getCd().name())
					.bankTransactionCodeFamily(r.getBkTxCd().getDomn().getFmly().getCd().name())
					.debitorBic(r.getNtryDtls() != null ? r.getNtryDtls().getTxDtls().getRltdAgts().getDbtrAgt().getFinInstnId().getBICFI() : null)
					.creditorBic(r.getNtryDtls() != null ? r.getNtryDtls().getTxDtls().getRltdAgts().getCdtrAgt().getFinInstnId().getBICFI() : null)
					.tipsCamt053BankAccountStatement(t)
					.build();
			
			this.tipsCamt053BankAccountStatementEntryRepository.save(tipsCamt053BankAccountStatementEntry);
		}
	}
	
	@Override
	public int hashCode() {
		
		final int prime = 2;
	    int result = 1;
	    result = prime * result + ((this.config == null) ? 0 : this.config.hashCode());
	    result = prime * result + ((this.minioService == null) ? 0 : this.minioService.hashCode());
	    result = prime * result + ((this.tipsCamt052BankAccountReportRepository == null) ? 0 : this.tipsCamt052BankAccountReportRepository.hashCode());
	    result = prime * result + ((this.tipsCamt053BankStatementRepository == null) ? 0 : this.tipsCamt053BankStatementRepository.hashCode());
	    result = prime * result + ((this.tipsCamt053BankAccountStatementRepository == null) ? 0 : this.tipsCamt053BankAccountStatementRepository.hashCode());
	    result = prime * result + ((this.tipsCamt053BankAccountStatementEntryRepository == null) ? 0 : this.tipsCamt053BankAccountStatementEntryRepository.hashCode());
	    
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (obj == this)
	        return true;
	    if (!(obj instanceof TipsServiceImpl))
	        return false;
	    
	    TipsServiceImpl other = (TipsServiceImpl) obj;
	    boolean c = (this.config == null && other.config == null) || (this.config != null && this.config.equals(other.config));
	    boolean minio = (this.minioService == null && other.minioService == null) 
	    		|| (this.minioService != null && this.minioService.equals(other.minioService));
	    boolean tCamt052 = (this.tipsCamt052BankAccountReportRepository == null && other.tipsCamt052BankAccountReportRepository == null) 
	    		|| (this.tipsCamt052BankAccountReportRepository != null && this.tipsCamt052BankAccountReportRepository.equals(other.tipsCamt052BankAccountReportRepository));
	    boolean tCamt053Bank = (this.tipsCamt053BankStatementRepository == null && other.tipsCamt053BankStatementRepository == null) 
	    		|| (this.tipsCamt053BankStatementRepository != null && this.tipsCamt053BankStatementRepository.equals(other.tipsCamt053BankStatementRepository));
	    boolean tCamt053BankAccount = (this.tipsCamt053BankAccountStatementRepository == null && other.tipsCamt053BankAccountStatementRepository == null)
	    		|| (this.tipsCamt053BankAccountStatementRepository != null && this.tipsCamt053BankAccountStatementRepository.equals(other.tipsCamt053BankAccountStatementRepository));				
	    boolean tCamt053BankAccStat = (this.tipsCamt053BankAccountStatementEntryRepository == null && other.tipsCamt053BankAccountStatementEntryRepository == null)
	    		|| (this.tipsCamt053BankAccountStatementEntryRepository != null && this.tipsCamt053BankAccountStatementEntryRepository.equals(other.tipsCamt053BankAccountStatementEntryRepository));
	    return c && minio && tCamt052 && tCamt053Bank && tCamt053BankAccount && tCamt053BankAccStat;
	}
	
}
