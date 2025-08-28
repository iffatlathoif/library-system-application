package com.enigma.library_app.common;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dlg2zlwbo",
                "api_key", "797578554935497",
                "api_secret", "x1m99lIVKG6t-gBakJDx1dkJ7aM"
        ));
    }
}
