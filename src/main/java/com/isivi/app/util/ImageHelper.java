package com.isivi.app.util;

public class ImageHelper {

    public static final String CLOUDINARY = "CLOUDINARY";
    public static final String BASE64 = "BASE64";
    public static final String UNKNOWN = "UNKNOWN";

    public static String clasificarOrigen(String url) {
        if (url == null || url.trim().isEmpty()) {
            return UNKNOWN;
        }
        if (url.startsWith("data:image/")) {
            return BASE64;
        }
        if (url.contains("res.cloudinary.com")) {
            return CLOUDINARY;
        }
        return UNKNOWN;
    }
}
