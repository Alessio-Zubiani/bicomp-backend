package it.popso.bicomp.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import it.popso.bicomp.dto.InputStreamWithLength;

public class BaseServiceImpl {
	
	public InputStreamWithLength fromByteArraytoInputStream(InputStream inputStream) throws IOException {
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		inputStream.transferTo(buffer);
		byte[] data = buffer.toByteArray();
		
		return new InputStreamWithLength(new ByteArrayInputStream(data), data.length);
	}

}
