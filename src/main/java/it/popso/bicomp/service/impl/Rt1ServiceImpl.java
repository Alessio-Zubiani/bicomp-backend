package it.popso.bicomp.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
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
import it.popso.bicomp.dto.PageableRt1EntryDto;
import it.popso.bicomp.dto.Rt1EntryDto;
import it.popso.bicomp.dto.Rt1LacDto;
import it.popso.bicomp.dto.Rt1TotalDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.FileManagerException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.flat.PsrFileHeader;
import it.popso.bicomp.flat.PsrParticipantBody;
import it.popso.bicomp.flat.PsrParticipantHeader;
import it.popso.bicomp.flat.PsrSettlementBicHeader;
import it.popso.bicomp.model.IsoExternalCodeSet;
import it.popso.bicomp.model.Rt1Bulk;
import it.popso.bicomp.model.Rt1BulkPayment;
import it.popso.bicomp.model.Rt1PsrFileHeader;
import it.popso.bicomp.model.Rt1PsrParticipantBody;
import it.popso.bicomp.model.Rt1PsrParticipantHeader;
import it.popso.bicomp.model.Rt1PsrSettlementBic;
import it.popso.bicomp.properties.BicompConfig;
import it.popso.bicomp.repository.IsoExternalCodeSetRepository;
import it.popso.bicomp.repository.Rt1BulkPaymentRepository;
import it.popso.bicomp.repository.Rt1BulkRepository;
import it.popso.bicomp.repository.Rt1PsrFileHeaderRepository;
import it.popso.bicomp.repository.Rt1PsrParticipantBodyRepository;
import it.popso.bicomp.repository.Rt1PsrParticipantHeaderRepository;
import it.popso.bicomp.repository.Rt1PsrSettlementBicRepository;
import it.popso.bicomp.service.MinioService;
import it.popso.bicomp.service.Rt1Service;
import it.popso.bicomp.utils.DateUtils;
import it.popso.bicomp.utils.FileManager;
import it.popso.bicomp.utils.GenericSpecification;
import it.popso.bicomp.utils.XMLUtils;
import it.popso.bicomp.xjc.rt1.bulk.SCTInstRsfBlkCredTrf;
import it.popso.bicomp.xjc.rt1.pacs002.SCTFIToFIPaymentStatusReportV10;
import it.popso.bicomp.xjc.rt1.pacs002.SCTPaymentTransaction110;
import jakarta.xml.bind.JAXBException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@Scope("prototype")
@RequiredArgsConstructor
public class Rt1ServiceImpl extends BaseServiceImpl implements Rt1Service {

	private static final String ALREADY_EXISTS = "] already exists";
	
	private final BicompConfig config;
	private final MinioService minioService;
	private final Rt1BulkRepository rt1BulkRepository;
	private final Rt1BulkPaymentRepository rt1BulkPaymentRepository;
	private final Rt1PsrFileHeaderRepository rt1PsrFileHeaderRepository;
	private final Rt1PsrSettlementBicRepository rt1PsrSettlementBicRepository;
	private final Rt1PsrParticipantHeaderRepository rt1PsrParticipantHeaderRepository;
	private final Rt1PsrParticipantBodyRepository rt1PsrParticipantBodyRepository;
	private final IsoExternalCodeSetRepository isoExternalCodeSetRepository;
	
	private Rt1TotalDto dto;
	
	
	@Override
	public List<Item> getBulkReport() throws IOException {
		
		log.info("Getting list of RSF files in: [{}]", this.config.getRt1().getShare());
		List<Item> list = this.minioService.getObjectsByPrefixAndSuffix(
				this.config.getMinio().getBucket().getName(), 
				this.config.getRt1().getShare(), 
				this.config.getRt1().getBulkPrefix(), 
				this.config.getRt1().getBulkSuffix(), 
				true);
				
		log.info("Trovati [{}] files da elaborare", list.size());
		
		return list;
	}
	
	@Override
	public List<Item> getPsrReport() throws IOException {
		
		log.info("Getting list of PSR files in: [{}]", this.config.getRt1().getShare());
		List<Item> list = this.minioService.getObjectsByPrefixAndSuffix(
				this.config.getMinio().getBucket().getName(), 
				this.config.getRt1().getShare(), 
				this.config.getRt1().getPsrPrefix(), 
				this.config.getRt1().getPsrSuffix(), 
				true);
				
		log.info("Trovati [{}] files da elaborare", list.size());
		
		return list;
	}

