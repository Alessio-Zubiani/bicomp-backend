package it.popso.bicomp.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.minio.messages.Item;
import it.popso.bicomp.exception.BicompException;
import jakarta.xml.bind.JAXBException;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class CgsCamt053ReportProcessorBeanTest {
	
	@Mock
	private CgsService cgsService;
	
	@Mock
	private NotificationService notificationService;
	
	@Mock
	private TimerService timerService;
	
	private CgsCamt053ReportProcessorBean bean;
	
	
	@BeforeEach
    public void setup() {
		this.bean = new CgsCamt053ReportProcessorBean(this.cgsService, this.notificationService, this.timerService);
	}
	
	@Test
	@Order(1)
	void testExecuteListEmpty() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList());
		Mockito.when(this.cgsService.getLmrReport()).thenReturn(list);
		
		this.bean.execute();
		
		verify(this.cgsService, times(1)).getLmrReport();
		verify(this.cgsService, times(0)).processLmrReport(Mockito.anyList());
		verify(this.cgsService, times(0)).moveToBackupFolder(Mockito.anyList());
	}
	
	@Test
	@Order(2)
	void testExecute() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr.B")));
		
		Mockito.when(this.cgsService.getLmrReport()).thenReturn(list);
		Mockito.doNothing().when(this.cgsService).processLmrReport(Mockito.anyList());
		Mockito.doNothing().when(this.cgsService).moveToBackupFolder(Mockito.anyList());
		
		this.bean.execute();
		
		verify(this.cgsService, times(1)).getLmrReport();
		verify(this.cgsService, times(1)).processLmrReport(Mockito.anyList());
		verify(this.cgsService, times(1)).moveToBackupFolder(Mockito.anyList());
	}
	
	@Test
	@Order(3)
	void testExecuteBicompException() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr.B")));

		Mockito.when(this.cgsService.getLmrReport()).thenReturn(list);
		Mockito.doThrow(new BicompException("BicompException error")).when(this.cgsService).processLmrReport(Mockito.anyList());
		
		this.bean.execute();
		
		verify(this.cgsService, times(1)).getLmrReport();
		verify(this.cgsService, times(1)).processLmrReport(Mockito.anyList());
		verify(this.cgsService, times(0)).moveToBackupFolder(Mockito.anyList());
	}
	
	@Test
	@Order(4)
	void testExecuteDataIntegrityViolationException() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr.B")));
		
		Mockito.when(this.cgsService.getLmrReport()).thenReturn(list);
		Mockito.doThrow(new DataIntegrityViolationException("DataIntegrityViolationException error")).when(this.cgsService).processLmrReport(Mockito.anyList());
		
		this.bean.execute();
		
		verify(this.cgsService, times(1)).getLmrReport();
		verify(this.cgsService, times(1)).processLmrReport(Mockito.anyList());
		verify(this.cgsService, times(0)).moveToBackupFolder(Mockito.anyList());
	}
	
	@Test
	@Order(5)
	void testExecuteJAXBException() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr.B")));
		
		Mockito.when(this.cgsService.getLmrReport()).thenReturn(list);
		Mockito.doThrow(new JAXBException("JAXBException error")).when(this.cgsService).processLmrReport(Mockito.anyList());
		
		this.bean.execute();
		
		verify(this.cgsService, times(1)).getLmrReport();
		verify(this.cgsService, times(1)).processLmrReport(Mockito.anyList());
		verify(this.cgsService, times(0)).moveToBackupFolder(Mockito.anyList());
	}
	
	@Test
	@Order(6)
	void testExecuteIOException() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr.B")));
		
		Mockito.when(this.cgsService.getLmrReport()).thenReturn(list);
		Mockito.doThrow(new IOException("IOException error")).when(this.cgsService).processLmrReport(Mockito.anyList());
		
		this.bean.execute();
		
		verify(this.cgsService, times(1)).getLmrReport();
		verify(this.cgsService, times(1)).processLmrReport(Mockito.anyList());
		verify(this.cgsService, times(0)).moveToBackupFolder(Mockito.anyList());
	}
	
	@Test
	@Order(7)
	void testExecuteMoveIOException() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/cgs/S204SCTPOSOIT22_processLmrReport_LtinPlcr.B")));
		
		Mockito.when(this.cgsService.getLmrReport()).thenReturn(list);
		Mockito.doThrow(new BicompException("BicompException error")).when(this.cgsService).moveToBackupFolder(Mockito.anyList());
				
		this.bean.execute();
		
		verify(this.cgsService, times(1)).getLmrReport();
		verify(this.cgsService, times(1)).processLmrReport(Mockito.anyList());
		verify(this.cgsService, times(1)).moveToBackupFolder(Mockito.anyList());
	}
	
	@Test
	@Order(8)
	void testExecuteGetLmrReportIOException() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		Mockito.when(this.cgsService.getLmrReport()).thenThrow(new BicompException("BicompException error"));
				
		this.bean.execute();
		
		verify(this.cgsService, times(1)).getLmrReport();
		verify(this.cgsService, times(0)).processLmrReport(Mockito.anyList());
		verify(this.cgsService, times(0)).moveToBackupFolder(Mockito.anyList());
	}
	
	private Item mockItem(String objectName) {
		Item item = Mockito.mock(Item.class);
		Mockito.when(item.objectName()).thenReturn(objectName);
		
		return item;
	}

}
