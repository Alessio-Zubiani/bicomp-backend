package it.popso.bicomp.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import jakarta.xml.bind.JAXBException;

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


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class TipsCamt053ReportProcessorBeanTest {
	
	@Mock
	private TipsService tipsService;
	
	@Mock
	private TimerService timerService;
	
	@Mock
	private NotificationService notificationService;
	
	private TipsCamt053ReportProcessorBean bean;
	
	
	@BeforeEach
    public void setup() {
		this.bean = new TipsCamt053ReportProcessorBean(this.tipsService, this.notificationService, this.timerService);
	}
	
	@Test
	@Order(1)
	void testExecuteListEmpty() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList());
		Mockito.when(this.tipsService.getCamt053Report()).thenReturn(list);
		
		this.bean.execute();
		
		verify(this.tipsService, times(1)).getCamt053Report();
		verify(this.tipsService, times(0)).processCamt053Report(Mockito.anyList());
		verify(this.tipsService, times(0)).moveToBackupFolder053(Mockito.anyList());
	}
	
	@Test
	@Order(2)
	void testExecute() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/tips/camt.053.xml")));
		Mockito.when(this.tipsService.getCamt053Report()).thenReturn(list);
		Mockito.doNothing().when(this.tipsService).processCamt053Report(Mockito.anyList());
		Mockito.doNothing().when(this.tipsService).moveToBackupFolder053(Mockito.anyList());
		
		this.bean.execute();
		
		verify(this.tipsService, times(1)).getCamt053Report();
		verify(this.tipsService, times(1)).processCamt053Report(Mockito.anyList());
		verify(this.tipsService, times(1)).moveToBackupFolder053(Mockito.anyList());
	}
	
	@Test
	@Order(3)
	void testExecuteBicompException() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/tips/camt.053.xml")));
		Mockito.when(this.tipsService.getCamt053Report()).thenReturn(list);
		Mockito.doThrow(new BicompException("BicompException error")).when(this.tipsService).processCamt053Report(Mockito.anyList());
		
		this.bean.execute();
		
		verify(this.tipsService, times(1)).getCamt053Report();
		verify(this.tipsService, times(1)).processCamt053Report(Mockito.anyList());
		verify(this.tipsService, times(0)).moveToBackupFolder053(Mockito.anyList());
	}
	
	@Test
	@Order(4)
	void testExecuteDataIntegrityViolationException() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/tips/camt.053.xml")));
		Mockito.when(this.tipsService.getCamt053Report()).thenReturn(list);
		Mockito.doThrow(new DataIntegrityViolationException("DataIntegrityViolationException error")).when(this.tipsService).processCamt053Report(Mockito.anyList());
		
		this.bean.execute();
		
		verify(this.tipsService, times(1)).getCamt053Report();
		verify(this.tipsService, times(1)).processCamt053Report(Mockito.anyList());
		verify(this.tipsService, times(0)).moveToBackupFolder053(Mockito.anyList());
	}
	
	@Test
	@Order(5)
	void testExecuteJAXBException() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/tips/camt.053.xml")));
		Mockito.when(this.tipsService.getCamt053Report()).thenReturn(list);
		Mockito.doThrow(new JAXBException("JAXBException error")).when(this.tipsService).processCamt053Report(Mockito.anyList());
		
		this.bean.execute();
		
		verify(this.tipsService, times(1)).getCamt053Report();
		verify(this.tipsService, times(1)).processCamt053Report(Mockito.anyList());
		verify(this.tipsService, times(0)).moveToBackupFolder053(Mockito.anyList());
	}
	
	@Test
	@Order(6)
	void testExecuteIOException() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/tips/camt.053.xml")));
		Mockito.when(this.tipsService.getCamt053Report()).thenReturn(list);
		Mockito.doThrow(new IOException("IOException error")).when(this.tipsService).processCamt053Report(Mockito.anyList());
		
		this.bean.execute();
		
		verify(this.tipsService, times(1)).getCamt053Report();
		verify(this.tipsService, times(1)).processCamt053Report(Mockito.anyList());
		verify(this.tipsService, times(0)).moveToBackupFolder053(Mockito.anyList());
	}
	
	@Test
	@Order(7)
	void testExecuteMoveIOException() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/tips/camt.053.xml")));
		Mockito.when(this.tipsService.getCamt053Report()).thenReturn(list);
		Mockito.doThrow(new IOException("IOException error")).when(this.tipsService).moveToBackupFolder053(Mockito.anyList());
				
		this.bean.execute();
		
		verify(this.tipsService, times(1)).getCamt053Report();
		verify(this.tipsService, times(1)).processCamt053Report(Mockito.anyList());
		verify(this.tipsService, times(1)).moveToBackupFolder053(Mockito.anyList());
	}
	
	@Test
	@Order(8)
	void testExecuteGetCamt053ReportIOException() throws IOException, DataIntegrityViolationException, BicompException, JAXBException {
		
		Mockito.when(this.tipsService.getCamt053Report()).thenThrow(new IOException("IOException error"));
				
		this.bean.execute();
		
		verify(this.tipsService, times(1)).getCamt053Report();
		verify(this.tipsService, times(0)).processCamt053Report(Mockito.anyList());
		verify(this.tipsService, times(0)).moveToBackupFolder053(Mockito.anyList());
	}
	
	private Item mockItem(String objectName) {
		Item item = Mockito.mock(Item.class);
		Mockito.when(item.objectName()).thenReturn(objectName);
		
		return item;
	}

}
