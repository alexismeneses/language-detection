package com.cybozu.labs.langdetect;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cybozu.labs.langdetect.util.LangProfile;

/**
 * Unit test for {@link Detector} and {@link DetectorFactory}.
 * @author Nakatani Shuyo
 * @author Alexis Meneses
 *
 */
public class DetectorTest {

    private static final String TRAINING_EN = "a a a b b c c d e";
    private static final String TRAINING_FR = "a b b c c c d d d";
    private static final String TRAINING_JA = "\u3042 \u3042 \u3042 \u3044 \u3046 \u3048 \u3048";
    private static final String JSON_LANG1 = "{\"freq\":{\"A\":3,\"B\":6,\"C\":3,\"AB\":2,\"BC\":1,\"ABC\":2,\"BBC\":1,\"CBA\":1},\"n_words\":[12,3,4],\"name\":\"lang1\"}";
    private static final String JSON_LANG2 = "{\"freq\":{\"A\":6,\"B\":3,\"C\":3,\"AA\":3,\"AB\":2,\"ABC\":1,\"ABA\":1,\"CAA\":1},\"n_words\":[12,5,3],\"name\":\"lang2\"}";

    private DetectorFactory detectorFactory;

    @Before
    public void setUp() throws Exception {
        detectorFactory = DetectorFactory.newInstance();

        LangProfile profile_en = new LangProfile("en");
        for (String w : TRAINING_EN.split(" "))
            profile_en.add(w);
        detectorFactory.addProfile(profile_en);

        LangProfile profile_fr = new LangProfile("fr");
        for (String w : TRAINING_FR.split(" "))
            profile_fr.add(w);
        detectorFactory.addProfile(profile_fr);

        LangProfile profile_ja = new LangProfile("ja");
        for (String w : TRAINING_JA.split(" "))
            profile_ja.add(w);
        detectorFactory.addProfile(profile_ja);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testDetector1() {
        Detector detect = detectorFactory.create();
        detect.append("a");
        assertEquals(detect.detect(), "en");
    }

    @Test
    public final void testDetector2() {
        Detector detect = detectorFactory.create();
        detect.append("b d");
        assertEquals(detect.detect(), "fr");
    }

    @Test
    public final void testDetector3() {
        Detector detect = detectorFactory.create();
        detect.append("d e");
        assertEquals(detect.detect(), "en");
    }

    @Test
    public final void testDetector4() {
        Detector detect = detectorFactory.create();
        detect.append("\u3042\u3042\u3042\u3042a");
        assertEquals(detect.detect(), "ja");
    }

    @Test
    public final void testLangList() {
        List<String> langList = detectorFactory.getLangList();
        assertEquals(langList.size(), 3);
        assertEquals(langList.get(0), "en");
        assertEquals(langList.get(1), "fr");
        assertEquals(langList.get(2), "ja");
    }

    @Test(expected = UnsupportedOperationException.class)
    public final void testLangListException() {
        List<String> langList = detectorFactory.getLangList();
        langList.add("hoge");
    }

    @Test
    public final void testFactoryFromJsonString() throws IOException {
        detectorFactory.clear();
        ArrayList<String> profiles = new ArrayList<String>();
        profiles.add(JSON_LANG1);
        profiles.add(JSON_LANG2);
        detectorFactory.loadProfiles(profiles);
        List<String> langList = detectorFactory.getLangList();
        assertEquals(langList.size(), 2);
        assertEquals(langList.get(0), "lang1");
        assertEquals(langList.get(1), "lang2");
    }
}