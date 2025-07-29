package it.popso.bicomp.dto;

import java.io.InputStream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InputStreamWithLength {
	
	private InputStream inputStream;
	private int length;

}
