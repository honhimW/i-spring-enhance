package io.github.honhimw.util;


import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;

/**
 * @author honhimW
 * @since 2025-05-15
 */

public class LangUtils {

    /**
     * implementation 'com.github.pemistahl:lingua:1.2.2'
     *
     * @param text text
     * @return language
     */
    public static Language detect(String text) {
        LanguageDetector languageDetector = LanguageDetectorBuilder.fromAllLanguages().build();
        return languageDetector.detectLanguageOf(text);
    }

}
