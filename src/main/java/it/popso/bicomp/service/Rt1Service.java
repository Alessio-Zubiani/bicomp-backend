package it.popso.bicomp.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;

import io.minio.messages.Item;
import it.popso.bicomp.dto.PageableRt1EntryDto;
import it.popso.bicomp.dto.Rt1LacDto;
import it.popso.bicomp.dto.Rt1TotalDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.FileManagerException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import jakarta.xml.bind.JAXBException;

public interface Rt1Service {
	
	List<Item> getBulkReport() throws IOException;
	
	List<Item> getPsrReport() throws IOException;
	
	void processBulkReport(List<Item> list) throws IOException, JAXBException, DataIntegrityViolationException, BicompException;
	
	void processPreSettlementReport(List<Item> list) throws IOException, FileManagerException, DataIntegrityViolationException, BicompException, ParseException;
	
	void moveToBackupFolder(List<Item> list) throws IOException;
	
	List<Rt1LacDto> getRt1LastBalanceByDate(String date) throws ResourceNotFoundException, ParseException;
	
	List<Rt1LacDto> getRt1DailyLac(String date) throws ResourceNotFoundException, ParseException;
	
	Rt1TotalDto getRt1LacDetail(BigDecimal id) throws ResourceNotFoundException;
	
	PageableRt1EntryDto getPayments(Character side, String status, BigDecimal amountFrom, BigDecimal amountTo, String settlementDateTimeFrom, String settlementDateTimeTo, String lac, Pageable paging) throws ResourceNotFoundException, ParseException;
	
	PageableRt1EntryDto getLiquidityTransfers(Character side, BigDecimal amountFrom, BigDecimal amountTo, String settlementDateTimeFrom, String settlementDateTimeTo, String lac, String status, Pageable paging) throws ResourceNotFoundException, ParseException;

}
