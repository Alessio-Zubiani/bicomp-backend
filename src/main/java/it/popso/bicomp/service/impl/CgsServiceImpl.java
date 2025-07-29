package it.popso.bicomp.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.Join;
import jakarta.xml.bind.JAXBException;

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
import it.popso.bicomp.dto.CgsLacDetailDto;
import it.popso.bicomp.dto.CgsLacDto;
import it.popso.bicomp.dto.CgsLacEntryDto;
import it.popso.bicomp.dto.InputStreamWithLength;
import it.popso.bicomp.dto.PageableCgsEntryDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.CgsLmr;
import it.popso.bicomp.model.CgsLmrLacBulk;
import it.popso.bicomp.model.CgsLmrLacBulkStatement;
import it.popso.bicomp.model.CgsLmrLtinBulk;
import it.popso.bicomp.model.CgsLmrLtinBulkStatement;
import it.popso.bicomp.model.CgsLmrLtinBulkStatementEntry;
import it.popso.bicomp.model.CgsLmrPlcrBulk;
import it.popso.bicomp.model.CgsLmrPlcrBulkStatement;
import it.popso.bicomp.model.CgsLmrPlcrBulkStatementEntry;
import it.popso.bicomp.properties.BicompConfig;
import it.popso.bicomp.repository.CgsLmrLacBulkRepository;
import it.popso.bicomp.repository.CgsLmrLacBulkStatementRepository;
import it.popso.bicomp.repository.CgsLmrLtinBulkRepository;
import it.popso.bicomp.repository.CgsLmrLtinBulkStatementEntryRepository;
import it.popso.bicomp.repository.CgsLmrLtinBulkStatementRepository;
import it.popso.bicomp.repository.CgsLmrPlcrBulkRepository;
import it.popso.bicomp.repository.CgsLmrPlcrBulkStatementEntryRepository;
import it.popso.bicomp.repository.CgsLmrPlcrBulkStatementRepository;
import it.popso.bicomp.repository.CgsLmrRepository;
import it.popso.bicomp.service.CgsService;
import it.popso.bicomp.service.MinioService;
import it.popso.bicomp.utils.BicompConstants;
import it.popso.bicomp.utils.DateUtils;
import it.popso.bicomp.utils.GenericSpecification;
import it.popso.bicomp.utils.XMLUtils;
import it.popso.bicomp.xjc.cgs.bulk.S2CgsBlk;
import it.popso.bicomp.xjc.cgs.camt053.AccountStatement9;
import it.popso.bicomp.xjc.cgs.camt053.BankToCustomerStatementV08;
import it.popso.bicomp.xjc.cgs.camt053.CashBalance8;
import it.popso.bicomp.xjc.cgs.camt053.CreditDebitCode;
import it.popso.bicomp.xjc.cgs.camt053.ReportEntry10;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@Scope("prototype")
@RequiredArgsConstructor
public class CgsServiceImpl extends BaseServiceImpl implements CgsService {
	
	private final BicompConfig config;
	private final MinioService minioService;
	private final CgsLmrRepository cgsLmrRepository;
	private final CgsLmrLacBulkRepository cgsLmrLacBulkRepository;
	private final CgsLmrLacBulkStatementRepository cgsLmrLacBulkStatementRepository;
	private final CgsLmrLtinBulkRepository cgsLmrLtinBulkRepository;
	private final CgsLmrLtinBulkStatementRepository cgsLmrLtinBulkStatementRepository;
	private final CgsLmrLtinBulkStatementEntryRepository cgsLmrLtinBulkStatementEntryRepository;
	private final CgsLmrPlcrBulkRepository cgsLmrPlcrBulkRepository;
	private final CgsLmrPlcrBulkStatementRepository cgsLmrPlcrBulkStatementRepository;
	private final CgsLmrPlcrBulkStatementEntryRepository cgsLmrPlcrBulkStatementEntryRepository;
	
	private CgsLmr cgsLmr;
	boolean isLtinPresent = false;

	
	@Override
	public List<CgsLacDto> getCgsLastBalanceByDate(String date) throws ResourceNotFoundException, ParseException {
		
		log.info("Getting CGS last closing balance by date");
		Date dataRegolamento = DateUtils.dateUtils().stringToDate(date);
    	log.info("Current date is: [{}]", dataRegolamento);
    	
    	Optional<CgsLmr> lmr = this.cgsLmrRepository.findLastStatementByDate(dataRegolamento, PageRequest.of(0, 1));
		if(!lmr.isPresent()) {
			throw new ResourceNotFoundException(new StringBuilder("No CGS report found for date: [").append(dataRegolamento).append("]").toString());
		}
		
		log.info("Retrieved LAC [{}]", lmr.get().getFileLac());
		Optional<CgsLmrLacBulkStatement> o = this.cgsLmrLacBulkStatementRepository.findByLmrId(lmr.get().getId());
		if(!o.isPresent()) {
			throw new ResourceNotFoundException(new StringBuilder("LacBulkStatement with ID [{}] not found").append(lmr.get().getId()).append("]").toString());
		}
		
		List<CgsLacDto> listDtos = new ArrayList<>();
		listDtos.add(CgsLacDto.builder()
				.lacId(o.get().getId())
				.lacNumber(lmr.get().getFileLac())
				.openingBalance(o.get().getOpeningBalance())
				.closingBalance(o.get().getClosingBalance())
				.currency("EUR")
				.fromDateTime(DateUtils.dateUtils().dateToLocalDateTime(o.get().getFromDateTime()))
				.toDateTime(DateUtils.dateUtils().dateToLocalDateTime(o.get().getToDateTime()))
				.build());
		
		return listDtos;
	}
	
	@Override
	public List<CgsLacDto> getCurrentDateLac(String date) throws ResourceNotFoundException, ParseException {
		
		List<CgsLacDto> listCgsLacDto = new ArrayList<>();
		
		Date dataRegolamento = DateUtils.dateUtils().stringToDate(date);
		log.info("Getting LACs for date: [{}]", dataRegolamento);
    	log.info("Current date is: [{}]", dataRegolamento);
    	
		List<CgsLmr> currentDateLacs = this.cgsLmrRepository.findByFileSettlementDate(dataRegolamento);
		if(currentDateLacs.isEmpty()) {
			throw new ResourceNotFoundException(new StringBuilder("No report found for date: [").append(dataRegolamento).append("]").toString());
		}
		
		currentDateLacs.forEach(c -> {
			log.info("LAC: [{}]", c.getFileLac());
			Optional<CgsLmrLacBulkStatement> o = this.cgsLmrLacBulkStatementRepository.findByLmrId(c.getId());
			if(!o.isPresent()) {
				throw new ResourceNotFoundException(new StringBuilder("LacBulkStatement with ID [{}] not found").append(c.getId()).append("]").toString());
			}
			
			log.info("Find LacBulkStatement with Statement Reference: [{}]", o.get().getStatementReference());
			listCgsLacDto.add(CgsLacDto.builder()
					.lacId(c.getId())
					.lacNumber(c.getFileLac())
					.fromDateTime(DateUtils.dateUtils().dateToLocalDateTime(o.get().getFromDateTime()))
					.toDateTime(DateUtils.dateUtils().dateToLocalDateTime(o.get().getToDateTime()))
					.openingBalance(o.get().getOpeningBalance())
					.closingBalance(o.get().getClosingBalance())
					.currency("EUR")
					.build()
			);
		});
		
		return listCgsLacDto;
	}

