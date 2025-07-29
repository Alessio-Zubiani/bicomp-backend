package it.popso.bicomp.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.popso.bicomp.aspect.BicompLogger;
import it.popso.bicomp.dto.CgsLacDetailDto;
import it.popso.bicomp.dto.CgsLacDto;
import it.popso.bicomp.dto.PageableCgsEntryDto;
import it.popso.bicomp.dto.Response;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.CgsLmrLtinBulkStatementEntry;
import it.popso.bicomp.model.CgsLmrPlcrBulkStatementEntry;
import it.popso.bicomp.service.CgsService;
import it.popso.bicomp.utils.BicompConstants;
import it.popso.bicomp.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@RestController
@RequestMapping("/api/v1/cgs")
@RequiredArgsConstructor
public class CgsController {
	
	private final CgsService cgsService;

	
	@GetMapping("/lacs/last")
	@BicompLogger
	public ResponseEntity<Response<List<CgsLacDto>>> getLastCgsLac(@RequestParam(name = "date") String date) throws ResourceNotFoundException, BicompException {
		
		try {
			List<CgsLacDto> listCgsBalanceDto = this.cgsService.getCgsLastBalanceByDate(date);
			log.info("Get following CGS balance: {}", Arrays.toString(listCgsBalanceDto.toArray()).replaceAll(BicompConstants.LOG_CRLF, ""));
			
			return ResponseEntity.ok()
					.body(Response.<List<CgsLacDto>>builder()
						.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
						.response(listCgsBalanceDto)
						.message("Balance retrieved successfully")
						.isSuccess(true)
						.status(HttpStatus.OK.name())
						.statusCode(HttpStatus.OK.value())
						.build()
			);
		}
		catch(ParseException e) {
			throw new BicompException(e);
		}
	}
	
	@GetMapping("/lacs")
	@BicompLogger
	public ResponseEntity<Response<List<CgsLacDto>>> getCgsLacs(@RequestParam(name = "date") String date) throws ResourceNotFoundException, BicompException {
		
		try {
			List<CgsLacDto> dtos = this.cgsService.getCurrentDateLac(date);
			log.info("Get following LAC details: {}", Arrays.toString(dtos.toArray()).replaceAll(BicompConstants.LOG_CRLF, ""));
			
			return ResponseEntity.ok()
					.body(Response.<List<CgsLacDto>>builder()
						.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
						.response(dtos)
						.message("LACs retrieved successfully")
						.isSuccess(true)
						.status(HttpStatus.OK.name())
						.statusCode(HttpStatus.OK.value())
						.build()
			);
		}
		catch(ParseException e) {
			throw new BicompException(e);
		}
	}
	
	@GetMapping("/lacs/{id}")
	@BicompLogger
	public ResponseEntity<Response<CgsLacDetailDto>> getCgsLacDetail(@PathVariable("id") BigDecimal lacId) throws ResourceNotFoundException {
		
		CgsLacDetailDto dto = this.cgsService.getLacDetail(lacId);
		log.info("Get following LAC details: [{}]", dto.toString().replaceAll(BicompConstants.LOG_CRLF, ""));
		
		return ResponseEntity.ok()
				.body(Response.<CgsLacDetailDto>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(dto)
					.message("LAC details retrieved successfully")
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}
	
	@GetMapping("/payments")
	@BicompLogger
	public ResponseEntity<Response<PageableCgsEntryDto>> getPayments(@RequestParam(name = "side") Character side, @RequestParam(name = "status") String status, 
			@RequestParam(name = "amountFrom", required = false) BigDecimal amountFrom, @RequestParam(name = "amountTo", required = false) BigDecimal amountTo, 
			@RequestParam(name = "settlementDateFrom", required = false) String settlementDateFrom, @RequestParam(name = "settlementDateTo", required = false) String settlementDateTo, 
			@RequestParam(name = "service", required = false) String service, @RequestParam(name = "lac", required = false) String lac, 
			@RequestParam(name = "page") int page, @RequestParam(name = "size") int size) throws ResourceNotFoundException, BicompException {
		
		try {
			PageableCgsEntryDto p = this.cgsService.getPayments(side, amountFrom, amountTo, settlementDateFrom, settlementDateTo, service, lac, status, PageRequest.of(page, size));
			log.info("Filtered Payments: [{}]", p.toString().replaceAll(BicompConstants.LOG_CRLF, ""));
			
			return ResponseEntity.ok()
					.body(Response.<PageableCgsEntryDto>builder()
						.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
						.response(p)
						.message("Filtered Payments retrieved successfully")
						.isSuccess(true)
						.status(HttpStatus.OK.name())
						.statusCode(HttpStatus.OK.value())
						.build()
			);
		}
		catch (ParseException e) {
			throw new BicompException(e);
		}
	}
	
	@GetMapping("/liquidity-transfers")
	@BicompLogger
	public ResponseEntity<Response<PageableCgsEntryDto>> getLiquidityTransfers(@RequestParam(name = "side") Character side, @RequestParam(name = "status") String status, 
			@RequestParam(name = "amountFrom", required = false) BigDecimal amountFrom, @RequestParam(name = "amountTo", required = false) BigDecimal amountTo, 
			@RequestParam(name = "settlementDateFrom", required = false) String settlementDateFrom, @RequestParam(name = "settlementDateTo", required = false) String settlementDateTo, 
			@RequestParam(name = "lac", required = false) String lac, @RequestParam(name = "page") int page, @RequestParam(name = "size") int size) throws ResourceNotFoundException, BicompException {
		
		try {
			PageableCgsEntryDto p = this.cgsService.getLiquidityTransfers(side, amountFrom, amountTo, settlementDateFrom, settlementDateTo, lac, status, PageRequest.of(page, size));
			log.info("Filtered Liquidity Transfers: [{}]", p.toString().replaceAll(BicompConstants.LOG_CRLF, ""));
			
			return ResponseEntity.ok()
					.body(Response.<PageableCgsEntryDto>builder()
						.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
						.response(p)
						.message("Filtered Liquidity Transfers retrieved successfully")
						.isSuccess(true)
						.status(HttpStatus.OK.name())
						.statusCode(HttpStatus.OK.value())
						.build()
			);
		}
		catch (ParseException e) {
			throw new BicompException(e);
		}
	}
	
	@PostMapping("/extractor")
	@BicompLogger
	public ResponseEntity<Response<String>> cgsExtemporaryExtractor(@RequestParam(name = "date") String date) throws BicompException {
		
		try {
			Date dataRegolamento = DateUtils.dateUtils().stringToDate(date);
			
			List<CgsLmrLtinBulkStatementEntry> ltList = this.cgsService.extractCgsLiquidityTransfer(dataRegolamento);
			List<CgsLmrPlcrBulkStatementEntry> paymentList = this.cgsService.extractCgsPayment(dataRegolamento);
			
			String fileName = this.cgsService.createCgsPaymentFile(dataRegolamento, ltList, paymentList);
			
			return ResponseEntity.ok()
					.body(Response.<String>builder()
						.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
						.response(fileName)
						.message(new StringBuilder("Successfully created CGS file: [").append(fileName).append("]").toString())
						.status(HttpStatus.OK.name())
						.statusCode(HttpStatus.OK.value())
						.build()
			);
		}
		catch(IOException | BicompException | ParseException e) {
			throw new BicompException(e);
		}
	}
	
}
