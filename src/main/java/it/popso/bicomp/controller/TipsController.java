package it.popso.bicomp.controller;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
import it.popso.bicomp.dto.PageableTipsEntryDto;
import it.popso.bicomp.dto.PageableTipsReportDto;
import it.popso.bicomp.dto.Response;
import it.popso.bicomp.dto.TipsBalanceDto;
import it.popso.bicomp.dto.TipsTotalDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.TipsCamt053BankAccountStatementEntry;
import it.popso.bicomp.service.TipsService;
import it.popso.bicomp.utils.BicompConstants;
import it.popso.bicomp.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@RestController
@RequestMapping("/api/v1/tips")
@RequiredArgsConstructor
public class TipsController {
	
	private final TipsService tipsService;
	

	@GetMapping("/reports/last")
	@BicompLogger
	public ResponseEntity<Response<List<TipsBalanceDto>>> getLastTipsReport(@RequestParam(name = "date") String date) throws ResourceNotFoundException, BicompException {
		
		try {
			List<TipsBalanceDto> listTipsBalanceDto = this.tipsService.getTipsLastBalanceByDate(date);
			log.debug("Get following TIPS balance: {}".replaceAll(BicompConstants.LOG_CRLF, ""), Arrays.toString(listTipsBalanceDto.toArray()));
			
			return ResponseEntity.ok()
					.body(Response.<List<TipsBalanceDto>>builder()
						.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
						.response(listTipsBalanceDto)
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
	
	@GetMapping("/reports")
	@BicompLogger
	public ResponseEntity<Response<PageableTipsReportDto>> getTipsDailyReports(@RequestParam(name = "date") String date, @RequestParam(name = "page", required = false) Optional<Integer> page) throws ResourceNotFoundException, ParseException {
		
		try {
			PageableTipsReportDto dto = null;
			if(page.isEmpty()) {
				dto = this.tipsService.getTipsDailyReport(date, null);
			}
			else {
				dto = this.tipsService.getTipsDailyReport(date, PageRequest.of(page.get(), 5));
			}
			
			log.debug("Get following Reports: {}", Arrays.toString(dto.getReports().toArray()).replaceAll(BicompConstants.LOG_CRLF, ""));
			
			return ResponseEntity.ok()
					.body(Response.<PageableTipsReportDto>builder()
						.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
						.response(dto)
						.message("Reports retrieved successfully")
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
	
	@GetMapping("/reports/{id}")
	@BicompLogger
	public ResponseEntity<Response<TipsTotalDto>> getTipsReportDetail(@PathVariable("id") BigDecimal reportId) {
		
		TipsTotalDto t = this.tipsService.getTipsReportDetail(reportId);
		log.debug("Get following totals: [{}]", t.toString().replaceAll(BicompConstants.LOG_CRLF, ""));
		
		return ResponseEntity.ok()
				.body(Response.<TipsTotalDto>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(t)
					.message("TIPS totals retrieved successfully")
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}
	
	@GetMapping("/payments")
	@BicompLogger
	public ResponseEntity<Response<PageableTipsEntryDto>> getPayments(@RequestParam(name = "side") Character side, @RequestParam(name = "status") String status, 
			@RequestParam(name = "amountFrom", required = false) BigDecimal amountFrom, @RequestParam(name = "amountTo", required = false) BigDecimal amountTo, 
			@RequestParam(name = "settlementDateFrom", required = false) String settlementDateFrom, @RequestParam(name = "settlementDateTo", required = false) String settlementDateTo, 
			@RequestParam(name = "page") int page, @RequestParam(name = "size") int size) throws ResourceNotFoundException, BicompException {
		
		try {
			PageableTipsEntryDto p = this.tipsService.getPayments(side, status, amountFrom, amountTo, settlementDateFrom, settlementDateTo, PageRequest.of(page, size));
			log.debug("Get following PMNT: [{}]", p.toString().replaceAll(BicompConstants.LOG_CRLF, ""));
			
			return ResponseEntity.ok()
					.body(Response.<PageableTipsEntryDto>builder()
						.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
						.response(p)
						.message("Payments retrieved successfully")
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
	public ResponseEntity<Response<String>> tipsExtemporaryExtractor(@RequestParam(name = "date") String date) throws BicompException {
		
		try {
			Date dataRegolamento = DateUtils.dateUtils().stringToDate(date);
			
			List<TipsCamt053BankAccountStatementEntry> list = this.tipsService.extractTipsPayment(dataRegolamento);
			
			String fileName = this.tipsService.createTipsPaymentFile(dataRegolamento, list);
			
			return ResponseEntity.ok()
					.body(Response.<String>builder()
						.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
						.response(fileName)
						.message(new StringBuilder("Successfully created TIPS file: [").append(fileName).append("]").toString())
						.status(HttpStatus.OK.name())
						.statusCode(HttpStatus.OK.value())
						.build()
			);
		}
		catch(BicompException | ParseException e) {
			throw new BicompException(e);
		}
	}
	
}
