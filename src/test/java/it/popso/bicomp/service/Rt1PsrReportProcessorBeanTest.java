package it.popso.bicomp.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.text.ParseException;
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
import it.popso.bicomp.exception.FileManagerException;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class Rt1PsrReportProcessorBeanTest {
	
	@Mock
	private Rt1Service rt1Service;
	
	@Mock
	private TimerService timerService;
	
	@Mock
	private NotificationService notificationService;
	
	private Rt1PsrReportProcessorBean bean;
	
	
	@BeforeEach
    public void setup() {
		this.bean = new Rt1PsrReportProcessorBean(this.rt1Service, this.notificationService, this.timerService);
	}
	
	@Test
	@Order(1)
	void testExecuteListEmpty() throws IOException, FileManagerException, DataIntegrityViolationException, BicompException, ParseException  {
		
		List<Item> list = new LinkedList<>(Arrays.asList());
		Mockito.when(this.rt1Service.getPsrReport()).thenReturn(list);
		Mockito.doNothing().when(this.notificationService).createNotification(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		
		this.bean.execute();
		
		verify(this.rt1Service, times(1)).getPsrReport();
		verify(this.rt1Service, times(0)).processPreSettlementReport(Mockito.anyList());
		verify(this.rt1Service, times(0)).moveToBackupFolder(Mockito.anyList());
		verify(this.notificationService, times(0)).createNotification(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(2)
	void testExecute() throws IOException, FileManagerException, DataIntegrityViolationException, BicompException, ParseException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22_multiple_payments.P")));
		Mockito.when(this.rt1Service.getPsrReport()).thenReturn(list);
		Mockito.doNothing().when(this.rt1Service).processPreSettlementReport(Mockito.anyList());
		Mockito.doNothing().when(this.rt1Service).moveToBackupFolder(Mockito.anyList());
		Mockito.doNothing().when(this.notificationService).createNotification(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		
		this.bean.execute();
		
		verify(this.rt1Service, times(1)).getPsrReport();
		verify(this.rt1Service, times(1)).processPreSettlementReport(Mockito.anyList());
		verify(this.rt1Service, times(1)).moveToBackupFolder(Mockito.anyList());
		verify(this.notificationService, times(1)).createNotification(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(3)
	void testExecuteBicompException() throws IOException, FileManagerException, DataIntegrityViolationException, BicompException, ParseException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22_multiple_payments.P")));
		Mockito.when(this.rt1Service.getPsrReport()).thenReturn(list);
		Mockito.doThrow(new BicompException("BicompException error")).when(this.rt1Service).processPreSettlementReport(Mockito.anyList());
		Mockito.doNothing().when(this.notificationService).createNotification(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		
		this.bean.execute();
		
		verify(this.rt1Service, times(1)).getPsrReport();
		verify(this.rt1Service, times(1)).processPreSettlementReport(Mockito.anyList());
		verify(this.rt1Service, times(0)).moveToBackupFolder(Mockito.anyList());
		verify(this.notificationService, times(1)).createNotification(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(4)
	void testExecuteDataIntegrityViolationException() throws IOException, FileManagerException, DataIntegrityViolationException, BicompException, ParseException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22_multiple_payments.P")));
		Mockito.when(this.rt1Service.getPsrReport()).thenReturn(list);
		Mockito.doThrow(new DataIntegrityViolationException("DataIntegrityViolationException error")).when(this.rt1Service).processPreSettlementReport(Mockito.anyList());
		Mockito.doNothing().when(this.notificationService).createNotification(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		
		this.bean.execute();
		
		verify(this.rt1Service, times(1)).getPsrReport();
		verify(this.rt1Service, times(1)).processPreSettlementReport(Mockito.anyList());
		verify(this.rt1Service, times(0)).moveToBackupFolder(Mockito.anyList());
		verify(this.notificationService, times(1)).createNotification(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(5)
	void testExecuteParseException() throws IOException, FileManagerException, DataIntegrityViolationException, BicompException, ParseException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22_multiple_payments.P")));
		Mockito.when(this.rt1Service.getPsrReport()).thenReturn(list);
		Mockito.doThrow(new ParseException("ParseException error", 0)).when(this.rt1Service).processPreSettlementReport(Mockito.anyList());
		Mockito.doNothing().when(this.notificationService).createNotification(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		
		this.bean.execute();
		
		verify(this.rt1Service, times(1)).getPsrReport();
		verify(this.rt1Service, times(1)).processPreSettlementReport(Mockito.anyList());
		verify(this.rt1Service, times(0)).moveToBackupFolder(Mockito.anyList());
		verify(this.notificationService, times(1)).createNotification(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(6)
	void testExecuteIOException() throws IOException, FileManagerException, DataIntegrityViolationException, BicompException, ParseException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22_multiple_payments.P")));
		Mockito.when(this.rt1Service.getPsrReport()).thenReturn(list);
		Mockito.doThrow(new IOException("IOException error")).when(this.rt1Service).processPreSettlementReport(Mockito.anyList());
		Mockito.doNothing().when(this.notificationService).createNotification(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		
		this.bean.execute();
		
		verify(this.rt1Service, times(1)).getPsrReport();
		verify(this.rt1Service, times(1)).processPreSettlementReport(Mockito.anyList());
		verify(this.rt1Service, times(0)).moveToBackupFolder(Mockito.anyList());
		verify(this.notificationService, times(1)).createNotification(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(7)
	void testExecuteMoveIOException() throws IOException, FileManagerException, DataIntegrityViolationException, BicompException, ParseException {
		
		List<Item> list = new LinkedList<>(Arrays.asList(this.mockItem("src/test/resources/rt1/RT02SCIPOSOIT22_multiple_payments.P")));
		Mockito.when(this.rt1Service.getPsrReport()).thenReturn(list);
		Mockito.doNothing().when(this.rt1Service).processPreSettlementReport(Mockito.anyList());
		Mockito.doThrow(new IOException("IOException error")).when(this.rt1Service).moveToBackupFolder(Mockito.anyList());
		Mockito.doNothing().when(this.notificationService).createNotification(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
				
		this.bean.execute();
		
		verify(this.rt1Service, times(1)).getPsrReport();
		verify(this.rt1Service, times(1)).processPreSettlementReport(Mockito.anyList());
		verify(this.rt1Service, times(1)).moveToBackupFolder(Mockito.anyList());
		verify(this.notificationService, times(1)).createNotification(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	@Order(8)
	void testExecuteGetBulkReportIOException() throws IOException, FileManagerException, DataIntegrityViolationException, BicompException, ParseException {
		
		Mockito.when(this.rt1Service.getPsrReport()).thenThrow(new IOException("IOException error"));
		Mockito.doNothing().when(this.notificationService).createNotification(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
				
		this.bean.execute();
		
		verify(this.rt1Service, times(1)).getPsrReport();
		verify(this.rt1Service, times(0)).processPreSettlementReport(Mockito.anyList());
		verify(this.rt1Service, times(0)).moveToBackupFolder(Mockito.anyList());
		verify(this.notificationService, times(1)).createNotification(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	}
	
	private Item mockItem(String objectName) {
		Item item = Mockito.mock(Item.class);
		Mockito.when(item.objectName()).thenReturn(objectName);
		
		return item;
	}

}
