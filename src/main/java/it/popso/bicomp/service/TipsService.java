package it.popso.bicomp.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;

import io.minio.messages.Item;
import it.popso.bicomp.dto.PageableTipsEntryDto;
import it.popso.bicomp.dto.PageableTipsReportDto;
import it.popso.bicomp.dto.TipsBalanceDto;
import it.popso.bicomp.dto.TipsTotalDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.TipsCamt052BankAccountReport;
import it.popso.bicomp.model.TipsCamt053BankAccountStatementEntry;
import jakarta.xml.bind.JAXBException;

public interface TipsService {
	
	List<Item> getCamt052Report() throws IOException;
	
	List<Item> getCamt053Report() throws IOException;
	
	void processCamt052Report(List<Item> list) throws IOException, JAXBException, DataIntegrityViolationException, BicompException;
	
	void processCamt053Report(List<Item> list) throws IOException, JAXBException, DataIntegrityViolationException, BicompException;
	
	List<TipsCamt053BankAccountStatementEntry> extractTipsPayment(Date date);

	String createTipsPaymentFile(Date date, List<TipsCamt053BankAccountStatementEntry> list) throws BicompException, ParseException;

	void moveToBackupFolder052(List<Item> list) throws IOException;
	
	void moveToBackupFolder053(List<Item> list) throws IOException;
	
	List<TipsBalanceDto> getTipsLastBalanceByDate(String date) throws ResourceNotFoundException, ParseException;
	
	PageableTipsReportDto getTipsDailyReport(String date, Pageable paging) throws ResourceNotFoundException, ParseException;
	
	TipsTotalDto getTipsReportDetail(BigDecimal id) throws ResourceNotFoundException;
	
	PageableTipsEntryDto getPayments(Character side, String status, BigDecimal amountFrom, BigDecimal amountTo, String settlementDateTimeFrom, String settlementDateTimeTo, Pageable paging) throws ResourceNotFoundException, ParseException;

	public List<TipsCamt052BankAccountReport> getSaldiTips() throws ResourceNotFoundException;
	
	public TipsCamt053BankAccountStatementEntry getLastTipsPaymentByDate(Date date) throws ResourceNotFoundException;
	
	public String updateFlagElaborato(String messageId) throws ResourceNotFoundException;
	
}
