package it.popso.bicomp.service;

import java.text.ParseException;
import java.util.List;

import it.popso.bicomp.dto.CNDetailDto;
import it.popso.bicomp.dto.CNDto;
import it.popso.bicomp.exception.ResourceNotFoundException;

public interface CompensazioneNazionaleService {
	
	List<CNDto> getCurrentDateCompensazioneNazionaleLastSettlement(String date) throws ResourceNotFoundException, ParseException;
	
	List<CNDto> getCurrentDateCompensazioneNazionaleSettlement(String date) throws ResourceNotFoundException, ParseException;
	
	List<CNDetailDto> getCurrentDateCompensazioneNazionaleDettaglioSettlement(String ciclo, String date) throws ResourceNotFoundException, ParseException;

}
