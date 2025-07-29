package it.popso.bicomp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.transform.stream.StreamSource;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.JAXBIntrospector;
import jakarta.xml.bind.Unmarshaller;


public class XMLUtils {
	
	private XMLUtils() {}

	public static <T> T unmarshall(Path path, Class<T> clazz) throws JAXBException, IOException {
        final JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { clazz });
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object value = JAXBIntrospector.getValue(unmarshaller.unmarshal(new StreamSource(Files.newInputStream(path)), clazz));
        T result = clazz.cast(value);
        if(result == null) {
            throw new JAXBException("Parsing error");
        }
        
        return result;
    }
	
	public static <T> T unmarshall(InputStream inputStream, Class<T> clazz) throws JAXBException, IOException {
		final JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { clazz });
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object value = JAXBIntrospector.getValue(unmarshaller.unmarshal(new StreamSource(inputStream), clazz));
        T result = clazz.cast(value);
        if(result == null) {
            throw new JAXBException("Parsing error");
        }
        
        return result;
    }

}
