package com.enigma.library_app.util;

import com.enigma.library_app.exception.ApiException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtil {
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	public static LocalDate formatDate(String date) {
		if (date == null) {
			return null;
		}

		try {
			return LocalDate.parse(date, formatter);
		} catch (DateTimeParseException e) {
			throw new ApiException("Invalid date format. Expected format: dd-MM-yyyy. Example: 01-08-2025");
		}
	}

}
