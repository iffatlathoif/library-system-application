package com.enigma.library_app.util;

import com.enigma.library_app.exception.ApiException;
import org.springframework.web.multipart.MultipartFile;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUploadUtil {
	// 2,097,152 byte = 2MB
	public static final long MAX_FILE_SIZE = 2 * 1024 * 1024;
	public static final String IMAGE_PATTERN = "([^\\s]+(\\.(?i)(jpg|png|gif|bmp))$)";
	public static final String DATE_FORMAT = "yyyyMMddHHmmss";
	public static final String FILE_NAME_FORMAT = "%s_%s";

	public static boolean isAllowedExtension(final String filename, final String pattern) {
		final Matcher matcher = Pattern.compile(filename, Pattern.CASE_INSENSITIVE)
				.matcher(filename);
		return matcher.matches();
	}

	public static void assertAllowed(MultipartFile file, String pattern) {
		final long size = file.getSize();
		if (size > MAX_FILE_SIZE) {
			throw new ApiException("Max file size is 2MB");
		}
		final String fileName = file.getOriginalFilename();
		if (!isAllowedExtension(fileName, pattern)) {
			throw new ApiException("Only jpg, png, gif, bmp files are allowed.");
		}
	}

	public static String removeExtension(String filename) {
		final String extension = filename.substring(filename.lastIndexOf(".") + 1);
		return filename.replace("." + extension, "");
	}

	public static String getFileName(String filename) {
		String name = removeExtension(filename);
		final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		final String date = dateFormat.format(System.currentTimeMillis());
		return String.format(FILE_NAME_FORMAT, name, date);
	}
}
