package com.cybozu.labs.langdetect;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

import com.cybozu.labs.langdetect.util.LangProfile;

/**
 * Language Detector Factory Class
 * <p>
 * This class manages an initialization and constructions of {@link Detector}.
 * <p>
 * First create a factory with {@link #newInstance()} and
 * load profiles with {@link DetectorFactory#loadProfile(String)} method
 * and set initialization parameters.
 * <p>
 * Then to detect language of a text fragment,
 * construct a {@code Detector} instance via {@link DetectorFactory#create()}.
 *
 * @see Detector
 *
 * @author Nakatani Shuyo
 * @author Elmer Garduno
 * @author Alexis Meneses
 */
public class DetectorFactory {


    /**
     * Create a new instance of the factory
     * @return a new factory instance
     */
    public static DetectorFactory newInstance()
    {
        return new DetectorFactory();
    }

    public HashMap<String, double[]> wordLangProbMap;
    public ArrayList<String> langlist;
    public Long seed = null;
    private DetectorFactory() {
        wordLangProbMap = new HashMap<String, double[]>();
        langlist = new ArrayList<String>();
    }

    /**
     * Load profiles from specified directory.
     * This method must be called once before language detection.
     *
     * @param profileDirectory profile directory path
     * @throws IOException  Can't open profiles or profile format is wrong
     */
    public void loadProfile(String profileDirectory) throws IOException {
        loadProfile(new File(profileDirectory));
    }

    /**
     * Load profiles from specified directory.
     * This method must be called once before language detection.
     *
     * @param profileDirectory profile directory path
     * @throws IOException  Can't open profiles or profile's format is wrong
     */
    public void loadProfile(File profileDirectory) throws IOException {
        File[] listFiles = profileDirectory.listFiles();
        if (listFiles == null) {
            throw new IOException("Couldn't open directory or directory is empty: " + profileDirectory);
        }

        int langsize = listFiles.length, index = 0;
        for (File file: listFiles) {
            if (file.getName().startsWith(".") || !file.isFile()) continue;
            FileInputStream is = null;
            try {
                is = new FileInputStream(file);
                LangProfile profile = JSON.decode(is, LangProfile.class);
                addProfile(profile, index, langsize);
                ++index;
            } catch (JSONException e) {
                throw new IOException("Profile format error in '" + file.getName() + "'", e);
            } finally {
                try {
                    if (is!=null) is.close();
                } catch (IOException e) {}
            }
        }
    }

    /**
     * Load profiles from specified directory.
     * This method must be called once before language detection.
     *
     * @param json_profiles list of json encoded language profiles
     * @throws IllegalArgumentException  Can't open profiles or profile format is wrong
     */
    public void loadProfile(List<String> json_profiles) throws IOException {
        int index = 0;
        int langsize = json_profiles.size();
        if (langsize < 2) {
            throw new IllegalArgumentException("Need more than 2 profiles");
        }

        for (String json: json_profiles) {
            try {
                LangProfile profile = JSON.decode(json, LangProfile.class);
                addProfile(profile, index, langsize);
                ++index;
            } catch (JSONException e) {
                throw new IllegalArgumentException("Profile format error");
            }
        }
    }

    /**
     * @param profile internal profile structure
     * @param index Index of the added profile
     * @param langsize Total number of profiles
     * @throws IllegalArgumentException If the added profile already exists
     */
    void addProfile(LangProfile profile, int index, int langsize) {
        String lang = profile.name;
        if (this.langlist.contains(lang)) {
            throw new IllegalArgumentException("Duplicate language profile for [" + lang + "]");
        }
        this.langlist.add(lang);
        for (String word: profile.freq.keySet()) {
            if (!this.wordLangProbMap.containsKey(word)) {
                this.wordLangProbMap.put(word, new double[langsize]);
            }
            int length = word.length();
            if (length >= 1 && length <= 3) {
                double prob = profile.freq.get(word).doubleValue() / profile.n_words[length - 1];
                this.wordLangProbMap.get(word)[index] = prob;
            }
        }
    }

    /**
     * Clear loaded language profiles (reinitialization to be available)
     */
    public void clear() {
        this.langlist.clear();
        this.wordLangProbMap.clear();
    }

    /**
     * Construct Detector instance
     *
     * @return Detector instance
     */
    public Detector create() {
        return createDetector();
    }

    /**
     * Construct Detector instance with smoothing parameter
     *
     * @param alpha smoothing parameter (default value = 0.5)
     * @return Detector instance
     */
    public Detector create(double alpha) {
        Detector detector = createDetector();
        detector.setAlpha(alpha);
        return detector;
    }

    private Detector createDetector() {
        if (this.langlist.size()==0) {
            throw new IllegalStateException("Profiles need to be loaded first");
        }
        Detector detector = new Detector(this);
        return detector;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public final List<String> getLangList() {
        return Collections.unmodifiableList(this.langlist);
    }
}
