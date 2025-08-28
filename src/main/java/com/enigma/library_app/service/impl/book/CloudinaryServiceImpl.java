package com.enigma.library_app.service.impl.book;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.enigma.library_app.dto.CloudinaryResponse;
import com.enigma.library_app.exception.ApiException;
import com.enigma.library_app.service.contract.book.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

	private final Cloudinary cloudinary;

	@Transactional
	@Override
	public CloudinaryResponse uploadFile(MultipartFile file, String filename, String folderName) {
		final Map result;
		try {
			String folder = "libraryApp/" + folderName;
			result = cloudinary.uploader().upload(file.getBytes(),
					Map.of("folder", folder,
							"public_id", folder + "/" + filename));
			final String url = (String) result.get("secure_url");
			final String publicId = (String) result.get("public_id");
			return CloudinaryResponse.builder()
					.publicId(publicId)
					.url(url)
					.build();
		} catch (IOException e) {
			throw new ApiException("Failed to upload file");
		}
	}

	@Override
	public Map deleteFile(String publicId) {
		try {
			return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
		} catch (IOException e) {
			throw new ApiException("Failed to delete file with id: " + publicId);
		}
	}
}
