package it.popso.bicomp.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import it.popso.bicomp.exception.BicompException;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class DateUtilsTest {
	
	private DateUtils dateUtils;
	private DatatypeFactory datatypeFactory;
	private MockedStatic<DatatypeFactory> mockDatatypeFactory;
	
	private XMLGregorianCalendar xCal;
	private Date date;
	private GregorianCalendar cal;
	private String stringDate;
	
	
	@BeforeEach
	public void setup(TestInfo testInfo) throws IOException, DatatypeConfigurationException {
		
		this.date = new Date();
		this.stringDate = new SimpleDateFormat("yyyyMMdd").format(this.date);
		this.cal = new GregorianCalendar();
	    this.cal.setTime(this.date);
	    
	    if(testInfo.getDisplayName().equals("testGregorianCalendarToDateWithoutTime")) {
	    	this.cal.set(Calendar.HOUR_OF_DAY, 0);
			this.cal.set(Calendar.MINUTE, 0);
			this.cal.set(Calendar.SECOND, 0);
			this.cal.set(Calendar.MILLISECOND, 0);
	    }
	    else if(testInfo.getDisplayName().equals("testGregorianCalendarToDateHour")) {
	    	this.cal.set(Calendar.HOUR_OF_DAY, 10);
	    	this.cal.set(Calendar.MINUTE, 0);
	    	this.cal.set(Calendar.SECOND, 0);
	    	this.cal.set(Calendar.MILLISECOND, 0);
	    }
	    else if(testInfo.getDisplayName().equals("testGregorianCalendarToDateMinute")) {
	    	this.cal.set(Calendar.HOUR_OF_DAY, 0);
	    	this.cal.set(Calendar.MINUTE, 10);
			this.cal.set(Calendar.SECOND, 0);
			this.cal.set(Calendar.MILLISECOND, 0);
	    }
	    else if(testInfo.getDisplayName().equals("testGregorianCalendarToDateSecond")) {
	    	this.cal.set(Calendar.HOUR_OF_DAY, 0);
	    	this.cal.set(Calendar.MINUTE, 0);
	    	this.cal.set(Calendar.SECOND, 10);
	    	this.cal.set(Calendar.MILLISECOND, 0);
	    }
	    else if(testInfo.getDisplayName().equals("testGregorianCalendarToDateMilliSecond")) {
	    	this.cal.set(Calendar.HOUR_OF_DAY, 0);
	    	this.cal.set(Calendar.MINUTE, 0);
	    	this.cal.set(Calendar.SECOND, 0);
	    	this.cal.set(Calendar.MILLISECOND, 10);
	    }
	    else if(testInfo.getDisplayName().equals("testDateToXmlGregorianWithoutTime")) {
	    	this.cal.set(Calendar.HOUR_OF_DAY, 16);
	    	this.cal.set(Calendar.MINUTE, 15);
	    	this.cal.set(Calendar.SECOND, 14);
	    	this.cal.set(Calendar.MILLISECOND, 0);
	    	this.date = this.cal.getTime();
	    }
	    else if(testInfo.getDisplayName().equals("testDateToXmlGregorianString")) {
	    	this.cal.set(Calendar.HOUR_OF_DAY, 0);
	    	this.cal.set(Calendar.MINUTE, 0);
	    	this.cal.set(Calendar.SECOND, 0);
	    	this.cal.set(Calendar.MILLISECOND, 0);
	    	this.date = this.cal.getTime();
	    }
	    
	    this.xCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(this.cal);
	    
		this.dateUtils = DateUtils.dateUtils();
		this.datatypeFactory = Mockito.mock(DatatypeFactory.class);
		this.mockDatatypeFactory = Mockito.mockStatic(DatatypeFactory.class);
	}
	
	@Test
	@Order(1)
	void testGregorianCalendarToTimestamp() throws DatatypeConfigurationException {
		
		Timestamp timestamp = this.dateUtils.xmlGregorianCalendarToTimestamp(this.xCal);
		GregorianCalendar test = new GregorianCalendar();
		test.setTime(new Date(timestamp.getTime()));
		
		assertThat(test).isEqualByComparingTo(this.cal);
	}
	
	@Test
	@Order(2)
	@DisplayName("testGregorianCalendarToDateWithTime")
	void testGregorianCalendarToDateWithTime() throws DatatypeConfigurationException {
		
		Date date = this.dateUtils.gregorianCalendarToDate(this.xCal);
		GregorianCalendar test = new GregorianCalendar();
		test.setTime(date);
		
		assertThat(test).isEqualByComparingTo(this.cal);
	}
	
	@Test
	@Order(3)
	@DisplayName("testGregorianCalendarToDateWithoutTime")
	void testGregorianCalendarToDateWithoutTime() throws DatatypeConfigurationException {
		
		Date date = this.dateUtils.gregorianCalendarToDate(this.xCal);
		GregorianCalendar test = new GregorianCalendar();
		test.setTime(date);
		
		assertThat(test).isEqualByComparingTo(this.cal);
	}
	
	@Test
	@Order(4)
	@DisplayName("testGregorianCalendarToDateHour")
	void testGregorianCalendarToDateHour() throws DatatypeConfigurationException {
		
		Date date = this.dateUtils.gregorianCalendarToDate(this.xCal);
		GregorianCalendar test = new GregorianCalendar();
		test.setTime(date);
		
		assertThat(test).isEqualByComparingTo(this.cal);
	}
	
	@Test
	@Order(5)
	@DisplayName("testGregorianCalendarToDateMinute")
	void testGregorianCalendarToDateMinute() throws DatatypeConfigurationException {
		
		Date date = this.dateUtils.gregorianCalendarToDate(this.xCal);
		GregorianCalendar test = new GregorianCalendar();
		test.setTime(date);
		
		assertThat(test).isEqualByComparingTo(this.cal);
	}
	
	@Test
	@Order(6)
	@DisplayName("testGregorianCalendarToDateSecond")
	void testGregorianCalendarToDateSecond() throws DatatypeConfigurationException {
		
		Date date = this.dateUtils.gregorianCalendarToDate(this.xCal);
		GregorianCalendar test = new GregorianCalendar();
		test.setTime(date);
		
		assertThat(test).isEqualByComparingTo(this.cal);
	}
	
	@Test
	@Order(7)
	@DisplayName("testGregorianCalendarToDateMilliSecond")
	void testGregorianCalendarToDateMilliSecond() throws DatatypeConfigurationException {
		
		Date date = this.dateUtils.gregorianCalendarToDate(this.xCal);
		GregorianCalendar test = new GregorianCalendar();
		test.setTime(date);
		
		assertThat(test).isEqualByComparingTo(this.cal);
	}
	
	@Test
	@Order(8)
	void testGregorianCalendarToDateNull() {
		
		assertThrows(BicompException.class, () -> {
			this.dateUtils.gregorianCalendarToDate(null);
		});
	}
	
	@Test
	@Order(9)
	void testGetWorkingDayIsWorkingDay() {
		
		Date date = java.sql.Date.valueOf(LocalDate.of(2023, 6, 1));
		Date result = this.dateUtils.getWorkingDay(date);
		
		assertThat(result).isEqualTo(date);
	}
	
	@Test
	@Order(10)
	void testGetWorkingDayIsSaturday() {
		
		LocalDate localDate = LocalDate.of(2023, 6, 3);
		Date date = java.sql.Date.valueOf(localDate);
		Date result = this.dateUtils.getWorkingDay(date);
		
		Date test = java.sql.Date.valueOf(LocalDate.of(2023, 6, 5));
		
		assertThat(result.compareTo(date)).isPositive();
		assertThat(result).isEqualTo(test);
	}
	
	@Test
	@Order(11)
	void testGetWorkingDayIsSunday() {
		
		LocalDate localDate = LocalDate.of(2023, 6, 4);
		Date date = java.sql.Date.valueOf(localDate);
		Date result = this.dateUtils.getWorkingDay(date);
		
		Date test = java.sql.Date.valueOf(LocalDate.of(2023, 6, 5));
		
		assertThat(result.compareTo(date)).isPositive();
		assertThat(result).isEqualTo(test);
	}
	
	@Test
	@Order(12)
	void testGetWorkingDayIsHoliday() {
		
		LocalDate localDate = LocalDate.of(2023, 5, 1);
		Date date = java.sql.Date.valueOf(localDate);
		Date result = this.dateUtils.getWorkingDay(date);
		
		Date test = java.sql.Date.valueOf(LocalDate.of(2023, 5, 2));
		
		assertThat(result.compareTo(date)).isPositive();
		assertThat(result).isEqualTo(test);
	}
	
	@Test
	@Order(13)
	void testGetWorkingDayIsHolidayAndIsTarget() {
		
		LocalDate localDate = LocalDate.of(2023, 6, 2);
		Date date = java.sql.Date.valueOf(localDate);
		Date result = this.dateUtils.getWorkingDay(date);
		
		assertThat(result).isEqualTo(date);
	}
	
	@Test
	@Order(14)
	void testGetWorkingDayIsHolyFriday() {
		
		LocalDate localDate = LocalDate.of(2023, 4, 7);
		Date date = java.sql.Date.valueOf(localDate);
		Date result = this.dateUtils.getWorkingDay(date);
		
		Date test = java.sql.Date.valueOf(LocalDate.of(2023, 4, 11));
		
		assertThat(result.compareTo(date)).isPositive();
		assertThat(result).isEqualTo(test);
	}
	
	@Test
	@Order(15)
	void testStringToTimestamp() throws ParseException {
		
		String s = "2023-06-01T10:00";
		Timestamp result = this.dateUtils.stringToTimestamp(s);
		
		Date d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(s);
		LocalDateTime l = d.toInstant().atZone(ZoneId.of("Europe/Rome")).toLocalDateTime();
		
		Timestamp test = Timestamp.valueOf(l);
		
		assertThat(result).isEqualTo(test);
	}
	
	@Test
	@Order(16)
	void testStringToTimestampException() throws ParseException {
		
		String s = "2023-06-01A10:00";

		assertThrows(ParseException.class, () -> {
			this.dateUtils.stringToTimestamp(s);
		});
	}
	
	@Test
	@Order(17)
	void testLocalDateToDate() {
		
		LocalDate localDate = LocalDate.of(2023, 6, 1);
		Date result = this.dateUtils.localDateToDate(localDate);
		
		Date test = java.sql.Date.valueOf(localDate);
		
		assertThat(result).isEqualTo(test);
	}
	
	@Test
	@Order(18)
	void testLocalDateTimeToDate() {
		
		LocalDateTime localDateTime = LocalDateTime.now();
		Date result = this.dateUtils.localDateTimeToDate(localDateTime);
		
		Date test = Date.from(localDateTime.atZone(ZoneId.of("Europe/Rome")).toInstant());
		
		assertThat(result).isEqualTo(test);
	}
	
	@Test
	@Order(19)
	void testDateToLocalDateTime() {
		
		Date date = new Date();
		LocalDateTime result = this.dateUtils.dateToLocalDateTime(date);
		
		LocalDateTime test = date.toInstant().atZone(ZoneId.of("Europe/Rome")).toLocalDateTime();
		
		assertThat(result).isEqualTo(test);
	}
	
	@Test
	@Order(20)
	void testStringToDateTime() throws ParseException {
		
		String dateTime = "230628161005";
		Date result = this.dateUtils.stringToDateTime(dateTime);
		
		LocalDateTime test = LocalDateTime.of(2023, 6, 28, 16, 10, 05);
		LocalDateTime resultDateTime = LocalDateTime.ofInstant(result.toInstant(), ZoneId.of("Europe/Rome"));
		
		assertThat(resultDateTime).isEqualTo(test);
	}
	
	@Test
	@Order(21)
	void testStringToDateTimeParseException() throws ParseException {
		
		String dateTime = "230628abc161005";
		
		assertThrows(ParseException.class, () -> {
			this.dateUtils.stringToDateTime(dateTime);
		});
	}
	
	@Test
	@Order(22)
	void testStringToDate() throws ParseException {
		
		String date = "230628";
		Date result = this.dateUtils.stringToDate(date);
		
		LocalDate test = LocalDate.of(2023, 6, 28);
		Date resultTest = java.sql.Date.valueOf(test);
		
		assertThat(result).isEqualTo(resultTest);
	}
	
	@Test
	@Order(23)
	void testStringToDateParseException() throws ParseException {
		
		String date = "230abc628";
		
		assertThrows(ParseException.class, () -> {
			this.dateUtils.stringToDate(date);
		});
	}
	
	@Test
	@Order(24)
	void testCurrentTimestamp() throws DatatypeConfigurationException {
		
		this.mockDatatypeFactory.when(() -> DatatypeFactory.newInstance()).thenReturn(this.datatypeFactory);
		Mockito.when(this.datatypeFactory.newXMLGregorianCalendar(Mockito.any(GregorianCalendar.class)))
    		.thenReturn(this.xCal);
		
		assertThat(this.dateUtils.currentTimestamp()).isNotNull();
	}
	
	@Test
	@Order(25)
	void testCurrentTimestampException() throws DatatypeConfigurationException {
		
		this.mockDatatypeFactory.when(() -> DatatypeFactory.newInstance()).thenReturn(this.datatypeFactory);
		BDDMockito.given(this.datatypeFactory.newXMLGregorianCalendar(Mockito.any(GregorianCalendar.class)))
			.willAnswer(invocation -> {
				throw new DatatypeConfigurationException("DatatypeConfigurationException");
			});
		
		assertThrows(DatatypeConfigurationException.class, () -> {
			this.dateUtils.currentTimestamp();
		});
	}
	
	@Test
	@Order(26)
	@DisplayName("testDateToXmlGregorianWithoutTime")
	void testDateToXmlGregorianWithoutTime() throws DatatypeConfigurationException {
		
		this.mockDatatypeFactory.when(() -> DatatypeFactory.newInstance()).thenReturn(this.datatypeFactory);
	    
	    Mockito.when(this.datatypeFactory.newXMLGregorianCalendar(Mockito.any(GregorianCalendar.class)))
	    	.thenReturn(this.xCal);
		
		XMLGregorianCalendar xgc = this.dateUtils.dateToXmlGregorian(this.date);

		assertThat(xgc.getHour()).isEqualTo(this.xCal.getHour());
		assertThat(xgc.getMinute()).isEqualTo(this.xCal.getMinute());
		assertThat(xgc.getSecond()).isEqualTo(this.xCal.getSecond());
		assertThat(xgc.getMillisecond()).isEqualTo(this.xCal.getMillisecond());
		assertThat(xgc.getTimezone()).isEqualTo(DatatypeConstants.FIELD_UNDEFINED);
	}
	
	@Test
	@Order(27)
	void testDateToXmlGregorianWithTime() throws DatatypeConfigurationException {
		
		this.mockDatatypeFactory.when(() -> DatatypeFactory.newInstance()).thenReturn(this.datatypeFactory);
	    
	    Mockito.when(this.datatypeFactory.newXMLGregorianCalendar(Mockito.any(GregorianCalendar.class)))
	    	.thenReturn(this.xCal);
	    
		XMLGregorianCalendar xgc = this.dateUtils.dateToXmlGregorian(this.date);
		
		assertThat(xgc.getHour()).isEqualTo(this.xCal.getHour());
		assertThat(xgc.getMinute()).isEqualTo(this.xCal.getMinute());
		assertThat(xgc.getSecond()).isEqualTo(this.xCal.getSecond());
		assertThat(xgc.getMillisecond()).isEqualTo(this.xCal.getMillisecond());
	}
	
	@Test
	@Order(28)
	void testXmlGregorianToDate() {
		
		Date date = this.dateUtils.xmlGregorianToDate(this.xCal);
		
		assertThat(date).isNotNull().isEqualTo(this.xCal.toGregorianCalendar().getTime());
	}
	
	@Test
	@Order(29)
	void testTimestampToXmlGregorian() throws DatatypeConfigurationException {
		
		Timestamp t = new Timestamp(this.xCal.toGregorianCalendar().getTimeInMillis());
		
		this.mockDatatypeFactory.when(() -> DatatypeFactory.newInstance()).thenReturn(this.datatypeFactory);
		Mockito.when(this.datatypeFactory.newXMLGregorianCalendar(Mockito.any(GregorianCalendar.class)))
    		.thenReturn(this.xCal);
	    
		XMLGregorianCalendar xgc = this.dateUtils.timestampToXmlGregorian(t);
		
		assertThat(xgc).isNotNull();
		assertThat(t.toInstant().toEpochMilli()).isEqualTo(this.xCal.toGregorianCalendar().getTimeInMillis());
	}
	
	@Test
	@Order(30)
	void testTimestampToXmlGregorianDatatypeConfigurationException () throws DatatypeConfigurationException {
		
		Timestamp t = new Timestamp(this.xCal.toGregorianCalendar().getTimeInMillis());
		
		this.mockDatatypeFactory.when(() -> DatatypeFactory.newInstance()).thenReturn(this.datatypeFactory);
		BDDMockito.given(this.datatypeFactory.newXMLGregorianCalendar(Mockito.any(GregorianCalendar.class)))
			.willAnswer(invocation -> {
				throw new DatatypeConfigurationException("DatatypeConfigurationException");
			});
	    
	    assertThrows(DatatypeConfigurationException.class, () -> {
	    	this.dateUtils.timestampToXmlGregorian(t);
		});
	}
	
	@Test
	@Order(31)
	void testDateToXmlGregorianString() throws DatatypeConfigurationException {
		
		this.mockDatatypeFactory.when(() -> DatatypeFactory.newInstance()).thenReturn(this.datatypeFactory);
	    
	    Mockito.when(this.datatypeFactory.newXMLGregorianCalendar(Mockito.anyString())).thenReturn(this.xCal);
		
		XMLGregorianCalendar xgc = this.dateUtils.dateToXmlGregorian(this.stringDate);
		
		assertThat(xgc.getHour()).isEqualTo(this.xCal.getHour());
		assertThat(xgc.getMinute()).isEqualTo(this.xCal.getMinute());
		assertThat(xgc.getSecond()).isEqualTo(this.xCal.getSecond());
		assertThat(xgc.getMillisecond()).isEqualTo(this.xCal.getMillisecond());
		
	}

	@AfterEach
	public void cleanUp() {
		this.mockDatatypeFactory.close();
	}
	
}