	@Override
	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRES_NEW, 
		rollbackFor = { IOException.class, JAXBException.class, DataIntegrityViolationException.class, BicompException.class, JpaSystemException.class }
	)
	public void processBulkReport(List<Item> list) throws IOException, JAXBException, DataIntegrityViolationException, BicompException, JpaSystemException {
		
		for(Item i : list) {
			InputStream inputStream = this.minioService.getObject(this.config.getMinio().getBucket().getName(), i.objectName());
			log.info("Elaboro flusso: [{}]", i.objectName());
			
			InputStreamWithLength inputStreamWithLength = this.fromByteArraytoInputStream(inputStream);
			
			if(inputStreamWithLength.getLength() > 0) {
				SCTInstRsfBlkCredTrf bulk = XMLUtils.unmarshall(inputStreamWithLength.getInputStream(), SCTInstRsfBlkCredTrf.class);
				log.info("File reference: [{}]", bulk.getFileRef());
				
				Optional<Rt1Bulk> oBulk = this.rt1BulkRepository.findByFileReferenceAndReportName(bulk.getFileRef(), i.objectName());
				this.isPresent(oBulk, new StringBuilder("Rt1 Bulk with fileReference [").append(bulk.getFileRef()).append("] and reportName [").append(i.objectName()).append(ALREADY_EXISTS).toString());
				
				Rt1Bulk rt1Bulk = Rt1Bulk.builder()
						.envIndicator(bulk.getTstCode().value().charAt(0))
						.fileCycle(bulk.getFileLacNo())
						.fileDate(DateUtils.dateUtils().gregorianCalendarToDate(bulk.getFileBusDt()))
						.fileReference(bulk.getFileRef())
						.fileType(bulk.getFType().value())
						.receivingInstitute(bulk.getRcvgInst())
						.reportName(i.objectName())
						.sendingInstitute(bulk.getSndgInst())
						.build();
				this.rt1BulkRepository.save(rt1Bulk);
				
				this.processBulk(bulk, rt1Bulk);
			}
			else {
				log.warn("Size is 0. Nothing to do");
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRES_NEW, 
		rollbackFor = { IOException.class, FileManagerException.class, DataIntegrityViolationException.class, BicompException.class, ParseException.class, JpaSystemException.class }
	)
	public void processPreSettlementReport(List<Item> list) throws IOException, FileManagerException, DataIntegrityViolationException, BicompException, ParseException, JpaSystemException {
		
		for(Item i : list) {
			InputStream inputStream = this.minioService.getObject(this.config.getMinio().getBucket().getName(), i.objectName());
			log.info("Elaboro flusso: [{}]", i.objectName());
			
			InputStreamWithLength inputStreamWithLength = this.fromByteArraytoInputStream(inputStream);
			
			if(inputStreamWithLength.getLength() > 0) {
				String content = FileManager.fileManager().getPsrString(inputStreamWithLength.getInputStream());
				
				PsrFileHeader psrFileHeader = FileManager.fileManager().parseHpsr(content);
				Optional<Rt1PsrFileHeader> oPsr = this.rt1PsrFileHeaderRepository.findByReportNameAndSenderFileReference(i.objectName(), psrFileHeader.getSenderFileReference());
				this.isPresent(oPsr, new StringBuilder("Rt1 Psr File Header with reportName [").append(i.objectName()).append("] and senderFileReference [").append(psrFileHeader.getSenderFileReference()).append(ALREADY_EXISTS).toString());
				
				Rt1PsrFileHeader rt1PsrFileHeader = Rt1PsrFileHeader.builder()
						.dateTime(DateUtils.dateUtils().stringToDateTime(psrFileHeader.getDateTime()))
						.fileType(psrFileHeader.getFileType())
						.lac(psrFileHeader.getLac())
						.receivingInstitution(psrFileHeader.getReceivingInstitution())
						.recordNumber(this.stringToBigDecimal(psrFileHeader.getNumberOfRecord()))
						.reportName(i.objectName())
						.senderFileReference(psrFileHeader.getSenderFileReference())
						.sendingInstitution(psrFileHeader.getSendingInstitution())
						.serviceIdentifier(psrFileHeader.getServiceIdentifier())
						.settlementDate(DateUtils.dateUtils().stringToDate(psrFileHeader.getSettlementDate()))
						.testCode(psrFileHeader.getTestCode())
						.build();
				this.rt1PsrFileHeaderRepository.save(rt1PsrFileHeader);
				
				PsrSettlementBicHeader psrSettlementBicHeader = FileManager.fileManager().parsePsbh(content, 69);
				Rt1PsrSettlementBic rt1PsrSettlementBic = Rt1PsrSettlementBic.builder()
						.settlementBic(psrSettlementBicHeader.getSettlementBic())
						.initialPositionIndicator(psrSettlementBicHeader.getInitialPositionIndicator())
						.initialLiquidityPosition(this.stringToBigDecimal(psrSettlementBicHeader.getInitialLiquidityPosition()))
						.finalPositionIndicator(psrSettlementBicHeader.getFinalPositionIndicator())
						.finalLiquidityPosition(this.stringToBigDecimal(psrSettlementBicHeader.getFinalLiquidityPosition()))
						.rt1PsrFileHeader(rt1PsrFileHeader)
						.build();
				this.rt1PsrSettlementBicRepository.save(rt1PsrSettlementBic);
				
				Object[] participantAndBody = FileManager.fileManager().parsePdph(content, 124);
				for(PsrParticipantHeader ph : ((List<PsrParticipantHeader>)participantAndBody[0])) {
					Rt1PsrParticipantHeader rt1PsrParticipantHeader = Rt1PsrParticipantHeader.builder()
							.acceptedPrevPdngAmount(this.stringToBigDecimal(ph.getAcceptedAmountPreviousInstantPrrPending()))
							.acceptedPrevRt1aPdngAmount(this.stringToBigDecimal(ph.getAcceptedAmountPreviousInstantPrrRt1aPending()))
							.creditLtAmount(this.stringToBigDecimal(ph.getCreditLiquidityTransferAmount()))
							.debitLtAmount(this.stringToBigDecimal(ph.getDebitLiquidityTransferAmount()))
							.finalPartPosition(this.stringToBigDecimal(ph.getFinalParticipantPosition()))
							.finalPartPositionIndicator(ph.getFinalParticipantPositionIndicator())
							.initialPartPosition(this.stringToBigDecimal(ph.getInitialParticipantPosition()))
							.initialPartPositionIndicator(ph.getInitialParticipantPositionIndicator())
							.instantPrrPendingAmount(this.stringToBigDecimal(ph.getInstantPrrPendingAmount()))
							.instantPrrReceivedAmount(this.stringToBigDecimal(ph.getInstantPrrReceivedAmount()))
							.instantPrrRt1aPendingAmount(this.stringToBigDecimal(ph.getInstantPrrRt1aPendingAmount()))
							.instantPrrRt1aReceivedAmount(this.stringToBigDecimal(ph.getInstantPrrRt1aReceivedAmount()))
							.instantPrrRt1aSentAmount(this.stringToBigDecimal(ph.getInstantPrrRt1aSentAmount()))
							.instantPrrSentAmount(this.stringToBigDecimal(ph.getInstantPrrSentAmount()))
							.receivedFundingAmount(this.stringToBigDecimal(ph.getReceivedFundingAmount()))
							.rejectedDefundingAmount(this.stringToBigDecimal(ph.getRejectedDefundingAmount()))
							.rejectedPrevPdngAmount(this.stringToBigDecimal(ph.getRejectedAmountPreviousInstantPrrPending()))
							.rejectedPrevRt1aPdngAmount(this.stringToBigDecimal(ph.getRejectedAmountPreviousInstantPrrRt1aPending()))
							.requestedDefundingAmount(this.stringToBigDecimal(ph.getRequestedDefundingAmount()))
							.requestedFundingAmount(this.stringToBigDecimal(ph.getRequestedFundingAmount()))
							.settlementBic(ph.getSettlementBic())
							.rt1PsrSettlementBic(rt1PsrSettlementBic)
							.build();
					this.rt1PsrParticipantHeaderRepository.save(rt1PsrParticipantHeader);
					
					for(PsrParticipantBody pb : ((List<PsrParticipantBody>)participantAndBody[1])) {
						Rt1PsrParticipantBody rt1PsrParticipantBody = Rt1PsrParticipantBody.builder()
								.paymentAmount(this.stringToBigDecimal(pb.getLiquidityOperationAmount()))
								.paymentReference(pb.getLiquidityInstructionReference())
								.paymentStatus(pb.getLiquidityInstructionStatus())
								.operationType(pb.getOperationType())
								.rt1PsrParticipantHeader(rt1PsrParticipantHeader)
								.build();
						this.rt1PsrParticipantBodyRepository.save(rt1PsrParticipantBody);
					}
				}
			}
			else {
				log.warn("Size is 0. Nothing to do");
			}
		}
	}

	private void isPresent(Optional<?> o, String errorMessage) {
		if(o.isPresent()) {
			log.error(errorMessage);
			throw new BicompException(errorMessage);
		}
	}
	
	@Override
	public void moveToBackupFolder(List<Item> list) throws IOException {
		
		for(Item i : list) {
			String fileName = i.objectName().substring(this.config.getRt1().getShare().length());
			String year = "20".concat(fileName.substring(15, 17));
			
			log.info("Copio [{}] nella cartella di backup: [{}]", i.objectName(), this.config.getRt1().getShare().concat(year));
			this.minioService.copyFile(this.config.getMinio().getBucket().getName(), 
					i.objectName(),
					this.config.getMinio().getBucket().getName(), 
					this.config.getRt1().getShare().concat(year).concat("/").concat(fileName)
			);
			
			log.info("Rimuovo file: [{}]", i.objectName());
			this.minioService.removeFile(this.config.getMinio().getBucket().getName(), i.objectName());
		}
	}
	
	@Override
	public List<Rt1LacDto> getRt1LastBalanceByDate(String date) throws ResourceNotFoundException, ParseException {
		
		log.info("Getting RT1 last closing balance by date");
		Date dataRegolamento = DateUtils.dateUtils().stringToDate(date);
    	log.info("Current date is: [{}]", dataRegolamento);
    	
    	Rt1PsrSettlementBic rt1 = this.rt1PsrSettlementBicRepository.findLastLacByDate(dataRegolamento, PageRequest.of(0, 1))
    			.orElseThrow(() -> new ResourceNotFoundException(new StringBuilder("No RT1 LAC found for date: [").append(dataRegolamento).append("]").toString()));
		
		log.info("Retrieved LAC [{}]", rt1);
		List<Rt1LacDto> listDtos = new ArrayList<>();
		listDtos.add(Rt1LacDto.builder()
				.lacId(rt1.getId())
				.lacNumber(rt1.getRt1PsrFileHeader().getLac())
				.lacDateTime(DateUtils.dateUtils().dateToLocalDateTime(rt1.getRt1PsrFileHeader().getDateTime()))
				.openingBalance(this.positiveOrNegative(rt1.getInitialPositionIndicator(), rt1.getInitialLiquidityPosition()))
				.closingBalance(this.positiveOrNegative(rt1.getFinalPositionIndicator(), rt1.getFinalLiquidityPosition()))
				.currency("EUR")
				.build());
		
		return listDtos;
	}
	
	@Override
	public List<Rt1LacDto> getRt1DailyLac(String date) throws ResourceNotFoundException, ParseException {
		
		List<Rt1LacDto> listRt1LacDtos = new ArrayList<>();
		
		Date dataRegolamento = DateUtils.dateUtils().stringToDate(date);
		log.info("Getting LACs for date: [{}]", dataRegolamento);
    	log.info("Current date is: [{}]", dataRegolamento);
    	
    	List<Rt1PsrSettlementBic> rt1 = this.rt1PsrSettlementBicRepository.findLacsByDate(dataRegolamento);
		if(rt1.isEmpty()) {
			throw new ResourceNotFoundException(new StringBuilder("No RT1 LACs found for date: [").append(dataRegolamento).append("]").toString());
		}
		
		rt1.forEach(r -> 
			listRt1LacDtos.add(Rt1LacDto.builder()
					.lacId(r.getId())
					.lacNumber(r.getRt1PsrFileHeader().getLac())
					.lacDateTime(DateUtils.dateUtils().dateToLocalDateTime(r.getRt1PsrFileHeader().getDateTime()))
					.openingBalance(this.positiveOrNegative(r.getInitialPositionIndicator(), r.getInitialLiquidityPosition()))
					.closingBalance(this.positiveOrNegative(r.getFinalPositionIndicator(), r.getFinalLiquidityPosition()))
					.currency("EUR")
					.build())
		);
		
		return listRt1LacDtos;
	}

	@Override
	public Rt1TotalDto getRt1LacDetail(BigDecimal id) throws ResourceNotFoundException {
		
		log.info("Getting RT1 totals by lacId");
    	
    	Rt1PsrSettlementBic r = this.rt1PsrSettlementBicRepository.findById(id)
    			.orElseThrow(() -> new ResourceNotFoundException(new StringBuilder("No RT1 LAC found for ID: [").append(id).append("]").toString()));
    	
    	this.dto = Rt1TotalDto.builder()
    			.settlementDate(DateUtils.dateUtils().dateToLocalDate(r.getRt1PsrFileHeader().getSettlementDate()))
    			.creditLtAmount(BigDecimal.ZERO)
    			.debitLtAmount(BigDecimal.ZERO)
    			.creditPmntAmount(BigDecimal.ZERO)
    			.debitPmntAmount(BigDecimal.ZERO)
    			.rejectedCreditPmntAmount(BigDecimal.ZERO)
    			.rejectedDebitPmntAmount(BigDecimal.ZERO)
    			.currency("EUR")
    			.build();
    	
    	List<Rt1PsrParticipantBody> ltList = this.rt1PsrParticipantBodyRepository.findDailyLiquidityTransfer(id);
    	if(!ltList.isEmpty()) {
    		ltList.forEach(this::computeLtAmount);
    	}
    	
    	List<Object[]> paymentList = this.rt1BulkPaymentRepository.findTotalBySettlementDateAndLac(r.getRt1PsrFileHeader().getSettlementDate(), r.getRt1PsrFileHeader().getLac());
    	if(!paymentList.isEmpty()) {
    		paymentList.forEach(this::computePaymentAmount);
    	}
    	
    	return this.dto;
	}
	
	@Override
	public PageableRt1EntryDto getPayments(Character side, String status, BigDecimal amountFrom, BigDecimal amountTo, 
			String settlementDateFrom, String settlementDateTo, String lac, Pageable paging) throws ResourceNotFoundException, ParseException {
		
		log.info("Getting RT1 Payment with custom filters");
		
		Specification<Rt1BulkPayment> specification = Specification.where(null);
		if(side != null) {
			specification = specification.and(GenericSpecification.rt1PaymentHasSide(side));
		}
		if(amountFrom != null) {
			specification = specification.and(GenericSpecification.rt1PaymentHasAmountFrom(amountFrom));
		}
		if(amountTo != null) {
			specification = specification.and(GenericSpecification.rt1PaymentHasAmountTo(amountTo));
		}
		if(settlementDateFrom != null) {
			Date dateFrom = DateUtils.dateUtils().stringToDateYear(settlementDateFrom);
			specification = specification.and(GenericSpecification.rt1PaymentHasSettlementDateFrom(dateFrom));
		}
		if(settlementDateTo != null) {
			Date dateTo = DateUtils.dateUtils().stringToDateYear(settlementDateTo);
			specification = specification.and(GenericSpecification.rt1PaymentHasSettlementDateTo(dateTo));
		}
		if(status != null) {
			specification = specification.and(GenericSpecification.rt1PaymentHasStatus(status));
		}
		if(lac != null) {
			specification = specification.and(GenericSpecification.rt1PaymentHasLac(lac));
		}
		
		specification = specification.and(GenericSpecification.rt1PaymentHasMessageName());
		specification = specification.and(GenericSpecification.rt1PaymentSortByAcceptanceDateTimeAsc());
		
		Page<Rt1BulkPayment> page = this.rt1BulkPaymentRepository.findAll(specification, paging);
		if(page.isEmpty()) {
			throw new ResourceNotFoundException(new StringBuilder("No Payment found with filter: [side = ").append(side)
				.append("] [status = ").append(status).append("] [amountFrom = ").append(amountFrom).append("] [amountTo = ").append(amountTo)
				.append("] [settlementDateFrom = ").append(settlementDateFrom).append("] [settlementDateTo = ").append(settlementDateTo)
				.append("] [lac = ").append(lac).append("] [page = ").append(paging.getPageNumber()).append("] [size = ").append(paging.getPageSize()).append("]").toString());
		}
		
		log.info("List of payments: {}", Arrays.toString(page.getContent().toArray()));
		List<Rt1EntryDto> dtos = new ArrayList<>();
		
		PageableRt1EntryDto p = PageableRt1EntryDto.builder().build();
		page.getContent().forEach(e -> {
			log.info("Reference: [{}]", e.getMsgId());
			
			IsoExternalCodeSet i = null;
			if(e.getRejectReason() != null) {
				i = this.getExternalCodeSet(e.getRejectReason());
			}
			
			dtos.add(Rt1EntryDto.builder()
					.entryId(e.getId())
					.entryReference(e.getMsgId())
					.paymentAmount(e.getPaymentAmount())
					.currency(e.getCurrency())
					.settlementDateTime(e.getAcceptanceDateTime())
					.side(e.getSide())
					.status(e.getStatus())
					.debitor(e.getDebitor())
					.creditor(e.getCreditor())
					.rejectReasonCode(e.getRejectReason())
					.rejectReasonDescription(i != null ? i.getCodeDescription() : null)
					.build()
			);
		});	
		p.setTotalElements(new BigDecimal(page.getTotalElements()));
		p.setEntries(dtos);
		
		return p;
	}
	
	@Override
	public PageableRt1EntryDto getLiquidityTransfers(Character side, BigDecimal amountFrom, BigDecimal amountTo,
			String settlementDateFrom, String settlementDateTo, String lac, String status, Pageable paging)
			throws ResourceNotFoundException, ParseException {
		
		log.info("Getting RT1 Liquidity Transfers with custom filters");
		
		Specification<Rt1PsrParticipantBody> specification = Specification.where(null);
		if(side != null) {
			specification = specification.and(GenericSpecification.rt1LiquidityTransferHasSide(side));
		}
		if(amountFrom != null) {
			specification = specification.and(GenericSpecification.rt1LiquidityTransferHasAmountFrom(amountFrom));
		}
		if(amountTo != null) {
			specification = specification.and(GenericSpecification.rt1LiquidityTransferHasAmountTo(amountTo));
		}
		if(settlementDateFrom != null) {
			Date dateFrom = DateUtils.dateUtils().stringToDateYear(settlementDateFrom);
			specification = specification.and(GenericSpecification.rt1LiquidityTransferHasSettlementDateFrom(dateFrom));
		}
		if(settlementDateTo != null) {
			Date dateTo = DateUtils.dateUtils().stringToDateYear(settlementDateTo);
			specification = specification.and(GenericSpecification.rt1LiquidityTransferHasSettlementDateTo(dateTo));
		}
		if(status != null) {
			specification = specification.and(GenericSpecification.rt1LiquidityTransferHasStatus(status));
		}
		
		specification = specification.and(GenericSpecification.rt1LiquidityTransferSortBySettlementDateAsc());
		
		Page<Rt1PsrParticipantBody> page = this.rt1PsrParticipantBodyRepository.findAll(specification, paging);
		if(page.isEmpty()) {
			throw new ResourceNotFoundException(new StringBuilder("No Payment found with filter: [side = ").append(side)
				.append("] [status = ").append(status).append("] [amountFrom = ").append(amountFrom).append("] [amountTo = ").append(amountTo)
				.append("] [settlementDateFrom = ").append(settlementDateFrom).append("] [settlementDateTo = ").append(settlementDateTo)
				.append("] [lac = ").append(lac).append("] [page = ").append(paging.getPageNumber()).append("] [size = ").append(paging.getPageSize()).append("]").toString());
		}
		
		log.info("List of payments: {}", Arrays.toString(page.getContent().toArray()));
		List<Rt1EntryDto> dtos = new ArrayList<>();
		
		PageableRt1EntryDto p = PageableRt1EntryDto.builder().build();
		page.getContent().forEach(e -> {
			log.info("Reference: [{}]", e.getPaymentReference());
			
			dtos.add(Rt1EntryDto.builder()
					.entryId(e.getId())
					.entryReference(e.getPaymentReference())
					.paymentAmount(e.getPaymentAmount())
					.currency("EUR")
					.settlementDateTime(e.getRt1PsrParticipantHeader().getRt1PsrSettlementBic().getRt1PsrFileHeader().getDateTime())
					.side(e.getOperationType().equals("CLRR") ? 'D' : 'C')
					.status(e.getPaymentStatus())
					.build()
			);
		});		
		p.setTotalElements(new BigDecimal(page.getTotalElements()));
		p.setEntries(dtos);
		
		return p;
	}
	
	private void processBulk(SCTInstRsfBlkCredTrf bulk, Rt1Bulk rt1Bulk) {
		
		for(SCTFIToFIPaymentStatusReportV10 f : bulk.getFIToFIPmtStsRptS2()) {
			log.info("Payment MSG_ID: [{}]", f.getGrpHdr().getMsgId());
			if(f.getTxInfAndSts().size() == 1) {
				SCTPaymentTransaction110 p = f.getTxInfAndSts().get(0);

				Optional<Rt1BulkPayment> oPayment = this.rt1BulkPaymentRepository.findByMsgId(f.getGrpHdr().getMsgId());
				this.isPresent(oPayment, new StringBuilder("Rt1 Payment with msgId [").append(f.getGrpHdr().getMsgId()).append(ALREADY_EXISTS).toString());

				Rt1BulkPayment rt1BulkPayment = Rt1BulkPayment.builder()
						.acceptanceDateTime(p.getAccptncDtTm() != null 
							? DateUtils.dateUtils().xmlGregorianCalendarToTimestamp(p.getAccptncDtTm())
							: null)
						.creationDateTime(DateUtils.dateUtils().xmlGregorianCalendarToTimestamp(f.getGrpHdr().getCreDtTm()))
						.creditor(p.getOrgnlTxRef().getCdtrAgt().getFinInstnId().getBICFI())
						.currency(p.getOrgnlTxRef().getIntrBkSttlmAmt().getCcy().value())
						.debitor(p.getOrgnlTxRef().getDbtrAgt().getFinInstnId().getBICFI())
						.msgId(f.getGrpHdr().getMsgId())
						.originalAmount(f.getOrgnlGrpInfAndSts().getOrgnlCtrlSum())
						.originalEndToEndId(p.getOrgnlEndToEndId())
						.originalInstructionId(p.getOrgnlInstrId())
						.originalMsgId(f.getOrgnlGrpInfAndSts().getOrgnlMsgId())
						.originalMsgName(f.getOrgnlGrpInfAndSts().getOrgnlMsgNmId())
						.originalTransactionId(p.getOrgnlTxId())
						.originalTransactionNumber(new BigDecimal(f.getOrgnlGrpInfAndSts().getOrgnlNbOfTxs()))
						.paymentAmount(p.getOrgnlTxRef().getIntrBkSttlmAmt().getValue())
						.rejectReason(f.getOrgnlGrpInfAndSts().getGrpSts().equals("RJCT") 
								? f.getOrgnlGrpInfAndSts().getStsRsnInf().getRsn().getPrtry()
								: null)
						.settlementDate(DateUtils.dateUtils().gregorianCalendarToDate(p.getOrgnlTxRef().getIntrBkSttlmDt()))
						.side(this.debitOrCredit(f.getOrgnlGrpInfAndSts().getOrgnlMsgNmId(), p.getOrgnlTxRef().getDbtrAgt().getFinInstnId().getBICFI()))
						.status(p.getTxSts())
						.statusId(p.getStsId())
						.rt1Bulk(rt1Bulk)
						.build();
				this.rt1BulkPaymentRepository.save(rt1BulkPayment);
			}
			else {
				throw new BicompException(new StringBuilder("Found [").append(f.getTxInfAndSts().size()).append("] TxInfAndSts").toString());
			}
		}
	}
	
	private Character debitOrCredit(String msgName, String side) {
		if(msgName.equals("pacs.004") || msgName.equals("camt.056")) {
			return side.contains("POSOIT") ? 'C' : 'D';
		}
		
		return side.contains("POSOIT") ? 'D' : 'C';
	}
	
	private BigDecimal stringToBigDecimal(String amount) {
		
		return new BigDecimal(amount.replace(',', '.'));
	}
	
	private BigDecimal positiveOrNegative(String indicator, BigDecimal amount) {
		
		return indicator.equals("CR") ? amount : amount.negate();
	}
	
	private void computeLtAmount(Rt1PsrParticipantBody r) {
		
		if(r.getOperationType().equals("CPFR")) {
			this.dto.setCreditLtAmount(this.dto.getCreditLtAmount().add(r.getPaymentAmount()));
		} else {
			this.dto.setDebitLtAmount(this.dto.getDebitLtAmount().add(r.getPaymentAmount()));
		}
	}
	
	private void computePaymentAmount(Object[] obj) {
		
		if(obj[0].toString().equals("C")) {
			if(obj[1].toString().equals("ACSC")) {
				this.dto.setCreditPmntAmount((BigDecimal) obj[2]);
			} else {
				this.dto.setRejectedCreditPmntAmount((BigDecimal) obj[2]);
			}
		}
		else {
			if(obj[1].toString().equals("ACSC")) {
				this.dto.setDebitPmntAmount((BigDecimal) obj[2]);
			} else {
				this.dto.setRejectedDebitPmntAmount((BigDecimal) obj[2]);
			}
		}
	}
	
	private IsoExternalCodeSet getExternalCodeSet(String codeValue) {
		
		log.info("Searching external code with CODE_VALUE: [{}]", codeValue);
		List<IsoExternalCodeSet> list = this.isoExternalCodeSetRepository.findByCodeValue(codeValue);
		if(list.isEmpty()) {
			return null;
		}
		
		return list.get(0);
	}
	
	@Override
	public int hashCode() {
		
		final int prime = 2;
	    int result = 1;
	    result = prime * result + ((this.config == null) ? 0 : this.config.hashCode());
	    result = prime * result + ((this.minioService == null) ? 0 : this.minioService.hashCode());
	    result = prime * result + ((this.rt1BulkRepository == null) ? 0 : this.rt1BulkRepository.hashCode());
	    result = prime * result + ((this.rt1BulkPaymentRepository == null) ? 0 : this.rt1BulkPaymentRepository.hashCode());
	    result = prime * result + ((this.rt1PsrFileHeaderRepository == null) ? 0 : this.rt1PsrFileHeaderRepository.hashCode());
	    result = prime * result + ((this.rt1PsrSettlementBicRepository == null) ? 0 : this.rt1PsrSettlementBicRepository.hashCode());
	    result = prime * result + ((this.rt1PsrParticipantHeaderRepository == null) ? 0 : this.rt1PsrParticipantHeaderRepository.hashCode());
	    result = prime * result + ((this.rt1PsrParticipantBodyRepository == null) ? 0 : this.rt1PsrParticipantBodyRepository.hashCode());
	    result = prime * result + ((this.isoExternalCodeSetRepository == null) ? 0 : this.isoExternalCodeSetRepository.hashCode());
	    
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (obj == this)
	        return true;
	    if (!(obj instanceof Rt1ServiceImpl))
	        return false;
	    
	    Rt1ServiceImpl other = (Rt1ServiceImpl) obj;
	    boolean c = (this.config == null && other.config == null) || (this.config != null && this.config.equals(other.config));
	    boolean minio = (this.minioService == null && other.minioService == null) 
	    		|| (this.minioService != null && this.minioService.equals(other.minioService));
	    boolean rt1Bulk = (this.rt1BulkRepository == null && other.rt1BulkRepository == null) 
	    		|| (this.rt1BulkRepository != null && this.rt1BulkRepository.equals(other.rt1BulkRepository));
	    boolean rt1BulkPay = (this.rt1BulkPaymentRepository == null && other.rt1BulkPaymentRepository == null) 
	    		|| (this.rt1BulkPaymentRepository != null && this.rt1BulkPaymentRepository.equals(other.rt1BulkPaymentRepository));
	    boolean rt1Psr = (this.rt1PsrFileHeaderRepository == null && other.rt1PsrFileHeaderRepository == null)
	    		|| (this.rt1PsrFileHeaderRepository != null && this.rt1PsrFileHeaderRepository.equals(other.rt1PsrFileHeaderRepository));				
	    boolean rt1PsrBic = (this.rt1PsrSettlementBicRepository == null && other.rt1PsrSettlementBicRepository == null)
	    		|| (this.rt1PsrSettlementBicRepository != null && this.rt1PsrSettlementBicRepository.equals(other.rt1PsrSettlementBicRepository));				
	    boolean rt1PsrPart = (this.rt1PsrParticipantHeaderRepository == null && other.rt1PsrParticipantHeaderRepository == null)
	    		|| (this.rt1PsrParticipantHeaderRepository != null && this.rt1PsrParticipantHeaderRepository.equals(other.rt1PsrParticipantHeaderRepository));				
	    boolean rt1PsrPartBody = (this.rt1PsrParticipantBodyRepository == null && other.rt1PsrParticipantBodyRepository == null)
	    		|| (this.rt1PsrParticipantBodyRepository != null && this.rt1PsrParticipantBodyRepository.equals(other.rt1PsrParticipantBodyRepository));
	    boolean iso = (this.isoExternalCodeSetRepository == null && other.isoExternalCodeSetRepository == null)
	    		|| (this.isoExternalCodeSetRepository != null && this.isoExternalCodeSetRepository.equals(other.isoExternalCodeSetRepository));
	
	    return c && minio && rt1Bulk && rt1BulkPay && rt1Psr && rt1PsrBic && rt1PsrPart && rt1PsrPartBody && iso;
	}
	
}
