package com.enigma.library_app.service.contract.book;

import com.enigma.library_app.dto.CloudinaryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CloudinaryService {
	CloudinaryResponse uploadFile(MultipartFile file, String filename, String folderName);
	Map deleteFile(String publicId);
}
