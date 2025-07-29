package it.popso.bicomp.controller;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.popso.bicomp.aspect.BicompLogger;
import it.popso.bicomp.dto.CNDetailDto;
import it.popso.bicomp.dto.CNDto;
import it.popso.bicomp.dto.Response;
import it.popso.bicomp.exception.BicompException;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.service.CompensazioneNazionaleService;
import it.popso.bicomp.utils.BicompConstants;
import it.popso.bicomp.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("squid:S1452")

@Slf4j

@RestController
@RequestMapping("/api/v1/compensazione-nazionale")
@RequiredArgsConstructor
public class CompensazioneNazionaleController {
	
	private final CompensazioneNazionaleService compensazioneNazionaleService;
	
	
	@GetMapping("/cycles/last")
	@BicompLogger
	public ResponseEntity<Response<List<CNDto>>> getLastCompensazioneNazionaleSettlement(@RequestParam(name = "date") String date) throws ResourceNotFoundException, BicompException {
		
		try {
			List<CNDto> dtos = this.compensazioneNazionaleService.getCurrentDateCompensazioneNazionaleLastSettlement(date);
			log.debug("Last CN Settlement is: {}", Arrays.toString(dtos.toArray()).replaceAll(BicompConstants.LOG_CRLF, ""));
			
			return ResponseEntity.ok()
					.body(Response.<List<CNDto>>builder()
						.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
						.response(dtos)
						.message("Last cycle retrieved successfully")
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
	
	@GetMapping("/cycles")
	@BicompLogger
	public ResponseEntity<Response<?>> getCompensazioneNazionaleSettlementOrDetail(@RequestParam(name = "date") String date, @RequestParam(name = "cycle") Optional<String> cycle) throws ResourceNotFoundException, BicompException {
		
		try {
			if(cycle.isPresent()) {
				List<CNDetailDto> dtos = this.compensazioneNazionaleService.getCurrentDateCompensazioneNazionaleDettaglioSettlement(cycle.get(), date);
				log.debug("Get following cycle detail: {}", Arrays.toString(dtos.toArray()).replaceAll(BicompConstants.LOG_CRLF, ""));
				
				return ResponseEntity.ok()
						.body(Response.<List<CNDetailDto>>builder()
							.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
							.response(dtos)
							.message("Cycle detail retrieved successfully")
							.isSuccess(true)
							.status(HttpStatus.OK.name())
							.statusCode(HttpStatus.OK.value())
							.build()
				);
			}
			else {
				List<CNDto> dtos = this.compensazioneNazionaleService.getCurrentDateCompensazioneNazionaleSettlement(date);
				log.debug("Get following CN Settlement: {}", Arrays.toString(dtos.toArray()).replaceAll(BicompConstants.LOG_CRLF, ""));
				
				return ResponseEntity.ok()
						.body(Response.<List<CNDto>>builder()
							.timeStamp(DateUtils.dateUtils().getCurrentTimestamp())
							.response(dtos)
							.message("CN Settlement retrieved successfully")
							.isSuccess(true)
							.status(HttpStatus.OK.name())
							.statusCode(HttpStatus.OK.value())
							.build()
				);
			}
		}
		catch(ParseException e) {
			throw new BicompException(e);
		}
	}
	
}