	@Override
	public CgsLacDetailDto getLacDetail(BigDecimal lacId) throws ResourceNotFoundException {
		
		log.info("Getting details for LAC: [{}]", lacId);
		Optional<CgsLmrLacBulkStatement> o = this.cgsLmrLacBulkStatementRepository.findByLmrId(lacId);
		if(!o.isPresent()) {
			throw new ResourceNotFoundException(new StringBuilder("LacBulkStatement with ID [").append(lacId).append("] not found").toString());
		}
		
		CgsLacDetailDto dto = CgsLacDetailDto.builder()
				.lacId(lacId)
				.lacNumber(o.get().getCgsLmrLacBulk().getCgsLmr().getFileLac())
				.creditLtinAmount(o.get().getCreditLiquidityTransfer())
				.debitLtinAmount(o.get().getDebitLiquidityTransfer())
				.creditPmntAmount(o.get().getCreditPayments())
				.sctCreditPmntAmount(new BigDecimal(0))
				.corCreditPmntAmount(new BigDecimal(0))
				.b2bCreditPmntAmount(new BigDecimal(0))
				.debitPmntAmount(o.get().getDebitPayments())
				.sctDebitPmntAmount(new BigDecimal(0))
				.corDebitPmntAmount(new BigDecimal(0))
				.b2bDebitPmntAmount(new BigDecimal(0))
				.pendingDebitPmntAmount(new BigDecimal(0))
				.currency("EUR")
				.build();
		
		log.info("Getting total grouped by service [SCT, COR, B2B]");
		List<CgsLmrPlcrBulkStatement> listByServices = this.cgsLmrPlcrBulkStatementRepository.findByLmrId(lacId);
		
		for(CgsLmrPlcrBulkStatement c : listByServices) {
			if(c.getService().equals("SCT")) {
				dto.setSctCreditPmntAmount(dto.getSctCreditPmntAmount().add(c.getCreditPayments()));
				dto.setSctDebitPmntAmount(dto.getSctDebitPmntAmount().add(c.getDebitPayments()));
				dto.setPendingDebitPmntAmount(dto.getPendingDebitPmntAmount().add(c.getPendingPayments()));
			}
			else if(c.getService().equals("COR")) {
				dto.setCorCreditPmntAmount(dto.getCorCreditPmntAmount().add(c.getCreditPayments()));
				dto.setCorDebitPmntAmount(dto.getCorDebitPmntAmount().add(c.getDebitPayments()));
				dto.setPendingDebitPmntAmount(dto.getPendingDebitPmntAmount().add(c.getPendingPayments()));
			}
			else {
				dto.setB2bCreditPmntAmount(dto.getB2bCreditPmntAmount().add(c.getCreditPayments()));
				dto.setB2bDebitPmntAmount(dto.getB2bDebitPmntAmount().add(c.getDebitPayments()));
				dto.setPendingDebitPmntAmount(dto.getPendingDebitPmntAmount().add(c.getPendingPayments()));
			}
		}
		
		return dto;
	}
	
	@Override
	public List<Item> getLmrReport() throws BicompException {
		
		log.info("Getting list of XML files in: [{}]", this.config.getCgs().getShare());
		List<Item> list = this.minioService.getObjectsByPrefixAndSuffix(
				this.config.getMinio().getBucket().getName(), 
				this.config.getCgs().getShare(), 
				this.config.getCgs().getPrefix(), 
				this.config.getCgs().getSuffix(), 
				true);
				
		log.info("Trovati [{}] files da elaborare", list.size());
		
		return list;
	}

