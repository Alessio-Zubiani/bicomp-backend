package it.popso.bicomp.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;

import io.minio.messages.Item;
import it.popso.bicomp.dto.CgsLacDetailDto;
import it.popso.bicomp.dto.CgsLacDto;
import it.popso.bicomp.dto.PageableCgsEntryDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.CgsLmrLacBulkStatement;
import it.popso.bicomp.model.CgsLmrLtinBulkStatementEntry;
import it.popso.bicomp.model.CgsLmrPlcrBulkStatementEntry;
import jakarta.xml.bind.JAXBException;

public interface CgsService {
	
	List<CgsLacDto> getCgsLastBalanceByDate(String date) throws ResourceNotFoundException, ParseException;
	
	List<CgsLacDto> getCurrentDateLac(String date) throws ResourceNotFoundException, ParseException;
	
	CgsLacDetailDto getLacDetail(BigDecimal lacId) throws ResourceNotFoundException;
	
	List<Item> getLmrReport() throws BicompException;
	
	void processLmrReport(List<Item> list) throws IOException, JAXBException, DataIntegrityViolationException, BicompException;
	
	List<CgsLmrLtinBulkStatementEntry> extractCgsLiquidityTransfer(Date date);
	
	List<CgsLmrPlcrBulkStatementEntry> extractCgsPayment(Date date);

	String createCgsPaymentFile(Date date, List<CgsLmrLtinBulkStatementEntry> ltList, List<CgsLmrPlcrBulkStatementEntry> paymentList) throws IOException, BicompException, ParseException;

	void moveToBackupFolder(List<Item> list) throws BicompException;
	
	PageableCgsEntryDto getPayments(Character side, BigDecimal amountFrom, BigDecimal amountTo, String settlementDateTimeFrom, String settlementDateTimeTo, String service, String lac, String status, Pageable paging) throws ResourceNotFoundException, ParseException;
	
	PageableCgsEntryDto getLiquidityTransfers(Character side, BigDecimal amountFrom, BigDecimal amountTo, String settlementDateTimeFrom, String settlementDateTimeTo, String lac, String status, Pageable paging) throws ResourceNotFoundException, ParseException;
	
	List<CgsLmrLacBulkStatement> getSaldiCgs(Date date) throws ResourceNotFoundException;
	
	CgsLmrLacBulkStatement getLastCgsBalance(Date date) throws ResourceNotFoundException;

}
