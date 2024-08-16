package io.github.honhimw.spring.web.util;

import org.springframework.http.MediaType;
import org.springframework.util.MimeType;

/**
 * @author hon_him
 * @since 2024-08-08
 */

public class MimeTypeSupports {

    public static final MediaType TEXT_CSV = MediaType.parseMediaType("text/csv");

    public static boolean isRawType(String mediaType) {
        try {
            return isRawType(MediaType.parseMediaType(mediaType));
        } catch (Exception ignore) {
            return false;
        }
    }

    public static boolean isRawType(MimeType mimeType) {
        return MediaType.APPLICATION_JSON.isCompatibleWith(mimeType) ||
               MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mimeType) ||
               MediaType.APPLICATION_XML.isCompatibleWith(mimeType) ||
               MediaType.TEXT_PLAIN.isCompatibleWith(mimeType) ||
               MediaType.TEXT_XML.isCompatibleWith(mimeType) ||
               TEXT_CSV.isCompatibleWith(mimeType)
            ;
    }

}
