package it.popso.bicomp.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import it.popso.bicomp.exception.FileManagerException;
import it.popso.bicomp.model.IsoExternalCodeSet;
import it.popso.bicomp.model.IsoFile;
import it.popso.bicomp.properties.BicompConfig;
import it.popso.bicomp.repository.IsoExternalCodeSetRepository;
import it.popso.bicomp.repository.IsoFileRepository;
import it.popso.bicomp.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("findsecbugs:PATH_TRAVERSAL_IN")

@Slf4j

@Service
@Scope("prototype")
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
	
	private final BicompConfig config;
	private final IsoFileRepository isoFileRepository;
	private final IsoExternalCodeSetRepository isoExternalCodeSetRepository;
	
	
	@Override
	@Transactional(value = "bicompTransactionManager", propagation = Propagation.REQUIRES_NEW, 
		rollbackFor = { DataIntegrityViolationException.class, FileManagerException.class, IOException.class }
	)
	public void upload(MultipartFile file) throws FileManagerException, IOException {
		
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		
		if(fileName.contains("..")) {
			throw new IOException("Filename contains invalid path sequence: [".concat(fileName).concat("]"));
        }
		
		log.info("Received file: [{}]", fileName);
		
		if(fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
			this.save(file);
		}
		else if(fileName.startsWith(this.config.getCgs().getPrefix()) && fileName.endsWith(this.config.getCgs().getSuffix())) {
			this.transferToFolder(this.config.getCgs().getShare(), file);
		}
		else if(fileName.startsWith(this.config.getTips().getCamt052Prefix()) && fileName.endsWith(this.config.getTips().getCamt052Suffix())) {
			this.transferToFolder(this.config.getTips().getCamt052Share(), file);
		}
		else if(fileName.startsWith(this.config.getTips().getCamt053Prefix()) && fileName.endsWith(this.config.getTips().getCamt053Suffix())) {
			this.transferToFolder(this.config.getTips().getCamt053Share(), file);
		}
		else if(fileName.startsWith(this.config.getRt1().getBulkPrefix()) && fileName.endsWith(this.config.getRt1().getBulkSuffix())) {
			this.transferToFolder(this.config.getRt1().getShare(), file);
		}
		else if(fileName.startsWith(this.config.getRt1().getPsrPrefix()) && fileName.endsWith(this.config.getRt1().getPsrSuffix())) {
			this.transferToFolder(this.config.getRt1().getShare(), file);
		}
		else {
			throw new FileManagerException("File type not managed");
		}
	}
	
	private void save(MultipartFile file) throws IOException {
		
		log.info("Reading excel file: [{}]", file.getOriginalFilename());
		IsoFile isoFile = IsoFile.builder()
				.filaName(file.getOriginalFilename())
				.build();
		isoFile = this.isoFileRepository.save(isoFile);
		
		XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
		Sheet sheet = workbook.getSheet("AllCodeSets");
		Iterator<Row> rows = sheet.iterator();
		
		int rowNumber = 0;
		while(rows.hasNext()) {
			Row currentRow = rows.next();
			
			// skip first row
			if(rowNumber == 0) {
				rowNumber++;
				continue;
			}
			
			Iterator<Cell> cells = currentRow.iterator();
			
			int cellIndex = 0;
			
			IsoExternalCodeSet i = IsoExternalCodeSet.builder().build();
			while(cells.hasNext()) {
	        	Cell currentCell = cells.next();
	        	
	        	switch(cellIndex) {
	        		case 1:
	        			i.setCodeValue(currentCell.getStringCellValue());
	        			break;
	        			
	        		case 2:
	        			i.setCodeName(currentCell.getStringCellValue());
	        			break;
	        			
	        		case 3:
	        			i.setCodeDescription(currentCell.getStringCellValue());
	        			break;
	        			
	        		default:
	        			break;
	        	}
	        	
	        	cellIndex++;
	        }
	        
			i.setIsoFile(isoFile);
	        i = this.isoExternalCodeSetRepository.save(i);
        	log.debug("Saved ISO EXTERNAL CODE: [{}]", i.getCodeValue());
		}
		
		workbook.close();
	}
	
	private void transferToFolder(String share, MultipartFile file) throws IllegalStateException, IOException {
		
		file.transferTo(new File(share, FilenameUtils.getName(file.getOriginalFilename())));
	}

}
