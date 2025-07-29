package it.popso.bicomp.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import de.focus_shift.jollyday.core.Holiday;
import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;
import it.popso.bicomp.exception.BicompException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DateUtils {
	
	private static final String ZONE_ID = "Europe/Rome";
	
	public static DateUtils dateUtils() {
		return new DateUtils();
	}
	
	
	public Timestamp xmlGregorianCalendarToTimestamp(XMLGregorianCalendar xmlGregorianCalendar) {
		
		if(xmlGregorianCalendar.toString().indexOf("+") == -1) {
			log.info("Is RT1 timestamp");
			GregorianCalendar gregorianCalendar = xmlGregorianCalendar.toGregorianCalendar();
			Instant instant = gregorianCalendar.toInstant();

	        return Timestamp.from(instant);
		}
		else {
			return Timestamp.valueOf(xmlGregorianCalendar.toString().replace('T', ' ').substring(0, xmlGregorianCalendar.toString().indexOf("+")));
		}
	}
	
	public Date gregorianCalendarToDate(XMLGregorianCalendar xgc) throws BicompException {
		
		if(xgc == null) {
			throw new BicompException("XMLGregorianCalendar cannot be null");
		}
		
		Date d = null;
		boolean isTimestamp = xgc.toGregorianCalendar().get(Calendar.HOUR_OF_DAY) > 0 || xgc.toGregorianCalendar().get(Calendar.MINUTE) > 0 || 
				xgc.toGregorianCalendar().get(Calendar.SECOND) > 0 || xgc.toGregorianCalendar().get(Calendar.MILLISECOND) > 0;
		
		if(isTimestamp) {
			ZonedDateTime zdt = xgc.toGregorianCalendar().toZonedDateTime().withZoneSameInstant(ZoneId.of(ZONE_ID));
			d = Date.from(zdt.toInstant());
		}
		else {
			ZonedDateTime zdt = xgc.toGregorianCalendar().toZonedDateTime().withZoneSameInstant(ZoneId.of(ZONE_ID));
			java.time.LocalDate localDate = zdt.toLocalDate();
			d = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		}
		
		return d;
	}
	
	public Date getWorkingDay(Date date) {
		
		LocalDate l = this.dateToLocalDate(date);
		log.info("Giorno della settimana: [{}]", l.getDayOfWeek());
		
		boolean isLavorativo = false;
		while(!isLavorativo) {
			if(l.getDayOfWeek() == DayOfWeek.SATURDAY || l.getDayOfWeek() == DayOfWeek.SUNDAY) {
				l = l.plusDays(1);
			}
			else if(this.isHoliday(l)) {
				if(this.isTarget(l)) {
					isLavorativo = true;
				}
				else {
					l = l.plusDays(1);
				}
			}
			else {
				isLavorativo = true;
			}
		}
		
		return Date.from(l.atStartOfDay().atZone(ZoneId.of(ZONE_ID)).toInstant());
	}
	
	private boolean isHoliday(LocalDate l) {
		
		boolean holiday = false;
		log.info("ITALY's holidays for year: [{}]", l.getYear());
		HolidayManager manager = HolidayManager.getInstance(ManagerParameters.create(Locale.ITALY));
		
		Set<Holiday> holidays = manager.getHolidays(Year.of(l.getYear()));
		
		for(Holiday h : holidays) {
			log.info("Holiday is: [{}]", h.getDate().toString().concat("  ").concat(h.getDescription()));
			if(LocalDate.of(h.getDate().getYear(), h.getDate().getMonthValue(), h.getDate().getDayOfMonth()).compareTo(l) == 0) {
				holiday = true;
				break;
			}
			
			// condizione per gestione venerdì santo, unico giorno lavorativo ma NON TARGET
			if(h.getDescription().equals("Easter")) {
				LocalDate holyFriday = LocalDate.of(h.getDate().getYear(), h.getDate().getMonthValue(), h.getDate().getDayOfMonth()).minusDays(2);
				if(holyFriday.compareTo(l) == 0) {
					log.info("Data corrente e' VENERDI' SANTO");
					holiday = true;
					break;
				}
			}
		}
		
		return holiday;
	}
	
	private boolean isTarget(LocalDate l) {
		
		boolean target = false;
		
		Map<LocalDate, String> targetDays = new HashMap<>();
		targetDays.put(LocalDate.of(l.getYear(), 1, 6), "Epiphany");
		targetDays.put(LocalDate.of(l.getYear(), 4, 25), "Liberation Day");
		targetDays.put(LocalDate.of(l.getYear(), 6, 2), "Republic Day");
		targetDays.put(LocalDate.of(l.getYear(), 8, 15), "Assumption Day");
		targetDays.put(LocalDate.of(l.getYear(), 11, 1), "All Saints");
		targetDays.put(LocalDate.of(l.getYear(), 12, 8), "Immaculate Conception Day");
		
		for(Entry<LocalDate, String> entry : targetDays.entrySet()) {
			log.info("Target Day is: [{}]", entry.getKey().toString().concat("  ").concat(entry.getValue()));
			if(l.compareTo(entry.getKey()) == 0) {
				target = true;
				break;
			}
		}
		
		return target;
	}
	
	public Timestamp stringToTimestamp(String s) throws ParseException {
		
		Date d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(s);
		LocalDateTime l = this.dateToLocalDateTime(d);
		
		return Timestamp.valueOf(l);
	}
	
	public LocalDate dateToLocalDate(Date date) {
		
		java.sql.Date d = new java.sql.Date(date.getTime());
		
		return d.toLocalDate();
	}
	
	public Date localDateToDate(LocalDate localDate) {
		
		return Date.from(localDate.atStartOfDay().atZone(ZoneId.of(ZONE_ID)).toInstant());
	}
	
	public LocalDateTime dateToLocalDateTime(Date date) {
		
		return date.toInstant().atZone(ZoneId.of(ZONE_ID)).toLocalDateTime();
	}
	
	public Date localDateTimeToDate(LocalDateTime localDateTime) {
		
		return Date.from(localDateTime.atZone(ZoneId.of(ZONE_ID)).toInstant());
	}
	
	public LocalDateTime getCurrentTimestamp() {
		
		return LocalDateTime.now().atZone(ZoneId.of(ZONE_ID)).toLocalDateTime();
	}
	
	public Date stringToDateTime(String dt) throws ParseException {
		
		return new SimpleDateFormat("yyMMddHHmmss").parse(dt);
	}
	
	public Date stringToDate(String d) throws ParseException {
		
		return new SimpleDateFormat("yyMMdd").parse(d);
	}
	
	public Date stringToDateYear(String d) throws ParseException {
		
		return new SimpleDateFormat("yyyy-MM-dd").parse(d);
	}

	public String dateToString(Date d) throws ParseException {
		
		return new SimpleDateFormat("yyyyMMdd").format(d);
	}
	
	public String dateToStringFormatted(Date d) throws ParseException {
		
		return new SimpleDateFormat("yyyy-MM-dd").format(d);
	}
	
	public XMLGregorianCalendar currentTimestamp() throws DatatypeConfigurationException {
		
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
	}
	
	public XMLGregorianCalendar dateToXmlGregorian(Date date) throws DatatypeConfigurationException {
		
		log.info("Date: [{}]", date);
		
		XMLGregorianCalendar xmlDate = null;
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		
		log.info("GregorianCalendar: {}", calendar);		
		if(calendar.get(Calendar.MILLISECOND) != 0) {
			log.info("La data contiene anche l'orario");
			xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
		}
		else {
			xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
			xmlDate.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
		}
		
		return xmlDate;
	}
	
	public XMLGregorianCalendar dateToXmlGregorian(String date) throws DatatypeConfigurationException {
		
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(date);
	}
	
	public Date xmlGregorianToDate(XMLGregorianCalendar xgc) {
		
		log.info("XGC: [{}]", xgc);
		return xgc.toGregorianCalendar().getTime();
	}
	
	public XMLGregorianCalendar timestampToXmlGregorian(Timestamp timestamp) throws DatatypeConfigurationException {
		
		log.info("Timestamp: [{}]", timestamp);
		
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeInMillis(timestamp.toInstant().toEpochMilli());
        
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
	}
	
}
