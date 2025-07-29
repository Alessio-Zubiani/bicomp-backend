package it.popso.bicomp.controller;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.popso.bicomp.aspect.BicompLogger;
import it.popso.bicomp.dto.PageableRt1EntryDto;
import it.popso.bicomp.dto.Response;
import it.popso.bicomp.dto.Rt1LacDto;
import it.popso.bicomp.dto.Rt1TotalDto;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.service.Rt1Service;
import it.popso.bicomp.utils.BicompConstants;
import it.popso.bicomp.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@RestController
@RequestMapping("/api/v1/rt1")
@RequiredArgsConstructor
public class Rt1Controller {
	
	private final Rt1Service rt1Service;
	

	@GetMapping("/lacs/last")
	@BicompLogger
	public ResponseEntity<Response<List<Rt1LacDto>>> getLastRt1Lac(@RequestParam(name = "date") String date) throws ResourceNotFoundException, ParseException {
		
		try {
			List<Rt1LacDto> listRt1BalanceDto = this.rt1Service.getRt1LastBalanceByDate(date);
			log.info("Get following RT1 balance: {}", Arrays.toString(listRt1BalanceDto.toArray()).replaceAll(BicompConstants.LOG_CRLF, ""));
			
			return ResponseEntity.ok()
					.body(Response.<List<Rt1LacDto>>builder()
						.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
						.response(listRt1BalanceDto)
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
	public ResponseEntity<Response<List<Rt1LacDto>>> getRt1DailyLacs(@RequestParam(name = "date") String date) throws ResourceNotFoundException, ParseException {
		
		try {
			List<Rt1LacDto> dtos = this.rt1Service.getRt1DailyLac(date);
			log.info("Get following LACs: {}", Arrays.toString(dtos.toArray()).replaceAll(BicompConstants.LOG_CRLF, ""));
			
			return ResponseEntity.ok()
					.body(Response.<List<Rt1LacDto>>builder()
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
	public ResponseEntity<Response<Rt1TotalDto>> getRt1LacDetail(@PathVariable("id") BigDecimal lacId) {
		
		Rt1TotalDto r = this.rt1Service.getRt1LacDetail(lacId);
		log.info("Get following totals: [{}]", r.toString().replaceAll(BicompConstants.LOG_CRLF, ""));
		
		return ResponseEntity.ok()
				.body(Response.<Rt1TotalDto>builder()
					.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
					.response(r)
					.message("RT1 totals retrieved successfully")
					.isSuccess(true)
					.status(HttpStatus.OK.name())
					.statusCode(HttpStatus.OK.value())
					.build()
		);
	}
	
	@GetMapping("/payments")
	@BicompLogger
	public ResponseEntity<Response<PageableRt1EntryDto>> getDebitCreditPayment(@RequestParam(name = "side") Character side, @RequestParam(name = "status") String status, 
			@RequestParam(name = "amountFrom", required = false) BigDecimal amountFrom, @RequestParam(name = "amountTo", required = false) BigDecimal amountTo, 
			@RequestParam(name = "settlementDateFrom", required = false) String settlementDateFrom, @RequestParam(name = "settlementDateTo", required = false) String settlementDateTo, 
			@RequestParam(name = "lac", required = false) String lac, @RequestParam(name = "page") int page, @RequestParam(name = "size") int size) throws ResourceNotFoundException, BicompException {
		
		try {
			PageableRt1EntryDto p = this.rt1Service.getPayments(side, status, amountFrom, amountTo, settlementDateFrom, settlementDateTo, lac, PageRequest.of(page, size));
			log.info("Get following Payments: [{}]", p.toString().replaceAll(BicompConstants.LOG_CRLF, ""));
			
			return ResponseEntity.ok()
					.body(Response.<PageableRt1EntryDto>builder()
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
	
	@GetMapping("/liquidity-transfers")
	@BicompLogger
	public ResponseEntity<Response<PageableRt1EntryDto>> getLiquidityTransfers(@RequestParam(name = "side") Character side, @RequestParam(name = "status") String status, 
			@RequestParam(name = "amountFrom", required = false) BigDecimal amountFrom, @RequestParam(name = "amountTo", required = false) BigDecimal amountTo, 
			@RequestParam(name = "settlementDateTimeFrom", required = false) String settlementDateFrom, @RequestParam(name = "settlementDateTimeTo", required = false) String settlementDateTo, 
			@RequestParam(name = "lac", required = false) String lac, @RequestParam(name = "page") int page, @RequestParam(name = "size") int size) throws ResourceNotFoundException, BicompException {
		
		try {
			PageableRt1EntryDto p = this.rt1Service.getLiquidityTransfers(side, amountFrom, amountTo, settlementDateFrom, settlementDateTo, lac, status, PageRequest.of(page, size));
			log.info("Filtered Liquidity Transfers: [{}]", p.toString().replaceAll(BicompConstants.LOG_CRLF, ""));
			
			return ResponseEntity.ok()
					.body(Response.<PageableRt1EntryDto>builder()
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
	
}
