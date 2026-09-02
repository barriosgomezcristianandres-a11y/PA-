package com.isivi.app.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Value("${isivi.cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${isivi.cloudinary.api-key:}")
    private String apiKey;

    @Value("${isivi.cloudinary.api-secret:}")
    private String apiSecret;

    public boolean configurado() {
        return cloudName != null && !cloudName.isBlank()
                && apiKey != null && !apiKey.isBlank()
                && apiSecret != null && !apiSecret.isBlank();
    }

    public String getCloudName() {
        return cloudName == null ? "" : cloudName.trim();
    }

    public String getApiKey() {
        return apiKey == null ? "" : apiKey.trim();
    }

    public String getApiSecret() {
        return apiSecret == null ? "" : apiSecret.trim();
    }

    @Bean
    public Cloudinary cloudinary() {
        if (!configurado()) {
            return null;
        }
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", getCloudName(),
                "api_key", getApiKey(),
                "api_secret", getApiSecret(),
                "secure", true
        ));
    }
}