	@Override
	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRES_NEW, 
		rollbackFor = {IOException.class, JAXBException.class, DataIntegrityViolationException.class, BicompException.class, JpaSystemException.class}
	)
	public void processLmrReport(List<Item> list) throws IOException, JAXBException, DataIntegrityViolationException, BicompException, JpaSystemException {
		
		for(Item i : list) {
			InputStream inputStream = this.minioService.getObject(this.config.getMinio().getBucket().getName(), i.objectName());
			log.info("Elaboro flusso: [{}]", i.objectName());
			
			InputStreamWithLength inputStreamWithLength = this.fromByteArraytoInputStream(inputStream);
			
			if(inputStreamWithLength.getLength() > 0) {
				S2CgsBlk lmr = XMLUtils.unmarshall(inputStreamWithLength.getInputStream(), S2CgsBlk.class);
				log.info("LMR reference: [{}]", lmr.getFileRef());

				Optional<CgsLmr> oLmr = this.cgsLmrRepository.findByFileRef(lmr.getFileRef());
				this.isPresent(oLmr, new StringBuilder("Lmr with fileRef [").append(lmr.getFileRef()).append(BicompConstants.ALREADY_EXISTS).toString());
								
				// Creazione entity CgsLmr
				this.cgsLmr = CgsLmr.builder()
						.reportName(i.objectName())
						.sendingInstitute(lmr.getSndgInst())
						.receivingInstitute(lmr.getRcvgInst())
						.serviceId(lmr.getSrvcId().name())
						.environment(lmr.getTstCode().name())
						.fileType(lmr.getFileType().name())
						.fileRef(lmr.getFileRef())
						.fileSettlementDate(DateUtils.dateUtils().gregorianCalendarToDate(lmr.getFileBusDt()))
						.fileLac(lmr.getFileLac())
						.fileCreationDateTime(DateUtils.dateUtils().xmlGregorianCalendarToTimestamp(lmr.getFileDtTm()))
						.settlementBic(lmr.getSttlmtBIC())
						.build();
				this.cgsLmrRepository.save(this.cgsLmr);
				
				this.processStatement(lmr);
			}
			else {
				log.warn("Size is 0. Nothing to do");
			}
		}
	}
	
	@Override
	public List<CgsLmrLtinBulkStatementEntry> extractCgsLiquidityTransfer(Date date) {

		List<CgsLmrLtinBulkStatementEntry> list = this.cgsLmrLtinBulkStatementEntryRepository.findBySettlementDate(date);
		log.info("Extracted [{}] CGS liquidity transfers with settlement date [{}]", list.size(), date);

		return list;
	}
	
	@Override
	public List<CgsLmrPlcrBulkStatementEntry> extractCgsPayment(Date date) {

		List<CgsLmrPlcrBulkStatementEntry> list = this.cgsLmrPlcrBulkStatementEntryRepository.findBySettlementDate(date);
		log.info("Extracted [{}] CGS payments with settlement date [{}]", list.size(), date);

		return list;
	}

	@Override
	public String createCgsPaymentFile(Date date, List<CgsLmrLtinBulkStatementEntry> ltList, List<CgsLmrPlcrBulkStatementEntry> paymentList) throws IOException, BicompException, ParseException {

		String fileName = this.config.getCgs().getPaymentReportPrefix().concat(DateUtils.dateUtils().dateToString(date)).concat(this.config.getCgs().getPaymentReportSuffix());
		String objectName = this.config.getCgs().getPaymentReportFolder().concat(fileName);
		
		log.info("Scrittura dati su file: [{}]", objectName);

		StringBuilder sb = new StringBuilder();
		for(CgsLmrLtinBulkStatementEntry c : ltList) {
			sb = sb.append(c.getCgsLmrLtinBulkStatement().getAccountOwner().substring(0, 8)).append(";")
					.append("CGS").append(";")
					.append("TEUEUREBAXBEBBCGS").append(";")
					.append(c.getCurrency()).append(";")
					.append("005620").append(";")
					.append(DateUtils.dateUtils().dateToStringFormatted(c.getCgsLmrLtinBulkStatement().getSettlementDate())).append(";")
					.append(c.getEntryReference()).append(";")
					.append(c.getEntryReference()).append(";")
					.append(c.getEntryReference()).append(";")
					.append(c.getEntryReference()).append(";")
					.append(c.getPaymentAmount().setScale(2, RoundingMode.HALF_UP).toString()).append(";")
					.append(c.getSide()).append(";")
					.append(c.getStatus()).append(";")
					.append("camt.054").append(";")
					.append("LT").append(";")
					.append(c.getSide().equals('C') ? "EBAXBEBBXXX" : c.getCgsLmrLtinBulkStatement().getCgsLmrLtinBulk().getCgsLmr().getSettlementBic()).append(";")
					.append(c.getSide().equals('C') ? c.getCgsLmrLtinBulkStatement().getCgsLmrLtinBulk().getCgsLmr().getSettlementBic() : "EBAXBEBBXXX").append(";")
					.append(c.getSettlementDateTime().toString()).append(";")
					.append(c.getCgsLmrLtinBulkStatement().getCreationDateTime().toString())
				.append(System.lineSeparator());
		}
		
		for(CgsLmrPlcrBulkStatementEntry c : paymentList) {
			sb = sb.append(c.getCgsLmrPlcrBulkStatement().getAccountOwner().substring(0, 8)).append(";")
					.append("CGS").append(";")
					.append("TEUEUREBAXBEBBCGS").append(";")
					.append(c.getCurrency()).append(";")
					.append("005620").append(";")
					.append(DateUtils.dateUtils().dateToStringFormatted(c.getCgsLmrPlcrBulkStatement().getSettlementDate())).append(";")
					.append(c.getEntryReference()).append(";")
					.append(c.getEntryReference()).append(";")
					.append(c.getEntryReference()).append(";")
					.append(c.getEntryReference()).append(";")
					.append(c.getPaymentAmount().setScale(2, RoundingMode.HALF_UP).toString()).append(";")
					.append(c.getSide()).append(";")
					.append(c.getStatus()).append(";")
					.append(c.getCgsLmrPlcrBulkStatement().getService().equals("SCT") ? "pacs.004|pacs.008" : "pacs.003").append(";")
					.append(c.getCgsLmrPlcrBulkStatement().getService()).append(";")
					.append(c.getCreditorBic()).append(";")
					.append(c.getDebitorBic()).append(";")
					.append(c.getSettlementDateTime().toString()).append(";")
					.append(c.getCgsLmrPlcrBulkStatement().getCreationDateTime().toString())
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
	public void moveToBackupFolder(List<Item> list) throws BicompException {
		
		for(Item i : list) {
			String fileName = i.objectName().substring(this.config.getCgs().getShare().length());
			String year = "20".concat(fileName.substring(15, 17));
			
			log.info("Copio [{}] nella cartella di backup: [{}]", i.objectName(), this.config.getCgs().getShare().concat(year));
			this.minioService.copyFile(this.config.getMinio().getBucket().getName(), 
					i.objectName(),
					this.config.getMinio().getBucket().getName(), 
					this.config.getCgs().getShare().concat(year).concat("/").concat(fileName)
			);
			
			log.info("Rimuovo file: [{}]", i.objectName());
			this.minioService.removeFile(this.config.getMinio().getBucket().getName(), i.objectName());
		}
	}

	@Override
	public PageableCgsEntryDto getPayments(Character side, BigDecimal amountFrom, BigDecimal amountTo, 
			String settlementDateFrom, String settlementDateTo, String service, String lac, String status, Pageable paging) 
					throws ResourceNotFoundException, ParseException {
		
		Specification<CgsLmrPlcrBulkStatementEntry> specification = Specification.where(null);
		if(side != null) {
			specification = specification.and((root, query, cb) -> cb.equal(root.get(BicompConstants.SIDE), side));
		}
		if(amountFrom != null) {
			specification = specification.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get(BicompConstants.PAYMENT_AMOUNT), amountFrom));
		}
		if(amountTo != null) {
			specification = specification.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get(BicompConstants.PAYMENT_AMOUNT), amountTo));
		}
		if(settlementDateFrom != null) {
			Date dateFrom = DateUtils.dateUtils().stringToDateYear(settlementDateFrom);
			specification = specification.and((root, query, cb) -> {
				Join<CgsLmrPlcrBulkStatementEntry, CgsLmrPlcrBulkStatement> plcrBulkStatement = root.join(BicompConstants.CGS_PLCR_BULK_STATEMENT);
				
		        return cb.greaterThanOrEqualTo(plcrBulkStatement.get(BicompConstants.SETTLEMENT_DATE), dateFrom);
			});
		}
		if(settlementDateTo != null) {
			Date dateTo = DateUtils.dateUtils().stringToDateYear(settlementDateTo);
			specification = specification.and((root, query, cb) -> {
				Join<CgsLmrPlcrBulkStatementEntry, CgsLmrPlcrBulkStatement> plcrBulkStatement = root.join(BicompConstants.CGS_PLCR_BULK_STATEMENT);
				
		        return cb.lessThanOrEqualTo(plcrBulkStatement.get(BicompConstants.SETTLEMENT_DATE), dateTo);
			});
		}
		if(status != null) {
			specification = specification.and((root, query, cb) -> cb.equal(root.get(BicompConstants.STATUS), status));
		}
		if(service != null) {
			specification = specification.and((root, query, cb) -> {
				Join<CgsLmrPlcrBulkStatementEntry, CgsLmrPlcrBulkStatement> plcrBulkStatement = root.join(BicompConstants.CGS_PLCR_BULK_STATEMENT);
				
		        return cb.equal(plcrBulkStatement.get("service"), service);
			});
		}
		if(lac != null) {
			specification = specification.and((root, query, cb) -> {
				Join<CgsLmrPlcrBulkStatementEntry, CgsLmrPlcrBulkStatement> plcrBulkStatement = root.join(BicompConstants.CGS_PLCR_BULK_STATEMENT);
				Join<CgsLmrPlcrBulkStatement, CgsLmrPlcrBulk> plcrBulk = plcrBulkStatement.join("cgsLmrPlcrBulk");
				Join<CgsLmrPlcrBulk, CgsLmr> plcrLmr = plcrBulk.join("cgsLmr");
				
		        return cb.equal(plcrLmr.get(BicompConstants.FILE_LAC), lac);
			});
		}
		
		specification = specification.and((root, query, cb) -> {
    		query.orderBy(cb.asc(root.get(BicompConstants.SETTLEMENT_DATE_TIME)));
    		
    		return cb.conjunction();
    	});
		
		Page<CgsLmrPlcrBulkStatementEntry> page = this.cgsLmrPlcrBulkStatementEntryRepository.findAll(specification, paging);
		if(page.isEmpty()) {
			throw new ResourceNotFoundException(new StringBuilder("No payments found with filter: [side = ").append(side)
				.append("] [amountFrom = ").append(amountFrom).append("] [amountTo = ").append(amountTo).append("] [settlementDateFrom = ")
				.append(settlementDateFrom).append("] [settlementDateTo = ").append(settlementDateTo).append("] [service = ")
				.append(service).append("] [lac = ").append(lac).append("] [status = ").append(status).append("] [page = ")
				.append(paging.getPageNumber()).append("] [size = ").append(paging.getPageSize()).append("]").toString());
		}

		log.info("List of payments: {}", Arrays.toString(page.getContent().toArray()));
		List<CgsLacEntryDto> dtos = new ArrayList<>();
		PageableCgsEntryDto p = PageableCgsEntryDto.builder().build();
		page.getContent().forEach(e -> {
			log.info("Reference: [{}]", e.getEntryReference());
			
			dtos.add(CgsLacEntryDto.builder()
					.entryId(e.getId())
					.entryReference(e.getEntryReference())
					.paymentAmount(e.getPaymentAmount())
					.currency(e.getCurrency())
					.settlementDateTime(e.getSettlementDateTime())
					.side(e.getSide())
					.service(e.getCgsLmrPlcrBulkStatement().getService())
					.status(e.getStatus())
					.debitor(e.getDebitorBic())
					.creditor(e.getCreditorBic())
					.additionalInfo(e.getAdditionalInfo())
					.build()
			);
		});
		p.setTotalElements(new BigDecimal(page.getTotalElements()));
		p.setEntries(dtos);
		
		return p;
	}
	
	@Override
	public PageableCgsEntryDto getLiquidityTransfers(Character side, BigDecimal amountFrom, BigDecimal amountTo, 
			String settlementDateFrom, String settlementDateTo, String lac, String status, Pageable paging) 
					throws ResourceNotFoundException, ParseException {
		
		Specification<CgsLmrLtinBulkStatementEntry> specification = Specification.where(null);
		if(side != null) {
			specification = specification.and(GenericSpecification.cgsLiquidityTransferHasSide(side));
		}
		if(amountFrom != null) {
			specification = specification.and(GenericSpecification.cgsLiquidityTransferHasAmountFrom(amountFrom));
		}
		if(amountTo != null) {
			specification = specification.and(GenericSpecification.cgsLiquidityTransferHasAmountTo(amountTo));
		}
		if(settlementDateFrom != null) {
			Date dateFrom = DateUtils.dateUtils().stringToDateYear(settlementDateFrom);
			specification = specification.and(GenericSpecification.cgsLiquidityTransferHasSettlementDateFrom(dateFrom));
		}
		if(settlementDateTo != null) {
			Date dateTo = DateUtils.dateUtils().stringToDateYear(settlementDateTo);
			specification = specification.and(GenericSpecification.cgsLiquidityTransferHasSettlementDateTo(dateTo));
		}
		if(status != null) {
			specification = specification.and(GenericSpecification.cgsLiquidityTransferHasStatus(status));
		}
		if(lac != null) {
			specification = specification.and(GenericSpecification.cgsLiquidityTransferHasLac(lac));
		}
		
		specification = specification.and(GenericSpecification.cgsLiquidityTransferSortBySettlementDateTimeAsc());
		
		Page<CgsLmrLtinBulkStatementEntry> page = this.cgsLmrLtinBulkStatementEntryRepository.findAll(specification, paging);
		if(page.isEmpty()) {
			throw new ResourceNotFoundException(new StringBuilder("No payments found with filter: [side = ").append(side)
				.append("] [amountFrom = ").append(amountFrom).append("] [amountTo = ").append(amountTo).append("] [settlementDateFrom = ")
				.append(settlementDateFrom).append("] [settlementDateTo = ").append(settlementDateTo).append("] [lac = ")
				.append(lac).append("] [status = ").append(status).append("] [page = ").append(paging.getPageNumber())
				.append("] [size = ").append(paging.getPageSize()).append("]").toString());
		}

		log.info("List of liquidity transfers: {}", Arrays.toString(page.getContent().toArray()));
		List<CgsLacEntryDto> dtos = new ArrayList<>();
		PageableCgsEntryDto p = PageableCgsEntryDto.builder().build();
		page.getContent().forEach(e -> {
			log.info("Reference: [{}]", e.getEntryReference());
			
			dtos.add(CgsLacEntryDto.builder()
					.entryId(e.getId())
					.entryReference(e.getEntryReference())
					.paymentAmount(e.getPaymentAmount())
					.currency(e.getCurrency())
					.settlementDateTime(e.getSettlementDateTime())
					.side(e.getSide())
					.status(e.getStatus())
					.additionalInfo(e.getAdditionalInfo())
					.build()
			);
		});
		p.setTotalElements(new BigDecimal(page.getTotalElements()));
		p.setEntries(dtos);
		
		return p;
	}
	
	@Override
	public List<CgsLmrLacBulkStatement> getSaldiCgs(Date date) throws ResourceNotFoundException {
		
		log.info("Getting LACs for date: [{}]", date);
		return this.cgsLmrLacBulkStatementRepository.findBySettlementDate(date);
	}
	
	@Override
	public CgsLmrLacBulkStatement getLastCgsBalance(Date date) throws ResourceNotFoundException {
		
		Page<CgsLmrLacBulkStatement> p = this.cgsLmrLacBulkStatementRepository.findBySettlementDate(date, PageRequest.of(0, 1));
		if(p.getContent().isEmpty()) {
			throw new ResourceNotFoundException(new StringBuilder("No Balance found for SettlementDate: [").append(date)
					.append("]").toString());
		}
		
		return p.getContent().get(0);
	}
	
	private void processStatement(S2CgsBlk lmr) {
		
		// Estrazione bulk da LMR
		int i = 0;
		for(BankToCustomerStatementV08 bulk : lmr.getBkToCstmrStmt()) {
			
			if(i == 0) {
				this.processBalanceStatement(bulk);
			}
			else {
				// Se presenti LTIN allora elaboro il relativo BULK e poi salto al BULK successivo
				if(this.isLtinPresent) {
					this.processLtinStatement(bulk);
				}
				else {
					this.processPaymentStatement(bulk);
				}
			}
			
			i++;
		}
	}
	
	private void processBalanceStatement(BankToCustomerStatementV08 bulk) {
		
		log.info("Bulk reference: [{}]", bulk.getGrpHdr().getMsgId());
		Optional<CgsLmrLacBulk> oLmrLacBulk = this.cgsLmrLacBulkRepository.findByBulkReference(bulk.getGrpHdr().getMsgId());
		this.isPresent(oLmrLacBulk, new StringBuilder("Lac Bulk with bulkReference [").append(bulk.getGrpHdr().getMsgId()).append(BicompConstants.ALREADY_EXISTS).toString());
		
		CgsLmrLacBulk cgsCamt053Bulk = CgsLmrLacBulk.builder()
				.bulkReference(bulk.getGrpHdr().getMsgId())
				.creationDateTime(DateUtils.dateUtils().xmlGregorianCalendarToTimestamp(bulk.getGrpHdr().getCreDtTm()))
				.cgsLmr(this.cgsLmr)
				.build();
		this.cgsLmrLacBulkRepository.save(cgsCamt053Bulk);
		
		// Verifico che sia presente un solo Statement, altrimenti bulk non valido
		if(bulk.getStmt().isEmpty() || bulk.getStmt().size() > 1) {
			log.info("Bulk non valido. Presenti [{}] statement", bulk.getStmt().size());
			throw new BicompException(new StringBuilder("Bulk non valido. Presenti [").append(bulk.getStmt().size()).append("] statement").toString());
		}
		else {
			AccountStatement9 stmt = bulk.getStmt().get(0);
			log.info("Statement reference: [{}]", stmt.getId());
			Optional<CgsLmrLacBulkStatement> oLmrLacBulkStatement = this.cgsLmrLacBulkStatementRepository.findByStatementReference(stmt.getId());
			this.isPresent(oLmrLacBulkStatement, new StringBuilder("Lac Bulk Statement with statementReference [").append(stmt.getId()).append(BicompConstants.ALREADY_EXISTS).toString());
			
			CgsLmrLacBulkStatement cgsLmrLacBulkStatement = CgsLmrLacBulkStatement.builder()
					.statementReference(stmt.getId())
					.creationDateTime(DateUtils.dateUtils().xmlGregorianCalendarToTimestamp(stmt.getCreDtTm()))
					.fromDateTime(DateUtils.dateUtils().xmlGregorianCalendarToTimestamp(stmt.getFrToDt().getFrDtTm()))
					.toDateTime(DateUtils.dateUtils().xmlGregorianCalendarToTimestamp(stmt.getFrToDt().getToDtTm()))
					.accountOwner(stmt.getAcct().getOwnr().getId().getOrgId().getAnyBIC())
					.build();
			
			if(stmt.getBal().isEmpty()) {
				log.info("Statement non valido. Presenti [{}] balances", stmt.getBal().size());
				throw new BicompException(new StringBuilder("Statement non valido. Presenti [").append(stmt.getBal().size()).append("]").toString());
			}
			else {
				log.info("Trovati [{}] balances", stmt.getBal().size());
				for(CashBalance8 balance : stmt.getBal()) {
					
					this.processBalance(cgsLmrLacBulkStatement, balance);
				}
			}
			
			cgsLmrLacBulkStatement.setCgsLmrLacBulk(cgsCamt053Bulk);
			this.cgsLmrLacBulkStatementRepository.save(cgsLmrLacBulkStatement);
		}
	}
	
	// Il secondo bulk, se presente, riporta le informazioni relative ai Liquidity Transfer regolati nel LAC
	private void processLtinStatement(BankToCustomerStatementV08 bulk) {
		
		log.info("Bulk reference: [{}]", bulk.getGrpHdr().getMsgId());
		Optional<CgsLmrLtinBulk> oLtinBulk = this.cgsLmrLtinBulkRepository.findByBulkReference(bulk.getGrpHdr().getMsgId());
		this.isPresent(oLtinBulk, new StringBuilder("Ltin Bulk with bulkReference [").append(bulk.getGrpHdr().getMsgId()).append(BicompConstants.ALREADY_EXISTS).toString());
		
		// Creazione entity CgsLmrLtinBulk
		CgsLmrLtinBulk cgsLmrLtinBulk = CgsLmrLtinBulk.builder()
				.bulkReference(bulk.getGrpHdr().getMsgId())
				.creationDateTime(DateUtils.dateUtils().xmlGregorianCalendarToTimestamp(bulk.getGrpHdr().getCreDtTm()))
				.cgsLmr(this.cgsLmr)
				.build();
		this.cgsLmrLtinBulkRepository.save(cgsLmrLtinBulk);
		
		if(bulk.getStmt().size() > 1) {
			log.info("Bulk non valido. Presenti [{}] LTIN statements", bulk.getStmt().size());
			throw new BicompException(new StringBuilder().append("Bulk non valido. Presenti [").append(bulk.getStmt().size()).append("] LTIN statements").toString());
		}
		
		AccountStatement9 stmt = bulk.getStmt().get(0);
		log.info("Statement reference: [{}]", stmt.getId());
		Optional<CgsLmrLtinBulkStatement> oLtinBulkStatement = this.cgsLmrLtinBulkStatementRepository.findByStatementReference(stmt.getId());
		this.isPresent(oLtinBulkStatement, new StringBuilder("Ltin Bulk Statement with statementReference [").append(stmt.getId()).append(BicompConstants.ALREADY_EXISTS).toString());
		
		CgsLmrLtinBulkStatement cgsLmrLtinBulkStatement = CgsLmrLtinBulkStatement.builder()
				.statementReference(stmt.getId())
				.creationDateTime(DateUtils.dateUtils().xmlGregorianCalendarToTimestamp(stmt.getCreDtTm()))
				.fromDateTime(DateUtils.dateUtils().xmlGregorianCalendarToTimestamp(stmt.getFrToDt().getFrDtTm()))
				.toDateTime(DateUtils.dateUtils().xmlGregorianCalendarToTimestamp(stmt.getFrToDt().getToDtTm()))
				.accountOwner(stmt.getAcct().getOwnr().getId().getOrgId().getAnyBIC())
				.build();
		
		log.info("Trovati [{}] balances", stmt.getBal().size());
		for(CashBalance8 balance : stmt.getBal()) {
			cgsLmrLtinBulkStatement.setSettlementDate(DateUtils.dateUtils().gregorianCalendarToDate(balance.getDt().getDt()));
			
			this.processBalance(cgsLmrLtinBulkStatement, balance);
		}
		
		cgsLmrLtinBulkStatement.setCgsLmrLtinBulk(cgsLmrLtinBulk);
		this.cgsLmrLtinBulkStatementRepository.save(cgsLmrLtinBulkStatement);
		
		log.info("Trovate [{}] entries", stmt.getNtry().size());
		if(stmt.getNtry().isEmpty()) {
			if(!stmt.getBal().isEmpty() && stmt.getBal().get(0).getAmt().getValue().compareTo(BigDecimal.ZERO) != 0) {
				throw new BicompException(new StringBuilder("Bulk non valido. Presenti [").append(stmt.getNtry().size()).append("] entries, ma il balance dice diversamente").toString());
			}
		}
		
		for(ReportEntry10 n : stmt.getNtry()) {
			log.info("Entry reference: [{}]", n.getNtryRef());
			Optional<CgsLmrLtinBulkStatementEntry> oLtinEntry = this.cgsLmrLtinBulkStatementEntryRepository.findByEntryReference(n.getNtryRef());
			this.isPresent(oLtinEntry, new StringBuilder("Ltin Entry with entryReference [").append(n.getNtryRef()).append(BicompConstants.ALREADY_EXISTS).toString());

			CgsLmrLtinBulkStatementEntry cgsLmrLtinBulkStatementEntry = CgsLmrLtinBulkStatementEntry.builder()
					.entryReference(n.getNtryRef())
					.paymentAmount(n.getAmt().getValue())
					.currency(n.getAmt().getCcy())
					.side(this.debitOrCredit(n.getCdtDbtInd()))
					.status(n.getSts().getCd())
					.settlementDateTime(DateUtils.dateUtils().xmlGregorianCalendarToTimestamp(n.getBookgDt().getDtTm()))
					.additionalInfo(n.getAddtlNtryInf())
					.cgsLmrLtinBulkStatement(cgsLmrLtinBulkStatement)
					.build();
			this.cgsLmrLtinBulkStatementEntryRepository.save(cgsLmrLtinBulkStatementEntry);
		}
		
		this.isLtinPresent = false;
	}
	
	private void processPaymentStatement(BankToCustomerStatementV08 bulk) {
		
		// Il terzo (o secondo se LTIN non è presente) BULK, se presente, riporta le informazioni relative ai Pagamenti regolati/pending nel LAC
		log.info("Bulk reference: [{}]", bulk.getGrpHdr().getMsgId());
		Optional<CgsLmrPlcrBulk> oPlcrBulk = this.cgsLmrPlcrBulkRepository.findByBulkReference(bulk.getGrpHdr().getMsgId());
		this.isPresent(oPlcrBulk, new StringBuilder("Plcr Bulk with bulkReference [").append(bulk.getGrpHdr().getMsgId()).append(BicompConstants.ALREADY_EXISTS).toString());

		// Creazione entity CgsLmrPlcrBulk
		CgsLmrPlcrBulk cgsLmrPlcrBulk = CgsLmrPlcrBulk.builder()
				.bulkReference(bulk.getGrpHdr().getMsgId())
				.creationDateTime(DateUtils.dateUtils().xmlGregorianCalendarToTimestamp(bulk.getGrpHdr().getCreDtTm()))
				.cgsLmr(this.cgsLmr)
				.build();
		this.cgsLmrPlcrBulkRepository.save(cgsLmrPlcrBulk);
		
		for(AccountStatement9 stmt : bulk.getStmt()) {
			log.info("Statement reference: [{}]", stmt.getId());
			Optional<CgsLmrPlcrBulkStatement> oPlcrBulkStatement = this.cgsLmrPlcrBulkStatementRepository.findByStatementReference(stmt.getId());
			this.isPresent(oPlcrBulkStatement, new StringBuilder("Plcr Bulk Statement with statementReference [").append(stmt.getId()).append(BicompConstants.ALREADY_EXISTS).toString());
			
			CgsLmrPlcrBulkStatement cgsLmrPlcrBulkStatement = CgsLmrPlcrBulkStatement.builder()
					.statementReference(stmt.getId())
					.creationDateTime(DateUtils.dateUtils().xmlGregorianCalendarToTimestamp(stmt.getCreDtTm()))
					.fromDateTime(DateUtils.dateUtils().xmlGregorianCalendarToTimestamp(stmt.getFrToDt().getFrDtTm()))
					.toDateTime(DateUtils.dateUtils().xmlGregorianCalendarToTimestamp(stmt.getFrToDt().getToDtTm()))
					.accountOwner(stmt.getAcct().getOwnr().getId().getOrgId().getAnyBIC())
					.cgsLmrPlcrBulk(cgsLmrPlcrBulk)
					.build();
			
			log.info("Trovati [{}] balances", stmt.getBal().size());
			for(CashBalance8 balance : stmt.getBal()) {
				cgsLmrPlcrBulkStatement = (CgsLmrPlcrBulkStatement) this.processBalance(cgsLmrPlcrBulkStatement, balance);
			}
			
			if((cgsLmrPlcrBulkStatement.getDebitPayments().compareTo(BigDecimal.ZERO) > 0 
					|| cgsLmrPlcrBulkStatement.getCreditPayments().compareTo(BigDecimal.ZERO) > 0) && stmt.getNtry().isEmpty()) {
				throw new BicompException(new StringBuilder("Bulk non valido. Presenti [").append(stmt.getNtry().size()).append("] entries, ma il balance dice diversamente").toString());
			}
			
			cgsLmrPlcrBulkStatement.setService(this.sctOrSdd(stmt));
			this.cgsLmrPlcrBulkStatementRepository.save(cgsLmrPlcrBulkStatement);
			log.info("CgsLmrPlcrBulkStatement ID: [{}]", cgsLmrPlcrBulkStatement.getId());
			
			for(ReportEntry10 n : stmt.getNtry()) {
				log.info("Entry reference: [{}]", n.getNtryRef());
				Optional<CgsLmrPlcrBulkStatementEntry> oPlcrEntry = this.cgsLmrPlcrBulkStatementEntryRepository.findByEntryReferenceAndSideAndCgsLmrPlcrBulkStatement_Id(n.getNtryRef(), this.debitOrCredit(n.getCdtDbtInd()), cgsLmrPlcrBulkStatement.getId());
				this.isPresent(oPlcrEntry, new StringBuilder("Plcr Entry with entryReference [").append(n.getNtryRef()).append("] and side [").append(this.debitOrCredit(n.getCdtDbtInd())).append("] and cgsLmrPlcrBulkStatement_id [").append(cgsLmrPlcrBulkStatement.getId()).append(BicompConstants.ALREADY_EXISTS).toString());
				
				CgsLmrPlcrBulkStatementEntry entry = CgsLmrPlcrBulkStatementEntry.builder()
						.entryReference(n.getNtryRef())
						.paymentAmount(n.getAmt().getValue())
						.currency(n.getAmt().getCcy())
						.side(this.debitOrCredit(n.getCdtDbtInd()))
						.status(this.getPaymentStatus(n))
						.settlementDateTime(this.getPaymentSettlementDate(n))
						.additionalInfo(n.getAddtlNtryInf())
						.debitorBic(n.getNtryDtls() != null ? n.getNtryDtls().getTxDtls().getRltdAgts().getDbtrAgt().getFinInstnId().getBICFI() : null)
						.creditorBic(n.getNtryDtls() != null ? n.getNtryDtls().getTxDtls().getRltdAgts().getCdtrAgt().getFinInstnId().getBICFI() : null)
						.cgsLmrPlcrBulkStatement(cgsLmrPlcrBulkStatement)
						.build();
				this.cgsLmrPlcrBulkStatementEntryRepository.save(entry);
			}
		}
	}

	private void isPresent(Optional<?> o, String errorMessage) {
		if(o.isPresent()) {
			log.error(errorMessage);
			throw new BicompException(errorMessage);
		}
	}
	
	private Object processBalance(Object o, CashBalance8 balance) {
		
		switch(balance.getTp().getCdOrPrtry().getCd()) {
			case "OPAV": 
				// Balance OPAV presente solo nello statement 1
				return this.manageOpeningBalance(o, balance);
				
			case "CLAV": 
				// Balance CLAV presente in tutti e 3 i tipi di Statement
				return this.manageClosingBalance(o, balance);
			
			case "XPCT":
				return this.managePendingBalance(o, balance);
				
			default:
				throw new BicompException("Attenzione... Saldo NON ammesso");
		}
	}
	
	private CgsLmrLacBulkStatement manageOpeningBalance(Object o, CashBalance8 balance) {
		
		CgsLmrLacBulkStatement cgsLmrLacBulkStatement = (CgsLmrLacBulkStatement) o;
		if(this.debitOrCredit(balance.getCdtDbtInd()) == 'C') {
			cgsLmrLacBulkStatement.setOpeningBalance(balance.getAmt().getValue());
			cgsLmrLacBulkStatement.setOpeningBalanceSide('C');
			cgsLmrLacBulkStatement.setSettlementDate(DateUtils.dateUtils().gregorianCalendarToDate(balance.getDt().getDt()));
		}
		else {
			throw new BicompException("Attenzione... Saldo di APERTURA negativo non ammesso");
		}
		
		return this.checkNullBalance(cgsLmrLacBulkStatement);
	}
	
	private Object manageClosingBalance(Object o, CashBalance8 balance) {
		
		if(this.isLtinBalance(balance)) {
			return this.manageLtinBalance(o, balance);
		}
		else if(this.isPlcrBalance(balance)) {
			return this.managePlcrBalance(o, balance);
		}
		else {
			CgsLmrLacBulkStatement cgsLmrLacBulkStatement = (CgsLmrLacBulkStatement) o;
			if(this.debitOrCredit(balance.getCdtDbtInd()) == 'C') {
				cgsLmrLacBulkStatement.setClosingBalance(balance.getAmt().getValue());
				cgsLmrLacBulkStatement.setClosingBalanceSide('C');
			}
			else {
				throw new BicompException("Attenzione... Saldo di CHIUSURA negativo non ammesso");
			}
			
			return this.checkNullBalance(cgsLmrLacBulkStatement);
		}
	}
	
	private Object manageLtinBalance(Object o, CashBalance8 balance) {
		
		if(o instanceof CgsLmrLacBulkStatement) {
			CgsLmrLacBulkStatement cgsLmrLacBulkStatement = (CgsLmrLacBulkStatement) o;
			
			this.isLtinPresent = true;
			if(this.debitOrCredit(balance.getCdtDbtInd()) == 'C') {
				cgsLmrLacBulkStatement.setCreditLiquidityTransfer(balance.getAmt().getValue());
			}
			else {
				cgsLmrLacBulkStatement.setDebitLiquidityTransfer(balance.getAmt().getValue());
			}
			
			return this.checkNullBalance(cgsLmrLacBulkStatement);
		}
		else {
			CgsLmrLtinBulkStatement cgsLmrLtinBulkStatement = (CgsLmrLtinBulkStatement) o;
			if(this.debitOrCredit(balance.getCdtDbtInd()) == 'C') {
				cgsLmrLtinBulkStatement.setCreditLiquidityTransfer(balance.getAmt().getValue());
			}
			else {
				cgsLmrLtinBulkStatement.setDebitLiquidityTransfer(balance.getAmt().getValue());
			}
			
			return this.checkNullBalance(cgsLmrLtinBulkStatement);
		}
	}
	
	private Object managePlcrBalance(Object o, CashBalance8 balance) {
		
		if(o instanceof CgsLmrLacBulkStatement) {
			CgsLmrLacBulkStatement cgsLmrLacBulkStatement = (CgsLmrLacBulkStatement) o;
			
			if(this.debitOrCredit(balance.getCdtDbtInd()) == 'C') {
				cgsLmrLacBulkStatement.setCreditPayments(balance.getAmt().getValue());
			}
			else {
				cgsLmrLacBulkStatement.setDebitPayments(balance.getAmt().getValue());
			}
			
			return this.checkNullBalance(cgsLmrLacBulkStatement);
		}
		else {
			CgsLmrPlcrBulkStatement cgsLmrPlcrBulkStatement = (CgsLmrPlcrBulkStatement) o;
			cgsLmrPlcrBulkStatement.setSettlementDate(DateUtils.dateUtils().gregorianCalendarToDate(balance.getDt().getDt()));
			
			this.manageSettledPayments(cgsLmrPlcrBulkStatement, balance);		
			
			return this.checkNullBalance(cgsLmrPlcrBulkStatement);
		}
	}
	
	private Object managePendingBalance(Object o, CashBalance8 balance) {
		
		CgsLmrPlcrBulkStatement cgsLmrPlcrBulkStatement = (CgsLmrPlcrBulkStatement) o;
		cgsLmrPlcrBulkStatement.setSettlementDate(DateUtils.dateUtils().gregorianCalendarToDate(balance.getDt().getDt()));
		
		this.managePendingPayments(cgsLmrPlcrBulkStatement, balance);			
		
		return this.checkNullBalance(cgsLmrPlcrBulkStatement);
	}
	
	private CgsLmrPlcrBulkStatement manageSettledPayments(CgsLmrPlcrBulkStatement cgsLmrPlcrBulkStatement, CashBalance8 balance) {
		
		if(this.debitOrCredit(balance.getCdtDbtInd()) == 'C') {
			cgsLmrPlcrBulkStatement.setCreditPayments(balance.getAmt().getValue());
		}
		else {
			cgsLmrPlcrBulkStatement.setDebitPayments(balance.getAmt().getValue());
		}
		
		return cgsLmrPlcrBulkStatement;
	}
	
	private CgsLmrPlcrBulkStatement managePendingPayments(CgsLmrPlcrBulkStatement cgsLmrPlcrBulkStatement, CashBalance8 balance) {
		
		if(this.debitOrCredit(balance.getCdtDbtInd()) == 'D') {
			cgsLmrPlcrBulkStatement.setPendingPayments(balance.getAmt().getValue());
		}
		else {
			throw new BicompException("Attenzione... Non ammessi importi a Credito in stato PENDING");
		}
		
		return cgsLmrPlcrBulkStatement;
	}
	
	private CgsLmrLacBulkStatement checkNullBalance(CgsLmrLacBulkStatement cgsLmrLacBulkStatement) {
		
		if(cgsLmrLacBulkStatement.getCreditLiquidityTransfer() == null) {
			cgsLmrLacBulkStatement.setCreditLiquidityTransfer(new BigDecimal(0));
		}
		if(cgsLmrLacBulkStatement.getDebitLiquidityTransfer() == null) {
			cgsLmrLacBulkStatement.setDebitLiquidityTransfer(new BigDecimal(0));
		}
		if(cgsLmrLacBulkStatement.getCreditPayments() == null) {
			cgsLmrLacBulkStatement.setCreditPayments(new BigDecimal(0));
		}
		if(cgsLmrLacBulkStatement.getDebitPayments() == null) {
			cgsLmrLacBulkStatement.setDebitPayments(new BigDecimal(0));
		}
		
		return cgsLmrLacBulkStatement;
	}
	
	private CgsLmrLtinBulkStatement checkNullBalance(CgsLmrLtinBulkStatement cgsLmrLtinBulkStatement) {
		
		if(cgsLmrLtinBulkStatement.getCreditLiquidityTransfer() == null) {
			cgsLmrLtinBulkStatement.setCreditLiquidityTransfer(new BigDecimal(0));
		}
		if(cgsLmrLtinBulkStatement.getDebitLiquidityTransfer() == null) {
			cgsLmrLtinBulkStatement.setDebitLiquidityTransfer(new BigDecimal(0));
		}
		
		return cgsLmrLtinBulkStatement;
	}
	
	private CgsLmrPlcrBulkStatement checkNullBalance(CgsLmrPlcrBulkStatement cgsLmrPlcrBulkStatement) {
		
		if(cgsLmrPlcrBulkStatement.getCreditPayments() == null) {
			cgsLmrPlcrBulkStatement.setCreditPayments(new BigDecimal(0));
		}
		if(cgsLmrPlcrBulkStatement.getDebitPayments() == null) {
			cgsLmrPlcrBulkStatement.setDebitPayments(new BigDecimal(0));
		}
		if(cgsLmrPlcrBulkStatement.getPendingPayments() == null) {
			cgsLmrPlcrBulkStatement.setPendingPayments(new BigDecimal(0));
		}
		
		return cgsLmrPlcrBulkStatement;
	}
	
	private boolean isLtinBalance(CashBalance8 balance) {
		return balance.getTp().getSubTp() != null && balance.getTp().getSubTp().getPrtry().equals("LTIN");
	}
	
	private boolean isPlcrBalance(CashBalance8 balance) {
		return balance.getTp().getSubTp() != null && 
				(balance.getTp().getSubTp().getPrtry().equals("PLCR") || balance.getTp().getSubTp().getPrtry().equals("LCRS"));
	}
	
	private char debitOrCredit(CreditDebitCode creditDebitCode) {
		return creditDebitCode.value().equals("CRDT") ? 'C' : 'D';
	}
	
	private String sctOrSdd(AccountStatement9 a) {
		if(a.getId().contains("COR")) {
			return "COR";
		}
		else if(a.getId().contains("B2B")) {
			return "B2B";
		}
		else {
			return "SCT";
		}
	}
	
	private Timestamp getPaymentSettlementDate(ReportEntry10 n) {
		if(n.getSts().getCd() != null && n.getSts().getCd().equals("PDNG")) {
			return null;
		}
		
		if(n.getSts().getPrtry() != null && n.getSts().getPrtry().equals("CANC")) {
			return null;
		}
		
		if(n.getSts().getPrtry() != null && n.getSts().getPrtry().equals("RJCT")) {
			return null;
		}
		
		if(n.getSts().getPrtry() != null && n.getSts().getPrtry().equals("RVKD")) {
			return null;
		}
		
		return DateUtils.dateUtils().xmlGregorianCalendarToTimestamp(n.getBookgDt().getDtTm());
	}
	
	private String getPaymentStatus(ReportEntry10 n) {
		if(n.getSts().getCd() != null) {
			return n.getSts().getCd();
		}
		
		if(n.getSts().getPrtry() != null) {
			return n.getSts().getPrtry();
		}
		
		return null;
	}
	
	@Override
	public int hashCode() {
		
		final int prime = 2;
	    int result = 1;
	    result = prime * result + ((this.config == null) ? 0 : this.config.hashCode());
	    result = prime * result + ((this.minioService == null) ? 0 : this.minioService.hashCode());
	    result = prime * result + ((this.cgsLmrRepository == null) ? 0 : this.cgsLmrRepository.hashCode());
	    result = prime * result + ((this.cgsLmrLacBulkRepository == null) ? 0 : this.cgsLmrLacBulkRepository.hashCode());
	    result = prime * result + ((this.cgsLmrLacBulkStatementRepository == null) ? 0 : this.cgsLmrLacBulkStatementRepository.hashCode());
	    result = prime * result + ((this.cgsLmrLtinBulkRepository == null) ? 0 : this.cgsLmrLtinBulkRepository.hashCode());
	    result = prime * result + ((this.cgsLmrLtinBulkStatementRepository == null) ? 0 : this.cgsLmrLtinBulkStatementRepository.hashCode());
	    result = prime * result + ((this.cgsLmrLtinBulkStatementEntryRepository == null) ? 0 : this.cgsLmrLtinBulkStatementEntryRepository.hashCode());
	    result = prime * result + ((this.cgsLmrPlcrBulkRepository == null) ? 0 : this.cgsLmrPlcrBulkRepository.hashCode());
	    result = prime * result + ((this.cgsLmrPlcrBulkStatementRepository == null) ? 0 : this.cgsLmrPlcrBulkStatementRepository.hashCode());
	    result = prime * result + ((this.cgsLmrPlcrBulkStatementEntryRepository == null) ? 0 : this.cgsLmrPlcrBulkStatementEntryRepository.hashCode());
	    
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (obj == this)
	        return true;
	    if (!(obj instanceof CgsServiceImpl))
	        return false;
	    
	    CgsServiceImpl other = (CgsServiceImpl) obj;
	    boolean c = (this.config == null && other.config == null) || (this.config != null && this.config.equals(other.config));
	    boolean minio = (this.minioService == null && other.minioService == null) 
	    		|| (this.minioService != null && this.minioService.equals(other.minioService));
	    boolean cl = (this.cgsLmrRepository == null && other.cgsLmrRepository == null) 
	    		|| (this.cgsLmrRepository != null && this.cgsLmrRepository.equals(other.cgsLmrRepository));
	    boolean cgsLmrLacBulk = (this.cgsLmrLacBulkRepository == null && other.cgsLmrLacBulkRepository == null) 
	    		|| (this.cgsLmrLacBulkRepository != null && this.cgsLmrLacBulkRepository.equals(other.cgsLmrLacBulkRepository));
	    boolean cgsLmrLacBulkStat = (this.cgsLmrLacBulkStatementRepository == null && other.cgsLmrLacBulkStatementRepository == null)
	    		|| (this.cgsLmrLacBulkStatementRepository != null && this.cgsLmrLacBulkStatementRepository.equals(other.cgsLmrLacBulkStatementRepository));				
	    boolean cgsLmrLtinBulk = (this.cgsLmrLtinBulkRepository == null && other.cgsLmrLtinBulkRepository == null)
	    		|| (this.cgsLmrLtinBulkRepository != null && this.cgsLmrLtinBulkRepository.equals(other.cgsLmrLtinBulkRepository));				
	    boolean cgsLmrLtinBulkStat = (this.cgsLmrLtinBulkStatementRepository == null && other.cgsLmrLtinBulkStatementRepository == null)
	    		|| (this.cgsLmrLtinBulkStatementRepository != null && this.cgsLmrLtinBulkStatementRepository.equals(other.cgsLmrLtinBulkStatementRepository));				
	    boolean cgsLmrLtinBulkStatEntry = (this.cgsLmrLtinBulkStatementEntryRepository == null && other.cgsLmrLtinBulkStatementEntryRepository == null)
	    		|| (this.cgsLmrLtinBulkStatementEntryRepository != null && this.cgsLmrLtinBulkStatementEntryRepository.equals(other.cgsLmrLtinBulkStatementEntryRepository));	
	    boolean cgsLmrPlcrBulk = (this.cgsLmrPlcrBulkRepository == null && other.cgsLmrPlcrBulkRepository == null)
	    		|| (this.cgsLmrPlcrBulkRepository != null && this.cgsLmrPlcrBulkRepository.equals(other.cgsLmrPlcrBulkRepository));				
	    boolean cgsLmrPlcrBulkStat = (this.cgsLmrPlcrBulkStatementRepository == null && other.cgsLmrPlcrBulkStatementRepository == null)
	    		|| (this.cgsLmrPlcrBulkStatementRepository != null && this.cgsLmrPlcrBulkStatementRepository.equals(other.cgsLmrPlcrBulkStatementRepository));				
	    boolean cgsLmrPlcrBulkStatEntry = (this.cgsLmrPlcrBulkStatementEntryRepository == null && other.cgsLmrPlcrBulkStatementEntryRepository == null)
	    		|| (this.cgsLmrPlcrBulkStatementEntryRepository != null && this.cgsLmrPlcrBulkStatementEntryRepository.equals(other.cgsLmrPlcrBulkStatementEntryRepository));
	    return c && minio && cl && cgsLmrLacBulk && cgsLmrLacBulkStat && cgsLmrLtinBulk && cgsLmrLtinBulkStat && cgsLmrLtinBulkStatEntry 
	    		&& cgsLmrPlcrBulk && cgsLmrPlcrBulkStat && cgsLmrPlcrBulkStatEntry;
	}
	
}
