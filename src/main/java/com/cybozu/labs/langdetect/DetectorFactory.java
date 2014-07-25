package com.cybozu.labs.langdetect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

import com.cybozu.labs.langdetect.util.LangProfile;

/**
 * Language Detector Factory Class
 * <p>
 * This class manages an initialization and constructions of {@link Detector}.
 * <p>
 * First create a factory with {@link #newInstance()} and
 * load profiles with {@link DetectorFactory#loadDefaultProfiles()} or any other specific {@code loadProfile} method
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
    public static DetectorFactory newInstance() {
        return new DetectorFactory();
    }

    protected int capacity;
    protected HashMap<String, double[]> wordLangProbMap;
    protected ArrayList<String> langlist;
    protected Long seed = null;

    private DetectorFactory() {
        wordLangProbMap = new HashMap<String, double[]>();
        langlist = new ArrayList<String>();
        capacity = 0;
    }

    private void ensureCapacity(int newCapacity) {
        if (newCapacity > capacity) {
            for (Map.Entry<String, double[]> entry : wordLangProbMap.entrySet()) {
                   double[] newProb = new double[newCapacity];
                   System.arraycopy(entry.getValue(), 0, newProb, 0, capacity);
                   entry.setValue(newProb);
            }
            capacity = newCapacity;
        }
    }

    private void ensureRemainingCapacity(int freeCapacity) {
        ensureCapacity(langlist.size() + freeCapacity);
    }

    /**
     * Load the internal profiles bundled in the JAR file
     *
     * @throws IOException
     */
    public void loadDefaultProfiles() throws IOException {
        InputStream profileListStream = this.getClass().getResourceAsStream("profiles/profiles.lst");
        if (profileListStream == null) {
            throw new IOException("Couldn't find default profiles package");
        }

        InputStreamReader profileListReader = new InputStreamReader(profileListStream);
        BufferedReader profileListBuffer = new BufferedReader(profileListReader);
        String profileList = profileListBuffer.readLine();
        String[] profiles = profileList.split(",");

        ensureRemainingCapacity(profiles.length);
        for (String profileName: profiles) {
            InputStream is = null;
            try {
                is = this.getClass().getResourceAsStream("profiles/" + profileName);
                LangProfile profile = JSON.decode(is, LangProfile.class);
                addProfile(profile);
            } catch (JSONException e) {
                throw new IOException("Profile format error in '" + profileName + "'", e);
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
     * @param profileDirectory profile directory path
     * @throws IOException  Can't open profiles or profile format is wrong
     */
    public void loadProfiles(String profileDirectory) throws IOException {
        loadProfiles(new File(profileDirectory));
    }

    /**
     * Load profiles from specified directory.
     * This method must be called once before language detection.
     *
     * @param profileDirectory profile directory path
     * @throws IOException Can't open profiles or profile's format is wrong
     */
    public void loadProfiles(File profileDirectory) throws IOException {
        File[] listFiles = profileDirectory.listFiles();
        if (listFiles == null) {
            throw new IOException("Couldn't open directory or directory is empty: " + profileDirectory);
        }

        ensureRemainingCapacity(listFiles.length);
        for (File file: listFiles) {
            if (file.getName().startsWith(".") || !file.isFile()) continue;
            loadProfile(file);
        }
    }

    /**
     * Load profiles using the supplied json encoded strings
     * This method must be called once before language detection.
     *
     * @param json_profiles list of json encoded language profiles
     * @throws IllegalArgumentException profile format is wrong
     */
    public void loadProfiles(List<String> json_profiles) throws IOException {
        ensureRemainingCapacity(json_profiles.size());

        for (String json: json_profiles) {
            loadProfile(json);
        }
    }

    /**
     * Load profile from a file and add it to known profiles
     *
     * @param profileFile profile file path
     * @throws IOException Can't open profiles or profile's format is wrong
     */
    public void loadProfile(File profileFile) throws IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(profileFile);
            LangProfile profile = JSON.decode(is, LangProfile.class);
            addProfile(profile);
        } catch (JSONException e) {
            throw new IOException("Profile format error in '" + profileFile.getName() + "'", e);
        } finally {
            try {
                if (is!=null) is.close();
            } catch (IOException e) {}
        }
    }

    /**
     * Load a profile using the supplied json encoded string
     *
     * @param jsonProfile json encoded language profile
     * @throws IOException profile format is wrong
     */
    public void loadProfile(String jsonProfile) throws IOException {
        try {
            LangProfile profile = JSON.decode(jsonProfile, LangProfile.class);
            addProfile(profile);
        } catch (JSONException e) {
            throw new IOException("Profile format error");
        }
    }

    /**
     * @param profile internal profile structure
     * @param index Index of the added profile
     * @param langsize Total number of profiles
     * @throws IllegalArgumentException If the added profile already exists
     */
    void addProfile(LangProfile profile) {
        String lang = profile.name;
        if (langlist.contains(lang)) {
            throw new IllegalArgumentException("Duplicate language profile for [" + lang + "]");
        }
        int index = langlist.size();
        if (capacity <= index) {
            ensureCapacity(capacity + 1);
        }
        this.langlist.add(lang);
        for (String word: profile.freq.keySet()) {
            if (!this.wordLangProbMap.containsKey(word)) {
                this.wordLangProbMap.put(word, new double[capacity]);
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
