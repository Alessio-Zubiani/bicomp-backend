package it.popso.bicomp.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.joutvhu.fixedwidth.parser.FixedParser;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import it.popso.bicomp.exception.FileManagerException;
import it.popso.bicomp.flat.PsrFileHeader;
import it.popso.bicomp.flat.PsrParticipantBody;
import it.popso.bicomp.flat.PsrParticipantHeader;
import it.popso.bicomp.flat.PsrSettlementBicHeader;

public class FileManager {
	
	public static FileManager fileManager() {
		return new FileManager();
	}
	
	
	public String getPsrString(InputStream inputStream) throws FileManagerException {
		
		List<String> list = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
		if(list.size() != 1) {
			throw new FileManagerException(new StringBuilder().append("File not valid. It contains [").append(list.size()).append("] lines").toString());
		}
		
		return list.get(0);
	}
	
	public PsrFileHeader parseHpsr(String psr) throws FileManagerException {
		
		int numHpsr = StringUtils.countMatches(psr, BicompConstants.HPSR);
		if(numHpsr != 1) {
			throw new FileManagerException(new StringBuilder().append("File not valid. Tag HPSR is present [").append(numHpsr).append("] times").toString());
		}
		
		int numTpsr = StringUtils.countMatches(psr, BicompConstants.TPSR);
		if(numTpsr != 1) {
			throw new FileManagerException(new StringBuilder().append("File not valid. Tag TPSR is present [").append(numTpsr).append("] times").toString());
		}
		
		return FixedParser.parser().parse(PsrFileHeader.class, psr);
	}
	
	public PsrSettlementBicHeader parsePsbh(String psr, int offset) throws FileManagerException {
		
		int numPsbh = StringUtils.countMatches(psr, BicompConstants.PSBH);
		if(numPsbh != 1) {
			throw new FileManagerException(new StringBuilder().append("File not valid. Tag PSBH is present [").append(numPsbh).append("] times").toString());
		}
		
		int numPsbt = StringUtils.countMatches(psr, BicompConstants.PSBT);
		if(numPsbt != 1) {
			throw new FileManagerException(new StringBuilder().append("File not valid. Tag PSBT is present [").append(numPsbt).append("] times").toString());
		}
		
		return FixedParser.parser().parse(PsrSettlementBicHeader.class, psr.substring(offset));
	}
	
	public Object[] parsePdph(String psr, int offset) throws FileManagerException {
		
		int numPdph = StringUtils.countMatches(psr, BicompConstants.PDPH);
		if(numPdph < 1) {
			throw new FileManagerException(new StringBuilder().append("File not valid. Tag PDPH is present [").append(numPdph).append("] times").toString());
		}
		
		int numPdpt = StringUtils.countMatches(psr, BicompConstants.PDPT);
		if(numPdpt < 1) {
			throw new FileManagerException(new StringBuilder().append("File not valid. Tag PDPT is present [").append(numPdpt).append("] times").toString());
		}
		
		List<PsrParticipantHeader> listHeader = new ArrayList<>();
		List<PsrParticipantBody> listBody = new ArrayList<>();		
		while(numPdph > 0) {
			PsrParticipantHeader psrParticipantHeader = FixedParser.parser().parse(PsrParticipantHeader.class, psr.substring(offset, offset + BicompConstants.PDPH_LENGTH));
			listHeader.add(psrParticipantHeader);
			
			int numPdpb = StringUtils.countMatches(psr.substring(offset, psr.indexOf(BicompConstants.PDPT, offset)), BicompConstants.PDPB);
			offset += BicompConstants.PDPH_LENGTH;
			while(numPdpb > 0) {
				PsrParticipantBody psrParticipantBody = FixedParser.parser().parse(PsrParticipantBody.class, psr.substring(offset, offset + BicompConstants.PDPB_LENGTH));
				listBody.add(psrParticipantBody);
				
				offset += BicompConstants.PDPB_LENGTH;
				
				numPdpb--;
			}
			
			offset += BicompConstants.PDPT_LENGTH;
			numPdph--;
		}
		
		// Array di oggetti che contiene la lista di PDPH e di eventuali PDPB
		return new Object[] { listHeader, listBody };
	}
	
}
