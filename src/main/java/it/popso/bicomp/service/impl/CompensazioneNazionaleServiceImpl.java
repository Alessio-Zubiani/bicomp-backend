package it.popso.bicomp.service.impl;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import it.popso.bicomp.dto.CNDetailDto;
import it.popso.bicomp.dto.CNDto;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.properties.BicompConfig;
import it.popso.bicomp.service.CompensazioneNazionaleService;
import it.popso.bicomp.slt.model.Rstbproc;
import it.popso.bicomp.slt.model.Rstbstan;
import it.popso.bicomp.slt.repository.RstbprocRepository;
import it.popso.bicomp.slt.repository.RstbstanRepository;
import it.popso.bicomp.t2c.model.EurRtgsOperazioni;
import it.popso.bicomp.t2c.repository.EurRtgsOperazioniRepository;
import it.popso.bicomp.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@Scope("prototype")
@RequiredArgsConstructor
public class CompensazioneNazionaleServiceImpl implements CompensazioneNazionaleService {
	
	private final BicompConfig config;
	private final EurRtgsOperazioniRepository eurRtgsOperazioniRepository;
	private final RstbstanRepository rstbstanRepository;
	private final RstbprocRepository rstbprocRepository;
	
	
	@Override
	public List<CNDto> getCurrentDateCompensazioneNazionaleLastSettlement(String date) throws ResourceNotFoundException, ParseException {
		
		Date dataRegolamento = DateUtils.dateUtils().stringToDate(date);
		log.info("Current date is: [{}]", dataRegolamento);
    	
    	List<EurRtgsOperazioni> list = this.eurRtgsOperazioniRepository.findLastCompensazioneNazionaleSettlement(dataRegolamento, this.config.getCnCodiceFamiglia(), PageRequest.of(0, 1));
		if(list.isEmpty()) {
			throw new ResourceNotFoundException(new StringBuilder("No NEXI settlement found for date: [").append(dataRegolamento).append("]").toString());
		}
		
		return this.buildListCnDto(list);
	}
	
	@Override
	public List<CNDto> getCurrentDateCompensazioneNazionaleSettlement(String date) throws ResourceNotFoundException, ParseException {
		
		Date dataRegolamento = DateUtils.dateUtils().stringToDate(date);
		log.info("Current date is: [{}]", dataRegolamento);
		
		List<EurRtgsOperazioni> list = this.eurRtgsOperazioniRepository.findCompensazioneNazionaleSettlement(dataRegolamento, this.config.getCnCodiceFamiglia());
		log.info("Estratti [{}] camt.054 Compensazione Nazionale", list.size());
		
		if(list.isEmpty()) {
			throw new ResourceNotFoundException(new StringBuilder("No CN settlement found for date: [").append(dataRegolamento).append("]").toString());
		}
		
		return this.buildListCnDto(list);
	}

	@Override
	public List<CNDetailDto> getCurrentDateCompensazioneNazionaleDettaglioSettlement(String ciclo, String date) throws ResourceNotFoundException, ParseException {
		
		Date dataRegolamento = DateUtils.dateUtils().stringToDate(date);
		log.info("Current date is: [{}]", dataRegolamento);
		
		List<Rstbstan> listRstbstan = this.rstbstanRepository.findByStanDataAndStanMessaggioAndStanNumeroCicli(dataRegolamento, "340", new BigDecimal(ciclo));
		if(listRstbstan.isEmpty()) {
			log.error("No details found for date [{}] and cycle [{}]", dataRegolamento, ciclo);
			throw new ResourceNotFoundException(new StringBuilder("No CN details found for date [").append(dataRegolamento).append("] and cycle [").append(ciclo).append("]").toString()); 
		}
		log.info("Estratti [{}] RNI 340 Compensazione Nazionale", listRstbstan.size());
		
		List<CNDetailDto> listCNDetailDto = new ArrayList<>();
		listRstbstan.forEach(r -> {
			
			Optional<Rstbproc> o = this.rstbprocRepository.findByProcTipoVoce(r.getId().getStanTipoVoce());
			if(!o.isPresent()) {
				log.error("No procedure found for tipoVoce: [{}]", r.getId().getStanTipoVoce());
				throw new ResourceNotFoundException(new StringBuilder("No procedure found for tipoVoce [").append(r.getId().getStanTipoVoce()).append("]").toString()); 
			}
			
			listCNDetailDto.add(CNDetailDto.builder()
					.stanCycle(ciclo)
					.stanTipoMessaggio(r.getId().getStanMessaggio())
					.stanTipoVoce(r.getId().getStanTipoVoce())
					.stanDescrizioneVoce(o.get().getProcDescrizione().trim())
					.stanSettlementDate(DateUtils.dateUtils().dateToLocalDate(r.getId().getStanData()))
					.stanSettlementDateTime(DateUtils.dateUtils().dateToLocalDateTime(r.getStanTimestamp()))
					.stanSettledCreditAmount(r.getStanImpoDefAv().divide(new BigDecimal(100)))
					.stanSettledDebitAmount(r.getStanImpoDefDa().divide(new BigDecimal(100)))
					.currency("EUR")
					.build()
			);
		});
		
		return listCNDetailDto;
	}
	
	private List<CNDto> buildListCnDto(List<EurRtgsOperazioni> list) {
		
		List<CNDto> listCNDto = new ArrayList<>();
		list.forEach(e -> {
			String cycle = "3";
			log.info("Settlement cycle: [{}]", cycle);
			
			listCNDto.add(CNDto.builder()
					.cycleId(e.getProgressivoOperazione())
					.cycleNumber(cycle)
					.cycleDateTime(DateUtils.dateUtils().dateToLocalDateTime(e.getTimbroRegolamento()))
					.cycleAmount(e.getImportoOperazione())
					.currency(e.getCodiceDivisa())
					.build()
			);
		});
		
		return listCNDto;
	}

	@Override
	public int hashCode() {
		
		final int prime = 2;
	    int result = 1;
	    result = prime * result + ((this.config == null) ? 0 : this.config.hashCode());
	    result = prime * result + ((this.eurRtgsOperazioniRepository == null) ? 0 : this.eurRtgsOperazioniRepository.hashCode());
	    result = prime * result + ((this.rstbprocRepository == null) ? 0 : this.rstbprocRepository.hashCode());
	    result = prime * result + ((this.rstbstanRepository == null) ? 0 : this.rstbstanRepository.hashCode());
	    
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (obj == this)
	        return true;
	    if (!(obj instanceof CompensazioneNazionaleServiceImpl))
	        return false;
	    
	    CompensazioneNazionaleServiceImpl other = (CompensazioneNazionaleServiceImpl) obj;
	    boolean c = (this.config == null && other.config == null) || (this.config != null && this.config.equals(other.config));
	    boolean eurRtgs = (this.eurRtgsOperazioniRepository == null && other.eurRtgsOperazioniRepository == null) 
	    		|| (this.eurRtgsOperazioniRepository != null && this.eurRtgsOperazioniRepository.equals(other.eurRtgsOperazioniRepository));
	    boolean proc = (this.rstbprocRepository == null && other.rstbprocRepository == null) 
	    		|| (this.rstbprocRepository != null && this.rstbprocRepository.equals(other.rstbprocRepository));
	    boolean stan = (this.rstbstanRepository == null && other.rstbstanRepository == null)
	    		|| (this.rstbstanRepository != null && this.rstbstanRepository.equals(other.rstbstanRepository));
	    return c && eurRtgs && proc && stan;
	}
	
}
