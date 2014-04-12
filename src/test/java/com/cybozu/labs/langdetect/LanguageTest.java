/**
 *
 */
package com.cybozu.labs.langdetect;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Nakatani Shuyo
 *
 */
public class LanguageTest {

    /**
     * Test method for {@link com.cybozu.labs.langdetect.LanguageProbability#LanguageProbability(String, double)}.
     */
    @Test
    public final void testLanguage() {
        LanguageProbability lang = new LanguageProbability(null, 0);
        assertEquals(lang.getLanguage(), null);
        assertEquals(lang.getProbability(), 0.0, 0.0001);
        assertEquals(lang.toString(), "");

        LanguageProbability lang2 = new LanguageProbability("en", 1.0);
        assertEquals(lang2.getLanguage(), "en");
        assertEquals(lang2.getProbability(), 1.0, 0.0001);
        assertEquals(lang2.toString(), "en:1.0");

    }

}
